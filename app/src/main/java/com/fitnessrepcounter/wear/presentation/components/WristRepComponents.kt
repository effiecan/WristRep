package com.fitnessrepcounter.wear.presentation.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.compose.runtime.remember
import androidx.compose.ui.text.rememberTextMeasurer
import com.fitnessrepcounter.wear.presentation.state.AmbientModeState
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
fun WorkoutKeepScreenOnEffect(
    enabled: Boolean,
) {
    val view = LocalView.current

    DisposableEffect(view, enabled) {
        val window = view.context.findActivity()?.window
        if (enabled) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@Composable
fun WristRepScreenScaffold(
    modifier: Modifier = Modifier,
    ambientModeState: AmbientModeState = AmbientModeState(),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    val ambientShift = when {
        !ambientModeState.isAmbient || !ambientModeState.burnInProtectionRequired -> 0.dp
        ambientModeState.ambientUpdateCount % 2 == 0 -> 2.dp
        else -> (-2).dp
    }

    Scaffold(timeText = { TimeText() }) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .offset(x = ambientShift, y = ambientShift)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}

@Composable
fun WearListScreenScaffold(
    modifier: Modifier = Modifier,
    state: ScalingLazyListState = rememberScalingLazyListState(),
    contentPadding: PaddingValues = PaddingValues(
        start = 12.dp,
        top = 18.dp,
        end = 12.dp,
        bottom = 56.dp,
    ),
    content: ScalingLazyListScope.() -> Unit,
) {
    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = state) },
    ) {
        ScalingLazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            state = state,
            contentPadding = contentPadding,
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
    maxLines: Int = 2,
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
        AdaptiveButtonLabel(text = text, fontWeight = FontWeight.SemiBold, maxLines = maxLines)
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    height: Dp = 40.dp,
    maxLines: Int = 2,
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
        AdaptiveButtonLabel(text = text, fontWeight = FontWeight.Medium, maxLines = maxLines)
    }
}

@Composable
fun CompactUtilityChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    height: Dp = 28.dp,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.surface,
            contentColor = WatchTextSecondary,
            disabledBackgroundColor = MaterialTheme.colors.surface,
            disabledContentColor = WatchTextSecondary.copy(alpha = 0.45f),
        ),
    ) {
        AdaptiveButtonLabel(text = text, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
fun ListActionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    selected: Boolean = false,
    height: Dp = 34.dp,
    maxLines: Int = 2,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (selected) WatchAccentSoft else WatchSurfaceSecondary,
            contentColor = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface,
            disabledBackgroundColor = MaterialTheme.colors.surface,
            disabledContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.45f),
        ),
    ) {
        AdaptiveButtonLabel(
            text = text,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = maxLines,
        )
    }
}

@Composable
private fun AdaptiveButtonLabel(
    text: String,
    fontWeight: FontWeight,
    maxLines: Int,
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val candidateStyles = listOf(16.sp, 14.sp, 12.sp).map { size ->
        MaterialTheme.typography.body1.copy(
            fontSize = size,
            fontWeight = fontWeight,
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val contentWidthPx = (with(density) { maxWidth.roundToPx() } - with(density) { 24.dp.roundToPx() })
            .coerceAtLeast(1)
        val selectedStyle = remember(text, fontWeight, maxLines, contentWidthPx, candidateStyles, textMeasurer) {
            candidateStyles.firstOrNull { style ->
                !textMeasurer.measure(
                    text = AnnotatedString(text),
                    style = style,
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis,
                    constraints = Constraints(maxWidth = contentWidthPx),
                ).hasVisualOverflow
            } ?: candidateStyles.last()
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                modifier = Modifier.fillMaxWidth(),
                style = selectedStyle,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
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

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}
