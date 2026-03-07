package com.fitnessrepcounter.wear.presentation.viewmodel

import com.fitnessrepcounter.wear.MainDispatcherRule
import com.fitnessrepcounter.wear.domain.model.EntitlementState
import com.fitnessrepcounter.wear.domain.model.WorkoutSession
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import com.fitnessrepcounter.wear.domain.repository.WorkoutRepository
import com.fitnessrepcounter.wear.navigation.AppRoute
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        )

        advanceUntilIdle()

        assertThat(viewModel.startWorkoutDestination()).isEqualTo(AppRoute.WorkoutFlow.route)
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

    override fun observeEntitlement(): Flow<EntitlementState> = state.asStateFlow()

    override suspend fun reserveActiveTrialSessionIfNeeded(exercise: com.fitnessrepcounter.wear.domain.model.Exercise): Boolean = true

    override suspend fun consumeActiveTrialSessionIfNeeded() = Unit

    override suspend fun clearActiveTrialSession() = Unit

    override suspend fun appendActiveTrialUsage(durationMs: Long) = Unit

    override suspend fun refillFreeTrialsForDebug() = Unit

    override suspend fun unlockProStub() = Unit

    override suspend fun reconcileCompletedWorkoutUsage(completedWorkoutCount: Int) = Unit
}
