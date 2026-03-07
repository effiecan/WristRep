package com.fitnessrepcounter.wear.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitnessrepcounter.wear.presentation.components.BadgeTone
import com.fitnessrepcounter.wear.presentation.components.PrimaryActionButton
import com.fitnessrepcounter.wear.presentation.components.ScreenTitle
import com.fitnessrepcounter.wear.presentation.components.SecondaryActionButton
import com.fitnessrepcounter.wear.presentation.components.WristRepScreenScaffold
import com.fitnessrepcounter.wear.presentation.state.HomeUiState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onStartWorkout: () -> Unit,
    onHistoryClick: () -> Unit,
) {
    val badgeLabel = when {
        uiState.entitlementState.isProUnlocked -> "Pro unlocked"
        uiState.entitlementState.remainingFreeWorkouts > 0 -> "${uiState.entitlementState.remainingFreeWorkouts} free left"
        else -> "Trial used"
    }
    val badgeTone = when {
        uiState.entitlementState.isProUnlocked -> BadgeTone.ACCENT
        uiState.entitlementState.remainingFreeWorkouts > 0 -> BadgeTone.WARM
        else -> BadgeTone.MUTED
    }
    WristRepScreenScaffold(verticalArrangement = Arrangement.Top) {
        ScreenTitle(
            title = "WristRep",
            badgeLabel = badgeLabel,
            badgeTone = badgeTone,
        )
        Spacer(modifier = Modifier.height(18.dp))
        PrimaryActionButton(
            text = "Start workout",
            onClick = onStartWorkout,
            modifier = Modifier.fillMaxWidth(0.82f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        SecondaryActionButton(
            text = "History",
            onClick = onHistoryClick,
            modifier = Modifier.fillMaxWidth(0.68f),
        )
    }
}
