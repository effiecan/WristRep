package com.fitnessrepcounter.wear.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.repository.WorkoutRuntimeRepository
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState
import kotlinx.coroutines.flow.StateFlow

class WorkoutViewModel(
    private val workoutRuntimeRepository: WorkoutRuntimeRepository,
) : ViewModel() {
    val uiState: StateFlow<WorkoutUiState> = workoutRuntimeRepository.uiState

    fun prepareNewWorkout() {
        workoutRuntimeRepository.prepareNewWorkout()
    }

    fun selectExercise(exercise: Exercise) {
        workoutRuntimeRepository.selectExercise(exercise)
    }

    fun startReadyCountdown() {
        workoutRuntimeRepository.startReadyCountdown()
    }

    fun addManualRep() {
        workoutRuntimeRepository.addManualRep()
    }

    fun removeManualRep() {
        workoutRuntimeRepository.removeManualRep()
    }

    fun endCurrentSet(): Boolean {
        return workoutRuntimeRepository.endCurrentSet()
    }

    fun beginRestTimer() {
        workoutRuntimeRepository.beginRestTimer()
    }

    fun skipRestTimer() {
        workoutRuntimeRepository.skipRestTimer()
    }

    fun finishWorkout(): Boolean {
        return workoutRuntimeRepository.finishWorkout()
    }

    suspend fun saveWorkout(): Boolean {
        return workoutRuntimeRepository.saveWorkout()
    }

    fun discardWorkout() {
        workoutRuntimeRepository.discardWorkout()
    }

    companion object {
        internal fun shouldConsumeTrialForRepCount(repCount: Int): Boolean {
            return repCount >= 3
        }
    }
}
