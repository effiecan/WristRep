package com.fitnessrepcounter.wear.domain.rep

import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.ExerciseSupportLevel
import com.fitnessrepcounter.wear.domain.model.isVisibleInList
import com.fitnessrepcounter.wear.domain.model.isSelectable
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExerciseMotionProfileTest {
    @Test
    fun supportLevels_matchProductIntent() {
        assertThat(Exercise.BICEPS_CURL.supportLevel).isEqualTo(ExerciseSupportLevel.OPTIMIZED)
        assertThat(Exercise.HAMMER_CURL.supportLevel).isEqualTo(ExerciseSupportLevel.OPTIMIZED)
        assertThat(Exercise.LATERAL_RAISE.supportLevel).isEqualTo(ExerciseSupportLevel.OPTIMIZED)
        assertThat(Exercise.FRONT_RAISE.supportLevel).isEqualTo(ExerciseSupportLevel.OPTIMIZED)
        assertThat(Exercise.SHOULDER_PRESS.supportLevel).isEqualTo(ExerciseSupportLevel.EXPERIMENTAL)
        assertThat(Exercise.TRICEPS_EXTENSION.supportLevel).isEqualTo(ExerciseSupportLevel.EXPERIMENTAL)
        assertThat(Exercise.DUMBBELL_ROW.supportLevel).isEqualTo(ExerciseSupportLevel.INTERNAL)
        assertThat(Exercise.DUMBBELL_CHEST_PRESS.supportLevel).isEqualTo(ExerciseSupportLevel.INTERNAL)
        assertThat(Exercise.DUMBBELL_ROW.isSelectable).isFalse()

        newExercises().forEach { exercise ->
            assertThat(exercise.supportLevel).isEqualTo(ExerciseSupportLevel.EXPERIMENTAL)
            assertThat(exercise.isSelectable).isTrue()
            assertThat(exercise.isVisibleInList).isTrue()
        }
    }

    @Test
    fun profiles_are_explicit_for_activeExercises() {
        val biceps = Exercise.BICEPS_CURL.motionProfile()
        val hammer = Exercise.HAMMER_CURL.motionProfile()
        val lateral = Exercise.LATERAL_RAISE.motionProfile()
        val front = Exercise.FRONT_RAISE.motionProfile()
        val shoulderPress = Exercise.SHOULDER_PRESS.motionProfile()
        val triceps = Exercise.TRICEPS_EXTENSION.motionProfile()

        assertThat(hammer.gyroGate).isLessThan(biceps.gyroGate)
        assertThat(lateral.minAmplitudeDelta).isGreaterThan(biceps.minAmplitudeDelta)
        assertThat(front.minRepMs).isAtLeast(lateral.minRepMs - 50L)
        assertThat(shoulderPress.repConfidenceFloor).isLessThan(biceps.repConfidenceFloor)
        assertThat(triceps.maxRepMs).isGreaterThan(shoulderPress.maxRepMs)
    }

    @Test
    fun newMachineProfiles_reuseSafeMotionFamilies() {
        val dumbbellChestPress = Exercise.DUMBBELL_CHEST_PRESS.motionProfile()
        val dumbbellRow = Exercise.DUMBBELL_ROW.motionProfile()
        val chestPress = Exercise.CHEST_PRESS.motionProfile()
        val latPulldown = Exercise.LAT_PULLDOWN.motionProfile()
        val highPulley = Exercise.HIGH_PULLEY.motionProfile()
        val lowPulley = Exercise.LOW_PULLEY.motionProfile()
        val pecFly = Exercise.PEC_FLY.motionProfile()
        val rearDelt = Exercise.REAR_DELT.motionProfile()

        assertThat(chestPress.repConfidenceFloor).isAtLeast(dumbbellChestPress.repConfidenceFloor)
        assertThat(latPulldown.minRepMs).isAtLeast(dumbbellRow.minRepMs)
        assertThat(highPulley.gyroGate).isGreaterThan(lowPulley.gyroGate)
        assertThat(rearDelt.gyroGate).isGreaterThan(pecFly.gyroGate)
        assertThat(Exercise.STANDING_MULTI_FLY_PEC_FLY.motionProfile()).isEqualTo(pecFly)
        assertThat(Exercise.STANDING_MULTI_FLY_REAR_DELT.motionProfile()).isEqualTo(rearDelt)
        assertThat(Exercise.STANDING_MULTI_FLY_LATERAL_RAISE.motionProfile())
            .isEqualTo(Exercise.LATERAL_RAISE.motionProfile())
        assertThat(Exercise.STANDING_MULTI_FLY_FRONT_RAISE.motionProfile())
            .isEqualTo(Exercise.FRONT_RAISE.motionProfile())
    }

    private fun newExercises(): List<Exercise> {
        return listOf(
            Exercise.CHEST_PRESS,
            Exercise.LAT_PULLDOWN,
            Exercise.PEC_FLY,
            Exercise.REAR_DELT,
            Exercise.HIGH_PULLEY,
            Exercise.LOW_PULLEY,
            Exercise.DELTS_MACHINE,
            Exercise.STANDING_MULTI_FLY_PEC_FLY,
            Exercise.STANDING_MULTI_FLY_REAR_DELT,
            Exercise.STANDING_MULTI_FLY_LATERAL_RAISE,
            Exercise.STANDING_MULTI_FLY_FRONT_RAISE,
        )
    }
}
