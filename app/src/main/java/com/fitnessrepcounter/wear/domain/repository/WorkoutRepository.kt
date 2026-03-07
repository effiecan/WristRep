package com.fitnessrepcounter.wear.domain.repository

import com.fitnessrepcounter.wear.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeHistory(): Flow<List<WorkoutSession>>
    suspend fun saveCompletedWorkout(session: WorkoutSession)
    suspend fun countCompletedWorkouts(): Int
}
