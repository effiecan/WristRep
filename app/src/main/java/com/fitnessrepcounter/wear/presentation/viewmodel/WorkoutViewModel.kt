package com.fitnessrepcounter.wear.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessrepcounter.wear.domain.model.EntitlementState
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.isSelectable
import com.fitnessrepcounter.wear.domain.model.RepDetectionState
import com.fitnessrepcounter.wear.domain.rep.RepCounterEngine
import com.fitnessrepcounter.wear.domain.rep.motionProfile
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import com.fitnessrepcounter.wear.domain.repository.MotionRepository
import com.fitnessrepcounter.wear.domain.repository.WorkoutRepository
import com.fitnessrepcounter.wear.domain.session.WorkoutSessionManager
import com.fitnessrepcounter.wear.platform.HapticsManager
import com.fitnessrepcounter.wear.presentation.state.WorkoutStep
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val workoutRepository: WorkoutRepository,
    private val entitlementRepository: EntitlementRepository,
    private val motionRepository: MotionRepository,
    private val workoutSessionManager: WorkoutSessionManager,
    private val hapticsManager: HapticsManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var restTimerJob: Job? = null
    private var repCounterEngine: RepCounterEngine? = null
    private var trialConsumptionJob: Job? = null
    private var activeWorkoutStartedAtEpochMs: Long? = null
    private var latestEntitlementState: EntitlementState = EntitlementState()

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        viewModelScope.launch {
            entitlementRepository.observeEntitlement().collect { entitlement ->
                latestEntitlementState = entitlement
            }
        }

        viewModelScope.launch {
            motionRepository.motionSamples.collect { sample ->
                val currentState = _uiState.value
                if (!currentState.isTracking || currentState.currentStep != WorkoutStep.ACTIVE) {
                    return@collect
                }

                val update = repCounterEngine?.process(sample) ?: return@collect
                if (update.repDelta > 0) {
                    workoutSessionManager.recordAutoRep(sample.timestampMs)
                    hapticsManager.performRepConfirmed()
                    consumeReservedTrialIfNeeded(
                        force = shouldConsumeTrialForRepCount(_uiState.value.currentRepCount + update.repDelta),
                    )
                }

                applySessionSnapshot(
                    detectionState = update.state,
                    motionSignal = update.smoothedSignal,
                    confidence = update.confidence,
                )
            }
        }
    }

    fun prepareNewWorkout() {
        countdownJob?.cancel()
        restTimerJob?.cancel()
        trialConsumptionJob?.cancel()
        activeWorkoutStartedAtEpochMs = null
        motionRepository.stopTracking()
        repCounterEngine = null
        workoutSessionManager.reset()
        _uiState.value = WorkoutUiState()
    }

    fun selectExercise(exercise: Exercise) {
        if (!exercise.isSelectable) return
        workoutSessionManager.selectExercise(exercise)
        repCounterEngine = RepCounterEngine(exercise.motionProfile())
        viewModelScope.launch {
            ensureActiveTrialReservation(exercise)
        }
        applySessionSnapshot(currentStep = WorkoutStep.READY)
        _uiState.update {
            it.copy(
                selectedExercise = exercise,
                countdownValue = 3,
            )
        }
    }

    fun startReadyCountdown() {
        if (_uiState.value.selectedExercise == null || countdownJob?.isActive == true) return

        _uiState.update {
            it.copy(
                currentStep = WorkoutStep.READY,
                countdownValue = 3,
                detectionState = RepDetectionState.IDLE,
            )
        }

        countdownJob = viewModelScope.launch {
            for (value in 3 downTo 1) {
                _uiState.update { state -> state.copy(countdownValue = value) }
                hapticsManager.performCountdownTick()
                delay(1_000L)
            }
            _uiState.update { it.copy(countdownValue = 0) }
            startActiveTracking()
        }
    }

    fun addManualRep() {
        workoutSessionManager.adjustRepManually(delta = 1)
        applySessionSnapshot()
        consumeReservedTrialIfNeeded(force = true)
    }

    fun removeManualRep() {
        workoutSessionManager.adjustRepManually(delta = -1)
        applySessionSnapshot()
        consumeReservedTrialIfNeeded(force = true)
    }

    fun endCurrentSet(): Boolean {
        val set = workoutSessionManager.endCurrentSet() ?: return false
        consumeReservedTrialIfNeeded(force = true)
        stopTracking()
        hapticsManager.performSetFinished()
        applySessionSnapshot(
            currentStep = WorkoutStep.END_SET_CONFIRMATION,
            latestCompletedSetOverride = set,
            detectionState = RepDetectionState.IDLE,
        )
        return true
    }

    fun beginRestTimer() {
        if (restTimerJob?.isActive == true) return

        workoutSessionManager.startNextSet()
        repCounterEngine?.reset()
        _uiState.update {
            it.copy(
                currentStep = WorkoutStep.REST_TIMER,
                restSecondsRemaining = 60,
                isTracking = false,
            )
        }

        restTimerJob = viewModelScope.launch {
            for (remaining in 60 downTo 1) {
                _uiState.update { state -> state.copy(restSecondsRemaining = remaining) }
                delay(1_000L)
            }
            startActiveTracking()
        }
    }

    fun skipRestTimer() {
        restTimerJob?.cancel()
        startActiveTracking()
    }

    fun finishWorkout(): Boolean {
        consumeReservedTrialIfNeeded(force = true)
        stopTracking()
        val workout = workoutSessionManager.finishWorkout() ?: return false
        _uiState.update {
            it.copy(
                currentStep = WorkoutStep.SUMMARY,
                canSave = workout.sets.isNotEmpty(),
                detectionState = RepDetectionState.IDLE,
            )
        }
        applySessionSnapshot(currentStep = WorkoutStep.SUMMARY)
        return true
    }

    suspend fun saveWorkout(): Boolean {
        val workout = workoutSessionManager.getCompletedWorkout() ?: return false
        workoutRepository.saveCompletedWorkout(workout)
        entitlementRepository.consumeActiveTrialSessionIfNeeded()
        entitlementRepository.clearActiveTrialSession()
        prepareNewWorkout()
        return true
    }

    fun discardWorkout() {
        viewModelScope.launch {
            entitlementRepository.clearActiveTrialSession()
        }
        prepareNewWorkout()
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        restTimerJob?.cancel()
        trialConsumptionJob?.cancel()
        persistActiveTrialUsage()
        motionRepository.stopTracking()
        ioScope.launch {
            if (latestEntitlementState.hasActiveTrialReservation) {
                entitlementRepository.clearActiveTrialSession()
            }
        }
    }

    private fun startActiveTracking() {
        countdownJob?.cancel()
        restTimerJob?.cancel()
        repCounterEngine?.reset()
        motionRepository.startTracking()
        activeWorkoutStartedAtEpochMs = System.currentTimeMillis()
        viewModelScope.launch {
            ensureActiveTrialReservation(_uiState.value.selectedExercise)
            scheduleTrialConsumptionTimer()
        }
        applySessionSnapshot(
            currentStep = WorkoutStep.ACTIVE,
            detectionState = RepDetectionState.IDLE,
        )
        _uiState.update {
            it.copy(
                isTracking = true,
                countdownValue = 0,
            )
        }
    }

    private fun stopTracking() {
        persistActiveTrialUsage()
        motionRepository.stopTracking()
        _uiState.update { it.copy(isTracking = false) }
    }

    private fun consumeReservedTrialIfNeeded(force: Boolean) {
        if (!force) return
        viewModelScope.launch {
            val reserved = ensureActiveTrialReservation(_uiState.value.selectedExercise)
            if (reserved) {
                entitlementRepository.consumeActiveTrialSessionIfNeeded()
            }
        }
    }

    private fun scheduleTrialConsumptionTimer() {
        trialConsumptionJob?.cancel()
        if (latestEntitlementState.isProUnlocked || latestEntitlementState.activeTrialConsumed || _uiState.value.selectedExercise == null) {
            return
        }

        val remainingMs = (ACTIVE_TRIAL_CONSUME_THRESHOLD_MS - latestEntitlementState.activeTrialAccumulatedActiveMs)
            .coerceAtLeast(0L)
        if (remainingMs == 0L) {
            consumeReservedTrialIfNeeded(force = true)
            return
        }

        trialConsumptionJob = viewModelScope.launch {
            delay(remainingMs)
            entitlementRepository.consumeActiveTrialSessionIfNeeded()
        }
    }

    private fun persistActiveTrialUsage(nowEpochMs: Long = System.currentTimeMillis()) {
        trialConsumptionJob?.cancel()
        val startedAt = activeWorkoutStartedAtEpochMs ?: return
        activeWorkoutStartedAtEpochMs = null
        val elapsedMs = (nowEpochMs - startedAt).coerceAtLeast(0L)
        if (elapsedMs <= 0L || latestEntitlementState.isProUnlocked || !latestEntitlementState.hasActiveTrialReservation) {
            return
        }
        ioScope.launch {
            entitlementRepository.appendActiveTrialUsage(elapsedMs)
        }
    }

    private suspend fun ensureActiveTrialReservation(exercise: Exercise?): Boolean {
        if (exercise == null || latestEntitlementState.isProUnlocked || latestEntitlementState.hasActiveTrialReservation) {
            return exercise != null
        }
        return entitlementRepository.reserveActiveTrialSessionIfNeeded(exercise)
    }

    companion object {
        const val ACTIVE_TRIAL_CONSUME_THRESHOLD_MS = 30_000L
        private const val MIN_REPS_TO_CONSUME_TRIAL = 3

        internal fun shouldConsumeTrialForRepCount(repCount: Int): Boolean {
            return repCount >= MIN_REPS_TO_CONSUME_TRIAL
        }
    }

    private fun applySessionSnapshot(
        currentStep: WorkoutStep? = null,
        detectionState: RepDetectionState = _uiState.value.detectionState,
        motionSignal: Float = _uiState.value.motionSignal,
        confidence: Float = _uiState.value.confidence,
        latestCompletedSetOverride: com.fitnessrepcounter.wear.domain.model.WorkoutSet? = null,
    ) {
        val snapshot = workoutSessionManager.snapshot()
        _uiState.update {
            it.copy(
                selectedExercise = snapshot.selectedExercise,
                currentStep = currentStep ?: it.currentStep,
                currentSetNumber = snapshot.currentSetNumber,
                currentRepCount = snapshot.currentRepCount,
                totalReps = snapshot.totalReps,
                completedSets = snapshot.completedSets,
                latestCompletedSet = latestCompletedSetOverride ?: snapshot.lastCompletedSet,
                workoutStartedAtEpochMs = snapshot.workoutStartedAtEpochMs,
                canSave = snapshot.completedSets.isNotEmpty() && (currentStep ?: it.currentStep) == WorkoutStep.SUMMARY,
                detectionState = detectionState,
                motionSignal = motionSignal,
                confidence = confidence,
            )
        }
    }
}
