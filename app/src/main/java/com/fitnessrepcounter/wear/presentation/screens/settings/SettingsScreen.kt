package com.fitnessrepcounter.wear.presentation.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fitnessrepcounter.wear.R
import com.fitnessrepcounter.wear.presentation.components.CompactUtilityChip
import com.fitnessrepcounter.wear.presentation.components.ListActionChip
import com.fitnessrepcounter.wear.presentation.components.ScreenTitle
import com.fitnessrepcounter.wear.presentation.components.WearListScreenScaffold
import com.fitnessrepcounter.wear.presentation.state.SettingsUiState
import androidx.compose.foundation.layout.height
import androidx.wear.compose.foundation.lazy.items

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onHapticsClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onPremiumClick: () -> Unit,
    onBack: () -> Unit,
) {
    val rows = listOf(
        stringResource(R.string.haptics) to onHapticsClick,
        stringResource(R.string.language) to onLanguageClick,
        stringResource(
            if (uiState.entitlementState.isProUnlocked) R.string.manage_premium else R.string.go_premium,
        ) to onPremiumClick,
    )

    WearListScreenScaffold {
        item {
            CompactUtilityChip(
                text = stringResource(R.string.back),
                onClick = onBack,
                modifier = Modifier.fillParentMaxWidth(0.42f),
            )
        }
        item {
            ScreenTitle(title = stringResource(R.string.settings))
        }
        items(rows) { (label, onClick) ->
            ListActionChip(
                text = label,
                onClick = onClick,
                modifier = Modifier
                    .fillParentMaxWidth(0.82f)
                    .testTag("settings_row_$label"),
            )
        }
        item {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
