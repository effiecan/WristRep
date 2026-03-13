package com.fitnessrepcounter.wear.presentation.screens.workout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.annotation.StringRes
import com.fitnessrepcounter.wear.R
import com.fitnessrepcounter.wear.domain.model.RepDetectionState
import com.fitnessrepcounter.wear.presentation.components.BadgeTone
import com.fitnessrepcounter.wear.presentation.components.CorrectionControlButton
import com.fitnessrepcounter.wear.presentation.components.PrimaryActionButton
import com.fitnessrepcounter.wear.presentation.components.StatusBadge
import com.fitnessrepcounter.wear.presentation.components.WorkoutKeepScreenOnEffect
import com.fitnessrepcounter.wear.presentation.components.WristRepScreenScaffold
import com.fitnessrepcounter.wear.presentation.state.AmbientModeState
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState
import com.fitnessrepcounter.wear.ui.theme.WatchTextSecondary

@Composable
fun ActiveWorkoutScreen(
    uiState: WorkoutUiState,
    ambientModeState: AmbientModeState = AmbientModeState(),
    shouldKeepScreenOn: Boolean = false,
    onAddRep: () -> Unit,
    onRemoveRep: () -> Unit,
    onEndSet: () -> Unit,
) {
    val exercise = uiState.selectedExercise
    val statusText = if (ambientModeState.isAmbient) {
        stringResource(if (uiState.isTracking) R.string.status_tracking else R.string.status_ready)
    } else {
        stringResource(uiState.detectionState.toUserFacingStatusRes())
    }

    WorkoutKeepScreenOnEffect(enabled = shouldKeepScreenOn)

    WristRepScreenScaffold(
        ambientModeState = ambientModeState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            AdaptiveExerciseTitle(
                text = exercise?.let { stringResource(it.displayNameRes) } ?: stringResource(R.string.workout),
                modifier = Modifier
                    .fillMaxWidth(0.76f)
                    .widthIn(max = 128.dp)
                    .testTag("active_workout_title"),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.set_number, uiState.currentSetNumber),
                style = MaterialTheme.typography.body2,
                color = WatchTextSecondary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = uiState.currentRepCount.toString(),
                style = MaterialTheme.typography.display1,
                color = MaterialTheme.colors.primary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            StatusBadge(
                label = statusText,
                tone = if (ambientModeState.isAmbient || uiState.detectionState == RepDetectionState.PAUSED) {
                    BadgeTone.MUTED
                } else {
                    BadgeTone.ACCENT
                },
                modifier = Modifier.testTag("active_workout_status"),
                horizontalPadding = 9.dp,
                verticalPadding = 2.dp,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (!ambientModeState.isAmbient) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.76f)
                            .widthIn(max = 152.dp)
                            .testTag("active_workout_actions"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CorrectionControlButton(
                            label = stringResource(R.string.rep_adjust_minus_one),
                            onClick = onRemoveRep,
                            size = 40.dp,
                        )
                        PrimaryActionButton(
                            text = stringResource(R.string.watch_cta_end_set),
                            onClick = onEndSet,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("end_set_button"),
                            height = 34.dp,
                            maxLines = 2,
                        )
                        CorrectionControlButton(
                            label = stringResource(R.string.rep_adjust_plus_one),
                            onClick = onAddRep,
                            size = 40.dp,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AdaptiveExerciseTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val titleStyles = listOf(24.sp, 22.sp, 20.sp, 18.sp, 16.sp).map { size ->
        MaterialTheme.typography.title1.copy(
            fontSize = size,
            fontWeight = FontWeight.Bold,
        )
    }

    BoxWithConstraints(modifier = modifier) {
        val maxWidthPx = with(density) { maxWidth.roundToPx() }
        val titleStyle = remember(text, maxWidthPx, titleStyles, textMeasurer) {
            titleStyles.firstOrNull { style ->
                !textMeasurer.measure(
                    text = AnnotatedString(text),
                    style = style,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    constraints = Constraints(maxWidth = maxWidthPx),
                ).hasVisualOverflow
            } ?: titleStyles.last()
        }

        Text(
            text = text,
            style = titleStyle,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@StringRes
private fun RepDetectionState.toUserFacingStatusRes(): Int {
    return when (this) {
        RepDetectionState.IDLE -> R.string.status_ready
        RepDetectionState.MOVING_UP,
        RepDetectionState.MOVING_DOWN,
        -> R.string.status_tracking
        RepDetectionState.REP_CONFIRMED -> R.string.status_rep_detected
        RepDetectionState.PAUSED -> R.string.status_paused
    }
}
