package com.fitnessrepcounter.wear.presentation.state

import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.RepDetectionState
import com.fitnessrepcounter.wear.domain.model.WorkoutSet

enum class WorkoutStep {
    EXERCISE_SELECTION,
    READY,
    ACTIVE,
    END_SET_CONFIRMATION,
    REST_TIMER,
    SUMMARY,
}

data class WorkoutUiState(
    val selectedExercise: Exercise? = null,
    val currentStep: WorkoutStep = WorkoutStep.EXERCISE_SELECTION,
    val currentSetNumber: Int = 1,
    val currentRepCount: Int = 0,
    val totalReps: Int = 0,
    val countdownValue: Int = 3,
    val restSecondsRemaining: Int = 60,
    val detectionState: RepDetectionState = RepDetectionState.IDLE,
    val canSave: Boolean = false,
    val isTracking: Boolean = false,
    val completedSets: List<WorkoutSet> = emptyList(),
    val latestCompletedSet: WorkoutSet? = null,
    val workoutStartedAtEpochMs: Long? = null,
    val motionSignal: Float = 0f,
    val confidence: Float = 0f,
)
