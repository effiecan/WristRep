package com.fitnessrepcounter.wear.presentation.resources

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessrepcounter.wear.R
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StringResourcesLocalizationTest {
    @Test
    fun turkishStrings_includeExpectedTranslationsAndPlaceholders() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val configuration = Configuration(baseContext.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag("tr"))
        val turkishContext = baseContext.createConfigurationContext(configuration)

        assertEquals("2 ücretsiz hak kaldı", turkishContext.getString(R.string.free_workouts_left, 2))
        assertEquals("Ayarlar", turkishContext.getString(R.string.settings))
        assertEquals("Pro aktif", turkishContext.getString(R.string.pro_unlocked))
        assertEquals("Premium", turkishContext.getString(R.string.manage_premium))
        assertEquals("Antrenmana dön", turkishContext.getString(R.string.resume_workout))
        assertEquals("Triceps Extension", turkishContext.getString(R.string.exercise_triceps_extension))
    }
}
