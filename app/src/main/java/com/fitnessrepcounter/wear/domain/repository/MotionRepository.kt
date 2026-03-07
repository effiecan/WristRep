package com.fitnessrepcounter.wear.domain.repository

import com.fitnessrepcounter.wear.domain.model.MotionSample
import kotlinx.coroutines.flow.Flow

interface MotionRepository {
    val motionSamples: Flow<MotionSample>
    fun startTracking()
    fun stopTracking()
}
