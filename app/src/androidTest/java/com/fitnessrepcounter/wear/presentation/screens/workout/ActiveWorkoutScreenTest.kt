package com.fitnessrepcounter.wear.presentation.screens.workout

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState
import com.fitnessrepcounter.wear.ui.theme.FitnessRepCounterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActiveWorkoutScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun activeWorkout_showsBottomActionRow_andNoExperimentalHelperCopy() {
        composeTestRule.setContent {
            FitnessRepCounterTheme {
                ActiveWorkoutScreen(
                    uiState = WorkoutUiState(
                        selectedExercise = Exercise.TRICEPS_EXTENSION,
                        currentSetNumber = 1,
                        currentRepCount = 0,
                    ),
                    onAddRep = {},
                    onRemoveRep = {},
                    onEndSet = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Triceps Extension").assertIsDisplayed()
        composeTestRule.onNodeWithText("Set 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("active_workout_status").assertIsDisplayed()
        composeTestRule.onNodeWithTag("active_workout_actions").assertIsDisplayed()
        composeTestRule.onNodeWithText("-1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("end_set_button").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithText("+1").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Experimental").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Manual correction stays available.").assertCountEquals(0)
    }

    @Test
    fun activeWorkout_keepsLongTitleOnSingleVisibleLine() {
        composeTestRule.setContent {
            FitnessRepCounterTheme {
                ActiveWorkoutScreen(
                    uiState = WorkoutUiState(
                        selectedExercise = Exercise.TRICEPS_EXTENSION,
                        currentSetNumber = 2,
                        currentRepCount = 12,
                    ),
                    onAddRep = {},
                    onRemoveRep = {},
                    onEndSet = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("active_workout_title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Triceps Extension").assertIsDisplayed()
        composeTestRule.onNodeWithTag("active_workout_actions").assertIsDisplayed()
    }
}
