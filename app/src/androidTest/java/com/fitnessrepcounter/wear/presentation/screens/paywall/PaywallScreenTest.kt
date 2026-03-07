package com.fitnessrepcounter.wear.presentation.screens.paywall

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessrepcounter.wear.presentation.state.PaywallUiState
import com.fitnessrepcounter.wear.ui.theme.FitnessRepCounterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaywallScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun paywallScreen_showsSimplifiedCopy_withoutBackButton() {
        composeTestRule.setContent {
            FitnessRepCounterTheme {
                PaywallScreen(
                    uiState = PaywallUiState(),
                    onUnlockClick = {},
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Unlock Pro").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pay once").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unlimited workouts").assertIsDisplayed()
        composeTestRule.onNodeWithText("Keep history").assertIsDisplayed()
        composeTestRule.onNodeWithText("No trial limits").assertIsDisplayed()
        composeTestRule.onNodeWithTag("paywall_unlock_button").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag("paywall_reassurance").assertIsDisplayed()
        composeTestRule.onNodeWithText("No subscription • $9.99").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Train without limits on this watch.").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Back").assertCountEquals(0)
    }
}
