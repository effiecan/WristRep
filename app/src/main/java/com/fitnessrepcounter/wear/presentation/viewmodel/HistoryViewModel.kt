package com.fitnessrepcounter.wear.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessrepcounter.wear.domain.repository.WorkoutRepository
import com.fitnessrepcounter.wear.presentation.state.HistoryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            workoutRepository.observeHistory().collect { workouts ->
                _uiState.value = HistoryUiState(workouts = workouts)
            }
        }
    }
}
