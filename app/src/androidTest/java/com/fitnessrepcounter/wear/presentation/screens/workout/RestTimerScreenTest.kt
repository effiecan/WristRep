package com.fitnessrepcounter.wear.presentation.screens.workout

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessrepcounter.wear.R
import com.fitnessrepcounter.wear.presentation.state.WorkoutUiState
import com.fitnessrepcounter.wear.ui.theme.FitnessRepCounterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RestTimerScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun restTimerScreen_showsCompactSkipButton() {
        composeTestRule.setContent {
            FitnessRepCounterTheme {
                RestTimerScreen(
                    uiState = WorkoutUiState(restSecondsRemaining = 52),
                    onSkip = {},
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.skip)).assertIsDisplayed().assertIsEnabled()
    }
}
