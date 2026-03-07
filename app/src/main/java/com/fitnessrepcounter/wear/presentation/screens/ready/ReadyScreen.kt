package com.fitnessrepcounter.wear.presentation.screens.ready

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.fitnessrepcounter.wear.R
import com.fitnessrepcounter.wear.presentation.components.HeroMetric
import com.fitnessrepcounter.wear.presentation.components.ScreenTitle
import com.fitnessrepcounter.wear.presentation.components.WristRepScreenScaffold
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState

@Composable
fun ReadyScreen(
    uiState: WorkoutUiState,
) {
    val countdownText = if (uiState.countdownValue <= 0) {
        stringResource(R.string.go)
    } else {
        uiState.countdownValue.toString()
    }

    WristRepScreenScaffold(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ScreenTitle(
            title = uiState.selectedExercise?.let { stringResource(it.displayNameRes) }
                ?: stringResource(R.string.get_ready),
        )
        Spacer(modifier = androidx.compose.ui.Modifier.weight(1f))
        HeroMetric(
            value = countdownText,
            label = stringResource(R.string.starting_now),
        )
        Spacer(modifier = androidx.compose.ui.Modifier.weight(1f))
    }
}
