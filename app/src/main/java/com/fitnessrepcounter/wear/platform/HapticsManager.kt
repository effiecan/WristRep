package com.fitnessrepcounter.wear.platform

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticsManager(
    private val vibrate: (durationMs: Long, amplitude: Int) -> Unit = { _, _ -> },
) {
    fun performCountdownTick() {
        vibrate(35L, 100)
    }

    fun performRepConfirmed() {
        vibrate(50L, 180)
    }

    fun performSetFinished() {
        vibrate(80L, 200)
    }

    fun performPaywallTap() {
        vibrate(120L, 220)
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

            return HapticsManager { durationMs, amplitude ->
                if (vibrator.hasVibrator()) {
                    val effect = VibrationEffect.createOneShot(durationMs, amplitude)
                    vibrator.vibrate(effect)
                }
            }
        }
    }
}
