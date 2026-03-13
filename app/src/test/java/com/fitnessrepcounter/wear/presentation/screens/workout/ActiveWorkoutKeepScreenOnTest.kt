package com.fitnessrepcounter.wear.presentation.screens.workout

import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.HapticMode
import com.fitnessrepcounter.wear.presentation.state.AmbientModeState
import com.fitnessrepcounter.wear.presentation.state.WorkoutStep
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ActiveWorkoutKeepScreenOnTest {
    @Test
    fun shouldKeepScreenOn_duringReadyCountdown_whenEveryRepAndNonAmbient() {
        assertThat(
            shouldKeepScreenOnDuringWorkout(
                hapticMode = HapticMode.EVERY_REP,
                workoutUiState = WorkoutUiState(
                    selectedExercise = Exercise.BICEPS_CURL,
                    currentStep = WorkoutStep.READY,
                    countdownValue = 3,
                ),
                ambientModeState = AmbientModeState(isAmbient = false),
            ),
        ).isTrue()
    }

    @Test
    fun shouldKeepScreenOn_duringActiveWorkout_whenEveryRepTrackingAndNonAmbient() {
        assertThat(
            shouldKeepScreenOnDuringWorkout(
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
            shouldKeepScreenOnDuringWorkout(
                hapticMode = HapticMode.IMPORTANT_ONLY,
                workoutUiState = WorkoutUiState(
                    selectedExercise = Exercise.BICEPS_CURL,
                    currentStep = WorkoutStep.READY,
                    countdownValue = 3,
                ),
                ambientModeState = AmbientModeState(),
            ),
        ).isFalse()

        assertThat(
            shouldKeepScreenOnDuringWorkout(
                hapticMode = HapticMode.EVERY_REP,
                workoutUiState = WorkoutUiState(
                    currentStep = WorkoutStep.REST_TIMER,
                    isTracking = false,
                ),
                ambientModeState = AmbientModeState(),
            ),
        ).isFalse()

        assertThat(
            shouldKeepScreenOnDuringWorkout(
                hapticMode = HapticMode.EVERY_REP,
                workoutUiState = WorkoutUiState(
                    currentStep = WorkoutStep.ACTIVE,
                    isTracking = true,
                ),
                ambientModeState = AmbientModeState(isAmbient = true),
            ),
        ).isFalse()

        assertThat(
            shouldKeepScreenOnDuringWorkout(
                hapticMode = HapticMode.EVERY_REP,
                workoutUiState = WorkoutUiState(
                    selectedExercise = Exercise.BICEPS_CURL,
                    currentStep = WorkoutStep.READY,
                    countdownValue = 0,
                ),
                ambientModeState = AmbientModeState(),
            ),
        ).isFalse()
    }
}
