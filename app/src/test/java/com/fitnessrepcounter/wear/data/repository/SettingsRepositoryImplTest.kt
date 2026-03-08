package com.fitnessrepcounter.wear.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.fitnessrepcounter.wear.domain.model.HapticMode
import com.fitnessrepcounter.wear.data.local.datastore.SettingsDataStore
import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryImplTest {
    @Test
    fun defaults_areImportantOnly_andSystemLanguage() = runTest {
        val repository = buildRepository()

        val state = repository.observeSettings().first()

        assertThat(state.hapticMode).isEqualTo(HapticMode.IMPORTANT_ONLY)
        assertThat(state.selectedLanguageTag).isNull()
    }

    @Test
    fun updates_persistHapticMode_andLanguage() = runTest {
        val repository = buildRepository()

        repository.setHapticMode(HapticMode.EVERY_REP)
        repository.setSelectedLanguageTag("tr")

        val state = repository.observeSettings().first()

        assertThat(state.hapticMode).isEqualTo(HapticMode.EVERY_REP)
        assertThat(state.selectedLanguageTag).isEqualTo("tr")
    }

    private fun buildRepository(): SettingsRepositoryImpl {
        val tempFile = File.createTempFile("settings-test", ".preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { tempFile },
        )
        return SettingsRepositoryImpl(SettingsDataStore(dataStore))
    }
}
