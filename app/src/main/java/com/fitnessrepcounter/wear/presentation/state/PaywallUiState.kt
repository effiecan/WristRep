package com.fitnessrepcounter.wear.presentation.state

import com.fitnessrepcounter.wear.domain.model.EntitlementState

data class PaywallUiState(
    val entitlementState: EntitlementState = EntitlementState(),
)
