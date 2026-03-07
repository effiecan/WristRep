package com.fitnessrepcounter.wear.domain.rep

import com.fitnessrepcounter.wear.domain.model.Exercise
import com.fitnessrepcounter.wear.domain.model.ExerciseSupportLevel
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
}
