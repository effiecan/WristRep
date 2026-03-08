package com.fitnessrepcounter.wear.presentation.screens.settings

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessrepcounter.wear.R
import com.fitnessrepcounter.wear.domain.model.HapticMode
import com.fitnessrepcounter.wear.ui.theme.FitnessRepCounterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HapticSettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun everyRepMode_showsKeepScreenAwakeControl() {
        composeTestRule.setContent {
            FitnessRepCounterTheme {
                HapticSettingsScreen(
                    selectedMode = HapticMode.EVERY_REP,
                    onSelectMode = {},
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.haptics_keep_screen_awake_description)).assertIsDisplayed()
    }

    @Test
    fun nonEveryRepMode_hidesKeepScreenAwakeControl() {
        composeTestRule.setContent {
            FitnessRepCounterTheme {
                HapticSettingsScreen(
                    selectedMode = HapticMode.IMPORTANT_ONLY,
                    onSelectMode = {},
                    onBack = {},
                )
            }
        }

        composeTestRule.onAllNodesWithText(context.getString(R.string.haptics_keep_screen_awake_description)).assertCountEquals(0)
    }
}
