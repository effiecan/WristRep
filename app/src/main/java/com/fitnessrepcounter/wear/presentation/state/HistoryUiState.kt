package com.fitnessrepcounter.wear.presentation.state

import com.fitnessrepcounter.wear.domain.model.WorkoutSession

data class HistoryUiState(
    val workouts: List<WorkoutSession> = emptyList(),
)
