package com.fitnessrepcounter.wear.data.repository

import com.fitnessrepcounter.wear.data.local.datastore.EntitlementDataStore
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.EntitlementState
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import kotlinx.coroutines.flow.Flow

class EntitlementRepositoryImpl(
    private val entitlementDataStore: EntitlementDataStore,
) : EntitlementRepository {
    override fun observeEntitlement(): Flow<EntitlementState> = entitlementDataStore.entitlementState

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

    override suspend fun unlockProStub() {
        entitlementDataStore.setProUnlocked(true)
    }

    override suspend fun reconcileCompletedWorkoutUsage(completedWorkoutCount: Int) {
        entitlementDataStore.reconcileCompletedWorkoutUsage(completedWorkoutCount)
    }
}
