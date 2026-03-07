package com.fitnessrepcounter.wear.presentation.viewmodel

import android.app.Activity
import com.fitnessrepcounter.wear.MainDispatcherRule
import com.fitnessrepcounter.wear.domain.model.BillingAvailabilityState
import com.fitnessrepcounter.wear.domain.model.BillingPurchaseLaunchResult
import com.fitnessrepcounter.wear.domain.model.EntitlementState
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import com.fitnessrepcounter.wear.platform.HapticsManager
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaywallViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_syncsBilling_andEnablesUnlockWhenOfferIsReady() = runTest {
        val entitlementRepository = FakePaywallEntitlementRepository(
            initialState = EntitlementState(),
            initialBillingState = BillingAvailabilityState(
                isBillingReady = true,
                isProductAvailable = true,
            ),
        )
        val viewModel = PaywallViewModel(
            entitlementRepository = entitlementRepository,
            hapticsManager = HapticsManager(),
        )

        advanceUntilIdle()

        assertThat(entitlementRepository.syncCallCount).isEqualTo(1)
        assertThat(viewModel.uiState.value.canUnlockPro).isTrue()
    }

    @Test
    fun init_keepsUnlockDisabledWhenProductIsUnavailable() = runTest {
        val entitlementRepository = FakePaywallEntitlementRepository(
            initialState = EntitlementState(),
            initialBillingState = BillingAvailabilityState(
                isBillingReady = true,
                isProductAvailable = false,
            ),
        )
        val viewModel = PaywallViewModel(
            entitlementRepository = entitlementRepository,
            hapticsManager = HapticsManager(),
        )

        advanceUntilIdle()

        assertThat(viewModel.uiState.value.canUnlockPro).isFalse()
        assertThat(viewModel.uiState.value.isBillingReady).isTrue()
    }
}

private class FakePaywallEntitlementRepository(
    initialState: EntitlementState,
    initialBillingState: BillingAvailabilityState,
) : EntitlementRepository {
    val state = MutableStateFlow(initialState)
    val billingState = MutableStateFlow(initialBillingState)
    var syncCallCount: Int = 0
    var launchPurchaseCallCount: Int = 0

    override fun observeEntitlement(): Flow<EntitlementState> = state.asStateFlow()

    override fun observeBillingAvailability(): Flow<BillingAvailabilityState> = billingState.asStateFlow()

    override suspend fun syncBillingState() {
        syncCallCount += 1
    }

    override suspend fun launchProPurchase(activity: Activity): BillingPurchaseLaunchResult {
        launchPurchaseCallCount += 1
        return BillingPurchaseLaunchResult.Launched
    }

    override suspend fun reserveActiveTrialSessionIfNeeded(exercise: Exercise): Boolean = true

    override suspend fun consumeActiveTrialSessionIfNeeded() = Unit

    override suspend fun clearActiveTrialSession() = Unit

    override suspend fun appendActiveTrialUsage(durationMs: Long) = Unit

    override suspend fun refillFreeTrialsForDebug() = Unit

    override suspend fun reconcileCompletedWorkoutUsage(completedWorkoutCount: Int) = Unit
}
