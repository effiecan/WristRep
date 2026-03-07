package com.fitnessrepcounter.wear.domain.model

enum class ExerciseSupportLevel(
    val statusLabel: String,
    val supportingLabel: String,
) {
    OPTIMIZED(
        statusLabel = "Optimized",
        supportingLabel = "Best tracking quality",
    ),
    EXPERIMENTAL(
        statusLabel = "Experimental",
        supportingLabel = "Manual correction may help",
    ),
    INTERNAL(
        statusLabel = "Soon",
        supportingLabel = "Tuning in progress",
    ),
}

enum class Exercise(
    val displayName: String,
    val supportLevel: ExerciseSupportLevel,
) {
    BICEPS_CURL("Biceps Curl", ExerciseSupportLevel.OPTIMIZED),
    HAMMER_CURL("Hammer Curl", ExerciseSupportLevel.OPTIMIZED),
    LATERAL_RAISE("Lateral Raise", ExerciseSupportLevel.OPTIMIZED),
    FRONT_RAISE("Front Raise", ExerciseSupportLevel.OPTIMIZED),
    SHOULDER_PRESS("Shoulder Press", ExerciseSupportLevel.EXPERIMENTAL),
    TRICEPS_EXTENSION("Triceps Extension", ExerciseSupportLevel.EXPERIMENTAL),
    DUMBBELL_ROW("Dumbbell Row", ExerciseSupportLevel.INTERNAL),
    DUMBBELL_CHEST_PRESS("Dumbbell Chest Press", ExerciseSupportLevel.INTERNAL),
}

val Exercise.isSelectable: Boolean
    get() = supportLevel != ExerciseSupportLevel.INTERNAL

val Exercise.isVisibleInList: Boolean
    get() = supportLevel != ExerciseSupportLevel.INTERNAL

val Exercise.isOptimized: Boolean
    get() = supportLevel == ExerciseSupportLevel.OPTIMIZED

val Exercise.isExperimental: Boolean
    get() = supportLevel == ExerciseSupportLevel.EXPERIMENTAL

val Exercise.statusLabel: String
    get() = supportLevel.statusLabel

val Exercise.supportingLabel: String
    get() = supportLevel.supportingLabel
