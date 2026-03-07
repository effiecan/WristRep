package com.fitnessrepcounter.wear.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.fitnessrepcounter.wear.domain.model.MotionSample
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MotionSensorManager(
    context: Context,
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val _motionSamples = MutableSharedFlow<MotionSample>(
        extraBufferCapacity = 64,
    )
    val motionSamples: Flow<MotionSample> = _motionSamples.asSharedFlow()

    private var latestAccel: FloatArray? = null
    private var latestGyro: FloatArray? = null
    private var isTracking = false

    fun startTracking() {
        if (isTracking) return
        isTracking = true
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
        gyroscope?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopTracking() {
        if (!isTracking) return
        isTracking = false
        sensorManager.unregisterListener(this)
        latestAccel = null
        latestGyro = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!isTracking) return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> latestAccel = event.values.copyOf()
            Sensor.TYPE_GYROSCOPE -> latestGyro = event.values.copyOf()
        }

        val accel = latestAccel ?: return
        val gyro = latestGyro ?: return
        _motionSamples.tryEmit(
            MotionSample(
                timestampMs = event.timestamp / 1_000_000L,
                accelX = accel[0],
                accelY = accel[1],
                accelZ = accel[2],
                gyroX = gyro[0],
                gyroY = gyro[1],
                gyroZ = gyro[2],
            ),
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
