package com.fitnessrepcounter.wear.presentation.state

import com.fitnessrepcounter.wear.domain.model.EntitlementState
import com.fitnessrepcounter.wear.domain.model.WorkoutSession

data class HomeUiState(
    val entitlementState: EntitlementState = EntitlementState(),
    val recentWorkouts: List<WorkoutSession> = emptyList(),
)
