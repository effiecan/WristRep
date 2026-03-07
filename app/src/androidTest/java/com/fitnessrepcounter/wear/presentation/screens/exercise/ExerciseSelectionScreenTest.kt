package com.fitnessrepcounter.wear.presentation.screens.exercise

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState
import com.fitnessrepcounter.wear.ui.theme.FitnessRepCounterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseSelectionScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun exerciseSelection_showsOnlySelectableVisibleExercises_withoutSectionHeaders() {
        composeTestRule.setContent {
            FitnessRepCounterTheme {
                ExerciseSelectionScreen(
                    uiState = WorkoutUiState(),
                    onSelectExercise = {},
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Biceps Curl").assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithText("Shoulder Press").assertIsDisplayed().assertHasClickAction()
        composeTestRule.onAllNodesWithText("Optimized").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Experimental").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Soon").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Dumbbell Row").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Dumbbell Chest Press").assertCountEquals(0)
    }
}
