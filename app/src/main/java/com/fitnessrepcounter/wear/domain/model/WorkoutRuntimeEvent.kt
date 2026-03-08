package com.fitnessrepcounter.wear.domain.model

sealed interface WorkoutRuntimeEvent {
    data object CountdownTick : WorkoutRuntimeEvent
    data object RepConfirmed : WorkoutRuntimeEvent
    data object SetFinished : WorkoutRuntimeEvent
}
