package com.wardengambits.app.ui.character

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.wardengambits.app.i18n.AppLanguage
import com.wardengambits.app.i18n.AppScript
import com.wardengambits.app.i18n.ProvideLanguageAndScript
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.model.Difficulty
import com.wardengambits.engine.story.RunResult
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es-w411dp-h1600dp")
class HistoryScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test fun everyLapIsAnEntryAndOnlyCompletedOnesAreSealed() {
        val history = listOf(
            HistoryRowUi(2, RunResult.COMPLETED, "Monte del Destino", 72, 35, "16/10/2026 10:00", deaths = 5, completed = true),
            HistoryRowUi(1, RunResult.DEFEATED, "Bosque de Chet", 4, 1, "01/01/1970 00:00", deaths = 1),
        )
        composeRule.setContent { WardenTheme { HistoryScreen(history = history, onBack = {}) } }
        composeRule.onNodeWithText("Crónica de vueltas").assertIsDisplayed()
        composeRule.onNodeWithText("Vuelta 2 — Completada").assertIsDisplayed()
        composeRule.onNodeWithText("Monte del Destino · Nivel 72 · 35 victorias · 5 muertes").assertIsDisplayed()
        composeRule.onNodeWithText("16/10/2026 10:00").assertIsDisplayed()
        composeRule.onNodeWithText("Vuelta 1 — Caído").assertIsDisplayed()
        composeRule.onNodeWithText("Bosque de Chet · Nivel 4 · 1 victoria · 1 muerte").assertIsDisplayed()
        composeRule.onNodeWithText("01/01/1970 00:00").assertIsDisplayed()
        assertEquals(1, composeRule.onAllNodesWithContentDescription("Vuelta completada").fetchSemanticsNodes().size)
        composeRule.onNode(hasContentDescription("Vuelta completada")).assertIsDisplayed()
    }

    @Test fun inEnglish() {
        val history = listOf(
            HistoryRowUi(2, RunResult.COMPLETED, "Mount Doom", 72, 35, "16/10/2026 10:00", deaths = 5, completed = true),
            HistoryRowUi(1, RunResult.DEFEATED, "The Chetwood", 4, 1, "01/01/1970 00:00", deaths = 1),
        )
        composeRule.setContent {
            ProvideLanguageAndScript(AppLanguage.EN, AppScript.LATIN) { WardenTheme { HistoryScreen(history = history, onBack = {}) } }
        }
        composeRule.onNodeWithText("Chronicle of Laps").assertIsDisplayed()
        composeRule.onNodeWithText("Lap 2 — Completed").assertIsDisplayed()
        composeRule.onNodeWithText("Mount Doom · Level 72 · 35 victories · 5 deaths").assertIsDisplayed()
        composeRule.onNodeWithText("Lap 1 — Fallen").assertIsDisplayed()
        composeRule.onNodeWithText("The Chetwood · Level 4 · 1 victory · 1 death").assertIsDisplayed()
        composeRule.onNode(hasContentDescription("Lap completed")).assertIsDisplayed()
    }

    @Test fun historyShowsDifficulty() {
        val history = listOf(HistoryRowUi(1, RunResult.COMPLETED, "Monte del Destino", 72, 35, "16/10/2026 10:00", completed = true, difficulty = Difficulty.RANGER))
        composeRule.setContent { WardenTheme { HistoryScreen(history = history, onBack = {}) } }
        composeRule.onNodeWithText("Dificultad: Senda del Montaraz").assertIsDisplayed()
    }

    @Test fun historyHidesMissingDifficulty() {
        val history = listOf(HistoryRowUi(1, RunResult.COMPLETED, "Monte del Destino", 72, 35, "16/10/2026 10:00", completed = true, difficulty = null))
        composeRule.setContent { WardenTheme { HistoryScreen(history = history, onBack = {}) } }
        composeRule.onNodeWithText("Dificultad", substring = true).assertDoesNotExist()
    }

    @Test fun noLapsShowsTheEmptyState() {
        composeRule.setContent { WardenTheme { HistoryScreen(history = emptyList(), onBack = {}) } }
        composeRule.onNodeWithText("Aún no has completado ninguna vuelta").assertIsDisplayed()
    }
}
