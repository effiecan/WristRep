package com.fitnessrepcounter.wear.navigation

import com.fitnessrepcounter.wear.presentation.state.WorkoutStep

private val protectedWorkoutRoutes = setOf(
    AppRoute.Ready.route,
    AppRoute.ActiveWorkout.route,
    AppRoute.EndSetConfirmation.route,
    AppRoute.RestTimer.route,
    AppRoute.WorkoutSummary.route,
)

fun workoutRouteForStep(step: WorkoutStep): String {
    return when (step) {
        WorkoutStep.EXERCISE_SELECTION -> AppRoute.WorkoutFlow.route
        WorkoutStep.READY -> AppRoute.Ready.route
        WorkoutStep.ACTIVE -> AppRoute.ActiveWorkout.route
        WorkoutStep.END_SET_CONFIRMATION -> AppRoute.EndSetConfirmation.route
        WorkoutStep.REST_TIMER -> AppRoute.RestTimer.route
        WorkoutStep.SUMMARY -> AppRoute.WorkoutSummary.route
    }
}

fun launchRouteForWorkoutState(
    hasActiveSession: Boolean,
    step: WorkoutStep,
): String {
    return if (hasActiveSession) {
        workoutRouteForStep(step)
    } else {
        AppRoute.Home.route
    }
}

fun isProtectedWorkoutRoute(route: String?): Boolean {
    return route in protectedWorkoutRoutes
}
