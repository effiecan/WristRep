package com.fitnessrepcounter.wear.presentation.screens.workout

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessrepcounter.wear.R
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

    private val context: Context = ApplicationProvider.getApplicationContext()

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

        composeTestRule.onNodeWithText(context.getString(R.string.exercise_triceps_extension)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.set_number, 1)).assertIsDisplayed()
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("active_workout_status").assertIsDisplayed()
        composeTestRule.onNodeWithTag("active_workout_actions").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.rep_adjust_minus_one)).assertIsDisplayed()
        composeTestRule.onNodeWithTag("end_set_button").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithText(context.getString(R.string.watch_cta_end_set)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.rep_adjust_plus_one)).assertIsDisplayed()
        composeTestRule.onAllNodesWithText(context.getString(R.string.label_experimental)).assertCountEquals(0)
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
        composeTestRule.onNodeWithText(context.getString(R.string.exercise_triceps_extension)).assertIsDisplayed()
        composeTestRule.onNodeWithTag("active_workout_actions").assertIsDisplayed()
    }
}
