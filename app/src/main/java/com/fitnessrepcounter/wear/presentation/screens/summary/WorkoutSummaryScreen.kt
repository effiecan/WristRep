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
import androidx.compose.ui.unit.dp
import com.fitnessrepcounter.wear.presentation.components.HistoryRowCard
import com.fitnessrepcounter.wear.presentation.components.PrimaryActionButton
import com.fitnessrepcounter.wear.presentation.components.ScreenTitle
import com.fitnessrepcounter.wear.presentation.components.SecondaryActionButton
import com.fitnessrepcounter.wear.presentation.components.SummaryCard
import com.fitnessrepcounter.wear.presentation.components.WristRepScreenScaffold
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState

@Composable
fun WorkoutSummaryScreen(
    uiState: WorkoutUiState,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
) {
    val scrollState = rememberScrollState()

    WristRepScreenScaffold(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        ScreenTitle(title = "Summary")
        Spacer(modifier = Modifier.height(18.dp))
        SummaryCard(
            title = "Workout total",
            subtitle = uiState.selectedExercise?.displayName ?: "Workout",
            value = "${uiState.totalReps} reps",
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            uiState.completedSets.forEach { set ->
                HistoryRowCard(
                    title = "Set ${set.setNumber}",
                    subtitle = "Manual edits ${set.manualAdjustmentCount}",
                    value = "${set.repCount}",
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        PrimaryActionButton(
            text = "Save",
            onClick = onSave,
            enabled = uiState.canSave,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SecondaryActionButton(
            text = "Discard",
            onClick = onDiscard,
        )
    }
}
