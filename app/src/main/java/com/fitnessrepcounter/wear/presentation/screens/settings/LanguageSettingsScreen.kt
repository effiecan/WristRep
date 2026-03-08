package com.fitnessrepcounter.wear.presentation.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.wear.compose.foundation.lazy.items
import com.fitnessrepcounter.wear.R
import com.fitnessrepcounter.wear.domain.model.LanguageOption
import com.fitnessrepcounter.wear.presentation.components.CompactUtilityChip
import com.fitnessrepcounter.wear.presentation.components.ListActionChip
import com.fitnessrepcounter.wear.presentation.components.ScreenTitle
import com.fitnessrepcounter.wear.presentation.components.WearListScreenScaffold

@Composable
fun LanguageSettingsScreen(
    selectedLanguageTag: String?,
    languageOptions: List<LanguageOption>,
    onSelectLanguage: (String?) -> Unit,
    onBack: () -> Unit,
) {
    WearListScreenScaffold {
        item {
            CompactUtilityChip(
                text = stringResource(R.string.back),
                onClick = onBack,
                modifier = Modifier.fillParentMaxWidth(0.42f),
            )
        }
        item {
            ScreenTitle(title = stringResource(R.string.language))
        }
        item {
            ListActionChip(
                text = stringResource(R.string.language_system_default),
                onClick = { onSelectLanguage(null) },
                selected = selectedLanguageTag == null,
                modifier = Modifier.fillParentMaxWidth(0.9f),
            )
        }
        items(languageOptions) { option ->
            ListActionChip(
                text = option.displayName,
                onClick = { onSelectLanguage(option.tag) },
                selected = selectedLanguageTag == option.tag,
                modifier = Modifier.fillParentMaxWidth(0.9f),
            )
        }
        item {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
