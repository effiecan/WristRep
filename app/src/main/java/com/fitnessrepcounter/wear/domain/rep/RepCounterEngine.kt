package com.fitnessrepcounter.wear.domain.rep

import com.fitnessrepcounter.wear.domain.model.MotionSample
import com.fitnessrepcounter.wear.domain.model.RepDetectionState
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class RepCounterUpdate(
    val state: RepDetectionState,
    val repDelta: Int,
    val confidence: Float,
    val smoothedSignal: Float,
)

class RepCounterEngine(
    private val profile: ExerciseMotionProfile,
) {
    private val gravity = FloatArray(3)
    private var gravityInitialized = false
    private var smoothedProjectedSignal = 0f
    private var smoothedGyroMagnitude = 0f
    private var state = RepDetectionState.IDLE
    private var phaseStartedAtMs = 0L
    private var repStartedAtMs = 0L
    private var lastRepConfirmedAtMs: Long? = null
    private var lastMeaningfulMotionAtMs = 0L
    private var upPeakProjected = 0f
    private var downPeakProjected = 0f
    private var peakGyroMagnitude = 0f
    private var peakMotionScore = 0f

    fun reset() {
        gravity.fill(0f)
        gravityInitialized = false
        smoothedProjectedSignal = 0f
        smoothedGyroMagnitude = 0f
        state = RepDetectionState.IDLE
        phaseStartedAtMs = 0L
        repStartedAtMs = 0L
        lastRepConfirmedAtMs = null
        lastMeaningfulMotionAtMs = 0L
        resetPhaseTracking()
    }

    fun process(sample: MotionSample): RepCounterUpdate {
        val projectedSignal = projectAgainstGravity(sample)
        smoothedProjectedSignal = smooth(previous = smoothedProjectedSignal, current = projectedSignal, alpha = 0.25f)
        val gyroMagnitude = vectorMagnitude(sample.gyroX, sample.gyroY, sample.gyroZ)
        smoothedGyroMagnitude = smooth(previous = smoothedGyroMagnitude, current = gyroMagnitude, alpha = 0.30f)
        val motionScore = currentMotionScore()
        val now = sample.timestampMs

        val significantMotion =
            abs(smoothedProjectedSignal) > profile.baselineWindow ||
                smoothedGyroMagnitude > profile.gyroGate * 0.45f ||
                motionScore > motionScoreEntryThreshold() * 0.5f

        if (significantMotion) {
            lastMeaningfulMotionAtMs = now
            if (state == RepDetectionState.PAUSED) {
                state = RepDetectionState.IDLE
            }
        } else if (
            lastMeaningfulMotionAtMs != 0L &&
            now - lastMeaningfulMotionAtMs >= profile.pauseAfterMs &&
            abs(smoothedProjectedSignal) <= profile.baselineWindow &&
            smoothedGyroMagnitude <= profile.gyroGate * 0.5f
        ) {
            state = RepDetectionState.PAUSED
        }

        updatePeaks()

        var emittedState = state
        var repDelta = 0
        var confidence = calculatePhaseConfidence(repDuration = now - repStartedAtMs)

        when (state) {
            RepDetectionState.IDLE -> {
                val outOfRefractory = lastRepConfirmedAtMs?.let { now - it >= profile.refractoryMs } ?: true
                if (
                    outOfRefractory &&
                    smoothedProjectedSignal >= profile.upThreshold &&
                    smoothedGyroMagnitude >= profile.gyroGate * 0.6f &&
                    motionScore >= motionScoreEntryThreshold()
                ) {
                    state = RepDetectionState.MOVING_UP
                    emittedState = state
                    repStartedAtMs = now
                    phaseStartedAtMs = now
                    resetPhaseTracking()
                    updatePeaks()
                }
            }

            RepDetectionState.MOVING_UP -> {
                if (now - repStartedAtMs > profile.maxRepMs) {
                    state = RepDetectionState.IDLE
                    emittedState = state
                    resetPhaseTracking()
                } else if (
                    now - phaseStartedAtMs >= profile.minPhaseMs &&
                    smoothedProjectedSignal <= profile.downThreshold
                ) {
                    state = RepDetectionState.MOVING_DOWN
                    emittedState = state
                    phaseStartedAtMs = now
                }
            }

            RepDetectionState.MOVING_DOWN -> {
                val repDuration = now - repStartedAtMs
                confidence = calculatePhaseConfidence(repDuration)
                if (repDuration > profile.maxRepMs) {
                    state = RepDetectionState.IDLE
                    emittedState = state
                    resetPhaseTracking()
                } else if (
                    now - phaseStartedAtMs >= profile.minPhaseMs &&
                    repDuration in profile.minRepMs..profile.maxRepMs &&
                    amplitudeDelta() >= profile.minAmplitudeDelta &&
                    hasReturnedToBaseline(projectedSignal) &&
                    confidence >= profile.repConfidenceFloor
                ) {
                    repDelta = 1
                    emittedState = RepDetectionState.REP_CONFIRMED
                    lastRepConfirmedAtMs = now
                    state = RepDetectionState.IDLE
                    resetPhaseTracking()
                } else if (
                    hasReturnedToBaseline(projectedSignal) &&
                    repDuration > profile.minRepMs &&
                    confidence < profile.repConfidenceFloor
                ) {
                    state = RepDetectionState.IDLE
                    emittedState = state
                    resetPhaseTracking()
                }
            }

            RepDetectionState.REP_CONFIRMED -> {
                state = RepDetectionState.IDLE
                emittedState = state
            }

            RepDetectionState.PAUSED -> {
                emittedState = RepDetectionState.PAUSED
            }
        }

        return RepCounterUpdate(
            state = emittedState,
            repDelta = repDelta,
            confidence = confidence.coerceIn(0f, 1f),
            smoothedSignal = smoothedProjectedSignal,
        )
    }

    private fun updatePeaks() {
        if (state == RepDetectionState.MOVING_UP || state == RepDetectionState.MOVING_DOWN) {
            upPeakProjected = max(upPeakProjected, smoothedProjectedSignal)
            downPeakProjected = min(downPeakProjected, smoothedProjectedSignal)
            peakGyroMagnitude = max(peakGyroMagnitude, smoothedGyroMagnitude)
            peakMotionScore = max(peakMotionScore, currentMotionScore())
        }
    }

    private fun calculatePhaseConfidence(repDuration: Long): Float {
        val amplitudeQuality = (amplitudeDelta() / profile.minAmplitudeDelta)
            .coerceIn(0f, 1.1f)
            .let { min(it, 1f) }
        val idealRepMs = (profile.minRepMs + profile.maxRepMs) / 2f
        val tolerance = ((profile.maxRepMs - profile.minRepMs) / 2f).coerceAtLeast(1f)
        val timingQuality = (1f - abs(repDuration - idealRepMs) / tolerance).coerceIn(0f, 1f)
        val accelSupport = (abs(upPeakProjected) / profile.upThreshold).coerceIn(0f, 1f)
        val gyroSupport = (peakGyroMagnitude / profile.gyroGate.coerceAtLeast(0.1f)).coerceIn(0f, 1f)
        val sensorAgreement = (
            profile.projectedAccelWeight * accelSupport +
                profile.gyroWeight * gyroSupport
            ).coerceIn(0f, 1f)

        return amplitudeQuality * 0.45f + timingQuality * 0.25f + sensorAgreement * 0.30f
    }

    private fun amplitudeDelta(): Float {
        if (upPeakProjected <= 0f || downPeakProjected >= 0f) return 0f
        return upPeakProjected - downPeakProjected
    }

    private fun currentMotionScore(): Float {
        return profile.projectedAccelWeight * abs(smoothedProjectedSignal) +
            profile.gyroWeight * smoothedGyroMagnitude
    }

    private fun motionScoreEntryThreshold(): Float {
        return profile.projectedAccelWeight * profile.upThreshold +
            profile.gyroWeight * profile.gyroGate
    }

    private fun hasReturnedToBaseline(projectedSignal: Float): Boolean {
        return abs(smoothedProjectedSignal) <= profile.baselineWindow ||
            (abs(projectedSignal) <= profile.baselineWindow * 2f && smoothedGyroMagnitude <= profile.gyroGate * 0.75f)
    }

    private fun resetPhaseTracking() {
        upPeakProjected = 0f
        downPeakProjected = 0f
        peakGyroMagnitude = 0f
        peakMotionScore = 0f
    }

    private fun projectAgainstGravity(sample: MotionSample): Float {
        val accel = floatArrayOf(sample.accelX, sample.accelY, sample.accelZ)
        if (!gravityInitialized) {
            accel.copyInto(gravity)
            gravityInitialized = true
        } else {
            for (i in gravity.indices) {
                gravity[i] = gravity[i] * 0.9f + accel[i] * 0.1f
            }
        }

        val gravityMagnitude = vectorMagnitude(gravity[0], gravity[1], gravity[2]).coerceAtLeast(0.0001f)
        val gravityUnitX = gravity[0] / gravityMagnitude
        val gravityUnitY = gravity[1] / gravityMagnitude
        val gravityUnitZ = gravity[2] / gravityMagnitude

        val linearX = accel[0] - gravity[0]
        val linearY = accel[1] - gravity[1]
        val linearZ = accel[2] - gravity[2]

        return -(linearX * gravityUnitX + linearY * gravityUnitY + linearZ * gravityUnitZ)
    }

    private fun smooth(previous: Float, current: Float, alpha: Float): Float {
        return previous + alpha * (current - previous)
    }

    private fun vectorMagnitude(x: Float, y: Float, z: Float): Float {
        return sqrt(x * x + y * y + z * z)
    }
}
