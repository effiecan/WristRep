package com.fitnessrepcounter.wear.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fitnessrepcounter.wear.domain.model.HapticMode
import com.fitnessrepcounter.wear.domain.model.SettingsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SettingsDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    val settingsState: Flow<SettingsState> = dataStore.data.map { preferences ->
        SettingsState(
            hapticMode = preferences[HAPTIC_MODE]
                ?.let { stored -> HapticMode.entries.firstOrNull { it.name == stored } }
                ?: HapticMode.IMPORTANT_ONLY,
            selectedLanguageTag = preferences[SELECTED_LANGUAGE_TAG],
        )
    }

    suspend fun setHapticMode(mode: HapticMode) {
        dataStore.edit { preferences ->
            preferences[HAPTIC_MODE] = mode.name
        }
    }

    suspend fun setSelectedLanguageTag(languageTag: String?) {
        dataStore.edit { preferences ->
            if (languageTag.isNullOrBlank()) {
                preferences.remove(SELECTED_LANGUAGE_TAG)
            } else {
                preferences[SELECTED_LANGUAGE_TAG] = languageTag
            }
        }
    }

    suspend fun currentSelectedLanguageTag(): String? {
        return settingsState.first().selectedLanguageTag
    }

    private companion object {
        val HAPTIC_MODE = stringPreferencesKey("haptic_mode")
        val SELECTED_LANGUAGE_TAG = stringPreferencesKey("selected_language_tag")
    }
}
