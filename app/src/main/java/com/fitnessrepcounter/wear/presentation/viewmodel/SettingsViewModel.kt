package com.fitnessrepcounter.wear.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessrepcounter.wear.domain.model.HapticMode
import com.fitnessrepcounter.wear.domain.model.LanguageOption
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import com.fitnessrepcounter.wear.domain.repository.SettingsRepository
import com.fitnessrepcounter.wear.presentation.state.SettingsUiState
import java.util.Locale
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val entitlementRepository: EntitlementRepository,
) : ViewModel() {
    private val languageOptions by lazy {
        SUPPORTED_LANGUAGE_TAGS.map { tag ->
            LanguageOption(
                tag = tag,
                displayName = Locale.forLanguageTag(tag).getDisplayName(Locale.forLanguageTag(tag)),
            )
        }
    }

    private val _uiState = MutableStateFlow(SettingsUiState(languageOptions = languageOptions))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            entitlementRepository.observeEntitlement()
                .combine(settingsRepository.observeSettings()) { entitlement, settings ->
                    SettingsUiState(
                        entitlementState = entitlement,
                        hapticMode = settings.hapticMode,
                        selectedLanguageTag = settings.selectedLanguageTag,
                        languageOptions = languageOptions,
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun setHapticMode(mode: HapticMode) {
        viewModelScope.launch {
            settingsRepository.setHapticMode(mode)
        }
    }

    fun setLanguage(languageTag: String?) {
        viewModelScope.launch {
            settingsRepository.setSelectedLanguageTag(languageTag)
        }
    }

    companion object {
        val SUPPORTED_LANGUAGE_TAGS: List<String> = listOf(
            "ar",
            "bn",
            "ca",
            "cs",
            "da",
            "de",
            "el",
            "es",
            "es-419",
            "fi",
            "fr",
            "he",
            "hi",
            "hr",
            "hu",
            "id",
            "it",
            "ja",
            "ko",
            "ms",
            "nb",
            "nl",
            "pl",
            "pt-BR",
            "pt-PT",
            "ro",
            "ru",
            "sk",
            "sv",
            "ta",
            "th",
            "fil",
            "tr",
            "uk",
            "ur",
            "vi",
            "zh-CN",
            "zh-TW",
        )
    }
}
