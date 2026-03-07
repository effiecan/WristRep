package com.fitnessrepcounter.wear.domain.repository

import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.EntitlementState
import kotlinx.coroutines.flow.Flow

interface EntitlementRepository {
    fun observeEntitlement(): Flow<EntitlementState>
    suspend fun reserveActiveTrialSessionIfNeeded(exercise: Exercise): Boolean
    suspend fun consumeActiveTrialSessionIfNeeded()
    suspend fun clearActiveTrialSession()
    suspend fun appendActiveTrialUsage(durationMs: Long)
    suspend fun refillFreeTrialsForDebug()
    suspend fun unlockProStub()
    suspend fun reconcileCompletedWorkoutUsage(completedWorkoutCount: Int)
}
