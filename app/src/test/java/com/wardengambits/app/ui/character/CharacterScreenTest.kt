package com.wardengambits.app.ui.character

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.model.Stats
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.wardengambits.engine.story.RunResult
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es-w411dp-h1600dp")
class CharacterScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ui = CharacterUi(
        level = 79, lap = 3, wins = 7, deaths = 2, zoneName = "Bosque de Chet", progress = "3/7",
        stats = Stats(maxHp = 166_000, maxPower = 300, powerRegen = 5, attack = 16_600, heal = 15_400, defense = 8_000),
        history = listOf(HistoryRowUi(2, RunResult.COMPLETED, "Monte del Destino", 72, 35, "16/10/2026 10:00", deaths = 5, completed = true)),
        completedLaps = 2,
    )

    private fun show(onHistory: () -> Unit = {}) =
        composeRule.setContent { WardenTheme { CharacterScreen(ui = ui, onBack = {}, onHistory = onHistory) } }

    @Test fun sheetShowsNameTitleZoneAndLevelSeal() {
        show()
        composeRule.onNodeWithText("Warden").assertIsDisplayed()
        composeRule.onNodeWithText("Guardián de Bree").assertIsDisplayed()
        composeRule.onNodeWithText("Bosque de Chet · 3/7").assertIsDisplayed()
        composeRule.onNode(hasText("NIVEL") and hasText("79")).assertIsDisplayed()
    }

    @Test fun sheetShowsEveryAttributeWithItsValue() {
        show()
        composeRule.onNodeWithText("Atributos").assertIsDisplayed()
        listOf(
            "Vida" to "166 000",
            "Power" to "300",
            "Regeneración" to "5",
            "Ataque" to "16 600",
            "Cura" to "15 400",
            "Defensa" to "8 000",
        ).forEach { (label, value) ->
            composeRule.onNode(hasText(label) and hasText(value)).assertIsDisplayed()
        }
    }

    @Test fun sheetShowsTheJourney() {
        show()
        composeRule.onNodeWithText("El viaje").assertIsDisplayed()
        composeRule.onNodeWithText("Vuelta: 3").assertIsDisplayed()
        composeRule.onNodeWithText("Victorias: 7").assertIsDisplayed()
        composeRule.onNodeWithText("Muertes: 2").assertIsDisplayed()
        composeRule.onNodeWithText("Vueltas completas: 2").assertIsDisplayed()
    }

    @Test fun chronicleButtonOpensTheHistory() {
        var opened = 0
        show(onHistory = { opened++ })
        composeRule.onNode(hasText("Crónica de vueltas") and hasClickAction()).performScrollTo().performClick()
        assertEquals(1, opened)
    }

    @Test fun statValuesGroupThousandsWithANarrowSpace() {
        assertEquals("5", formatStat(5))
        assertEquals("300", formatStat(300))
        assertEquals("10 000", formatStat(10_000))
        assertEquals("1 234 567", formatStat(1_234_567))
    }
}
