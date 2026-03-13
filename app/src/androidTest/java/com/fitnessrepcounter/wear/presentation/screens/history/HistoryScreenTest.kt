package com.fitnessrepcounter.wear.presentation.screens.history

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.WorkoutSession
import com.fitnessrepcounter.wear.domain.model.WorkoutSet
import com.fitnessrepcounter.wear.domain.model.WorkoutStatus
import com.fitnessrepcounter.wear.presentation.state.HistoryUiState
import com.fitnessrepcounter.wear.ui.theme.FitnessRepCounterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun historyScreen_rendersNewExerciseNames() {
        composeTestRule.setContent {
            FitnessRepCounterTheme {
                HistoryScreen(
                    uiState = HistoryUiState(
                        workouts = listOf(
                            WorkoutSession(
                                id = "history-1",
                                exercise = Exercise.CHEST_PRESS,
                                sets = listOf(
                                    WorkoutSet(
                                        setNumber = 1,
                                        repCount = 10,
                                        startedAtEpochMs = 1_000L,
                                        endedAtEpochMs = 2_000L,
                                        manualAdjustmentCount = 0,
                                    ),
                                ),
                                startedAtEpochMs = 1_000L,
                                endedAtEpochMs = 2_000L,
                                status = WorkoutStatus.COMPLETED,
                            ),
                        ),
                    ),
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(com.fitnessrepcounter.wear.R.string.exercise_chest_press))
            .assertIsDisplayed()
    }
}
