package com.fitnessrepcounter.wear.domain.model

data class EntitlementState(
    val completedFreeWorkoutsUsed: Int = 0,
    val freeWorkoutLimit: Int = 3,
    val isProUnlocked: Boolean = false,
    val activeTrialSessionId: String? = null,
    val activeTrialReservedAtEpochMs: Long? = null,
    val activeTrialConsumed: Boolean = false,
    val activeTrialExerciseName: String? = null,
    val activeTrialAccumulatedActiveMs: Long = 0L,
) {
    val hasActiveTrialReservation: Boolean = activeTrialSessionId != null
    val remainingFreeWorkouts: Int = (freeWorkoutLimit - completedFreeWorkoutsUsed).coerceAtLeast(0)
    val canStartWorkout: Boolean = isProUnlocked ||
        completedFreeWorkoutsUsed < freeWorkoutLimit ||
        (hasActiveTrialReservation && !activeTrialConsumed)
}
