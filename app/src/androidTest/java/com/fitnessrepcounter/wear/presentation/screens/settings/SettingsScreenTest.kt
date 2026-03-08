package com.fitnessrepcounter.wear.presentation.screens.settings

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessrepcounter.wear.R
import com.fitnessrepcounter.wear.domain.model.EntitlementState
import com.fitnessrepcounter.wear.presentation.state.SettingsUiState
import com.fitnessrepcounter.wear.ui.theme.FitnessRepCounterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun settingsScreen_usesPremiumLabelForPremiumUsers() {
        composeTestRule.setContent {
            FitnessRepCounterTheme {
                SettingsScreen(
                    uiState = SettingsUiState(
                        entitlementState = EntitlementState(isProUnlocked = true),
                    ),
                    onHapticsClick = {},
                    onLanguageClick = {},
                    onPremiumClick = {},
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.haptics)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.language)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.manage_premium)).assertIsDisplayed()
    }
}
