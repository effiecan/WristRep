package com.fitnessrepcounter.wear.presentation.viewmodel

import com.fitnessrepcounter.wear.MainDispatcherRule
import com.fitnessrepcounter.wear.domain.model.EntitlementState
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import com.fitnessrepcounter.wear.platform.HapticsManager
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
class PaywallViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun unlockPro_refillsFreeTrialsForDebug() = runTest {
        val entitlementRepository = FakePaywallEntitlementRepository(
            EntitlementState(
                completedFreeWorkoutsUsed = 3,
                activeTrialSessionId = "trial-session",
                activeTrialReservedAtEpochMs = 123L,
                activeTrialConsumed = true,
            ),
        )
        val viewModel = PaywallViewModel(
            entitlementRepository = entitlementRepository,
            hapticsManager = HapticsManager(),
        )

        viewModel.unlockPro()
        advanceUntilIdle()

        assertThat(entitlementRepository.refillCallCount).isEqualTo(1)
        assertThat(entitlementRepository.unlockStubCallCount).isEqualTo(0)
        assertThat(entitlementRepository.state.value.remainingFreeWorkouts).isEqualTo(3)
        assertThat(entitlementRepository.state.value.isProUnlocked).isFalse()
    }
}

private class FakePaywallEntitlementRepository(
    initialState: EntitlementState,
) : EntitlementRepository {
    val state = MutableStateFlow(initialState)
    var refillCallCount: Int = 0
    var unlockStubCallCount: Int = 0

    override fun observeEntitlement(): Flow<EntitlementState> = state.asStateFlow()

    override suspend fun reserveActiveTrialSessionIfNeeded(exercise: Exercise): Boolean = true

    override suspend fun consumeActiveTrialSessionIfNeeded() = Unit

    override suspend fun clearActiveTrialSession() = Unit

    override suspend fun appendActiveTrialUsage(durationMs: Long) = Unit

    override suspend fun refillFreeTrialsForDebug() {
        refillCallCount += 1
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

    override suspend fun unlockProStub() {
        unlockStubCallCount += 1
    }

    override suspend fun reconcileCompletedWorkoutUsage(completedWorkoutCount: Int) = Unit
}
