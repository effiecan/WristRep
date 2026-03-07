package com.fitnessrepcounter.wear.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE status = 'COMPLETED' ORDER BY endedAtEpochMs DESC")
    fun observeCompletedSessions(): Flow<List<WorkoutSessionWithSets>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<WorkoutSetEntity>)

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE status = 'COMPLETED'")
    suspend fun countCompletedWorkouts(): Int

    @Transaction
    suspend fun insertWorkout(session: WorkoutSessionEntity, sets: List<WorkoutSetEntity>) {
        insertSession(session)
        insertSets(sets)
    }
}
