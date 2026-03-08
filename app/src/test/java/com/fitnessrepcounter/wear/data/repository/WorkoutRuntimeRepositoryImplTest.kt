package com.fitnessrepcounter.wear.data.repository

import android.app.Activity
import com.fitnessrepcounter.wear.MainDispatcherRule
import com.fitnessrepcounter.wear.domain.model.BillingAvailabilityState
import com.fitnessrepcounter.wear.domain.model.BillingPurchaseLaunchResult
import com.fitnessrepcounter.wear.domain.model.EntitlementState
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.HapticMode
import com.fitnessrepcounter.wear.domain.model.MotionSample
import com.fitnessrepcounter.wear.domain.model.SettingsState
import com.fitnessrepcounter.wear.domain.model.WorkoutRuntimeEvent
import com.fitnessrepcounter.wear.domain.model.WorkoutSession
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import com.fitnessrepcounter.wear.domain.repository.MotionRepository
import com.fitnessrepcounter.wear.domain.repository.SettingsRepository
import com.fitnessrepcounter.wear.domain.repository.WorkoutRepository
import com.fitnessrepcounter.wear.domain.session.WorkoutSessionManager
import com.fitnessrepcounter.wear.platform.WorkoutServiceController
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutRuntimeRepositoryImplTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun defaultHaptics_keepCountdownAndSetFinished() = runTest {
        val settingsRepository = FakeSettingsRepository()
        val workoutEvents = mutableListOf<WorkoutRuntimeEvent>()
        val repository = WorkoutRuntimeRepositoryImpl(
            workoutRepository = FakeWorkoutRepository(),
            entitlementRepository = FakeEntitlementRepository(),
            motionRepository = FakeMotionRepository(),
            settingsRepository = settingsRepository,
            workoutSessionManager = WorkoutSessionManager(),
            workoutServiceController = FakeWorkoutServiceController(),
            repositoryScope = backgroundScope,
        )
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            repository.workoutEvents.collect { workoutEvents += it }
        }

        repository.prepareNewWorkout()
        repository.selectExercise(Exercise.BICEPS_CURL)
        advanceUntilIdle()
        repository.startReadyCountdown()

        advanceTimeBy(3_100L)
        advanceUntilIdle()
        repository.addManualRep()
        repository.endCurrentSet()
        advanceUntilIdle()

        assertThat(settingsRepository.state.value.hapticMode).isEqualTo(HapticMode.IMPORTANT_ONLY)
        assertThat(workoutEvents.count { it == WorkoutRuntimeEvent.CountdownTick }).isAtLeast(3)
    }

    @Test
    fun hapticMode_rules_matchProductRequirements() {
        assertThat(WorkoutRuntimeRepositoryImpl.shouldPerformImportantEventHaptic(HapticMode.OFF)).isFalse()
        assertThat(WorkoutRuntimeRepositoryImpl.shouldPerformImportantEventHaptic(HapticMode.IMPORTANT_ONLY)).isTrue()
        assertThat(WorkoutRuntimeRepositoryImpl.shouldPerformRepConfirmedHaptic(HapticMode.IMPORTANT_ONLY)).isFalse()
        assertThat(WorkoutRuntimeRepositoryImpl.shouldPerformRepConfirmedHaptic(HapticMode.EVERY_REP)).isTrue()
    }

    @Test
    fun activeWorkoutThreshold_consumesReservedTrialWithoutCompletion() = runTest {
        val entitlementRepository = FakeEntitlementRepository()
        val repository = WorkoutRuntimeRepositoryImpl(
            workoutRepository = FakeWorkoutRepository(),
            entitlementRepository = entitlementRepository,
            motionRepository = FakeMotionRepository(),
            settingsRepository = FakeSettingsRepository(),
            workoutSessionManager = WorkoutSessionManager(),
            workoutServiceController = FakeWorkoutServiceController(),
            repositoryScope = backgroundScope,
        )

        repository.prepareNewWorkout()
        repository.selectExercise(Exercise.BICEPS_CURL)
        repository.startReadyCountdown()

        advanceTimeBy(3_100L)
        advanceUntilIdle()
        advanceTimeBy(WorkoutRuntimeRepositoryImpl.ACTIVE_TRIAL_CONSUME_THRESHOLD_MS)
        advanceUntilIdle()

        assertThat(entitlementRepository.state.value.completedFreeWorkoutsUsed).isEqualTo(1)
    }

    @Test
    fun notificationPermissionDenied_keepsWorkoutActive_butDisablesResumeSurface() = runTest {
        val serviceController = FakeWorkoutServiceController(notificationsEnabled = false)
        val repository = WorkoutRuntimeRepositoryImpl(
            workoutRepository = FakeWorkoutRepository(),
            entitlementRepository = FakeEntitlementRepository(),
            motionRepository = FakeMotionRepository(),
            settingsRepository = FakeSettingsRepository(),
            workoutSessionManager = WorkoutSessionManager(),
            workoutServiceController = serviceController,
            repositoryScope = backgroundScope,
        )

        repository.prepareNewWorkout()
        repository.selectExercise(Exercise.BICEPS_CURL)
        repository.startReadyCountdown()
        advanceUntilIdle()

        assertThat(repository.hasActiveSession.value).isTrue()
        repository.onNotificationPermissionPromptHandled(granted = false)

        assertThat(repository.hasActiveSession.value).isTrue()
        assertThat(repository.canExposeOngoingEntry.value).isFalse()
        assertThat(serviceController.startCount).isEqualTo(1)
    }

    @Test
    fun saveWorkout_persistsWorkout_andConsumesFreeUse() = runTest {
        val workoutRepository = FakeWorkoutRepository()
        val entitlementRepository = FakeEntitlementRepository()
        val repository = WorkoutRuntimeRepositoryImpl(
            workoutRepository = workoutRepository,
            entitlementRepository = entitlementRepository,
            motionRepository = FakeMotionRepository(),
            settingsRepository = FakeSettingsRepository(),
            workoutSessionManager = WorkoutSessionManager(),
            workoutServiceController = FakeWorkoutServiceController(),
            repositoryScope = backgroundScope,
        )

        repository.prepareNewWorkout()
        repository.selectExercise(Exercise.BICEPS_CURL)
        advanceUntilIdle()
        repository.addManualRep()
        assertThat(repository.endCurrentSet()).isTrue()
        assertThat(repository.finishWorkout()).isTrue()

        val saved = repository.saveWorkout()
        advanceUntilIdle()

        assertThat(saved).isTrue()
        assertThat(workoutRepository.savedWorkouts).hasSize(1)
    }

    @Test
    fun everyRepMode_emitsRepConfirmedEventsAcrossLongActiveSession() = runTest {
        val motionRepository = FakeMotionRepository()
        val repository = WorkoutRuntimeRepositoryImpl(
            workoutRepository = FakeWorkoutRepository(),
            entitlementRepository = FakeEntitlementRepository(),
            motionRepository = motionRepository,
            settingsRepository = FakeSettingsRepository(
                SettingsState(hapticMode = HapticMode.EVERY_REP),
            ),
            workoutSessionManager = WorkoutSessionManager(),
            workoutServiceController = FakeWorkoutServiceController(),
            repositoryScope = backgroundScope,
        )
        val workoutEvents = mutableListOf<WorkoutRuntimeEvent>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            repository.workoutEvents.collect { workoutEvents += it }
        }

        repository.prepareNewWorkout()
        repository.selectExercise(Exercise.BICEPS_CURL)
        repository.startReadyCountdown()
        advanceTimeBy(3_100L)
        advanceUntilIdle()

        validRepSequence(startAtMs = 10_000L).forEach { motionRepository.emit(it) }
        validRepSequence(startAtMs = 20_000L).forEach { motionRepository.emit(it) }
        advanceUntilIdle()

        assertThat(workoutEvents.count { it == WorkoutRuntimeEvent.RepConfirmed }).isEqualTo(2)
    }
}

private class FakeWorkoutServiceController(
    private var notificationsEnabled: Boolean = true,
) : WorkoutServiceController {
    var startCount: Int = 0
    var stopCount: Int = 0

    override fun start() {
        startCount += 1
    }

    override fun stop() {
        stopCount += 1
    }

    override fun areNotificationsEnabled(): Boolean = notificationsEnabled
}

private class FakeSettingsRepository(
    initialState: SettingsState = SettingsState(),
) : SettingsRepository {
    val state = MutableStateFlow(initialState)

    override fun observeSettings(): Flow<SettingsState> = state.asStateFlow()

    override suspend fun setHapticMode(mode: HapticMode) {
        state.value = state.value.copy(hapticMode = mode)
    }

    override suspend fun setSelectedLanguageTag(languageTag: String?) {
        state.value = state.value.copy(selectedLanguageTag = languageTag)
    }

    override suspend fun currentSelectedLanguageTag(): String? = state.value.selectedLanguageTag
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

    override suspend fun refillFreeTrialsForDebug() = Unit

    override suspend fun reconcileCompletedWorkoutUsage(completedWorkoutCount: Int) = Unit
}

private class FakeMotionRepository : MotionRepository {
    private val samples = MutableSharedFlow<MotionSample>()

    override val motionSamples: Flow<MotionSample> = samples.asSharedFlow()

    override fun startTracking() = Unit

    override fun stopTracking() = Unit

    suspend fun emit(sample: MotionSample) {
        samples.emit(sample)
    }
}

private fun validRepSequence(startAtMs: Long): List<MotionSample> {
    return listOf(
        sample(startAtMs, 9.81f, 0f),
        sample(startAtMs + 300L, 4.0f, 3.0f),
        sample(startAtMs + 600L, 4.0f, 3.0f),
        sample(startAtMs + 900L, 15.0f, 3.0f),
        sample(startAtMs + 1_200L, 15.0f, 3.0f),
        sample(startAtMs + 1_500L, 9.81f, 0.2f),
        sample(startAtMs + 1_800L, 9.81f, 0.2f),
        sample(startAtMs + 2_100L, 9.81f, 0.2f),
        sample(startAtMs + 2_400L, 9.81f, 0.2f),
        sample(startAtMs + 2_700L, 9.81f, 0.2f),
    )
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
