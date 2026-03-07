package com.fitnessrepcounter.wear.data.repository

import com.fitnessrepcounter.wear.data.local.room.WorkoutDao
import com.fitnessrepcounter.wear.data.local.room.WorkoutSessionEntity
import com.fitnessrepcounter.wear.data.local.room.WorkoutSessionWithSets
import com.fitnessrepcounter.wear.data.local.room.WorkoutSetEntity
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.WorkoutSession
import com.fitnessrepcounter.wear.domain.model.WorkoutSet
import com.fitnessrepcounter.wear.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao,
) : WorkoutRepository {
    override fun observeHistory(): Flow<List<WorkoutSession>> {
        return workoutDao.observeCompletedSessions().map { sessions ->
            sessions.map { it.toDomain() }
        }
    }

    override suspend fun saveCompletedWorkout(session: WorkoutSession) {
        val sessionEntity = WorkoutSessionEntity(
            id = session.id,
            exercise = session.exercise.name,
            startedAtEpochMs = session.startedAtEpochMs,
            endedAtEpochMs = session.endedAtEpochMs,
            status = session.status.name,
            totalReps = session.totalReps,
            setCount = session.sets.size,
        )
        val setEntities = session.sets.map { set ->
            WorkoutSetEntity(
                sessionId = session.id,
                setNumber = set.setNumber,
                repCount = set.repCount,
                startedAtEpochMs = set.startedAtEpochMs,
                endedAtEpochMs = set.endedAtEpochMs,
                manualAdjustmentCount = set.manualAdjustmentCount,
            )
        }
        workoutDao.insertWorkout(sessionEntity, setEntities)
    }

    override suspend fun countCompletedWorkouts(): Int = workoutDao.countCompletedWorkouts()
}

private fun WorkoutSessionWithSets.toDomain(): WorkoutSession {
    return WorkoutSession(
        id = session.id,
        exercise = Exercise.valueOf(session.exercise),
        sets = sets.sortedBy { it.setNumber }.map { set ->
            WorkoutSet(
                setNumber = set.setNumber,
                repCount = set.repCount,
                startedAtEpochMs = set.startedAtEpochMs,
                endedAtEpochMs = set.endedAtEpochMs,
                manualAdjustmentCount = set.manualAdjustmentCount,
            )
        },
        startedAtEpochMs = session.startedAtEpochMs,
        endedAtEpochMs = session.endedAtEpochMs,
        status = com.fitnessrepcounter.wear.domain.model.WorkoutStatus.valueOf(session.status),
    )
}
