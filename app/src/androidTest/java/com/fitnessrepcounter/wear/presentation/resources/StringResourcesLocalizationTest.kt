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
    fun turkishStrings_includeExpectedTranslationsAndNewExerciseKeys() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val configuration = Configuration(baseContext.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag("tr"))
        val turkishContext = baseContext.createConfigurationContext(configuration)

        assertEquals("Ayarlar", turkishContext.getString(R.string.settings))
        assertEquals("Pro aktif", turkishContext.getString(R.string.pro_unlocked))
        assertEquals("Premium", turkishContext.getString(R.string.manage_premium))
        assertEquals("Triceps Extension", turkishContext.getString(R.string.exercise_triceps_extension))
        assertEquals("Chest Press", turkishContext.getString(R.string.exercise_chest_press))
        assertEquals(
            "Machine chest press, controlled tempo helps",
            turkishContext.getString(R.string.exercise_description_chest_press),
        )
    }
}
