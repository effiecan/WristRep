package com.fitnessrepcounter.wear.domain.repository

import android.app.Activity
import com.fitnessrepcounter.wear.domain.model.BillingAvailabilityState
import com.fitnessrepcounter.wear.domain.model.BillingPurchaseLaunchResult
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.EntitlementState
import kotlinx.coroutines.flow.Flow

interface EntitlementRepository {
    fun observeEntitlement(): Flow<EntitlementState>
    fun observeBillingAvailability(): Flow<BillingAvailabilityState>
    suspend fun syncBillingState()
    suspend fun launchProPurchase(activity: Activity): BillingPurchaseLaunchResult
    suspend fun reserveActiveTrialSessionIfNeeded(exercise: Exercise): Boolean
    suspend fun consumeActiveTrialSessionIfNeeded()
    suspend fun clearActiveTrialSession()
    suspend fun appendActiveTrialUsage(durationMs: Long)
    suspend fun refillFreeTrialsForDebug()
    suspend fun reconcileCompletedWorkoutUsage(completedWorkoutCount: Int)
}
