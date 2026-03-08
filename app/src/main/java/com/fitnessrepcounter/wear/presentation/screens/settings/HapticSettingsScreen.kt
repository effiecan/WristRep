package com.fitnessrepcounter.wear.presentation.screens.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.foundation.lazy.items
import com.fitnessrepcounter.wear.R
import com.fitnessrepcounter.wear.domain.model.HapticMode
import com.fitnessrepcounter.wear.presentation.components.CompactUtilityChip
import com.fitnessrepcounter.wear.presentation.components.ListActionChip
import com.fitnessrepcounter.wear.presentation.components.ScreenTitle
import com.fitnessrepcounter.wear.presentation.components.WearListScreenScaffold
import com.fitnessrepcounter.wear.ui.theme.WatchTextSecondary

@Composable
fun HapticSettingsScreen(
    selectedMode: HapticMode,
    onSelectMode: (HapticMode) -> Unit,
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
            ScreenTitle(title = stringResource(R.string.haptics))
        }
        items(HapticMode.entries.toList()) { mode ->
            ListActionChip(
                text = stringResource(mode.toLabelRes()),
                onClick = { onSelectMode(mode) },
                selected = mode == selectedMode,
                modifier = Modifier.fillParentMaxWidth(0.86f),
            )
        }
        if (selectedMode == HapticMode.EVERY_REP) {
            item {
                Text(
                    text = stringResource(R.string.haptics_keep_screen_awake_description),
                    modifier = Modifier
                        .fillParentMaxWidth(0.82f)
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.caption2,
                    color = WatchTextSecondary,
                )
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

private fun HapticMode.toLabelRes(): Int {
    return when (this) {
        HapticMode.OFF -> R.string.haptics_off
        HapticMode.IMPORTANT_ONLY -> R.string.haptics_important_only
        HapticMode.EVERY_REP -> R.string.haptics_every_rep
    }
}
