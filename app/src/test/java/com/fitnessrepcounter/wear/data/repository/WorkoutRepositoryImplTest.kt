package com.fitnessrepcounter.wear.data.repository

import com.fitnessrepcounter.wear.data.local.room.WorkoutDao
import com.fitnessrepcounter.wear.data.local.room.WorkoutSessionEntity
import com.fitnessrepcounter.wear.data.local.room.WorkoutSessionWithSets
import com.fitnessrepcounter.wear.data.local.room.WorkoutSetEntity
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.WorkoutSession
import com.fitnessrepcounter.wear.domain.model.WorkoutSet
import com.fitnessrepcounter.wear.domain.model.WorkoutStatus
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class WorkoutRepositoryImplTest {
    @Test
    fun saveAndObserveHistory_roundTripsNewExerciseNames() = runTest {
        val dao = FakeWorkoutDao()
        val repository = WorkoutRepositoryImpl(dao)
        val chestPress = session(exercise = Exercise.CHEST_PRESS, id = "session-1")
        val multiFlyFront = session(exercise = Exercise.STANDING_MULTI_FLY_FRONT_RAISE, id = "session-2")

        repository.saveCompletedWorkout(chestPress)
        repository.saveCompletedWorkout(multiFlyFront)

        val history = repository.observeHistory().first()

        assertThat(history.map { it.exercise })
            .containsExactly(Exercise.STANDING_MULTI_FLY_FRONT_RAISE, Exercise.CHEST_PRESS)
    }

    @Test
    fun saveAndObserveHistory_keepsExistingExercisesCompatible() = runTest {
        val dao = FakeWorkoutDao()
        val repository = WorkoutRepositoryImpl(dao)
        val existing = session(exercise = Exercise.BICEPS_CURL, id = "session-legacy")

        repository.saveCompletedWorkout(existing)

        val history = repository.observeHistory().first()

        assertThat(history.single().exercise).isEqualTo(Exercise.BICEPS_CURL)
    }
}

private class FakeWorkoutDao : WorkoutDao {
    private val state = MutableStateFlow<List<WorkoutSessionWithSets>>(emptyList())
    private val sessions = linkedMapOf<String, WorkoutSessionEntity>()
    private val setsBySession = linkedMapOf<String, MutableList<WorkoutSetEntity>>()

    override fun observeCompletedSessions(): Flow<List<WorkoutSessionWithSets>> = state

    override suspend fun insertSession(session: WorkoutSessionEntity) {
        sessions[session.id] = session
        refresh()
    }

    override suspend fun insertSets(sets: List<WorkoutSetEntity>) {
        sets.groupBy { it.sessionId }.forEach { (sessionId, groupedSets) ->
            setsBySession.getOrPut(sessionId) { mutableListOf() }.apply {
                removeAll { existing -> groupedSets.any { it.setNumber == existing.setNumber } }
                addAll(groupedSets)
            }
        }
        refresh()
    }

    override suspend fun countCompletedWorkouts(): Int = sessions.size

    private fun refresh() {
        state.value = sessions.values
            .sortedByDescending { it.endedAtEpochMs }
            .map { session ->
                WorkoutSessionWithSets(
                    session = session,
                    sets = setsBySession[session.id].orEmpty().sortedBy { it.setNumber },
                )
            }
    }
}

private fun session(exercise: Exercise, id: String): WorkoutSession {
    return WorkoutSession(
        id = id,
        exercise = exercise,
        sets = listOf(
            WorkoutSet(
                setNumber = 1,
                repCount = 12,
                startedAtEpochMs = 1_000L,
                endedAtEpochMs = 2_000L,
                manualAdjustmentCount = 0,
            ),
        ),
        startedAtEpochMs = 1_000L,
        endedAtEpochMs = 2_000L + id.length,
        status = WorkoutStatus.COMPLETED,
    )
}
