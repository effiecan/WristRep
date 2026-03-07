package com.fitnessrepcounter.wear.presentation.state

import com.fitnessrepcounter.wear.domain.model.EntitlementState

data class PaywallUiState(
    val entitlementState: EntitlementState = EntitlementState(),
    val isBillingReady: Boolean = false,
    val isProductAvailable: Boolean = false,
    val isPurchaseInProgress: Boolean = false,
    val formattedPrice: String? = null,
) {
    val canUnlockPro: Boolean = isBillingReady &&
        isProductAvailable &&
        !isPurchaseInProgress &&
        !entitlementState.isProUnlocked
}
