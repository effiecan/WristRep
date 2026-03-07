package com.fitnessrepcounter.wear.presentation.screens.history

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
import com.fitnessrepcounter.wear.presentation.components.CenterMessage
import com.fitnessrepcounter.wear.presentation.components.HistoryRowCard
import com.fitnessrepcounter.wear.presentation.components.ScreenTitle
import com.fitnessrepcounter.wear.presentation.components.SecondaryActionButton
import com.fitnessrepcounter.wear.presentation.components.WristRepScreenScaffold
import com.fitnessrepcounter.wear.presentation.state.HistoryUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onBack: () -> Unit,
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM").withZone(ZoneId.systemDefault())
    val scrollState = rememberScrollState()

    WristRepScreenScaffold(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        ScreenTitle(title = "History")
        Spacer(modifier = Modifier.height(18.dp))
        if (uiState.workouts.isEmpty()) {
            Spacer(modifier = Modifier.weight(1f))
            CenterMessage(
                title = "No workouts yet",
                body = "Completed sessions will appear here.",
            )
            Spacer(modifier = Modifier.weight(1f))
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                uiState.workouts.forEach { workout ->
                    HistoryRowCard(
                        title = workout.exercise.displayName,
                        subtitle = formatter.format(Instant.ofEpochMilli(workout.endedAtEpochMs)),
                        value = "${workout.totalReps}",
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        SecondaryActionButton(
            text = "Back",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(0.62f),
        )
    }
}
