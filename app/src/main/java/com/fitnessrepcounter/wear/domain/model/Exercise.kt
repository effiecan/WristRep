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
    @StringRes val catalogDescriptionRes: Int,
    val supportLevel: ExerciseSupportLevel,
) {
    BICEPS_CURL(
        R.string.exercise_biceps_curl,
        R.string.label_best_tracking_quality,
        ExerciseSupportLevel.OPTIMIZED,
    ),
    HAMMER_CURL(
        R.string.exercise_hammer_curl,
        R.string.label_best_tracking_quality,
        ExerciseSupportLevel.OPTIMIZED,
    ),
    LATERAL_RAISE(
        R.string.exercise_lateral_raise,
        R.string.label_best_tracking_quality,
        ExerciseSupportLevel.OPTIMIZED,
    ),
    FRONT_RAISE(
        R.string.exercise_front_raise,
        R.string.label_best_tracking_quality,
        ExerciseSupportLevel.OPTIMIZED,
    ),
    SHOULDER_PRESS(
        R.string.exercise_shoulder_press,
        R.string.label_manual_correction_may_help,
        ExerciseSupportLevel.EXPERIMENTAL,
    ),
    TRICEPS_EXTENSION(
        R.string.exercise_triceps_extension,
        R.string.label_manual_correction_may_help,
        ExerciseSupportLevel.EXPERIMENTAL,
    ),
    DUMBBELL_ROW(
        R.string.exercise_dumbbell_row,
        R.string.label_tuning_in_progress,
        ExerciseSupportLevel.INTERNAL,
    ),
    DUMBBELL_CHEST_PRESS(
        R.string.exercise_dumbbell_chest_press,
        R.string.label_tuning_in_progress,
        ExerciseSupportLevel.INTERNAL,
    ),
    CHEST_PRESS(
        R.string.exercise_chest_press,
        R.string.exercise_description_chest_press,
        ExerciseSupportLevel.EXPERIMENTAL,
    ),
    LAT_PULLDOWN(
        R.string.exercise_lat_pulldown,
        R.string.exercise_description_lat_pulldown,
        ExerciseSupportLevel.EXPERIMENTAL,
    ),
    PEC_FLY(
        R.string.exercise_pec_fly,
        R.string.exercise_description_pec_fly,
        ExerciseSupportLevel.EXPERIMENTAL,
    ),
    REAR_DELT(
        R.string.exercise_rear_delt,
        R.string.exercise_description_rear_delt,
        ExerciseSupportLevel.EXPERIMENTAL,
    ),
    HIGH_PULLEY(
        R.string.exercise_high_pulley,
        R.string.exercise_description_high_pulley,
        ExerciseSupportLevel.EXPERIMENTAL,
    ),
    LOW_PULLEY(
        R.string.exercise_low_pulley,
        R.string.exercise_description_low_pulley,
        ExerciseSupportLevel.EXPERIMENTAL,
    ),
    DELTS_MACHINE(
        R.string.exercise_delts_machine,
        R.string.exercise_description_delts_machine,
        ExerciseSupportLevel.EXPERIMENTAL,
    ),
    STANDING_MULTI_FLY_PEC_FLY(
        R.string.exercise_standing_multi_fly_pec_fly,
        R.string.exercise_description_standing_multi_fly_pec_fly,
        ExerciseSupportLevel.EXPERIMENTAL,
    ),
    STANDING_MULTI_FLY_REAR_DELT(
        R.string.exercise_standing_multi_fly_rear_delt,
        R.string.exercise_description_standing_multi_fly_rear_delt,
        ExerciseSupportLevel.EXPERIMENTAL,
    ),
    STANDING_MULTI_FLY_LATERAL_RAISE(
        R.string.exercise_standing_multi_fly_lateral_raise,
        R.string.exercise_description_standing_multi_fly_lateral_raise,
        ExerciseSupportLevel.EXPERIMENTAL,
    ),
    STANDING_MULTI_FLY_FRONT_RAISE(
        R.string.exercise_standing_multi_fly_front_raise,
        R.string.exercise_description_standing_multi_fly_front_raise,
        ExerciseSupportLevel.EXPERIMENTAL,
    ),
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
