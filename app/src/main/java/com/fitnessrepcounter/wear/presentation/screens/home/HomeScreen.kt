package com.fitnessrepcounter.wear.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitnessrepcounter.wear.R
import com.fitnessrepcounter.wear.presentation.components.BadgeTone
import com.fitnessrepcounter.wear.presentation.components.CompactUtilityChip
import com.fitnessrepcounter.wear.presentation.components.ListActionChip
import com.fitnessrepcounter.wear.presentation.components.PrimaryActionButton
import com.fitnessrepcounter.wear.presentation.components.ScreenTitle
import com.fitnessrepcounter.wear.presentation.components.WristRepScreenScaffold
import com.fitnessrepcounter.wear.presentation.state.HomeUiState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onStartWorkout: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPremiumClick: () -> Unit,
) {
    val badgeLabel = when {
        uiState.entitlementState.isProUnlocked -> stringResource(R.string.pro_unlocked)
        uiState.entitlementState.remainingFreeWorkouts > 0 -> stringResource(
            R.string.free_workouts_left,
            uiState.entitlementState.remainingFreeWorkouts,
        )
        else -> stringResource(R.string.trial_used)
    }
    val badgeTone = when {
        uiState.entitlementState.isProUnlocked -> BadgeTone.ACCENT
        uiState.entitlementState.remainingFreeWorkouts > 0 -> BadgeTone.WARM
        else -> BadgeTone.MUTED
    }
    WristRepScreenScaffold(verticalArrangement = Arrangement.Top) {
        CompactUtilityChip(
            text = stringResource(R.string.settings),
            onClick = onSettingsClick,
            modifier = Modifier.fillMaxWidth(0.42f),
        )
        Spacer(modifier = Modifier.height(12.dp))
        ScreenTitle(
            title = stringResource(R.string.app_name),
            badgeLabel = badgeLabel,
            badgeTone = badgeTone,
        )
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryActionButton(
            text = stringResource(
                if (uiState.hasActiveWorkout) R.string.resume_workout else R.string.start_workout,
            ),
            onClick = onStartWorkout,
            modifier = Modifier.fillMaxWidth(0.82f),
        )
        Spacer(modifier = Modifier.height(10.dp))
        ListActionChip(
            text = stringResource(R.string.history),
            onClick = onHistoryClick,
            modifier = Modifier.fillMaxWidth(0.72f),
        )
        if (!uiState.entitlementState.isProUnlocked) {
            Spacer(modifier = Modifier.height(8.dp))
            ListActionChip(
                text = stringResource(R.string.go_premium),
                onClick = onPremiumClick,
                modifier = Modifier.fillMaxWidth(0.7f),
            )
        }
    }
}
