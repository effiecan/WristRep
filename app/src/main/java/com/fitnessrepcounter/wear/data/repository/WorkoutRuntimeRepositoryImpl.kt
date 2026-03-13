package com.fitnessrepcounter.wear.data.repository

import android.os.Build
import com.fitnessrepcounter.wear.domain.model.EntitlementState
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.HapticMode
import com.fitnessrepcounter.wear.domain.model.RepDetectionState
import com.fitnessrepcounter.wear.domain.model.SettingsState
import com.fitnessrepcounter.wear.domain.model.WorkoutRuntimeEvent
import com.fitnessrepcounter.wear.domain.model.isSelectable
import com.fitnessrepcounter.wear.domain.rep.RepCounterEngine
import com.fitnessrepcounter.wear.domain.rep.motionProfile
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import com.fitnessrepcounter.wear.domain.repository.MotionRepository
import com.fitnessrepcounter.wear.domain.repository.SettingsRepository
import com.fitnessrepcounter.wear.domain.repository.WorkoutRepository
import com.fitnessrepcounter.wear.domain.repository.WorkoutRuntimeRepository
import com.fitnessrepcounter.wear.domain.session.WorkoutSessionManager
import com.fitnessrepcounter.wear.platform.WorkoutServiceController
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState
import com.fitnessrepcounter.wear.presentation.state.WorkoutStep
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkoutRuntimeRepositoryImpl(
    private val workoutRepository: WorkoutRepository,
    private val entitlementRepository: EntitlementRepository,
    private val motionRepository: MotionRepository,
    private val settingsRepository: SettingsRepository,
    private val workoutSessionManager: WorkoutSessionManager,
    private val workoutServiceController: WorkoutServiceController,
    private val repositoryScope: CoroutineScope,
) : WorkoutRuntimeRepository {
    private val _uiState = MutableStateFlow(WorkoutUiState())
    override val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private val _hasActiveSession = MutableStateFlow(false)
    override val hasActiveSession: StateFlow<Boolean> = _hasActiveSession.asStateFlow()

    private val _canExposeOngoingEntry = MutableStateFlow(false)
    override val canExposeOngoingEntry: StateFlow<Boolean> = _canExposeOngoingEntry.asStateFlow()

    private val _shouldRequestNotificationPermission = MutableStateFlow(false)
    override val shouldRequestNotificationPermission: StateFlow<Boolean> =
        _shouldRequestNotificationPermission.asStateFlow()
    private val workoutEventsChannel = Channel<WorkoutRuntimeEvent>(capacity = Channel.BUFFERED)
    override val workoutEvents: Flow<WorkoutRuntimeEvent> = workoutEventsChannel.receiveAsFlow()

    private var countdownJob: Job? = null
    private var restTimerJob: Job? = null
    private var trialConsumptionJob: Job? = null
    private var repCounterEngine: RepCounterEngine? = null
    private var activeWorkoutStartedAtEpochMs: Long? = null
    private var latestEntitlementState: EntitlementState = EntitlementState()
    private var latestSettingsState: SettingsState = SettingsState()
    private var serviceRunning: Boolean = false
    private var didHandleNotificationPromptForCurrentSession: Boolean = false

    init {
        repositoryScope.launch {
            entitlementRepository.observeEntitlement().collect { entitlement ->
                latestEntitlementState = entitlement
            }
        }

        repositoryScope.launch {
            settingsRepository.observeSettings().collect { settings ->
                latestSettingsState = settings
            }
        }

        repositoryScope.launch {
            motionRepository.motionSamples.collect { sample ->
                val currentState = _uiState.value
                if (!currentState.isTracking || currentState.currentStep != WorkoutStep.ACTIVE) {
                    return@collect
                }

                val update = repCounterEngine?.process(sample) ?: return@collect
                if (update.repDelta > 0) {
                    workoutSessionManager.recordAutoRep(sample.timestampMs)
                    if (shouldPerformRepConfirmedHaptic()) {
                        repeat(update.repDelta) {
                            emitWorkoutEvent(WorkoutRuntimeEvent.RepConfirmed)
                        }
                    }
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

        updateNotificationSurfaceAvailability()
    }

    override fun currentStep(): WorkoutStep = _uiState.value.currentStep

    override fun prepareNewWorkout() {
        countdownJob?.cancel()
        restTimerJob?.cancel()
        trialConsumptionJob?.cancel()
        activeWorkoutStartedAtEpochMs = null
        motionRepository.stopTracking()
        repCounterEngine = null
        workoutSessionManager.reset()
        serviceRunning = false
        didHandleNotificationPromptForCurrentSession = false
        _shouldRequestNotificationPermission.value = false
        _hasActiveSession.value = false
        _uiState.value = WorkoutUiState()
        workoutServiceController.stop()
        updateNotificationSurfaceAvailability()
    }

    override fun selectExercise(exercise: Exercise) {
        if (!exercise.isSelectable) return
        workoutSessionManager.selectExercise(exercise)
        repCounterEngine = RepCounterEngine(exercise.motionProfile())
        _hasActiveSession.value = true
        repositoryScope.launch {
            ensureActiveTrialReservation(exercise)
        }
        applySessionSnapshot(currentStep = WorkoutStep.READY)
        _uiState.update {
            it.copy(
                selectedExercise = exercise,
                countdownValue = 3,
            )
        }
        updateNotificationSurfaceAvailability()
    }

    override fun startReadyCountdown() {
        if (
            _uiState.value.selectedExercise == null ||
            countdownJob?.isActive == true ||
            _uiState.value.currentStep != WorkoutStep.READY
        ) {
            return
        }

        ensureForegroundServiceStarted()

        _uiState.update {
            it.copy(
                currentStep = WorkoutStep.READY,
                countdownValue = 3,
                detectionState = RepDetectionState.IDLE,
            )
        }

        countdownJob = repositoryScope.launch {
            for (value in 3 downTo 1) {
                _uiState.update { state -> state.copy(countdownValue = value) }
                if (shouldPerformImportantEventHaptic()) {
                    emitWorkoutEvent(WorkoutRuntimeEvent.CountdownTick)
                }
                delay(1_000L)
            }
            _uiState.update { it.copy(countdownValue = 0) }
            startActiveTracking()
        }
    }

    override fun addManualRep() {
        workoutSessionManager.adjustRepManually(delta = 1)
        applySessionSnapshot()
        consumeReservedTrialIfNeeded(force = true)
    }

    override fun removeManualRep() {
        workoutSessionManager.adjustRepManually(delta = -1)
        applySessionSnapshot()
        consumeReservedTrialIfNeeded(force = true)
    }

    override fun endCurrentSet(): Boolean {
        val set = workoutSessionManager.endCurrentSet() ?: return false
        if (shouldPerformImportantEventHaptic()) {
            emitWorkoutEvent(WorkoutRuntimeEvent.SetFinished)
        }
        consumeReservedTrialIfNeeded(force = true)
        stopTracking()
        applySessionSnapshot(
            currentStep = WorkoutStep.END_SET_CONFIRMATION,
            latestCompletedSetOverride = set,
            detectionState = RepDetectionState.IDLE,
        )
        return true
    }

    override fun beginRestTimer() {
        if (
            restTimerJob?.isActive == true ||
            _uiState.value.currentStep !in setOf(WorkoutStep.END_SET_CONFIRMATION, WorkoutStep.REST_TIMER)
        ) {
            return
        }

        ensureForegroundServiceStarted()
        workoutSessionManager.startNextSet()
        repCounterEngine?.reset()
        _uiState.update {
            it.copy(
                currentStep = WorkoutStep.REST_TIMER,
                restSecondsRemaining = 60,
                isTracking = false,
            )
        }

        restTimerJob = repositoryScope.launch {
            for (remaining in 60 downTo 1) {
                _uiState.update { state -> state.copy(restSecondsRemaining = remaining) }
                delay(1_000L)
            }
            startActiveTracking()
        }
    }

    override fun skipRestTimer() {
        restTimerJob?.cancel()
        startActiveTracking()
    }

    override fun finishWorkout(): Boolean {
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

    override suspend fun saveWorkout(): Boolean {
        val workout = workoutSessionManager.getCompletedWorkout() ?: return false
        workoutRepository.saveCompletedWorkout(workout)
        entitlementRepository.consumeActiveTrialSessionIfNeeded()
        entitlementRepository.clearActiveTrialSession()
        prepareNewWorkout()
        return true
    }

    override fun discardWorkout() {
        repositoryScope.launch {
            entitlementRepository.clearActiveTrialSession()
        }
        prepareNewWorkout()
    }

    override fun onNotificationPermissionPromptHandled(granted: Boolean) {
        didHandleNotificationPromptForCurrentSession = true
        _shouldRequestNotificationPermission.value = false
        updateNotificationSurfaceAvailability(isGrantedOverride = granted)
    }

    private fun startActiveTracking() {
        countdownJob?.cancel()
        restTimerJob?.cancel()
        repCounterEngine?.reset()
        ensureForegroundServiceStarted()
        motionRepository.startTracking()
        activeWorkoutStartedAtEpochMs = System.currentTimeMillis()
        repositoryScope.launch {
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

    private fun ensureForegroundServiceStarted() {
        if (serviceRunning) return
        workoutServiceController.start()
        serviceRunning = true
        updateNotificationSurfaceAvailability()
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !_canExposeOngoingEntry.value &&
            !didHandleNotificationPromptForCurrentSession
        ) {
            _shouldRequestNotificationPermission.value = true
        }
    }

    private fun updateNotificationSurfaceAvailability(isGrantedOverride: Boolean? = null) {
        val notificationsEnabled = isGrantedOverride ?: workoutServiceController.areNotificationsEnabled()
        _canExposeOngoingEntry.value = notificationsEnabled && serviceRunning && _hasActiveSession.value
    }

    private fun shouldPerformImportantEventHaptic(): Boolean {
        return shouldPerformImportantEventHaptic(latestSettingsState.hapticMode)
    }

    private fun shouldPerformRepConfirmedHaptic(): Boolean {
        return shouldPerformRepConfirmedHaptic(latestSettingsState.hapticMode)
    }

    private fun consumeReservedTrialIfNeeded(force: Boolean) {
        if (!force) return
        repositoryScope.launch {
            val reserved = ensureActiveTrialReservation(_uiState.value.selectedExercise)
            if (reserved) {
                entitlementRepository.consumeActiveTrialSessionIfNeeded()
            }
        }
    }

    private fun emitWorkoutEvent(event: WorkoutRuntimeEvent) {
        workoutEventsChannel.trySend(event)
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

        trialConsumptionJob = repositoryScope.launch {
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
        repositoryScope.launch {
            entitlementRepository.appendActiveTrialUsage(elapsedMs)
        }
    }

    private suspend fun ensureActiveTrialReservation(exercise: Exercise?): Boolean {
        if (exercise == null || latestEntitlementState.isProUnlocked || latestEntitlementState.hasActiveTrialReservation) {
            return exercise != null
        }
        return entitlementRepository.reserveActiveTrialSessionIfNeeded(exercise)
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

    companion object {
        const val ACTIVE_TRIAL_CONSUME_THRESHOLD_MS = 30_000L
        private const val MIN_REPS_TO_CONSUME_TRIAL = 3

        internal fun shouldConsumeTrialForRepCount(repCount: Int): Boolean {
            return repCount >= MIN_REPS_TO_CONSUME_TRIAL
        }

        internal fun shouldPerformImportantEventHaptic(mode: HapticMode): Boolean {
            return mode != HapticMode.OFF
        }

        internal fun shouldPerformRepConfirmedHaptic(mode: HapticMode): Boolean {
            return mode == HapticMode.EVERY_REP
        }
    }
}
