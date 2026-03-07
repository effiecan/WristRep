package com.fitnessrepcounter.wear.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navigation
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.fitnessrepcounter.wear.presentation.screens.exercise.ExerciseSelectionScreen
import com.fitnessrepcounter.wear.presentation.screens.history.HistoryScreen
import com.fitnessrepcounter.wear.presentation.screens.home.HomeScreen
import com.fitnessrepcounter.wear.presentation.screens.paywall.PaywallScreen
import com.fitnessrepcounter.wear.presentation.screens.ready.ReadyScreen
import com.fitnessrepcounter.wear.presentation.screens.summary.WorkoutSummaryScreen
import com.fitnessrepcounter.wear.presentation.screens.workout.ActiveWorkoutScreen
import com.fitnessrepcounter.wear.presentation.screens.workout.EndSetConfirmationScreen
import com.fitnessrepcounter.wear.presentation.screens.workout.RestTimerScreen
import com.fitnessrepcounter.wear.presentation.state.WorkoutStep
import com.fitnessrepcounter.wear.presentation.viewmodel.HistoryViewModel
import com.fitnessrepcounter.wear.presentation.viewmodel.HomeViewModel
import com.fitnessrepcounter.wear.presentation.viewmodel.PaywallViewModel
import com.fitnessrepcounter.wear.presentation.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    viewModelFactory: ViewModelProvider.Factory,
) {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = AppRoute.Home.route,
    ) {
        composable(AppRoute.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
            val uiState by homeViewModel.uiState.collectAsState()

            HomeScreen(
                uiState = uiState,
                onStartWorkout = {
                    navController.navigate(homeViewModel.startWorkoutDestination())
                },
                onHistoryClick = {
                    navController.navigate(AppRoute.History.route)
                },
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
            val coroutineScope = rememberCoroutineScope()

            PaywallScreen(
                uiState = uiState,
                onUnlockClick = {
                    coroutineScope.launch {
                        paywallViewModel.unlockPro()
                        navController.popBackStack()
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        navigation(
            route = AppRoute.WorkoutFlow.route,
            startDestination = AppRoute.ExerciseSelection.route,
        ) {
            composable(AppRoute.ExerciseSelection.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppRoute.WorkoutFlow.route)
                }
                val workoutViewModel: WorkoutViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = viewModelFactory,
                )
                val uiState by workoutViewModel.uiState.collectAsState()

                DisposableEffect(parentEntry, workoutViewModel) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_DESTROY) {
                            workoutViewModel.discardWorkout()
                        }
                    }
                    parentEntry.lifecycle.addObserver(observer)
                    onDispose {
                        parentEntry.lifecycle.removeObserver(observer)
                    }
                }

                LaunchedEffect(Unit) {
                    workoutViewModel.prepareNewWorkout()
                }

                ExerciseSelectionScreen(
                    uiState = uiState,
                    onSelectExercise = { exercise ->
                        workoutViewModel.selectExercise(exercise)
                        navController.navigate(AppRoute.Ready.route)
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(AppRoute.Ready.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppRoute.WorkoutFlow.route)
                }
                val workoutViewModel: WorkoutViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = viewModelFactory,
                )
                val uiState by workoutViewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    workoutViewModel.startReadyCountdown()
                }

                LaunchedEffect(uiState.currentStep) {
                    if (uiState.currentStep == WorkoutStep.ACTIVE) {
                        navController.navigate(AppRoute.ActiveWorkout.route) {
                            popUpTo(AppRoute.Ready.route) { inclusive = true }
                        }
                    }
                }

                ReadyScreen(
                    uiState = uiState,
                )
            }

            composable(AppRoute.ActiveWorkout.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppRoute.WorkoutFlow.route)
                }
                val workoutViewModel: WorkoutViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = viewModelFactory,
                )
                val uiState by workoutViewModel.uiState.collectAsState()

                ActiveWorkoutScreen(
                    uiState = uiState,
                    onAddRep = workoutViewModel::addManualRep,
                    onRemoveRep = workoutViewModel::removeManualRep,
                    onEndSet = {
                        if (workoutViewModel.endCurrentSet()) {
                            navController.navigate(AppRoute.EndSetConfirmation.route)
                        }
                    },
                )
            }

            composable(AppRoute.EndSetConfirmation.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppRoute.WorkoutFlow.route)
                }
                val workoutViewModel: WorkoutViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = viewModelFactory,
                )
                val uiState by workoutViewModel.uiState.collectAsState()

                EndSetConfirmationScreen(
                    uiState = uiState,
                    onRestClick = {
                        navController.navigate(AppRoute.RestTimer.route)
                    },
                    onFinishWorkout = {
                        if (workoutViewModel.finishWorkout()) {
                            navController.navigate(AppRoute.WorkoutSummary.route)
                        }
                    },
                )
            }

            composable(AppRoute.RestTimer.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppRoute.WorkoutFlow.route)
                }
                val workoutViewModel: WorkoutViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = viewModelFactory,
                )
                val uiState by workoutViewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    workoutViewModel.beginRestTimer()
                }

                LaunchedEffect(uiState.currentStep) {
                    if (uiState.currentStep == WorkoutStep.ACTIVE) {
                        navController.navigate(AppRoute.ActiveWorkout.route) {
                            popUpTo(AppRoute.RestTimer.route) { inclusive = true }
                        }
                    }
                }

                RestTimerScreen(
                    uiState = uiState,
                    onSkip = workoutViewModel::skipRestTimer,
                )
            }

            composable(AppRoute.WorkoutSummary.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppRoute.WorkoutFlow.route)
                }
                val workoutViewModel: WorkoutViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = viewModelFactory,
                )
                val uiState by workoutViewModel.uiState.collectAsState()
                val coroutineScope = rememberCoroutineScope()

                WorkoutSummaryScreen(
                    uiState = uiState,
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
