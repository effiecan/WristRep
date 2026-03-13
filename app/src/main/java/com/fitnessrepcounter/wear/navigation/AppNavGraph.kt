package com.fitnessrepcounter.wear.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.fitnessrepcounter.wear.domain.repository.WorkoutRuntimeRepository
import com.fitnessrepcounter.wear.presentation.screens.exercise.ExerciseSelectionScreen
import com.fitnessrepcounter.wear.presentation.screens.history.HistoryScreen
import com.fitnessrepcounter.wear.presentation.screens.home.HomeScreen
import com.fitnessrepcounter.wear.presentation.screens.paywall.PaywallScreen
import com.fitnessrepcounter.wear.presentation.screens.ready.ReadyScreen
import com.fitnessrepcounter.wear.presentation.screens.settings.HapticSettingsScreen
import com.fitnessrepcounter.wear.presentation.screens.settings.LanguageSettingsScreen
import com.fitnessrepcounter.wear.presentation.screens.settings.SettingsScreen
import com.fitnessrepcounter.wear.presentation.screens.summary.WorkoutSummaryScreen
import com.fitnessrepcounter.wear.presentation.screens.workout.ActiveWorkoutScreen
import com.fitnessrepcounter.wear.presentation.screens.workout.EndSetConfirmationScreen
import com.fitnessrepcounter.wear.presentation.screens.workout.RestTimerScreen
import com.fitnessrepcounter.wear.presentation.screens.workout.shouldKeepScreenOnDuringWorkout
import com.fitnessrepcounter.wear.presentation.state.AmbientModeState
import com.fitnessrepcounter.wear.presentation.viewmodel.HistoryViewModel
import com.fitnessrepcounter.wear.presentation.viewmodel.HomeViewModel
import com.fitnessrepcounter.wear.presentation.viewmodel.PaywallViewModel
import com.fitnessrepcounter.wear.presentation.viewmodel.SettingsViewModel
import com.fitnessrepcounter.wear.presentation.viewmodel.WorkoutViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    viewModelFactory: ViewModelProvider.Factory,
    workoutRuntimeRepository: WorkoutRuntimeRepository,
    resumeWorkoutRequests: StateFlow<Int>,
    ambientModeState: StateFlow<AmbientModeState>,
) {
    val navController = rememberSwipeDismissableNavController()
    val workoutViewModel: WorkoutViewModel = viewModel(factory = viewModelFactory)
    val workoutUiState by workoutViewModel.uiState.collectAsState()
    val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val resumeRequestCount by resumeWorkoutRequests.collectAsState()
    val hasActiveWorkout by workoutRuntimeRepository.hasActiveSession.collectAsState()
    val ambientState by ambientModeState.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryFlow.collectAsState(
        initial = navController.currentBackStackEntry,
    )
    val currentRoute = currentBackStackEntry?.destination?.route

    LaunchedEffect(resumeRequestCount, hasActiveWorkout, currentRoute) {
        if (resumeRequestCount == 0 || !hasActiveWorkout) return@LaunchedEffect
        navController.resumeWorkoutRoute(
            currentRoute = currentRoute,
            targetRoute = workoutRouteForStep(workoutUiState.currentStep),
        )
    }

    LaunchedEffect(hasActiveWorkout, workoutUiState.currentStep, currentRoute) {
        val targetRoute = workoutRouteSyncTarget(
            hasActiveSession = hasActiveWorkout,
            step = workoutUiState.currentStep,
            currentRoute = currentRoute,
        ) ?: return@LaunchedEffect

        navController.resumeWorkoutRoute(
            currentRoute = currentRoute,
            targetRoute = targetRoute,
        )
    }

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = AppRoute.Launch.route,
        userSwipeEnabled = !isProtectedWorkoutRoute(currentRoute),
    ) {
        composable(AppRoute.Launch.route) {
            val launchDestination = launchRouteForWorkoutState(
                hasActiveSession = hasActiveWorkout,
                step = workoutRuntimeRepository.currentStep(),
            )

            LaunchedEffect(launchDestination) {
                navController.navigate(launchDestination) {
                    popUpTo(AppRoute.Launch.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable(AppRoute.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
            val uiState by homeViewModel.uiState.collectAsState()

            HomeScreen(
                uiState = uiState,
                onStartWorkout = {
                    navController.navigate(homeViewModel.startWorkoutDestination()) {
                        launchSingleTop = true
                    }
                },
                onHistoryClick = {
                    navController.navigate(AppRoute.History.route)
                },
                onSettingsClick = {
                    navController.navigate(AppRoute.Settings.route)
                },
                onPremiumClick = {
                    navController.navigate(AppRoute.Paywall.route)
                },
            )
        }

        composable(AppRoute.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
            val uiState by settingsViewModel.uiState.collectAsState()

            SettingsScreen(
                uiState = uiState,
                onHapticsClick = { navController.navigate(AppRoute.HapticSettings.route) },
                onLanguageClick = { navController.navigate(AppRoute.LanguageSettings.route) },
                onPremiumClick = { navController.navigate(AppRoute.Paywall.route) },
                onBack = { navController.popBackStack() },
            )
        }

            composable(AppRoute.HapticSettings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
                val uiState by settingsViewModel.uiState.collectAsState()

                HapticSettingsScreen(
                    selectedMode = uiState.hapticMode,
                    onSelectMode = settingsViewModel::setHapticMode,
                    onBack = { navController.popBackStack() },
                )
            }

        composable(AppRoute.LanguageSettings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
            val uiState by settingsViewModel.uiState.collectAsState()

            LanguageSettingsScreen(
                selectedLanguageTag = uiState.selectedLanguageTag,
                languageOptions = uiState.languageOptions,
                onSelectLanguage = settingsViewModel::setLanguage,
                onBack = { navController.popBackStack() },
            )
        }

        composable(AppRoute.History.route) {
            val historyViewModel: HistoryViewModel = viewModel(factory = viewModelFactory)
            val uiState by historyViewModel.uiState.collectAsState()

            HistoryScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
            )
        }

        composable(AppRoute.Paywall.route) {
            val paywallViewModel: PaywallViewModel = viewModel(factory = viewModelFactory)
            val uiState by paywallViewModel.uiState.collectAsState()
            val activity = LocalContext.current.findActivity()
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(uiState.entitlementState.isProUnlocked) {
                if (uiState.entitlementState.isProUnlocked) {
                    navController.popBackStack()
                }
            }

            PaywallScreen(
                uiState = uiState,
                onUnlockClick = {
                    if (activity != null) {
                        coroutineScope.launch {
                            paywallViewModel.unlockPro(activity)
                        }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        navigation(
            route = AppRoute.WorkoutFlow.route,
            startDestination = AppRoute.ExerciseSelection.route,
        ) {
            composable(AppRoute.ExerciseSelection.route) {
                LaunchedEffect(hasActiveWorkout) {
                    if (!hasActiveWorkout) {
                        workoutViewModel.prepareNewWorkout()
                    }
                }

                ExerciseSelectionScreen(
                    uiState = workoutUiState,
                    onSelectExercise = { exercise ->
                        workoutViewModel.selectExercise(exercise)
                        navController.navigate(AppRoute.Ready.route) {
                            popUpTo(AppRoute.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onBack = {
                        workoutViewModel.discardWorkout()
                        navController.popBackStack()
                    },
                )
            }

            composable(AppRoute.Ready.route) {
                LaunchedEffect(Unit) {
                    workoutViewModel.startReadyCountdown()
                }

                ReadyScreen(
                    uiState = workoutUiState,
                    ambientModeState = ambientState,
                    shouldKeepScreenOn = shouldKeepScreenOnDuringWorkout(
                        hapticMode = settingsUiState.hapticMode,
                        workoutUiState = workoutUiState,
                        ambientModeState = ambientState,
                    ),
                )
            }

            composable(AppRoute.ActiveWorkout.route) {
                ActiveWorkoutScreen(
                    uiState = workoutUiState,
                    ambientModeState = ambientState,
                    shouldKeepScreenOn = shouldKeepScreenOnDuringWorkout(
                        hapticMode = settingsUiState.hapticMode,
                        workoutUiState = workoutUiState,
                        ambientModeState = ambientState,
                    ),
                    onAddRep = workoutViewModel::addManualRep,
                    onRemoveRep = workoutViewModel::removeManualRep,
                    onEndSet = {
                        if (workoutViewModel.endCurrentSet()) {
                            navController.navigate(AppRoute.EndSetConfirmation.route) {
                                launchSingleTop = true
                            }
                        }
                    },
                )
            }

            composable(AppRoute.EndSetConfirmation.route) {
                EndSetConfirmationScreen(
                    uiState = workoutUiState,
                    ambientModeState = ambientState,
                    onRestClick = {
                        navController.navigate(AppRoute.RestTimer.route) {
                            launchSingleTop = true
                        }
                    },
                    onFinishWorkout = {
                        if (workoutViewModel.finishWorkout()) {
                            navController.navigate(AppRoute.WorkoutSummary.route) {
                                launchSingleTop = true
                            }
                        }
                    },
                )
            }

            composable(AppRoute.RestTimer.route) {
                LaunchedEffect(Unit) {
                    workoutViewModel.beginRestTimer()
                }

                RestTimerScreen(
                    uiState = workoutUiState,
                    ambientModeState = ambientState,
                    onSkip = workoutViewModel::skipRestTimer,
                )
            }

            composable(AppRoute.WorkoutSummary.route) {
                val coroutineScope = rememberCoroutineScope()

                WorkoutSummaryScreen(
                    uiState = workoutUiState,
                    ambientModeState = ambientState,
                    onSave = {
                        coroutineScope.launch {
                            if (workoutViewModel.saveWorkout()) {
                                navController.navigate(AppRoute.Home.route) {
                                    popUpTo(AppRoute.WorkoutFlow.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    onDiscard = {
                        workoutViewModel.discardWorkout()
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(AppRoute.WorkoutFlow.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

private fun NavHostController.resumeWorkoutRoute(
    currentRoute: String?,
    targetRoute: String,
) {
    if (currentRoute == targetRoute) return

    navigate(targetRoute) {
        launchSingleTop = true
        when {
            currentRoute == AppRoute.Launch.route -> {
                popUpTo(AppRoute.Launch.route) { inclusive = true }
            }

            isProtectedWorkoutRoute(currentRoute) -> {
                popUpTo(AppRoute.WorkoutFlow.route)
            }

            else -> {
                popUpTo(AppRoute.Home.route) { inclusive = true }
            }
        }
    }
}
