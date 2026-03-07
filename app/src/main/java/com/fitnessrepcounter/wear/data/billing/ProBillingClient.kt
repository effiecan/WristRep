package com.fitnessrepcounter.wear.data.billing

import android.app.Activity
import com.fitnessrepcounter.wear.domain.model.BillingAvailabilityState
import com.fitnessrepcounter.wear.domain.model.BillingEntitlementStatus
import com.fitnessrepcounter.wear.domain.model.BillingPurchaseLaunchResult
import kotlinx.coroutines.flow.StateFlow

interface ProBillingClient {
    val availabilityState: StateFlow<BillingAvailabilityState>
    val entitlementStatus: StateFlow<BillingEntitlementStatus>

    suspend fun sync()

    suspend fun launchPurchase(activity: Activity): BillingPurchaseLaunchResult

    fun dispose()
}
