package com.fitnessrepcounter.wear.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WorkoutSessionEntity::class, WorkoutSetEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}
