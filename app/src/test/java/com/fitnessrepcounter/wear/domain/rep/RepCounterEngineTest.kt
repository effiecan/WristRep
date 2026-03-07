package com.fitnessrepcounter.wear.domain.rep

import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.MotionSample
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RepCounterEngineTest {
    @Test
    fun validBicepsCurlSequence_confirmsSingleRep() {
        val engine = RepCounterEngine(Exercise.BICEPS_CURL.motionProfile())

        val repCount = validRepSequence(startAtMs = 0L)
            .sumOf { sample -> engine.process(sample).repDelta }

        assertThat(repCount).isEqualTo(1)
    }

    @Test
    fun rapidSequence_isRejected() {
        val engine = RepCounterEngine(Exercise.BICEPS_CURL.motionProfile())

        val samples = listOf(
            sample(0L, 9.81f, 0f),
            sample(100L, 4.0f, 3.0f),
            sample(200L, 4.0f, 3.0f),
            sample(300L, 15.0f, 3.0f),
            sample(400L, 15.0f, 3.0f),
            sample(500L, 9.81f, 0.2f),
        )

        val repCount = samples.sumOf { sample -> engine.process(sample).repDelta }

        assertThat(repCount).isEqualTo(0)
    }

    @Test
    fun tinyMotion_isRejected() {
        val engine = RepCounterEngine(Exercise.BICEPS_CURL.motionProfile())

        val samples = listOf(
            sample(0L, 9.81f, 0f),
            sample(300L, 9.2f, 0.2f),
            sample(600L, 9.0f, 0.2f),
            sample(900L, 10.2f, 0.2f),
            sample(1200L, 9.81f, 0f),
        )

        val repCount = samples.sumOf { sample -> engine.process(sample).repDelta }

        assertThat(repCount).isEqualTo(0)
    }

    @Test
    fun refractoryWindow_blocksImmediateSecondRep() {
        val engine = RepCounterEngine(Exercise.BICEPS_CURL.motionProfile())

        val firstRep = validRepSequence(startAtMs = 0L)
        val secondRepTooSoon = validRepSequence(startAtMs = 2_100L)
        val repCount = (firstRep + secondRepTooSoon).sumOf { sample -> engine.process(sample).repDelta }

        assertThat(repCount).isEqualTo(1)
    }

    private fun validRepSequence(startAtMs: Long): List<MotionSample> {
        return listOf(
            sample(startAtMs, 9.81f, 0f),
            sample(startAtMs + 300L, 4.0f, 3.0f),
            sample(startAtMs + 600L, 4.0f, 3.0f),
            sample(startAtMs + 900L, 15.0f, 3.0f),
            sample(startAtMs + 1_200L, 15.0f, 3.0f),
            sample(startAtMs + 1_500L, 9.81f, 0.2f),
            sample(startAtMs + 1_800L, 9.81f, 0.2f),
            sample(startAtMs + 2_100L, 9.81f, 0.2f),
            sample(startAtMs + 2_400L, 9.81f, 0.2f),
            sample(startAtMs + 2_700L, 9.81f, 0.2f),
        )
    }

    private fun sample(timestampMs: Long, accelZ: Float, gyroMagnitude: Float): MotionSample {
        return MotionSample(
            timestampMs = timestampMs,
            accelX = 0f,
            accelY = 0f,
            accelZ = accelZ,
            gyroX = gyroMagnitude,
            gyroY = 0f,
            gyroZ = 0f,
        )
    }
}
