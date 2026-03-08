package com.fitnessrepcounter.wear.domain.model

data class SettingsState(
    val hapticMode: HapticMode = HapticMode.IMPORTANT_ONLY,
    val selectedLanguageTag: String? = null,
)
