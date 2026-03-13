package com.fitnessrepcounter.wear.navigation

import com.fitnessrepcounter.wear.presentation.state.WorkoutStep

private val protectedWorkoutRoutes = setOf(
    AppRoute.Ready.route,
    AppRoute.ActiveWorkout.route,
    AppRoute.EndSetConfirmation.route,
    AppRoute.RestTimer.route,
    AppRoute.WorkoutSummary.route,
)

private val workoutStepRoutes = mapOf(
    WorkoutStep.EXERCISE_SELECTION to setOf(AppRoute.WorkoutFlow.route, AppRoute.ExerciseSelection.route),
    WorkoutStep.READY to setOf(AppRoute.Ready.route),
    WorkoutStep.ACTIVE to setOf(AppRoute.ActiveWorkout.route),
    WorkoutStep.END_SET_CONFIRMATION to setOf(AppRoute.EndSetConfirmation.route),
    WorkoutStep.REST_TIMER to setOf(AppRoute.RestTimer.route),
    WorkoutStep.SUMMARY to setOf(AppRoute.WorkoutSummary.route),
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

fun workoutRouteSyncTarget(
    hasActiveSession: Boolean,
    step: WorkoutStep,
    currentRoute: String?,
): String? {
    if (!hasActiveSession) return null
    if (currentRoute in workoutStepRoutes.getValue(step)) return null
    return workoutRouteForStep(step)
}
