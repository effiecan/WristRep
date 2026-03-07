package com.fitnessrepcounter.wear.presentation.screens.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fitnessrepcounter.wear.R
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
        ScreenTitle(title = stringResource(R.string.set_complete))
        Spacer(modifier = Modifier.weight(1f))
        HeroMetric(
            value = "${set?.repCount ?: 0}",
            label = stringResource(R.string.reps_logged),
        )
        Spacer(modifier = Modifier.weight(1f))
        PrimaryActionButton(
            text = stringResource(R.string.watch_cta_rest),
            onClick = onRestClick,
            modifier = Modifier.fillMaxWidth(0.72f),
            height = 36.dp,
            maxLines = 2,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SecondaryActionButton(
            text = stringResource(R.string.watch_cta_finish_workout),
            onClick = onFinishWorkout,
            modifier = Modifier.fillMaxWidth(0.68f),
            height = 34.dp,
            maxLines = 2,
        )
    }
}
