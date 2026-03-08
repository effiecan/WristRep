package com.fitnessrepcounter.wear.navigation

import com.fitnessrepcounter.wear.presentation.state.WorkoutStep
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WorkoutNavigationTest {
    @Test
    fun launchRouteForWorkoutState_goesHomeWithoutActiveSession() {
        assertThat(
            launchRouteForWorkoutState(
                hasActiveSession = false,
                step = WorkoutStep.ACTIVE,
            ),
        ).isEqualTo(AppRoute.Home.route)
    }

    @Test
    fun launchRouteForWorkoutState_restoresExactWorkoutStepWhenSessionIsActive() {
        assertThat(
            launchRouteForWorkoutState(
                hasActiveSession = true,
                step = WorkoutStep.ACTIVE,
            ),
        ).isEqualTo(AppRoute.ActiveWorkout.route)

        assertThat(
            launchRouteForWorkoutState(
                hasActiveSession = true,
                step = WorkoutStep.SUMMARY,
            ),
        ).isEqualTo(AppRoute.WorkoutSummary.route)
    }

    @Test
    fun isProtectedWorkoutRoute_onlyProtectsActiveWorkoutScreens() {
        assertThat(isProtectedWorkoutRoute(AppRoute.Ready.route)).isTrue()
        assertThat(isProtectedWorkoutRoute(AppRoute.ActiveWorkout.route)).isTrue()
        assertThat(isProtectedWorkoutRoute(AppRoute.RestTimer.route)).isTrue()

        assertThat(isProtectedWorkoutRoute(AppRoute.Home.route)).isFalse()
        assertThat(isProtectedWorkoutRoute(AppRoute.ExerciseSelection.route)).isFalse()
        assertThat(isProtectedWorkoutRoute(null)).isFalse()
    }
}
