package com.fitnessrepcounter.wear.presentation.screens.summary

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
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
class WorkoutSummaryScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun workoutSummaryScreen_rendersNewExerciseName() {
        composeTestRule.setContent {
            FitnessRepCounterTheme {
                WorkoutSummaryScreen(
                    uiState = WorkoutUiState(
                        selectedExercise = Exercise.STANDING_MULTI_FLY_LATERAL_RAISE,
                        totalReps = 12,
                        canSave = true,
                    ),
                    onSave = {},
                    onDiscard = {},
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.exercise_standing_multi_fly_lateral_raise))
            .assertIsDisplayed()
    }
}
