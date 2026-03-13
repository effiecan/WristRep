package com.fitnessrepcounter.wear.presentation.resources

import com.google.common.truth.Truth.assertThat
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Test

class StringResourceCompletenessTest {
    @Test
    fun allLocaleFiles_includeNewExerciseKeys() {
        val resourceRoot = sequenceOf(
            File("app/src/main/res"),
            File("src/main/res"),
        ).firstOrNull { it.exists() }
            ?: error("Could not locate src/main/res")

        val expectedKeys = setOf(
            "exercise_chest_press",
            "exercise_lat_pulldown",
            "exercise_pec_fly",
            "exercise_rear_delt",
            "exercise_high_pulley",
            "exercise_low_pulley",
            "exercise_delts_machine",
            "exercise_standing_multi_fly_pec_fly",
            "exercise_standing_multi_fly_rear_delt",
            "exercise_standing_multi_fly_lateral_raise",
            "exercise_standing_multi_fly_front_raise",
            "exercise_description_chest_press",
            "exercise_description_lat_pulldown",
            "exercise_description_pec_fly",
            "exercise_description_rear_delt",
            "exercise_description_high_pulley",
            "exercise_description_low_pulley",
            "exercise_description_delts_machine",
            "exercise_description_standing_multi_fly_pec_fly",
            "exercise_description_standing_multi_fly_rear_delt",
            "exercise_description_standing_multi_fly_lateral_raise",
            "exercise_description_standing_multi_fly_front_raise",
        )

        val localeFiles = resourceRoot.listFiles()
            .orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values") }
            .map { File(it, "strings.xml") }
            .filter { it.exists() }

        assertThat(localeFiles).hasSize(39)

        localeFiles.forEach { file ->
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
            val names = buildSet {
                val nodes = document.getElementsByTagName("string")
                for (index in 0 until nodes.length) {
                    val item = nodes.item(index)
                    val name = item.attributes?.getNamedItem("name")?.nodeValue
                    if (name != null) add(name)
                }
            }

            assertThat(names).containsAtLeastElementsIn(expectedKeys)
        }
    }
}
