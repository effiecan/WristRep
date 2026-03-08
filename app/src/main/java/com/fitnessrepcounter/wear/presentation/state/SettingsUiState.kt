package com.fitnessrepcounter.wear.presentation.state

import com.fitnessrepcounter.wear.domain.model.EntitlementState
import com.fitnessrepcounter.wear.domain.model.HapticMode
import com.fitnessrepcounter.wear.domain.model.LanguageOption

data class SettingsUiState(
    val entitlementState: EntitlementState = EntitlementState(),
    val hapticMode: HapticMode = HapticMode.IMPORTANT_ONLY,
    val selectedLanguageTag: String? = null,
    val languageOptions: List<LanguageOption> = emptyList(),
)
