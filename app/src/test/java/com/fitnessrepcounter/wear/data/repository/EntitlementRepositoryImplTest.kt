package com.fitnessrepcounter.wear.data.repository

import android.app.Activity
import com.fitnessrepcounter.wear.data.billing.ProBillingClient
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.fitnessrepcounter.wear.data.local.datastore.EntitlementDataStore
import com.fitnessrepcounter.wear.domain.model.BillingAvailabilityState
import com.fitnessrepcounter.wear.domain.model.BillingEntitlementStatus
import com.fitnessrepcounter.wear.domain.model.BillingPurchaseLaunchResult
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EntitlementRepositoryImplTest {
    @Test
    fun consumeActiveTrialSession_capsAtThree_andOnlyConsumesOncePerReservation() = runTest {
        val repository = buildRepository(backgroundScope)

        repeat(4) {
            repository.reserveActiveTrialSessionIfNeeded(Exercise.BICEPS_CURL)
            repository.consumeActiveTrialSessionIfNeeded()
            repository.consumeActiveTrialSessionIfNeeded()
            repository.clearActiveTrialSession()
        }

        val state = repository.observeEntitlement().first()
        assertThat(state.completedFreeWorkoutsUsed).isEqualTo(3)
        assertThat(state.canStartWorkout).isFalse()
    }

    @Test
    fun reconcileCompletedWorkoutUsage_usesSavedWorkoutCount() = runTest {
        val repository = buildRepository(backgroundScope)

        repository.reserveActiveTrialSessionIfNeeded(Exercise.BICEPS_CURL)
        repository.consumeActiveTrialSessionIfNeeded()
        repository.reconcileCompletedWorkoutUsage(completedWorkoutCount = 3)

        val state = repository.observeEntitlement().first()
        assertThat(state.completedFreeWorkoutsUsed).isEqualTo(3)
        assertThat(state.remainingFreeWorkouts).isEqualTo(0)
    }

    @Test
    fun reserveAndClearActiveTrial_preservesFreeUseWhenNeverConsumed() = runTest {
        val repository = buildRepository(backgroundScope)

        val reserved = repository.reserveActiveTrialSessionIfNeeded(Exercise.TRICEPS_EXTENSION)
        repository.appendActiveTrialUsage(5_000L)
        repository.clearActiveTrialSession()

        val state = repository.observeEntitlement().first()
        assertThat(reserved).isTrue()
        assertThat(state.completedFreeWorkoutsUsed).isEqualTo(0)
        assertThat(state.activeTrialSessionId).isNull()
    }

    @Test
    fun activeReservation_keepsWorkoutStartAvailable_whenFreeCountIsExhausted() = runTest {
        val repository = buildRepository(backgroundScope)

        repeat(2) {
            repository.reserveActiveTrialSessionIfNeeded(Exercise.BICEPS_CURL)
            repository.consumeActiveTrialSessionIfNeeded()
            repository.clearActiveTrialSession()
        }

        repository.reserveActiveTrialSessionIfNeeded(Exercise.TRICEPS_EXTENSION)
        repository.consumeActiveTrialSessionIfNeeded()
        val consumedState = repository.observeEntitlement().first()
        assertThat(consumedState.canStartWorkout).isFalse()

        repository.clearActiveTrialSession()
        val blockedState = repository.observeEntitlement().first()
        assertThat(blockedState.canStartWorkout).isFalse()
    }

    @Test
    fun refillFreeTrialsForDebug_resetsUsage_withoutUnlockingPro() = runTest {
        val repository = buildRepository(backgroundScope)

        repository.reserveActiveTrialSessionIfNeeded(Exercise.BICEPS_CURL)
        repository.consumeActiveTrialSessionIfNeeded()
        repository.refillFreeTrialsForDebug()

        val state = repository.observeEntitlement().first()
        assertThat(state.completedFreeWorkoutsUsed).isEqualTo(0)
        assertThat(state.remainingFreeWorkouts).isEqualTo(3)
        assertThat(state.activeTrialSessionId).isNull()
        assertThat(state.isProUnlocked).isFalse()
    }

    @Test
    fun ownedBillingPurchase_unlocksPro() = runTest {
        val billingClient = FakeProBillingClient()
        val repository = buildRepository(backgroundScope, billingClient)

        billingClient.entitlementStatusFlow.value = BillingEntitlementStatus.OWNED
        advanceUntilIdle()
        val state = repository.observeEntitlement().first { it.isProUnlocked }
        assertThat(state.isProUnlocked).isTrue()
    }

    @Test
    fun pendingBillingPurchase_doesNotUnlockPro() = runTest {
        val billingClient = FakeProBillingClient()
        val repository = buildRepository(backgroundScope, billingClient)

        billingClient.entitlementStatusFlow.value = BillingEntitlementStatus.PENDING
        advanceUntilIdle()

        val state = repository.observeEntitlement().first()
        assertThat(state.isProUnlocked).isFalse()
    }

    @Test
    fun syncBillingState_delegatesToBillingClient() = runTest {
        val billingClient = FakeProBillingClient()
        val repository = buildRepository(backgroundScope, billingClient)

        repository.syncBillingState()

        assertThat(billingClient.syncCallCount).isEqualTo(1)
    }

    private fun buildRepository(
        scope: CoroutineScope,
        billingClient: FakeProBillingClient = FakeProBillingClient(),
    ): EntitlementRepositoryImpl {
        val file = File(createTempDirectory().toFile(), "entitlement.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file },
        )
        return EntitlementRepositoryImpl(
            entitlementDataStore = EntitlementDataStore(dataStore),
            billingClient = billingClient,
            repositoryScope = scope,
        )
    }
}

private class FakeProBillingClient : ProBillingClient {
    val availabilityFlow = MutableStateFlow(BillingAvailabilityState())
    val entitlementStatusFlow = MutableStateFlow(BillingEntitlementStatus.UNKNOWN)
    var syncCallCount: Int = 0

    override val availabilityState = availabilityFlow.asStateFlow()
    override val entitlementStatus = entitlementStatusFlow.asStateFlow()

    override suspend fun sync() {
        syncCallCount += 1
    }

    override suspend fun launchPurchase(activity: Activity): BillingPurchaseLaunchResult {
        return BillingPurchaseLaunchResult.Launched
    }

    override fun dispose() = Unit
}
