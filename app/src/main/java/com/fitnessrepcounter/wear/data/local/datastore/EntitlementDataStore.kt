package com.fitnessrepcounter.wear.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.EntitlementState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlin.math.min

class EntitlementDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    val entitlementState: Flow<EntitlementState> = dataStore.data.map { preferences ->
        EntitlementState(
            completedFreeWorkoutsUsed = preferences[COMPLETED_FREE_WORKOUTS_USED].orZero(),
            isProUnlocked = preferences[PRO_UNLOCKED] ?: false,
            activeTrialSessionId = preferences[ACTIVE_TRIAL_SESSION_ID],
            activeTrialReservedAtEpochMs = preferences[ACTIVE_TRIAL_RESERVED_AT_EPOCH_MS],
            activeTrialConsumed = preferences[ACTIVE_TRIAL_CONSUMED] ?: false,
            activeTrialExerciseName = preferences[ACTIVE_TRIAL_EXERCISE_NAME],
            activeTrialAccumulatedActiveMs = preferences[ACTIVE_TRIAL_ACCUMULATED_ACTIVE_MS] ?: 0L,
        )
    }

    suspend fun reserveActiveTrialSessionIfNeeded(
        exercise: Exercise,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): Boolean {
        var allowed = false
        dataStore.edit { preferences ->
            if (preferences[PRO_UNLOCKED] == true) {
                allowed = true
                return@edit
            }

            if (preferences[ACTIVE_TRIAL_SESSION_ID] != null) {
                allowed = true
                return@edit
            }

            val used = preferences[COMPLETED_FREE_WORKOUTS_USED].orZero()
            if (used >= 3) {
                allowed = false
                return@edit
            }

            preferences[ACTIVE_TRIAL_SESSION_ID] = UUID.randomUUID().toString()
            preferences[ACTIVE_TRIAL_RESERVED_AT_EPOCH_MS] = nowEpochMs
            preferences[ACTIVE_TRIAL_CONSUMED] = false
            preferences[ACTIVE_TRIAL_EXERCISE_NAME] = exercise.name
            preferences[ACTIVE_TRIAL_ACCUMULATED_ACTIVE_MS] = 0L
            allowed = true
        }
        return allowed
    }

    suspend fun consumeActiveTrialSessionIfNeeded() {
        dataStore.edit { preferences ->
            if (preferences[PRO_UNLOCKED] == true) return@edit
            if (preferences[ACTIVE_TRIAL_SESSION_ID] == null) return@edit
            if (preferences[ACTIVE_TRIAL_CONSUMED] == true) return@edit
            val used = preferences[COMPLETED_FREE_WORKOUTS_USED].orZero()
            preferences[COMPLETED_FREE_WORKOUTS_USED] = min(3, used + 1)
            preferences[ACTIVE_TRIAL_CONSUMED] = true
        }
    }

    suspend fun clearActiveTrialSession() {
        dataStore.edit { preferences ->
            preferences.remove(ACTIVE_TRIAL_SESSION_ID)
            preferences.remove(ACTIVE_TRIAL_RESERVED_AT_EPOCH_MS)
            preferences.remove(ACTIVE_TRIAL_CONSUMED)
            preferences.remove(ACTIVE_TRIAL_EXERCISE_NAME)
            preferences.remove(ACTIVE_TRIAL_ACCUMULATED_ACTIVE_MS)
        }
    }

    suspend fun appendActiveTrialUsage(durationMs: Long) {
        if (durationMs <= 0L) return
        dataStore.edit { preferences ->
            if (preferences[ACTIVE_TRIAL_SESSION_ID] == null) return@edit
            val current = preferences[ACTIVE_TRIAL_ACCUMULATED_ACTIVE_MS] ?: 0L
            preferences[ACTIVE_TRIAL_ACCUMULATED_ACTIVE_MS] = current + durationMs
        }
    }

    suspend fun refillFreeTrialsForDebug() {
        dataStore.edit { preferences ->
            preferences[COMPLETED_FREE_WORKOUTS_USED] = 0
            preferences[PRO_UNLOCKED] = false
            preferences.remove(ACTIVE_TRIAL_SESSION_ID)
            preferences.remove(ACTIVE_TRIAL_RESERVED_AT_EPOCH_MS)
            preferences.remove(ACTIVE_TRIAL_CONSUMED)
            preferences.remove(ACTIVE_TRIAL_EXERCISE_NAME)
            preferences.remove(ACTIVE_TRIAL_ACCUMULATED_ACTIVE_MS)
        }
    }

    suspend fun setProUnlocked(isUnlocked: Boolean) {
        dataStore.edit { preferences ->
            preferences[PRO_UNLOCKED] = isUnlocked
            if (isUnlocked) {
                preferences.remove(ACTIVE_TRIAL_SESSION_ID)
                preferences.remove(ACTIVE_TRIAL_RESERVED_AT_EPOCH_MS)
                preferences.remove(ACTIVE_TRIAL_CONSUMED)
                preferences.remove(ACTIVE_TRIAL_EXERCISE_NAME)
                preferences.remove(ACTIVE_TRIAL_ACCUMULATED_ACTIVE_MS)
            }
        }
    }

    suspend fun reconcileCompletedWorkoutUsage(completedWorkoutCount: Int) {
        dataStore.edit { preferences ->
            val current = preferences[COMPLETED_FREE_WORKOUTS_USED].orZero()
            val reconciled = min(3, maxOf(current, completedWorkoutCount))
            preferences[COMPLETED_FREE_WORKOUTS_USED] = reconciled
        }
    }

    private fun Int?.orZero(): Int = this ?: 0

    private companion object {
        val COMPLETED_FREE_WORKOUTS_USED = intPreferencesKey("completed_free_workouts_used")
        val PRO_UNLOCKED = booleanPreferencesKey("pro_unlocked")
        val ACTIVE_TRIAL_SESSION_ID = stringPreferencesKey("active_trial_session_id")
        val ACTIVE_TRIAL_RESERVED_AT_EPOCH_MS = longPreferencesKey("active_trial_reserved_at_epoch_ms")
        val ACTIVE_TRIAL_CONSUMED = booleanPreferencesKey("active_trial_consumed")
        val ACTIVE_TRIAL_EXERCISE_NAME = stringPreferencesKey("active_trial_exercise_name")
        val ACTIVE_TRIAL_ACCUMULATED_ACTIVE_MS = longPreferencesKey("active_trial_accumulated_active_ms")
    }
}
