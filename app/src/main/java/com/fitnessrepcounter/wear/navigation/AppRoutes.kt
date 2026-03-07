package com.fitnessrepcounter.wear.navigation

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object History : AppRoute("history")
    data object Paywall : AppRoute("paywall")
    data object WorkoutFlow : AppRoute("workout_flow")
    data object ExerciseSelection : AppRoute("exercise_selection")
    data object Ready : AppRoute("ready")
    data object ActiveWorkout : AppRoute("active_workout")
    data object EndSetConfirmation : AppRoute("end_set_confirmation")
    data object RestTimer : AppRoute("rest_timer")
    data object WorkoutSummary : AppRoute("workout_summary")
}
