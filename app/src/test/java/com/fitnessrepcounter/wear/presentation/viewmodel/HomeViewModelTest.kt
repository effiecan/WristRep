package com.fitnessrepcounter.wear.presentation.viewmodel

import android.app.Activity
import com.fitnessrepcounter.wear.MainDispatcherRule
import com.fitnessrepcounter.wear.domain.model.BillingAvailabilityState
import com.fitnessrepcounter.wear.domain.model.BillingPurchaseLaunchResult
import com.fitnessrepcounter.wear.domain.model.EntitlementState
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.WorkoutRuntimeEvent
import com.fitnessrepcounter.wear.domain.model.WorkoutSession
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import com.fitnessrepcounter.wear.domain.repository.WorkoutRepository
import com.fitnessrepcounter.wear.domain.repository.WorkoutRuntimeRepository
import com.fitnessrepcounter.wear.navigation.AppRoute
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun startWorkoutDestination_goesToPaywallWhenFreeLimitIsExhausted() = runTest {
        val entitlementRepository = HomeFakeEntitlementRepository(
            initialState = EntitlementState(completedFreeWorkoutsUsed = 3),
        )
        val viewModel = HomeViewModel(
            workoutRepository = HomeFakeWorkoutRepository(),
            entitlementRepository = entitlementRepository,
            workoutRuntimeRepository = FakeWorkoutRuntimeRepository(),
        )

        advanceUntilIdle()

        assertThat(viewModel.startWorkoutDestination()).isEqualTo(AppRoute.Paywall.route)
    }

    @Test
    fun startWorkoutDestination_allowsReservedActiveTrial() = runTest {
        val entitlementRepository = HomeFakeEntitlementRepository(
            initialState = EntitlementState(
                completedFreeWorkoutsUsed = 3,
                activeTrialSessionId = "trial-1",
                activeTrialReservedAtEpochMs = 123L,
            ),
        )
        val viewModel = HomeViewModel(
            workoutRepository = HomeFakeWorkoutRepository(),
            entitlementRepository = entitlementRepository,
            workoutRuntimeRepository = FakeWorkoutRuntimeRepository(),
        )

        advanceUntilIdle()

        assertThat(viewModel.startWorkoutDestination()).isEqualTo(AppRoute.WorkoutFlow.route)
    }

    @Test
    fun startWorkoutDestination_resumesCurrentWorkoutWhenActiveSessionExists() = runTest {
        val viewModel = HomeViewModel(
            workoutRepository = HomeFakeWorkoutRepository(),
            entitlementRepository = HomeFakeEntitlementRepository(initialState = EntitlementState()),
            workoutRuntimeRepository = FakeWorkoutRuntimeRepository(hasActiveSession = true),
        )

        advanceUntilIdle()

        assertThat(viewModel.startWorkoutDestination()).isEqualTo(AppRoute.ActiveWorkout.route)
    }
}

private class HomeFakeWorkoutRepository : WorkoutRepository {
    override fun observeHistory(): Flow<List<WorkoutSession>> =
        MutableStateFlow<List<WorkoutSession>>(emptyList()).asStateFlow()

    override suspend fun saveCompletedWorkout(session: WorkoutSession) = Unit

    override suspend fun countCompletedWorkouts(): Int = 0
}

private class HomeFakeEntitlementRepository(
    initialState: EntitlementState,
) : EntitlementRepository {
    private val state = MutableStateFlow(initialState)
    private val billingState = MutableStateFlow(BillingAvailabilityState())

    override fun observeEntitlement(): Flow<EntitlementState> = state.asStateFlow()

    override fun observeBillingAvailability(): Flow<BillingAvailabilityState> = billingState.asStateFlow()

    override suspend fun syncBillingState() = Unit

    override suspend fun launchProPurchase(activity: Activity): BillingPurchaseLaunchResult {
        return BillingPurchaseLaunchResult.ProductUnavailable
    }

    override suspend fun reserveActiveTrialSessionIfNeeded(exercise: com.fitnessrepcounter.wear.domain.model.Exercise): Boolean = true

    override suspend fun consumeActiveTrialSessionIfNeeded() = Unit

    override suspend fun clearActiveTrialSession() = Unit

    override suspend fun appendActiveTrialUsage(durationMs: Long) = Unit

    override suspend fun refillFreeTrialsForDebug() = Unit

    override suspend fun reconcileCompletedWorkoutUsage(completedWorkoutCount: Int) = Unit
}

private class FakeWorkoutRuntimeRepository(
    hasActiveSession: Boolean = false,
) : WorkoutRuntimeRepository {
    override val uiState = MutableStateFlow(com.fitnessrepcounter.wear.presentation.state.WorkoutUiState()).asStateFlow()
    override val hasActiveSession = MutableStateFlow(hasActiveSession).asStateFlow()
    override val canExposeOngoingEntry = MutableStateFlow(false).asStateFlow()
    override val shouldRequestNotificationPermission = MutableStateFlow(false).asStateFlow()
    override val workoutEvents: Flow<WorkoutRuntimeEvent> = emptyFlow()

    override fun currentStep() = com.fitnessrepcounter.wear.presentation.state.WorkoutStep.ACTIVE
    override fun prepareNewWorkout() = Unit
    override fun selectExercise(exercise: Exercise) = Unit
    override fun startReadyCountdown() = Unit
    override fun addManualRep() = Unit
    override fun removeManualRep() = Unit
    override fun endCurrentSet(): Boolean = false
    override fun beginRestTimer() = Unit
    override fun skipRestTimer() = Unit
    override fun finishWorkout(): Boolean = false
    override suspend fun saveWorkout(): Boolean = false
    override fun discardWorkout() = Unit
    override fun onNotificationPermissionPromptHandled(granted: Boolean) = Unit
}
