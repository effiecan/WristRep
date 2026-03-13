package com.fitnessrepcounter.wear.presentation.screens.workout

import com.fitnessrepcounter.wear.domain.model.HapticMode
import com.fitnessrepcounter.wear.presentation.state.AmbientModeState
import com.fitnessrepcounter.wear.presentation.state.WorkoutStep
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState

internal fun shouldKeepScreenOnDuringWorkout(
    hapticMode: HapticMode,
    workoutUiState: WorkoutUiState,
    ambientModeState: AmbientModeState,
): Boolean {
    if (hapticMode != HapticMode.EVERY_REP || ambientModeState.isAmbient) {
        return false
    }

    return when (workoutUiState.currentStep) {
        WorkoutStep.READY -> workoutUiState.selectedExercise != null && workoutUiState.countdownValue > 0
        WorkoutStep.ACTIVE -> workoutUiState.isTracking
        else -> false
    }
}
