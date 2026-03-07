package com.fitnessrepcounter.wear.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.fitnessrepcounter.wear.ui.theme.WatchAccentSoft
import com.fitnessrepcounter.wear.ui.theme.WatchBadgeMuted
import com.fitnessrepcounter.wear.ui.theme.WatchBlack
import com.fitnessrepcounter.wear.ui.theme.WatchSurfaceSecondary
import com.fitnessrepcounter.wear.ui.theme.WatchTextSecondary
import com.fitnessrepcounter.wear.ui.theme.WatchTextTertiary
import com.fitnessrepcounter.wear.ui.theme.WatchWarm
import com.fitnessrepcounter.wear.ui.theme.WatchWarmSoft

enum class BadgeTone {
    ACCENT,
    WARM,
    MUTED,
}

@Composable
fun WristRepScreenScaffold(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(timeText = { TimeText() }) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}

@Composable
fun ScreenTitle(
    title: String,
    badgeLabel: String? = null,
    badgeTone: BadgeTone = BadgeTone.MUTED,
) {
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.title1,
        textAlign = TextAlign.Center,
    )
    if (badgeLabel != null) {
        Spacer(modifier = Modifier.height(8.dp))
        StatusBadge(label = badgeLabel, tone = badgeTone)
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    height: Dp = 44.dp,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary,
            disabledBackgroundColor = MaterialTheme.colors.primaryVariant,
            disabledContentColor = MaterialTheme.colors.onPrimary.copy(alpha = 0.65f),
        ),
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    height: Dp = 40.dp,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.secondary,
            contentColor = MaterialTheme.colors.onSecondary,
            disabledBackgroundColor = MaterialTheme.colors.surface,
            disabledContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.45f),
        ),
    ) {
        Text(text = text, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CorrectionControlButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(size),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = WatchSurfaceSecondary,
            contentColor = MaterialTheme.colors.onSurface,
        ),
    ) {
        Text(text = label, style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatusBadge(
    label: String,
    tone: BadgeTone,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 10.dp,
    verticalPadding: Dp = 4.dp,
) {
    val background = when (tone) {
        BadgeTone.ACCENT -> WatchAccentSoft
        BadgeTone.WARM -> WatchWarmSoft
        BadgeTone.MUTED -> WatchBadgeMuted
    }
    val content = when (tone) {
        BadgeTone.ACCENT -> MaterialTheme.colors.primary
        BadgeTone.WARM -> WatchWarm
        BadgeTone.MUTED -> WatchTextSecondary
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            color = content,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.body2,
        color = WatchTextTertiary,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
fun SummaryCard(
    title: String,
    subtitle: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colors.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        SectionLabel(text = title)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = subtitle, style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun HistoryRowCard(
    title: String,
    subtitle: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colors.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.body1, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.body2, color = WatchTextSecondary)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
fun ExerciseRowCard(
    title: String,
    subtitle: String,
    statusLabel: String,
    badgeTone: BadgeTone,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 0.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(18.dp))
            .background(
                when {
                    enabled -> MaterialTheme.colors.surface
                    else -> WatchBadgeMuted
                },
            )
            .alpha(if (enabled) 1f else 0.58f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.body1, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.body2, color = WatchTextSecondary)
        }
        Spacer(modifier = Modifier.width(8.dp))
        StatusBadge(label = statusLabel, tone = badgeTone)
    }
}

@Composable
fun HeroMetric(
    value: String,
    label: String,
    supportingText: String? = null,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.body2,
        color = WatchTextSecondary,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = value,
        style = MaterialTheme.typography.display1,
        color = MaterialTheme.colors.primary,
    )
    if (supportingText != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = supportingText,
            style = MaterialTheme.typography.body2,
            color = WatchTextTertiary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun CenterMessage(
    title: String,
    body: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.title1)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.body2,
            color = WatchTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ScrollFadeHint(
    modifier: Modifier = Modifier,
    height: Dp = 24.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        WatchBlack.copy(alpha = 0f),
                        WatchBlack.copy(alpha = 0.72f),
                        WatchBlack,
                    ),
                ),
            ),
    )
}
