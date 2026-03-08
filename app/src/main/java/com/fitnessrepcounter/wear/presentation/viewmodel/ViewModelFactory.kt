package com.fitnessrepcounter.wear.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fitnessrepcounter.wear.di.AppContainer

class ViewModelFactory(
    private val appContainer: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    workoutRepository = appContainer.workoutRepository,
                    entitlementRepository = appContainer.entitlementRepository,
                    workoutRuntimeRepository = appContainer.workoutRuntimeRepository,
                ) as T
            }

            modelClass.isAssignableFrom(WorkoutViewModel::class.java) -> {
                WorkoutViewModel(
                    workoutRuntimeRepository = appContainer.workoutRuntimeRepository,
                ) as T
            }

            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(
                    workoutRepository = appContainer.workoutRepository,
                ) as T
            }

            modelClass.isAssignableFrom(PaywallViewModel::class.java) -> {
                PaywallViewModel(
                    entitlementRepository = appContainer.entitlementRepository,
                    hapticsManager = appContainer.hapticsManager,
                ) as T
            }

            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    settingsRepository = appContainer.settingsRepository,
                    entitlementRepository = appContainer.entitlementRepository,
                ) as T
            }

            else -> error("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
