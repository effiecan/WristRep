package com.fitnessrepcounter.wear.presentation.screens.ready

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.fitnessrepcounter.wear.presentation.components.HeroMetric
import com.fitnessrepcounter.wear.presentation.components.ScreenTitle
import com.fitnessrepcounter.wear.presentation.components.WristRepScreenScaffold
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState

@Composable
fun ReadyScreen(
    uiState: WorkoutUiState,
) {
    val countdownText = if (uiState.countdownValue <= 0) "Go" else uiState.countdownValue.toString()

    WristRepScreenScaffold(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ScreenTitle(title = uiState.selectedExercise?.displayName ?: "Get ready")
        Spacer(modifier = androidx.compose.ui.Modifier.weight(1f))
        HeroMetric(
            value = countdownText,
            label = "Starting now",
        )
        Spacer(modifier = androidx.compose.ui.Modifier.weight(1f))
    }
}
