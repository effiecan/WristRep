package com.fitnessrepcounter.wear.domain.rep

import com.fitnessrepcounter.wear.domain.model.Exercise

data class ExerciseMotionProfile(
    val projectedAccelWeight: Float,
    val gyroWeight: Float,
    val upThreshold: Float,
    val downThreshold: Float,
    val baselineWindow: Float,
    val gyroGate: Float,
    val minPhaseMs: Long,
    val minRepMs: Long,
    val maxRepMs: Long,
    val refractoryMs: Long,
    val pauseAfterMs: Long,
    val minAmplitudeDelta: Float,
    val repConfidenceFloor: Float,
)

fun Exercise.motionProfile(): ExerciseMotionProfile {
    return when (this) {
        Exercise.BICEPS_CURL -> ExerciseMotionProfile(
            projectedAccelWeight = 0.65f,
            gyroWeight = 0.35f,
            upThreshold = 1.20f,
            downThreshold = -0.80f,
            baselineWindow = 0.30f,
            gyroGate = 0.80f,
            minPhaseMs = 250L,
            minRepMs = 700L,
            maxRepMs = 3500L,
            refractoryMs = 900L,
            pauseAfterMs = 2000L,
            minAmplitudeDelta = 1.80f,
            repConfidenceFloor = 0.72f,
        )

        Exercise.HAMMER_CURL -> ExerciseMotionProfile(
            projectedAccelWeight = 0.75f,
            gyroWeight = 0.25f,
            upThreshold = 1.15f,
            downThreshold = -0.75f,
            baselineWindow = 0.30f,
            gyroGate = 0.65f,
            minPhaseMs = 250L,
            minRepMs = 700L,
            maxRepMs = 3500L,
            refractoryMs = 900L,
            pauseAfterMs = 2000L,
            minAmplitudeDelta = 1.70f,
            repConfidenceFloor = 0.68f,
        )

        Exercise.LATERAL_RAISE -> ExerciseMotionProfile(
            projectedAccelWeight = 0.80f,
            gyroWeight = 0.20f,
            upThreshold = 1.45f,
            downThreshold = -1.05f,
            baselineWindow = 0.28f,
            gyroGate = 0.55f,
            minPhaseMs = 320L,
            minRepMs = 950L,
            maxRepMs = 4200L,
            refractoryMs = 1100L,
            pauseAfterMs = 2200L,
            minAmplitudeDelta = 2.20f,
            repConfidenceFloor = 0.74f,
        )

        Exercise.FRONT_RAISE -> ExerciseMotionProfile(
            projectedAccelWeight = 0.78f,
            gyroWeight = 0.22f,
            upThreshold = 1.40f,
            downThreshold = -1.00f,
            baselineWindow = 0.28f,
            gyroGate = 0.50f,
            minPhaseMs = 300L,
            minRepMs = 900L,
            maxRepMs = 4000L,
            refractoryMs = 1050L,
            pauseAfterMs = 2200L,
            minAmplitudeDelta = 2.10f,
            repConfidenceFloor = 0.72f,
        )

        Exercise.SHOULDER_PRESS -> ExerciseMotionProfile(
            projectedAccelWeight = 0.55f,
            gyroWeight = 0.45f,
            upThreshold = 1.60f,
            downThreshold = -1.15f,
            baselineWindow = 0.32f,
            gyroGate = 0.95f,
            minPhaseMs = 320L,
            minRepMs = 900L,
            maxRepMs = 4200L,
            refractoryMs = 1200L,
            pauseAfterMs = 2200L,
            minAmplitudeDelta = 2.40f,
            repConfidenceFloor = 0.60f,
        )

        Exercise.TRICEPS_EXTENSION -> ExerciseMotionProfile(
            projectedAccelWeight = 0.50f,
            gyroWeight = 0.50f,
            upThreshold = 1.25f,
            downThreshold = -0.90f,
            baselineWindow = 0.32f,
            gyroGate = 0.85f,
            minPhaseMs = 300L,
            minRepMs = 800L,
            maxRepMs = 4500L,
            refractoryMs = 1150L,
            pauseAfterMs = 2200L,
            minAmplitudeDelta = 1.90f,
            repConfidenceFloor = 0.58f,
        )

        Exercise.DUMBBELL_ROW -> ExerciseMotionProfile(
            projectedAccelWeight = 0.60f,
            gyroWeight = 0.40f,
            upThreshold = 1.60f,
            downThreshold = -1.10f,
            baselineWindow = 0.34f,
            gyroGate = 0.95f,
            minPhaseMs = 340L,
            minRepMs = 950L,
            maxRepMs = 4500L,
            refractoryMs = 1300L,
            pauseAfterMs = 2200L,
            minAmplitudeDelta = 2.50f,
            repConfidenceFloor = 0.62f,
        )

        Exercise.DUMBBELL_CHEST_PRESS -> ExerciseMotionProfile(
            projectedAccelWeight = 0.58f,
            gyroWeight = 0.42f,
            upThreshold = 1.55f,
            downThreshold = -1.05f,
            baselineWindow = 0.34f,
            gyroGate = 0.90f,
            minPhaseMs = 340L,
            minRepMs = 950L,
            maxRepMs = 4500L,
            refractoryMs = 1300L,
            pauseAfterMs = 2200L,
            minAmplitudeDelta = 2.40f,
            repConfidenceFloor = 0.62f,
        )
    }
}
