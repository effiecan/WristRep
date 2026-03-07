package com.fitnessrepcounter.wear.domain.model

import androidx.annotation.StringRes
import com.fitnessrepcounter.wear.R

enum class ExerciseSupportLevel(
    @StringRes val statusLabelRes: Int,
    @StringRes val supportingLabelRes: Int,
) {
    OPTIMIZED(
        statusLabelRes = R.string.label_optimized,
        supportingLabelRes = R.string.label_best_tracking_quality,
    ),
    EXPERIMENTAL(
        statusLabelRes = R.string.label_experimental,
        supportingLabelRes = R.string.label_manual_correction_may_help,
    ),
    INTERNAL(
        statusLabelRes = R.string.label_soon,
        supportingLabelRes = R.string.label_tuning_in_progress,
    ),
}

enum class Exercise(
    @StringRes val displayNameRes: Int,
    val supportLevel: ExerciseSupportLevel,
) {
    BICEPS_CURL(R.string.exercise_biceps_curl, ExerciseSupportLevel.OPTIMIZED),
    HAMMER_CURL(R.string.exercise_hammer_curl, ExerciseSupportLevel.OPTIMIZED),
    LATERAL_RAISE(R.string.exercise_lateral_raise, ExerciseSupportLevel.OPTIMIZED),
    FRONT_RAISE(R.string.exercise_front_raise, ExerciseSupportLevel.OPTIMIZED),
    SHOULDER_PRESS(R.string.exercise_shoulder_press, ExerciseSupportLevel.EXPERIMENTAL),
    TRICEPS_EXTENSION(R.string.exercise_triceps_extension, ExerciseSupportLevel.EXPERIMENTAL),
    DUMBBELL_ROW(R.string.exercise_dumbbell_row, ExerciseSupportLevel.INTERNAL),
    DUMBBELL_CHEST_PRESS(R.string.exercise_dumbbell_chest_press, ExerciseSupportLevel.INTERNAL),
}

val Exercise.isSelectable: Boolean
    get() = supportLevel != ExerciseSupportLevel.INTERNAL

val Exercise.isVisibleInList: Boolean
    get() = supportLevel != ExerciseSupportLevel.INTERNAL

val Exercise.isOptimized: Boolean
    get() = supportLevel == ExerciseSupportLevel.OPTIMIZED

val Exercise.isExperimental: Boolean
    get() = supportLevel == ExerciseSupportLevel.EXPERIMENTAL

val Exercise.statusLabelRes: Int
    get() = supportLevel.statusLabelRes

val Exercise.supportingLabelRes: Int
    get() = supportLevel.supportingLabelRes
