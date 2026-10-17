package com.wardengambits.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.semantics.SemanticsProperties
import com.wardengambits.app.i18n.AppLanguage
import com.wardengambits.app.i18n.AppScript
import com.wardengambits.app.i18n.ProvideLanguageAndScript
import com.wardengambits.app.i18n.tengwar.Tengwar
import com.wardengambits.app.i18n.tengwar.TengwarMode
import com.wardengambits.app.ui.menu.MenuScreen
import com.wardengambits.app.ui.theme.WardenTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class MenuScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun menuShowsButtonsAndCallsBack() {
        var characterClicks = 0
        composeRule.setContent {
            WardenTheme {
                MenuScreen(onStory = {}, onCharacter = { characterClicks++ }, onHelp = {}, onNewAdventure = {})
            }
        }
        composeRule.onNodeWithText("Warden-Gambits").assertIsDisplayed()
        listOf("Modo Historia", "Personaje", "Ayuda de combos", "Nueva aventura").forEach {
            composeRule.onNodeWithText(it).assertIsDisplayed()
        }
        composeRule.onNodeWithText("Personaje").performClick()
        assertEquals(1, characterClicks)
    }

    @Test
    fun newAdventureAsksConfirmation() {
        var confirmed = 0
        composeRule.setContent {
            WardenTheme {
                MenuScreen(onStory = {}, onCharacter = {}, onHelp = {}, onNewAdventure = { confirmed++ })
            }
        }
        composeRule.onNodeWithText("Nueva aventura").performClick()
        assertEquals(0, confirmed)
        composeRule.onNodeWithText("Cancelar").performClick()
        assertEquals(0, confirmed)
        composeRule.onNodeWithText("Nueva aventura").performClick()
        composeRule.onNodeWithText("Empezar").performClick()
        assertEquals(1, confirmed)
    }

    @Test
    fun optionsButtonReplacesTheSoundButton() {
        var options = 0
        composeRule.setContent {
            WardenTheme {
                MenuScreen(onStory = {}, onCharacter = {}, onHelp = {}, onNewAdventure = {}, onOptions = { options++ })
            }
        }
        composeRule.onNodeWithText("Sonido: sí").assertDoesNotExist()
        composeRule.onNodeWithText("Opciones").assertIsDisplayed().performClick()
        assertEquals(1, options)
    }

    @Test
    fun aboutButtonCallsBack() {
        var about = 0
        composeRule.setContent {
            WardenTheme {
                MenuScreen(onStory = {}, onCharacter = {}, onHelp = {}, onNewAdventure = {}, onAbout = { about++ })
            }
        }
        composeRule.onNodeWithText("Acerca de").assertIsDisplayed().performClick()
        assertEquals(1, about)
    }

    @Test
    @Config(qualifiers = "es-w411dp-h400dp") // short / landscape: the buttons do not fit, the menu scrolls
    fun lastButtonScrollsIntoViewOnAShortScreen() {
        var about = 0
        composeRule.setContent {
            WardenTheme {
                MenuScreen(onStory = {}, onCharacter = {}, onHelp = {}, onNewAdventure = {}, onAbout = { about++ })
            }
        }
        composeRule.onNodeWithText("Acerca de").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(1, about)
    }

    @Test
    fun optionsStaysLatinInTengwar() {
        composeRule.setContent {
            ProvideLanguageAndScript(AppLanguage.ES, AppScript.TENGWAR) {
                WardenTheme { MenuScreen(onStory = {}, onCharacter = {}, onHelp = {}, onNewAdventure = {}) }
            }
        }
        composeRule.onNodeWithText("Modo Historia").assertDoesNotExist()
        composeRule.onNodeWithText(Tengwar.transcribe("Modo Historia", TengwarMode.SPANISH)).assertIsDisplayed()
        val texts = composeRule.onNodeWithText("Opciones").assertIsDisplayed()
            .fetchSemanticsNode().config[SemanticsProperties.Text]
        assertTrue(texts.all { it.spanStyles.isEmpty() })
    }
}
