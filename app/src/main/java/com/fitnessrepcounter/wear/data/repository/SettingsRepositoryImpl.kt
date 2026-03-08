package com.fitnessrepcounter.wear.data.repository

import com.fitnessrepcounter.wear.data.local.datastore.SettingsDataStore
import com.fitnessrepcounter.wear.domain.model.HapticMode
import com.fitnessrepcounter.wear.domain.model.SettingsState
import com.fitnessrepcounter.wear.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val settingsDataStore: SettingsDataStore,
) : SettingsRepository {
    override fun observeSettings(): Flow<SettingsState> = settingsDataStore.settingsState

    override suspend fun setHapticMode(mode: HapticMode) {
        settingsDataStore.setHapticMode(mode)
    }

    override suspend fun setSelectedLanguageTag(languageTag: String?) {
        settingsDataStore.setSelectedLanguageTag(languageTag)
    }

    override suspend fun currentSelectedLanguageTag(): String? {
        return settingsDataStore.currentSelectedLanguageTag()
    }
}
