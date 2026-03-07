package com.fitnessrepcounter.wear.domain.model

data class BillingAvailabilityState(
    val isBillingReady: Boolean = false,
    val isProductAvailable: Boolean = false,
    val isPurchaseInProgress: Boolean = false,
)

enum class BillingEntitlementStatus {
    UNKNOWN,
    NOT_OWNED,
    PENDING,
    OWNED,
}

sealed interface BillingPurchaseLaunchResult {
    data object Launched : BillingPurchaseLaunchResult
    data object AlreadyOwned : BillingPurchaseLaunchResult
    data object BillingUnavailable : BillingPurchaseLaunchResult
    data object ProductUnavailable : BillingPurchaseLaunchResult
    data object UserCanceled : BillingPurchaseLaunchResult
    data class Failed(val debugMessage: String) : BillingPurchaseLaunchResult
}
