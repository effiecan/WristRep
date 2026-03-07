package com.fitnessrepcounter.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val WearColors = Colors(
    primary = WatchAccent,
    primaryVariant = WatchAccentMuted,
    secondary = WatchSurfaceSecondary,
    secondaryVariant = WatchCard,
    error = WatchDanger,
    background = WatchBlack,
    surface = WatchSurface,
    onPrimary = WatchBlack,
    onSecondary = WatchText,
    onError = WatchBlack,
    onBackground = WatchText,
    onSurface = WatchText,
)

@Composable
fun FitnessRepCounterTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colors = WearColors,
        typography = FitnessTypography,
        content = content,
    )
}
