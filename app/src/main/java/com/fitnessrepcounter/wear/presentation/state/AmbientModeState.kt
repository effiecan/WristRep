package com.fitnessrepcounter.wear.presentation.state

data class AmbientModeState(
    val isAmbient: Boolean = false,
    val burnInProtectionRequired: Boolean = false,
    val deviceHasLowBitAmbient: Boolean = false,
    val ambientUpdateCount: Int = 0,
)
