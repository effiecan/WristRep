package com.fitnessrepcounter.wear.presentation.screens.home

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessrepcounter.wear.R
import com.fitnessrepcounter.wear.domain.model.EntitlementState
import com.fitnessrepcounter.wear.presentation.state.HomeUiState
import com.fitnessrepcounter.wear.ui.theme.FitnessRepCounterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun premiumHome_prioritizesWorkoutAndHistory_withoutPremiumUpsell() {
        composeTestRule.setContent {
            FitnessRepCounterTheme {
                HomeScreen(
                    uiState = HomeUiState(
                        entitlementState = EntitlementState(isProUnlocked = true),
                        hasActiveWorkout = true,
                    ),
                    onStartWorkout = {},
                    onHistoryClick = {},
                    onSettingsClick = {},
                    onPremiumClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.settings)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.pro_unlocked)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.resume_workout)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.history)).assertIsDisplayed()
        composeTestRule.onAllNodesWithText(context.getString(R.string.go_premium)).assertCountEquals(0)
        composeTestRule.onAllNodesWithText(context.getString(R.string.manage_premium)).assertCountEquals(0)
    }

    @Test
    fun freeHome_showsHistoryAndOptionalPremiumUpsell() {
        composeTestRule.setContent {
            FitnessRepCounterTheme {
                HomeScreen(
                    uiState = HomeUiState(
                        entitlementState = EntitlementState(completedFreeWorkoutsUsed = 3),
                        hasActiveWorkout = false,
                    ),
                    onStartWorkout = {},
                    onHistoryClick = {},
                    onSettingsClick = {},
                    onPremiumClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.start_workout)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.history)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.go_premium)).assertIsDisplayed()
    }
}
