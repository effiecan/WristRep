package com.fitnessrepcounter.wear.presentation.viewmodel

import android.app.Activity
import com.fitnessrepcounter.wear.MainDispatcherRule
import com.fitnessrepcounter.wear.domain.model.BillingAvailabilityState
import com.fitnessrepcounter.wear.domain.model.BillingPurchaseLaunchResult
import com.fitnessrepcounter.wear.domain.model.EntitlementState
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.MotionSample
import com.fitnessrepcounter.wear.domain.model.WorkoutSession
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import com.fitnessrepcounter.wear.domain.repository.MotionRepository
import com.fitnessrepcounter.wear.domain.repository.WorkoutRepository
import com.fitnessrepcounter.wear.domain.session.WorkoutSessionManager
import com.fitnessrepcounter.wear.platform.HapticsManager
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun saveWorkout_persistsWorkout_andConsumesFreeUse() = runTest {
        val workoutRepository = FakeWorkoutRepository()
        val entitlementRepository = FakeEntitlementRepository()
        val viewModel = WorkoutViewModel(
            workoutRepository = workoutRepository,
            entitlementRepository = entitlementRepository,
            motionRepository = FakeMotionRepository(),
            workoutSessionManager = WorkoutSessionManager(),
            hapticsManager = HapticsManager(),
        )

        viewModel.prepareNewWorkout()
        viewModel.selectExercise(Exercise.BICEPS_CURL)
        advanceUntilIdle()
        viewModel.addManualRep()
        assertThat(viewModel.endCurrentSet()).isTrue()
        assertThat(viewModel.finishWorkout()).isTrue()

        val saved = viewModel.saveWorkout()

        assertThat(saved).isTrue()
        assertThat(workoutRepository.savedWorkouts).hasSize(1)
        assertThat(entitlementRepository.state.value.completedFreeWorkoutsUsed).isEqualTo(1)
    }

    @Test
    fun discardWorkout_doesNotPersist_orConsumeFreeUse() = runTest {
        val workoutRepository = FakeWorkoutRepository()
        val entitlementRepository = FakeEntitlementRepository()
        val viewModel = WorkoutViewModel(
            workoutRepository = workoutRepository,
            entitlementRepository = entitlementRepository,
            motionRepository = FakeMotionRepository(),
            workoutSessionManager = WorkoutSessionManager(),
            hapticsManager = HapticsManager(),
        )

        viewModel.prepareNewWorkout()
        viewModel.selectExercise(Exercise.BICEPS_CURL)
        advanceUntilIdle()
        viewModel.addManualRep()
        assertThat(viewModel.endCurrentSet()).isTrue()
        viewModel.discardWorkout()

        assertThat(workoutRepository.savedWorkouts).isEmpty()
        assertThat(entitlementRepository.state.value.completedFreeWorkoutsUsed).isEqualTo(0)
    }

    @Test
    fun activeWorkoutThreshold_consumesReservedTrialWithoutCompletion() = runTest {
        val entitlementRepository = FakeEntitlementRepository()
        val viewModel = WorkoutViewModel(
            workoutRepository = FakeWorkoutRepository(),
            entitlementRepository = entitlementRepository,
            motionRepository = FakeMotionRepository(),
            workoutSessionManager = WorkoutSessionManager(),
            hapticsManager = HapticsManager(),
        )

        viewModel.prepareNewWorkout()
        viewModel.selectExercise(Exercise.BICEPS_CURL)
        viewModel.startReadyCountdown()

        advanceTimeBy(3_100L)
        advanceUntilIdle()
        advanceTimeBy(WorkoutViewModel.ACTIVE_TRIAL_CONSUME_THRESHOLD_MS)
        advanceUntilIdle()

        assertThat(entitlementRepository.state.value.completedFreeWorkoutsUsed).isEqualTo(1)
    }

    @Test
    fun manualRep_consumesReservedTrialImmediately() = runTest {
        val entitlementRepository = FakeEntitlementRepository()
        val viewModel = WorkoutViewModel(
            workoutRepository = FakeWorkoutRepository(),
            entitlementRepository = entitlementRepository,
            motionRepository = FakeMotionRepository(),
            workoutSessionManager = WorkoutSessionManager(),
            hapticsManager = HapticsManager(),
        )

        viewModel.prepareNewWorkout()
        viewModel.selectExercise(Exercise.BICEPS_CURL)
        viewModel.addManualRep()
        advanceUntilIdle()

        assertThat(entitlementRepository.state.value.completedFreeWorkoutsUsed).isEqualTo(1)
    }

    @Test
    fun repThreshold_requiresAtLeastThreeReps() {
        assertThat(WorkoutViewModel.shouldConsumeTrialForRepCount(1)).isFalse()
        assertThat(WorkoutViewModel.shouldConsumeTrialForRepCount(2)).isFalse()
        assertThat(WorkoutViewModel.shouldConsumeTrialForRepCount(3)).isTrue()
    }

    @Test
    fun threeDetectedReps_consumesTrial() = runTest {
        val entitlementRepository = FakeEntitlementRepository()
        val motionRepository = FakeMotionRepository()
        val viewModel = WorkoutViewModel(
            workoutRepository = FakeWorkoutRepository(),
            entitlementRepository = entitlementRepository,
            motionRepository = motionRepository,
            workoutSessionManager = WorkoutSessionManager(),
            hapticsManager = HapticsManager(),
        )

        viewModel.prepareNewWorkout()
        viewModel.selectExercise(Exercise.BICEPS_CURL)
        viewModel.startReadyCountdown()

        advanceTimeBy(3_100L)
        advanceUntilIdle()
        motionRepository.emitDetectedRepSequences(3)
        advanceUntilIdle()

        assertThat(entitlementRepository.state.value.completedFreeWorkoutsUsed).isEqualTo(1)
    }
}

private class FakeWorkoutRepository : WorkoutRepository {
    val savedWorkouts = mutableListOf<WorkoutSession>()
    private val history = MutableStateFlow<List<WorkoutSession>>(emptyList())

    override fun observeHistory(): Flow<List<WorkoutSession>> = history.asStateFlow()

    override suspend fun saveCompletedWorkout(session: WorkoutSession) {
        savedWorkouts += session
        history.value = savedWorkouts.toList()
    }

    override suspend fun countCompletedWorkouts(): Int = savedWorkouts.size
}

private class FakeEntitlementRepository : EntitlementRepository {
    val state = MutableStateFlow(EntitlementState())
    private val billingState = MutableStateFlow(BillingAvailabilityState())

    override fun observeEntitlement(): Flow<EntitlementState> = state.asStateFlow()

    override fun observeBillingAvailability(): Flow<BillingAvailabilityState> = billingState.asStateFlow()

    override suspend fun syncBillingState() = Unit

    override suspend fun launchProPurchase(activity: Activity): BillingPurchaseLaunchResult {
        return BillingPurchaseLaunchResult.ProductUnavailable
    }

    override suspend fun reserveActiveTrialSessionIfNeeded(exercise: Exercise): Boolean {
        val current = state.value
        if (current.isProUnlocked || current.activeTrialSessionId != null) return true
        if (current.completedFreeWorkoutsUsed >= current.freeWorkoutLimit) return false
        state.value = current.copy(
            activeTrialSessionId = "trial-session",
            activeTrialReservedAtEpochMs = System.currentTimeMillis(),
            activeTrialConsumed = false,
            activeTrialExerciseName = exercise.name,
            activeTrialAccumulatedActiveMs = 0L,
        )
        return true
    }

    override suspend fun consumeActiveTrialSessionIfNeeded() {
        val current = state.value
        if (current.isProUnlocked || current.activeTrialSessionId == null || current.activeTrialConsumed) return
        state.value = current.copy(
            completedFreeWorkoutsUsed = (current.completedFreeWorkoutsUsed + 1).coerceAtMost(3),
            activeTrialConsumed = true,
        )
    }

    override suspend fun clearActiveTrialSession() {
        val current = state.value
        state.value = current.copy(
            activeTrialSessionId = null,
            activeTrialReservedAtEpochMs = null,
            activeTrialConsumed = false,
            activeTrialExerciseName = null,
            activeTrialAccumulatedActiveMs = 0L,
        )
    }

    override suspend fun appendActiveTrialUsage(durationMs: Long) {
        val current = state.value
        if (current.activeTrialSessionId == null) return
        state.value = current.copy(
            activeTrialAccumulatedActiveMs = current.activeTrialAccumulatedActiveMs + durationMs,
        )
    }

    override suspend fun refillFreeTrialsForDebug() {
        state.value = state.value.copy(
            completedFreeWorkoutsUsed = 0,
            isProUnlocked = false,
            activeTrialSessionId = null,
            activeTrialReservedAtEpochMs = null,
            activeTrialConsumed = false,
            activeTrialExerciseName = null,
            activeTrialAccumulatedActiveMs = 0L,
        )
    }

    override suspend fun reconcileCompletedWorkoutUsage(completedWorkoutCount: Int) {
        state.value = state.value.copy(
            completedFreeWorkoutsUsed = completedWorkoutCount.coerceAtMost(3),
        )
    }
}

private class FakeMotionRepository : MotionRepository {
    private val samples = MutableSharedFlow<MotionSample>()
    private var timestampMs: Long = 1_000L

    override val motionSamples: Flow<MotionSample> = samples.asSharedFlow()

    override fun startTracking() = Unit

    override fun stopTracking() = Unit

    suspend fun emitRepSamples(repCount: Int) {
        repeat(repCount) {
            samples.emit(
                sample(timestampMs, accelZ = 9.81f, gyroMagnitude = 0f),
            )
            timestampMs += 300L
            samples.emit(sample(timestampMs, accelZ = 4.0f, gyroMagnitude = 3.0f))
            timestampMs += 300L
            samples.emit(sample(timestampMs, accelZ = 4.0f, gyroMagnitude = 3.0f))
            timestampMs += 300L
            samples.emit(sample(timestampMs, accelZ = 15.0f, gyroMagnitude = 3.0f))
            timestampMs += 300L
            samples.emit(sample(timestampMs, accelZ = 15.0f, gyroMagnitude = 3.0f))
            timestampMs += 300L
            samples.emit(sample(timestampMs, accelZ = 9.81f, gyroMagnitude = 0.2f))
            timestampMs += 300L
            samples.emit(sample(timestampMs, accelZ = 9.81f, gyroMagnitude = 0.2f))
            timestampMs += 300L
            samples.emit(sample(timestampMs, accelZ = 9.81f, gyroMagnitude = 0.2f))
            timestampMs += 300L
            samples.emit(sample(timestampMs, accelZ = 9.81f, gyroMagnitude = 0.2f))
            timestampMs += 300L
            samples.emit(sample(timestampMs, accelZ = 9.81f, gyroMagnitude = 0.2f))
            timestampMs += 1_200L
        }
    }

    suspend fun emitDetectedRepSequences(repCount: Int) {
        emitRepSamples(repCount)
    }

    private fun sample(timestampMs: Long, accelZ: Float, gyroMagnitude: Float): MotionSample {
        return MotionSample(
            timestampMs = timestampMs,
            accelX = 0f,
            accelY = 0f,
            accelZ = accelZ,
            gyroX = gyroMagnitude,
            gyroY = 0f,
            gyroZ = 0f,
        )
    }
}
