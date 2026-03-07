package com.fitnessrepcounter.wear.domain.model

data class MotionSample(
    val timestampMs: Long,
    val accelX: Float,
    val accelY: Float,
    val accelZ: Float,
    val gyroX: Float,
    val gyroY: Float,
    val gyroZ: Float,
)
