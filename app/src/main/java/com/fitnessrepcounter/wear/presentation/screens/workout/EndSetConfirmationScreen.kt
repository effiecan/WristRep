package com.fitnessrepcounter.wear.presentation.screens.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitnessrepcounter.wear.presentation.components.HeroMetric
import com.fitnessrepcounter.wear.presentation.components.PrimaryActionButton
import com.fitnessrepcounter.wear.presentation.components.ScreenTitle
import com.fitnessrepcounter.wear.presentation.components.SecondaryActionButton
import com.fitnessrepcounter.wear.presentation.components.WristRepScreenScaffold
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState

@Composable
fun EndSetConfirmationScreen(
    uiState: WorkoutUiState,
    onRestClick: () -> Unit,
    onFinishWorkout: () -> Unit,
) {
    val set = uiState.latestCompletedSet

    WristRepScreenScaffold(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        ScreenTitle(title = "Set complete")
        Spacer(modifier = Modifier.weight(1f))
        HeroMetric(
            value = "${set?.repCount ?: 0}",
            label = "Reps logged",
        )
        Spacer(modifier = Modifier.weight(1f))
        PrimaryActionButton(text = "Rest", onClick = onRestClick)
        Spacer(modifier = Modifier.height(8.dp))
        SecondaryActionButton(text = "Finish workout", onClick = onFinishWorkout)
    }
}
