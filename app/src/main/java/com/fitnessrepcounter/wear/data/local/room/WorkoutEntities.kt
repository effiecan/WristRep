package com.fitnessrepcounter.wear.data.local.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val exercise: String,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long,
    val status: String,
    val totalReps: Int,
    val setCount: Int,
)

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["sessionId"])],
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: String,
    val setNumber: Int,
    val repCount: Int,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long,
    val manualAdjustmentCount: Int,
)

data class WorkoutSessionWithSets(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId",
    )
    val sets: List<WorkoutSetEntity>,
)
