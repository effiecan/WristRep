package com.fitnessrepcounter.wear.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.fitnessrepcounter.wear.data.local.datastore.EntitlementDataStore
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EntitlementRepositoryImplTest {
    @Test
    fun consumeActiveTrialSession_capsAtThree_andOnlyConsumesOncePerReservation() = runTest {
        val repository = buildRepository()

        repeat(4) {
            repository.reserveActiveTrialSessionIfNeeded(Exercise.BICEPS_CURL)
            repository.consumeActiveTrialSessionIfNeeded()
            repository.consumeActiveTrialSessionIfNeeded()
            repository.clearActiveTrialSession()
        }

        val state = repository.observeEntitlement().first()
        assertThat(state.completedFreeWorkoutsUsed).isEqualTo(3)
        assertThat(state.canStartWorkout).isFalse()
    }

    @Test
    fun reconcileCompletedWorkoutUsage_usesSavedWorkoutCount() = runTest {
        val repository = buildRepository()

        repository.reserveActiveTrialSessionIfNeeded(Exercise.BICEPS_CURL)
        repository.consumeActiveTrialSessionIfNeeded()
        repository.reconcileCompletedWorkoutUsage(completedWorkoutCount = 3)

        val state = repository.observeEntitlement().first()
        assertThat(state.completedFreeWorkoutsUsed).isEqualTo(3)
        assertThat(state.remainingFreeWorkouts).isEqualTo(0)
    }

    @Test
    fun reserveAndClearActiveTrial_preservesFreeUseWhenNeverConsumed() = runTest {
        val repository = buildRepository()

        val reserved = repository.reserveActiveTrialSessionIfNeeded(Exercise.TRICEPS_EXTENSION)
        repository.appendActiveTrialUsage(5_000L)
        repository.clearActiveTrialSession()

        val state = repository.observeEntitlement().first()
        assertThat(reserved).isTrue()
        assertThat(state.completedFreeWorkoutsUsed).isEqualTo(0)
        assertThat(state.activeTrialSessionId).isNull()
    }

    @Test
    fun activeReservation_keepsWorkoutStartAvailable_whenFreeCountIsExhausted() = runTest {
        val repository = buildRepository()

        repeat(2) {
            repository.reserveActiveTrialSessionIfNeeded(Exercise.BICEPS_CURL)
            repository.consumeActiveTrialSessionIfNeeded()
            repository.clearActiveTrialSession()
        }

        repository.reserveActiveTrialSessionIfNeeded(Exercise.TRICEPS_EXTENSION)
        repository.consumeActiveTrialSessionIfNeeded()
        val consumedState = repository.observeEntitlement().first()
        assertThat(consumedState.canStartWorkout).isFalse()

        repository.clearActiveTrialSession()
        val blockedState = repository.observeEntitlement().first()
        assertThat(blockedState.canStartWorkout).isFalse()
    }

    @Test
    fun refillFreeTrialsForDebug_resetsUsage_withoutUnlockingPro() = runTest {
        val repository = buildRepository()

        repository.reserveActiveTrialSessionIfNeeded(Exercise.BICEPS_CURL)
        repository.consumeActiveTrialSessionIfNeeded()
        repository.refillFreeTrialsForDebug()

        val state = repository.observeEntitlement().first()
        assertThat(state.completedFreeWorkoutsUsed).isEqualTo(0)
        assertThat(state.remainingFreeWorkouts).isEqualTo(3)
        assertThat(state.activeTrialSessionId).isNull()
        assertThat(state.isProUnlocked).isFalse()
    }

    private fun buildRepository(): EntitlementRepositoryImpl {
        val file = File(createTempDirectory().toFile(), "entitlement.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO),
            produceFile = { file },
        )
        return EntitlementRepositoryImpl(EntitlementDataStore(dataStore))
    }
}
