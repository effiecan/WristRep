package com.fitnessrepcounter.wear.presentation.screens.exercise

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessrepcounter.wear.R
import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.isVisibleInList
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState
import com.fitnessrepcounter.wear.ui.theme.FitnessRepCounterTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseSelectionScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

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

        composeTestRule.onNodeWithText(context.getString(R.string.exercise_biceps_curl))
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNodeWithText(context.getString(R.string.exercise_shoulder_press))
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNodeWithText(context.getString(R.string.exercise_chest_press))
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNodeWithText(context.getString(R.string.exercise_lat_pulldown))
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNodeWithText(context.getString(R.string.exercise_standing_multi_fly_lateral_raise))
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onAllNodesWithText(context.getString(R.string.label_optimized)).assertCountEquals(0)
        composeTestRule.onAllNodesWithText(context.getString(R.string.label_experimental)).assertCountEquals(0)
        composeTestRule.onAllNodesWithText(context.getString(R.string.label_soon)).assertCountEquals(0)
        composeTestRule.onAllNodesWithText(context.getString(R.string.exercise_dumbbell_row)).assertCountEquals(0)
        composeTestRule.onAllNodesWithText(context.getString(R.string.exercise_dumbbell_chest_press)).assertCountEquals(0)
        assertEquals(17, Exercise.entries.count { it.isVisibleInList })
    }
}
