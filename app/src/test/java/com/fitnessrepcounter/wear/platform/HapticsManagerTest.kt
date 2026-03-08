package com.fitnessrepcounter.wear.platform

import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HapticsManagerTest {
    @Test
    fun backendTypeForSdk_usesVibrationAttributesOnApi33AndAbove() {
        assertThat(HapticsManager.backendTypeForSdk(Build.VERSION_CODES.TIRAMISU))
            .isEqualTo(HapticBackendType.VIBRATION_ATTRIBUTES)
        assertThat(HapticsManager.backendTypeForSdk(Build.VERSION_CODES.UPSIDE_DOWN_CAKE))
            .isEqualTo(HapticBackendType.VIBRATION_ATTRIBUTES)
    }

    @Test
    fun backendTypeForSdk_usesAudioAttributesBeforeApi33() {
        assertThat(HapticsManager.backendTypeForSdk(Build.VERSION_CODES.O))
            .isEqualTo(HapticBackendType.AUDIO_ATTRIBUTES)
        assertThat(HapticsManager.backendTypeForSdk(Build.VERSION_CODES.S_V2))
            .isEqualTo(HapticBackendType.AUDIO_ATTRIBUTES)
    }
}
