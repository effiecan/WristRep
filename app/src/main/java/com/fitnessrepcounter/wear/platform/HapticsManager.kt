package com.fitnessrepcounter.wear.platform

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.VibrationAttributes
import android.os.Vibrator
import android.os.VibratorManager

class HapticsManager private constructor(
    private val backend: HapticBackend = NoOpHapticBackend,
) {
    constructor() : this(backend = NoOpHapticBackend)

    constructor(
        vibrate: (durationMs: Long, amplitude: Int) -> Unit,
    ) : this(
        backend = object : HapticBackend {
            override fun vibrate(durationMs: Long, amplitude: Int) {
                vibrate(durationMs, amplitude)
            }
        },
    )

    fun performCountdownTick() {
        backend.vibrate(35L, 100)
    }

    fun performRepConfirmed() {
        backend.vibrate(50L, 180)
    }

    fun performSetFinished() {
        backend.vibrate(80L, 200)
    }

    fun performPaywallTap() {
        backend.vibrate(120L, 220)
    }

    companion object {
        fun fromContext(context: Context): HapticsManager {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            return HapticsManager(createBackend(vibrator, Build.VERSION.SDK_INT))
        }

        internal fun backendTypeForSdk(sdkInt: Int): HapticBackendType {
            return if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
                HapticBackendType.VIBRATION_ATTRIBUTES
            } else {
                HapticBackendType.AUDIO_ATTRIBUTES
            }
        }

        private fun createBackend(
            vibrator: Vibrator,
            sdkInt: Int,
        ): HapticBackend {
            return when (backendTypeForSdk(sdkInt)) {
                HapticBackendType.VIBRATION_ATTRIBUTES -> VibrationAttributesHapticBackend(vibrator)
                HapticBackendType.AUDIO_ATTRIBUTES -> AudioAttributesHapticBackend(vibrator)
            }
        }
    }
}

internal enum class HapticBackendType {
    VIBRATION_ATTRIBUTES,
    AUDIO_ATTRIBUTES,
}

private interface HapticBackend {
    fun vibrate(durationMs: Long, amplitude: Int)
}

private data object NoOpHapticBackend : HapticBackend {
    override fun vibrate(durationMs: Long, amplitude: Int) = Unit
}

private class VibrationAttributesHapticBackend(
    private val vibrator: Vibrator,
) : HapticBackend {
    private val attributes = VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH)

    override fun vibrate(durationMs: Long, amplitude: Int) {
        if (!vibrator.hasVibrator()) return
        val effect = VibrationEffect.createOneShot(durationMs, amplitude)
        vibrator.vibrate(effect, attributes)
    }
}

private class AudioAttributesHapticBackend(
    private val vibrator: Vibrator,
) : HapticBackend {
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    override fun vibrate(durationMs: Long, amplitude: Int) {
        if (!vibrator.hasVibrator()) return
        val effect = VibrationEffect.createOneShot(durationMs, amplitude)
        vibrator.vibrate(effect, attributes)
    }
}
