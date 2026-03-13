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
        Exercise.BICEPS_CURL -> bicepsCurlProfile()
        Exercise.HAMMER_CURL -> hammerCurlProfile()
        Exercise.LATERAL_RAISE -> lateralRaiseProfile()
        Exercise.FRONT_RAISE -> frontRaiseProfile()
        Exercise.SHOULDER_PRESS -> shoulderPressProfile()
        Exercise.TRICEPS_EXTENSION -> tricepsExtensionProfile()
        Exercise.DUMBBELL_ROW -> dumbbellRowProfile()
        Exercise.DUMBBELL_CHEST_PRESS -> dumbbellChestPressProfile()
        Exercise.CHEST_PRESS -> chestPressProfile()
        Exercise.LAT_PULLDOWN -> latPulldownProfile()
        Exercise.PEC_FLY -> pecFlyProfile()
        Exercise.REAR_DELT -> rearDeltProfile()
        Exercise.HIGH_PULLEY -> highPulleyProfile()
        Exercise.LOW_PULLEY -> lowPulleyProfile()
        Exercise.DELTS_MACHINE -> deltsMachineProfile()
        Exercise.STANDING_MULTI_FLY_PEC_FLY -> pecFlyProfile()
        Exercise.STANDING_MULTI_FLY_REAR_DELT -> rearDeltProfile()
        Exercise.STANDING_MULTI_FLY_LATERAL_RAISE -> lateralRaiseProfile()
        Exercise.STANDING_MULTI_FLY_FRONT_RAISE -> frontRaiseProfile()
    }
}

private fun bicepsCurlProfile() = ExerciseMotionProfile(
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

private fun hammerCurlProfile() = ExerciseMotionProfile(
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

private fun lateralRaiseProfile() = ExerciseMotionProfile(
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

private fun frontRaiseProfile() = ExerciseMotionProfile(
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

private fun shoulderPressProfile() = ExerciseMotionProfile(
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

private fun tricepsExtensionProfile() = ExerciseMotionProfile(
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

private fun dumbbellRowProfile() = ExerciseMotionProfile(
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

private fun dumbbellChestPressProfile() = ExerciseMotionProfile(
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

private fun chestPressProfile() = ExerciseMotionProfile(
    projectedAccelWeight = 0.58f,
    gyroWeight = 0.42f,
    upThreshold = 1.60f,
    downThreshold = -1.10f,
    baselineWindow = 0.34f,
    gyroGate = 0.95f,
    minPhaseMs = 360L,
    minRepMs = 1000L,
    maxRepMs = 4600L,
    refractoryMs = 1350L,
    pauseAfterMs = 2200L,
    minAmplitudeDelta = 2.50f,
    repConfidenceFloor = 0.65f,
)

private fun latPulldownProfile() = ExerciseMotionProfile(
    projectedAccelWeight = 0.60f,
    gyroWeight = 0.40f,
    upThreshold = 1.65f,
    downThreshold = -1.15f,
    baselineWindow = 0.34f,
    gyroGate = 1.00f,
    minPhaseMs = 350L,
    minRepMs = 1000L,
    maxRepMs = 4600L,
    refractoryMs = 1350L,
    pauseAfterMs = 2250L,
    minAmplitudeDelta = 2.60f,
    repConfidenceFloor = 0.65f,
)

private fun pecFlyProfile() = ExerciseMotionProfile(
    projectedAccelWeight = 0.72f,
    gyroWeight = 0.28f,
    upThreshold = 1.35f,
    downThreshold = -0.95f,
    baselineWindow = 0.30f,
    gyroGate = 0.70f,
    minPhaseMs = 330L,
    minRepMs = 1000L,
    maxRepMs = 4300L,
    refractoryMs = 1150L,
    pauseAfterMs = 2200L,
    minAmplitudeDelta = 2.20f,
    repConfidenceFloor = 0.68f,
)

private fun rearDeltProfile() = ExerciseMotionProfile(
    projectedAccelWeight = 0.68f,
    gyroWeight = 0.32f,
    upThreshold = 1.40f,
    downThreshold = -1.00f,
    baselineWindow = 0.30f,
    gyroGate = 0.75f,
    minPhaseMs = 340L,
    minRepMs = 1050L,
    maxRepMs = 4400L,
    refractoryMs = 1200L,
    pauseAfterMs = 2200L,
    minAmplitudeDelta = 2.30f,
    repConfidenceFloor = 0.70f,
)

private fun highPulleyProfile() = ExerciseMotionProfile(
    projectedAccelWeight = 0.58f,
    gyroWeight = 0.42f,
    upThreshold = 1.70f,
    downThreshold = -1.20f,
    baselineWindow = 0.34f,
    gyroGate = 1.05f,
    minPhaseMs = 360L,
    minRepMs = 1050L,
    maxRepMs = 4700L,
    refractoryMs = 1400L,
    pauseAfterMs = 2300L,
    minAmplitudeDelta = 2.70f,
    repConfidenceFloor = 0.67f,
)

private fun lowPulleyProfile() = ExerciseMotionProfile(
    projectedAccelWeight = 0.62f,
    gyroWeight = 0.38f,
    upThreshold = 1.55f,
    downThreshold = -1.05f,
    baselineWindow = 0.34f,
    gyroGate = 0.90f,
    minPhaseMs = 340L,
    minRepMs = 950L,
    maxRepMs = 4500L,
    refractoryMs = 1300L,
    pauseAfterMs = 2200L,
    minAmplitudeDelta = 2.45f,
    repConfidenceFloor = 0.64f,
)

private fun deltsMachineProfile() = ExerciseMotionProfile(
    projectedAccelWeight = 0.76f,
    gyroWeight = 0.24f,
    upThreshold = 1.50f,
    downThreshold = -1.10f,
    baselineWindow = 0.30f,
    gyroGate = 0.65f,
    minPhaseMs = 340L,
    minRepMs = 1000L,
    maxRepMs = 4300L,
    refractoryMs = 1150L,
    pauseAfterMs = 2200L,
    minAmplitudeDelta = 2.30f,
    repConfidenceFloor = 0.76f,
)
