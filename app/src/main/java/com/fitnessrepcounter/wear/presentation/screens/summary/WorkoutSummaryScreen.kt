package com.fitnessrepcounter.wear.presentation.screens.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fitnessrepcounter.wear.R
import com.fitnessrepcounter.wear.presentation.components.HistoryRowCard
import com.fitnessrepcounter.wear.presentation.components.PrimaryActionButton
import com.fitnessrepcounter.wear.presentation.components.ScreenTitle
import com.fitnessrepcounter.wear.presentation.components.SecondaryActionButton
import com.fitnessrepcounter.wear.presentation.components.SummaryCard
import com.fitnessrepcounter.wear.presentation.components.WristRepScreenScaffold
import com.fitnessrepcounter.wear.presentation.state.AmbientModeState
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState

@Composable
fun WorkoutSummaryScreen(
    uiState: WorkoutUiState,
    ambientModeState: AmbientModeState = AmbientModeState(),
    onSave: () -> Unit,
    onDiscard: () -> Unit,
) {
    val scrollState = rememberScrollState()

    WristRepScreenScaffold(
        ambientModeState = ambientModeState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        ScreenTitle(title = stringResource(R.string.summary))
        Spacer(modifier = Modifier.height(18.dp))
        SummaryCard(
            title = stringResource(R.string.workout_total),
            subtitle = uiState.selectedExercise?.let { stringResource(it.displayNameRes) }
                ?: stringResource(R.string.workout),
            value = "${uiState.totalReps} ${stringResource(R.string.reps)}",
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (!ambientModeState.isAmbient) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                uiState.completedSets.forEach { set ->
                    HistoryRowCard(
                        title = stringResource(R.string.set_number, set.setNumber),
                        subtitle = stringResource(R.string.manual_edits_count, set.manualAdjustmentCount),
                        value = "${set.repCount}",
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        if (!ambientModeState.isAmbient) {
            Spacer(modifier = Modifier.height(10.dp))
            PrimaryActionButton(
                text = stringResource(R.string.watch_cta_save),
                onClick = onSave,
                enabled = uiState.canSave,
                modifier = Modifier.fillMaxWidth(0.7f),
                height = 36.dp,
                maxLines = 2,
            )
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryActionButton(
                text = stringResource(R.string.watch_cta_discard),
                onClick = onDiscard,
                modifier = Modifier.fillMaxWidth(0.68f),
                height = 34.dp,
                maxLines = 2,
            )
        }
    }
}
