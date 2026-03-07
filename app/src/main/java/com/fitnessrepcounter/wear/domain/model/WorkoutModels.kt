package com.fitnessrepcounter.wear.domain.model

data class WorkoutSet(
    val setNumber: Int,
    val repCount: Int,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long,
    val manualAdjustmentCount: Int,
)

data class WorkoutSession(
    val id: String,
    val exercise: Exercise,
    val sets: List<WorkoutSet>,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long,
    val status: WorkoutStatus,
) {
    val totalReps: Int = sets.sumOf { it.repCount }
}

enum class WorkoutStatus {
    IN_PROGRESS,
    COMPLETED,
    ABANDONED,
}
