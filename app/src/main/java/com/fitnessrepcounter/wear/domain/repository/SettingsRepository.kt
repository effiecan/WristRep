package com.fitnessrepcounter.wear.domain.repository

import com.fitnessrepcounter.wear.domain.model.HapticMode
import com.fitnessrepcounter.wear.domain.model.SettingsState
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<SettingsState>
    suspend fun setHapticMode(mode: HapticMode)
    suspend fun setSelectedLanguageTag(languageTag: String?)
    suspend fun currentSelectedLanguageTag(): String?
}
