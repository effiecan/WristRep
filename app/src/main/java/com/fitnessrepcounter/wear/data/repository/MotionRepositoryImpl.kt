package com.fitnessrepcounter.wear.data.repository

import com.fitnessrepcounter.wear.data.sensor.MotionSensorManager
import com.fitnessrepcounter.wear.domain.model.MotionSample
import com.fitnessrepcounter.wear.domain.repository.MotionRepository
import kotlinx.coroutines.flow.Flow

class MotionRepositoryImpl(
    private val motionSensorManager: MotionSensorManager,
) : MotionRepository {
    override val motionSamples: Flow<MotionSample> = motionSensorManager.motionSamples

    override fun startTracking() {
        motionSensorManager.startTracking()
    }

    override fun stopTracking() {
        motionSensorManager.stopTracking()
    }
}
