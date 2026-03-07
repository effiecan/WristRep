package com.fitnessrepcounter.wear.presentation.screens.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitnessrepcounter.wear.presentation.components.HeroMetric
import com.fitnessrepcounter.wear.presentation.components.ScreenTitle
import com.fitnessrepcounter.wear.presentation.components.SecondaryActionButton
import com.fitnessrepcounter.wear.presentation.components.WristRepScreenScaffold
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState

@Composable
fun RestTimerScreen(
    uiState: WorkoutUiState,
    onSkip: () -> Unit,
) {
    WristRepScreenScaffold(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        ScreenTitle(title = "Rest")
        Spacer(modifier = Modifier.weight(1f))
        HeroMetric(
            value = uiState.restSecondsRemaining.toString(),
            label = "Seconds left",
        )
        Spacer(modifier = Modifier.weight(1f))
        SecondaryActionButton(
            text = "Skip",
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(0.54f),
            height = 34.dp,
        )
    }
}
