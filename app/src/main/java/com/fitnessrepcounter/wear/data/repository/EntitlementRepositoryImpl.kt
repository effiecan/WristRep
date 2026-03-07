package com.fitnessrepcounter.wear.data.repository

import android.app.Activity
import com.fitnessrepcounter.wear.data.billing.ProBillingClient
import com.fitnessrepcounter.wear.data.local.datastore.EntitlementDataStore
import com.fitnessrepcounter.wear.domain.model.BillingAvailabilityState
import com.fitnessrepcounter.wear.domain.model.BillingEntitlementStatus
import com.fitnessrepcounter.wear.domain.model.BillingPurchaseLaunchResult
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.EntitlementState
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class EntitlementRepositoryImpl(
    private val entitlementDataStore: EntitlementDataStore,
    private val billingClient: ProBillingClient,
    private val repositoryScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : EntitlementRepository {
    init {
        repositoryScope.launch {
            billingClient.entitlementStatus.collect { status ->
                when (status) {
                    BillingEntitlementStatus.OWNED -> entitlementDataStore.setProUnlocked(true)
                    BillingEntitlementStatus.NOT_OWNED,
                    BillingEntitlementStatus.PENDING,
                    -> entitlementDataStore.setProUnlocked(false)
                    BillingEntitlementStatus.UNKNOWN -> Unit
                }
            }
        }
    }

    override fun observeEntitlement(): Flow<EntitlementState> = entitlementDataStore.entitlementState

    override fun observeBillingAvailability(): Flow<BillingAvailabilityState> = billingClient.availabilityState

    override suspend fun syncBillingState() {
        billingClient.sync()
    }

    override suspend fun launchProPurchase(activity: Activity): BillingPurchaseLaunchResult {
        return billingClient.launchPurchase(activity)
    }

    override suspend fun reserveActiveTrialSessionIfNeeded(exercise: Exercise): Boolean {
        return entitlementDataStore.reserveActiveTrialSessionIfNeeded(exercise)
    }

    override suspend fun consumeActiveTrialSessionIfNeeded() {
        entitlementDataStore.consumeActiveTrialSessionIfNeeded()
    }

    override suspend fun clearActiveTrialSession() {
        entitlementDataStore.clearActiveTrialSession()
    }

    override suspend fun appendActiveTrialUsage(durationMs: Long) {
        entitlementDataStore.appendActiveTrialUsage(durationMs)
    }

    override suspend fun refillFreeTrialsForDebug() {
        entitlementDataStore.refillFreeTrialsForDebug()
    }

    override suspend fun reconcileCompletedWorkoutUsage(completedWorkoutCount: Int) {
        entitlementDataStore.reconcileCompletedWorkoutUsage(completedWorkoutCount)
    }
}
