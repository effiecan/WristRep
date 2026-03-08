package com.fitnessrepcounter.wear.presentation.screens.workout

import com.fitnessrepcounter.wear.domain.model.HapticMode
import com.fitnessrepcounter.wear.presentation.state.AmbientModeState
import com.fitnessrepcounter.wear.presentation.state.WorkoutStep
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ActiveWorkoutKeepScreenOnTest {
    @Test
    fun shouldKeepScreenOn_onlyWhenEveryRepToggleTrackingAndNonAmbient() {
        assertThat(
            shouldKeepScreenOnForEveryRep(
                hapticMode = HapticMode.EVERY_REP,
                workoutUiState = WorkoutUiState(
                    currentStep = WorkoutStep.ACTIVE,
                    isTracking = true,
                ),
                ambientModeState = AmbientModeState(isAmbient = false),
            ),
        ).isTrue()
    }

    @Test
    fun shouldKeepScreenOn_isFalseOutsideAllowedConditions() {
        assertThat(
            shouldKeepScreenOnForEveryRep(
                hapticMode = HapticMode.IMPORTANT_ONLY,
                workoutUiState = WorkoutUiState(
                    currentStep = WorkoutStep.ACTIVE,
                    isTracking = true,
                ),
                ambientModeState = AmbientModeState(),
            ),
        ).isFalse()

        assertThat(
            shouldKeepScreenOnForEveryRep(
                hapticMode = HapticMode.EVERY_REP,
                workoutUiState = WorkoutUiState(
                    currentStep = WorkoutStep.REST_TIMER,
                    isTracking = false,
                ),
                ambientModeState = AmbientModeState(),
            ),
        ).isFalse()

        assertThat(
            shouldKeepScreenOnForEveryRep(
                hapticMode = HapticMode.EVERY_REP,
                workoutUiState = WorkoutUiState(
                    currentStep = WorkoutStep.ACTIVE,
                    isTracking = true,
                ),
                ambientModeState = AmbientModeState(isAmbient = true),
            ),
        ).isFalse()
    }
}
