package com.fitnessrepcounter.wear.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import com.fitnessrepcounter.wear.platform.HapticsManager
import com.fitnessrepcounter.wear.presentation.state.PaywallUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaywallViewModel(
    private val entitlementRepository: EntitlementRepository,
    private val hapticsManager: HapticsManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            entitlementRepository.observeEntitlement().collect { entitlement ->
                _uiState.value = PaywallUiState(entitlementState = entitlement)
            }
        }
    }

    suspend fun unlockPro() {
        hapticsManager.performPaywallTap()
        entitlementRepository.refillFreeTrialsForDebug()
    }
}
