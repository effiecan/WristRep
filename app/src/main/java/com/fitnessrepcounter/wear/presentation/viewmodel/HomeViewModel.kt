package com.fitnessrepcounter.wear.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import com.fitnessrepcounter.wear.domain.repository.WorkoutRepository
import com.fitnessrepcounter.wear.domain.repository.WorkoutRuntimeRepository
import com.fitnessrepcounter.wear.navigation.AppRoute
import com.fitnessrepcounter.wear.navigation.workoutRouteForStep
import com.fitnessrepcounter.wear.presentation.state.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(
    private val workoutRepository: WorkoutRepository,
    private val entitlementRepository: EntitlementRepository,
    private val workoutRuntimeRepository: WorkoutRuntimeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val completedWorkouts = workoutRepository.countCompletedWorkouts()
            entitlementRepository.reconcileCompletedWorkoutUsage(completedWorkouts)
        }

        viewModelScope.launch {
            entitlementRepository.syncBillingState()
        }

        viewModelScope.launch {
            entitlementRepository.observeEntitlement()
                .combine(workoutRepository.observeHistory()) { entitlement, history ->
                    entitlement to history
                }
                .combine(workoutRuntimeRepository.hasActiveSession) { (entitlement, history), hasActiveSession ->
                    HomeUiState(
                        entitlementState = entitlement,
                        recentWorkouts = history.take(3),
                        hasActiveWorkout = hasActiveSession,
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun startWorkoutDestination(): String {
        if (_uiState.value.hasActiveWorkout) {
            return workoutRouteForStep(workoutRuntimeRepository.currentStep())
        }
        return if (_uiState.value.entitlementState.canStartWorkout) {
            AppRoute.WorkoutFlow.route
        } else {
            AppRoute.Paywall.route
        }
    }
}
