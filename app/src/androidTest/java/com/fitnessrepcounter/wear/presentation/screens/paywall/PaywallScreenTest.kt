package com.fitnessrepcounter.wear.presentation.screens.paywall

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
import com.fitnessrepcounter.wear.presentation.state.PaywallUiState
import com.fitnessrepcounter.wear.ui.theme.FitnessRepCounterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaywallScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun paywallScreen_showsSimplifiedCopy_withoutBackButton() {
        composeTestRule.setContent {
            FitnessRepCounterTheme {
                PaywallScreen(
                    uiState = PaywallUiState(
                        isBillingReady = true,
                        isProductAvailable = true,
                        formattedPrice = "$14.99",
                    ),
                    onUnlockClick = {},
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.unlock_pro)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.pay_once)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.unlimited_workouts)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.keep_history)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.no_trial_limits)).assertIsDisplayed()
        composeTestRule.onNodeWithTag("paywall_unlock_button").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag("paywall_reassurance").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.no_subscription_price, "$14.99")).assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Train without limits on this watch.").assertCountEquals(0)
        composeTestRule.onAllNodesWithText(context.getString(R.string.back)).assertCountEquals(0)
    }

    @Test
    fun paywallScreen_showsFallbackReassurance_whenPriceIsUnavailable() {
        composeTestRule.setContent {
            FitnessRepCounterTheme {
                PaywallScreen(
                    uiState = PaywallUiState(),
                    onUnlockClick = {},
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.no_subscription)).assertIsDisplayed()
    }
}
