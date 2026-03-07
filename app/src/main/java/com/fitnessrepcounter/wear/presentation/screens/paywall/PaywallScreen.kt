package com.fitnessrepcounter.wear.presentation.screens.paywall

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.fitnessrepcounter.wear.R
import com.fitnessrepcounter.wear.presentation.components.BadgeTone
import com.fitnessrepcounter.wear.presentation.components.PrimaryActionButton
import com.fitnessrepcounter.wear.presentation.components.StatusBadge
import com.fitnessrepcounter.wear.presentation.components.WristRepScreenScaffold
import com.fitnessrepcounter.wear.presentation.state.PaywallUiState
import com.fitnessrepcounter.wear.ui.theme.WatchTextSecondary
import com.fitnessrepcounter.wear.ui.theme.WatchTextTertiary

@Suppress("UNUSED_PARAMETER")
@Composable
fun PaywallScreen(
    uiState: PaywallUiState,
    onUnlockClick: () -> Unit,
    onBack: () -> Unit,
) {
    WristRepScreenScaffold(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.unlock_pro),
            style = MaterialTheme.typography.title1,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        StatusBadge(
            label = stringResource(R.string.pay_once),
            tone = BadgeTone.WARM,
            horizontalPadding = 7.dp,
            verticalPadding = 2.dp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.unlimited_workouts),
                style = MaterialTheme.typography.title2,
                textAlign = TextAlign.Center,
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                PaywallValueLine(text = stringResource(R.string.keep_history))
                PaywallValueLine(text = stringResource(R.string.no_trial_limits))
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        PrimaryActionButton(
            text = stringResource(R.string.unlock_pro),
            onClick = onUnlockClick,
            enabled = uiState.canUnlockPro,
            modifier = Modifier
                .fillMaxWidth(0.64f)
                .testTag("paywall_unlock_button"),
            height = 36.dp,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = uiState.formattedPrice?.let { price ->
                stringResource(R.string.no_subscription_price, price)
            } ?: stringResource(R.string.no_subscription),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("paywall_reassurance"),
            style = MaterialTheme.typography.caption2,
            color = WatchTextTertiary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PaywallValueLine(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.body2,
            color = WatchTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
