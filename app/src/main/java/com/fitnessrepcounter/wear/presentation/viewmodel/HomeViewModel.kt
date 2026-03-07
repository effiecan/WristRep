package com.fitnessrepcounter.wear.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessrepcounter.wear.domain.repository.EntitlementRepository
import com.fitnessrepcounter.wear.domain.repository.WorkoutRepository
import com.fitnessrepcounter.wear.navigation.AppRoute
import com.fitnessrepcounter.wear.presentation.state.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val workoutRepository: WorkoutRepository,
    private val entitlementRepository: EntitlementRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val completedWorkouts = workoutRepository.countCompletedWorkouts()
            entitlementRepository.reconcileCompletedWorkoutUsage(completedWorkouts)
        }

        viewModelScope.launch {
            entitlementRepository.observeEntitlement()
                .combine(workoutRepository.observeHistory()) { entitlement, history ->
                    HomeUiState(
                        entitlementState = entitlement,
                        recentWorkouts = history.take(3),
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun startWorkoutDestination(): String {
        return if (_uiState.value.entitlementState.canStartWorkout) {
            AppRoute.WorkoutFlow.route
        } else {
            AppRoute.Paywall.route
        }
    }
}
