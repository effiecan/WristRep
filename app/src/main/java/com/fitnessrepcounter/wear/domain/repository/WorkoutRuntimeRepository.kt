package com.fitnessrepcounter.wear.domain.repository

import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.WorkoutRuntimeEvent
import com.fitnessrepcounter.wear.presentation.state.WorkoutStep
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WorkoutRuntimeRepository {
    val uiState: StateFlow<WorkoutUiState>
    val hasActiveSession: StateFlow<Boolean>
    val canExposeOngoingEntry: StateFlow<Boolean>
    val shouldRequestNotificationPermission: StateFlow<Boolean>
    val workoutEvents: Flow<WorkoutRuntimeEvent>

    fun currentStep(): WorkoutStep
    fun prepareNewWorkout()
    fun selectExercise(exercise: Exercise)
    fun startReadyCountdown()
    fun addManualRep()
    fun removeManualRep()
    fun endCurrentSet(): Boolean
    fun beginRestTimer()
    fun skipRestTimer()
    fun finishWorkout(): Boolean
    suspend fun saveWorkout(): Boolean
    fun discardWorkout()
    fun onNotificationPermissionPromptHandled(granted: Boolean)
}
