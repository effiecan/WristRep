package com.fitnessrepcounter.wear.presentation.screens.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.isSelectable
import com.fitnessrepcounter.wear.domain.model.isVisibleInList
import com.fitnessrepcounter.wear.domain.model.statusLabel
import com.fitnessrepcounter.wear.domain.model.supportingLabel
import com.fitnessrepcounter.wear.presentation.components.BadgeTone
import com.fitnessrepcounter.wear.presentation.components.ExerciseRowCard
import com.fitnessrepcounter.wear.presentation.components.ScrollFadeHint
import com.fitnessrepcounter.wear.presentation.components.SecondaryActionButton
import com.fitnessrepcounter.wear.presentation.components.WristRepScreenScaffold
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun ExerciseSelectionScreen(
    uiState: WorkoutUiState,
    onSelectExercise: (Exercise) -> Unit,
    onBack: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val exercises = Exercise.entries.filter { it.isVisibleInList }

    WristRepScreenScaffold(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Exercises",
            style = MaterialTheme.typography.title1,
        )
        Spacer(modifier = Modifier.height(6.dp))
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .heightIn(min = 0.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                exercises.forEach { exercise ->
                    ExerciseRowCard(
                        title = exercise.displayName,
                        subtitle = exercise.supportingLabel,
                        statusLabel = exercise.statusLabel,
                        badgeTone = when (exercise.supportLevel) {
                            com.fitnessrepcounter.wear.domain.model.ExerciseSupportLevel.OPTIMIZED -> BadgeTone.ACCENT
                            com.fitnessrepcounter.wear.domain.model.ExerciseSupportLevel.EXPERIMENTAL -> BadgeTone.WARM
                            com.fitnessrepcounter.wear.domain.model.ExerciseSupportLevel.INTERNAL -> BadgeTone.MUTED
                        },
                        enabled = exercise.isSelectable,
                        onClick = { onSelectExercise(exercise) },
                        minHeight = 58.dp,
                    )
                }
            }
            ScrollFadeHint(modifier = Modifier.align(Alignment.BottomCenter))
        }
        Spacer(modifier = Modifier.height(8.dp))
        SecondaryActionButton(
            text = "Back",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(0.46f),
            height = 34.dp,
        )
    }
}
