package com.fitnessrepcounter.wear.domain.session

import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.WorkoutSession
import com.fitnessrepcounter.wear.domain.model.WorkoutSet
import com.fitnessrepcounter.wear.domain.model.WorkoutStatus
import java.util.UUID

data class WorkoutSessionSnapshot(
    val selectedExercise: Exercise? = null,
    val currentSetNumber: Int = 1,
    val currentRepCount: Int = 0,
    val totalReps: Int = 0,
    val completedSets: List<WorkoutSet> = emptyList(),
    val lastCompletedSet: WorkoutSet? = null,
    val workoutStartedAtEpochMs: Long? = null,
)

class WorkoutSessionManager {
    private var sessionId: String = ""
    private var selectedExercise: Exercise? = null
    private var workoutStartedAtEpochMs: Long? = null
    private var currentSetStartedAtEpochMs: Long? = null
    private var currentSetNumber: Int = 1
    private var currentRepCount: Int = 0
    private var manualAdjustmentCount: Int = 0
    private val completedSets: MutableList<WorkoutSet> = mutableListOf()
    private var lastCompletedSet: WorkoutSet? = null
    private var completedWorkout: WorkoutSession? = null

    fun reset() {
        sessionId = ""
        selectedExercise = null
        workoutStartedAtEpochMs = null
        currentSetStartedAtEpochMs = null
        currentSetNumber = 1
        currentRepCount = 0
        manualAdjustmentCount = 0
        completedSets.clear()
        lastCompletedSet = null
        completedWorkout = null
    }

    fun selectExercise(exercise: Exercise) {
        reset()
        selectedExercise = exercise
    }

    fun recordAutoRep(nowEpochMs: Long = System.currentTimeMillis()) {
        startIfNeeded(nowEpochMs)
        currentRepCount += 1
    }

    fun adjustRepManually(delta: Int, nowEpochMs: Long = System.currentTimeMillis()) {
        startIfNeeded(nowEpochMs)
        val nextCount = (currentRepCount + delta).coerceAtLeast(0)
        if (nextCount != currentRepCount) {
            currentRepCount = nextCount
            manualAdjustmentCount += 1
        }
    }

    fun endCurrentSet(nowEpochMs: Long = System.currentTimeMillis()): WorkoutSet? {
        val exercise = selectedExercise ?: return null
        if (currentRepCount <= 0) return null
        startIfNeeded(nowEpochMs)

        val workoutSet = WorkoutSet(
            setNumber = currentSetNumber,
            repCount = currentRepCount,
            startedAtEpochMs = currentSetStartedAtEpochMs ?: nowEpochMs,
            endedAtEpochMs = nowEpochMs,
            manualAdjustmentCount = manualAdjustmentCount,
        )
        completedSets += workoutSet
        lastCompletedSet = workoutSet

        currentSetNumber += 1
        currentRepCount = 0
        manualAdjustmentCount = 0
        currentSetStartedAtEpochMs = nowEpochMs
        completedWorkout = null

        check(exercise == selectedExercise)
        return workoutSet
    }

    fun startNextSet(nowEpochMs: Long = System.currentTimeMillis()) {
        startIfNeeded(nowEpochMs)
        currentSetStartedAtEpochMs = nowEpochMs
        currentRepCount = 0
        manualAdjustmentCount = 0
    }

    fun finishWorkout(nowEpochMs: Long = System.currentTimeMillis()): WorkoutSession? {
        val exercise = selectedExercise ?: return null
        val startedAt = workoutStartedAtEpochMs ?: return null
        if (completedSets.isEmpty()) return null

        completedWorkout = WorkoutSession(
            id = sessionId.ifBlank { UUID.randomUUID().toString() },
            exercise = exercise,
            sets = completedSets.toList(),
            startedAtEpochMs = startedAt,
            endedAtEpochMs = nowEpochMs,
            status = WorkoutStatus.COMPLETED,
        )
        return completedWorkout
    }

    fun getCompletedWorkout(): WorkoutSession? = completedWorkout

    fun snapshot(): WorkoutSessionSnapshot {
        return WorkoutSessionSnapshot(
            selectedExercise = selectedExercise,
            currentSetNumber = currentSetNumber,
            currentRepCount = currentRepCount,
            totalReps = completedSets.sumOf { it.repCount } + currentRepCount,
            completedSets = completedSets.toList(),
            lastCompletedSet = lastCompletedSet,
            workoutStartedAtEpochMs = workoutStartedAtEpochMs,
        )
    }

    private fun startIfNeeded(nowEpochMs: Long) {
        if (sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString()
        }
        if (workoutStartedAtEpochMs == null) {
            workoutStartedAtEpochMs = nowEpochMs
        }
        if (currentSetStartedAtEpochMs == null) {
            currentSetStartedAtEpochMs = nowEpochMs
        }
    }
}
