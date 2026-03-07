package com.fitnessrepcounter.wear.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import com.fitnessrepcounter.wear.platform.HapticsManager
import com.fitnessrepcounter.wear.presentation.state.PaywallUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PaywallViewModel(
    private val entitlementRepository: EntitlementRepository,
    private val hapticsManager: HapticsManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            entitlementRepository.syncBillingState()
        }

        viewModelScope.launch {
            entitlementRepository.observeEntitlement()
                .combine(entitlementRepository.observeBillingAvailability()) { entitlement, billing ->
                    PaywallUiState(
                        entitlementState = entitlement,
                        isBillingReady = billing.isBillingReady,
                        isProductAvailable = billing.isProductAvailable,
                        isPurchaseInProgress = billing.isPurchaseInProgress,
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    suspend fun unlockPro(activity: Activity) {
        if (!_uiState.value.canUnlockPro) {
            entitlementRepository.syncBillingState()
            return
        }

        hapticsManager.performPaywallTap()
        entitlementRepository.launchProPurchase(activity)
    }
}
