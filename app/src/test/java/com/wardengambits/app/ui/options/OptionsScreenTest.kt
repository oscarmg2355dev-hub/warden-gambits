package com.wardengambits.app.ui.options

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.wardengambits.app.i18n.AppLanguage
import com.wardengambits.app.i18n.AppScript
import com.wardengambits.app.i18n.ProvideLanguageAndScript
import com.wardengambits.app.i18n.tengwar.EquivalenceKind
import com.wardengambits.app.i18n.tengwar.Tengwar
import com.wardengambits.app.i18n.tengwar.TengwarMode
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// Tall screen: the buttons above the equivalence table, difficulty rows included, all fit.
@Config(qualifiers = "w411dp-h1600dp")
class OptionsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var music by mutableStateOf(true)
    private var sfx by mutableStateOf(true)
    private var narration by mutableStateOf(true)
    private var language by mutableStateOf(AppLanguage.ES)
    private var script by mutableStateOf(AppScript.LATIN)
    private var difficulty by mutableStateOf<Difficulty?>(Difficulty.RANGER)
    private val selected = mutableListOf<Difficulty>()
    private var musicToggles = 0
    private var sfxToggles = 0
    private var narrationToggles = 0
    private var backs = 0

    private fun show() {
        composeRule.setContent {
            ProvideLanguageAndScript(language, script) {
                WardenTheme {
                    OptionsScreen(
                        musicEnabled = music,
                        sfxEnabled = sfx,
                        narrationEnabled = narration,
                        language = language,
                        script = script,
                        difficulty = difficulty,
                        onSelectDifficulty = { selected += it; difficulty = it },
                        onToggleMusic = { musicToggles++; music = !music },
                        onToggleSfx = { sfxToggles++; sfx = !sfx },
                        onToggleNarration = { narrationToggles++; narration = !narration },
                        onToggleLanguage = { language = language.other() },
                        onToggleScript = { script = script.other() },
                        onBack = { backs++ },
                    )
                }
            }
        }
    }

    @Test fun musicEffectsAndNarrationToggleFromOptions() {
        show()
        composeRule.onNodeWithText("Efectos: sí").assertIsDisplayed()
        composeRule.onNodeWithText("Narración: sí").assertIsDisplayed()
        composeRule.onNodeWithText("Música: sí").assertIsDisplayed().performClick()
        assertEquals(listOf(1, 0, 0), listOf(musicToggles, sfxToggles, narrationToggles))
        composeRule.onNodeWithText("Música: no").assertIsDisplayed()
        composeRule.onNodeWithText("Efectos: sí").performClick()
        composeRule.onNodeWithText("Narración: sí").performClick()
        assertEquals(listOf(1, 1, 1), listOf(musicToggles, sfxToggles, narrationToggles))
        composeRule.onNodeWithText("Efectos: no").assertIsDisplayed()
        composeRule.onNodeWithText("Narración: no").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Volver").performClick()
        assertEquals(1, backs)
    }

    private val names = listOf("Paseo por la Comarca", "Senda del Montaraz", "Guardián de la Marca", "El Ojo de Sauron")
    private val hints = listOf(
        "Los rivales hacen la mitad de daño", "Los rivales hacen un 40 % menos de daño",
        "Los rivales hacen un 25 % menos de daño", "El reto tal como fue forjado",
    )

    private fun top(label: String) = composeRule.onNodeWithText(label).fetchSemanticsNode().boundsInRoot.top

    @Test fun optionsShowsFourDifficulties() {
        show()
        composeRule.onNodeWithText("Dificultad").assertIsDisplayed()
        names.forEach { composeRule.onNodeWithText(it, substring = true).performScrollTo().assertIsDisplayed() }
        hints.forEach { composeRule.onNodeWithText(it, substring = true).assertIsDisplayed() }
        val tops = names.map { top(it) }
        assertEquals(tops.sorted(), tops)
        names.forEachIndexed { i, name ->
            val node = composeRule.onNodeWithText(name, substring = true)
            if (i == 1) node.assertIsSelected() else node.assertIsNotSelected()
        }
    }

    @Test fun tapDifficultySelectsIt() {
        show()
        composeRule.onNodeWithText("Guardián de la Marca", substring = true).performScrollTo().performClick()
        assertEquals(listOf(Difficulty.MARK), selected)
        composeRule.onNodeWithText("Guardián de la Marca", substring = true).assertIsSelected()
    }

    @Test fun optionsDifficultyInEnglishAndTengwar() {
        language = AppLanguage.EN
        show()
        composeRule.onNodeWithText("Difficulty").assertIsDisplayed()
        composeRule.onNodeWithText("Warden of the Mark", substring = true).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Rivals deal 40% less damage", substring = true).assertIsDisplayed()
        script = AppScript.TENGWAR
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Warden of the Mark", substring = true).assertDoesNotExist()
        composeRule.onNodeWithText(Tengwar.transcribe("Warden of the Mark", TengwarMode.ENGLISH), substring = true)
            .assertExists()
    }

    @Test fun languageButtonSwitchesBetweenSpanishAndEnglish() {
        show()
        composeRule.onNodeWithText("Idioma: Español").assertIsDisplayed().performClick()
        assertEquals(AppLanguage.EN, language)
        // The whole screen follows the chosen language.
        composeRule.onNodeWithText("Language: English").assertIsDisplayed()
        listOf("Music: on", "Effects: on", "Narration: on").forEach {
            composeRule.onNodeWithText(it).assertIsDisplayed()
        }
        composeRule.onNodeWithText("Language: English").performClick()
        assertEquals(AppLanguage.ES, language)
        composeRule.onNodeWithText("Idioma: Español").assertIsDisplayed()
    }

    @Test fun scriptButtonSwitchesBetweenLatinAndTengwar() {
        show()
        composeRule.onNodeWithText("Escritura: Latina").assertIsDisplayed().performClick()
        assertEquals(AppScript.TENGWAR, script)
        composeRule.onNodeWithText("Escritura: Latina").assertDoesNotExist()
        listOf("Música: sí", "Efectos: sí", "Narración: sí").forEach {
            composeRule.onNodeWithText(it).assertDoesNotExist()
            composeRule.onNodeWithText(Tengwar.transcribe(it, TengwarMode.SPANISH)).assertIsDisplayed()
        }
        // The language and script toggles stay in Latin, with the normal font, so the player can switch back.
        listOf("Idioma: Español", "Escritura: Tengwar").forEach { label ->
            val texts = composeRule.onNodeWithText(label).assertIsDisplayed()
                .fetchSemanticsNode().config[SemanticsProperties.Text]
            assertTrue(texts.all { it.spanStyles.isEmpty() })
        }
        composeRule.onNodeWithText("Escritura: Tengwar").performClick()
        assertEquals(AppScript.LATIN, script)
        composeRule.onNodeWithText("Escritura: Latina").assertIsDisplayed()
    }

    @Test fun equivalenceTableShowsEveryLetterOfTheModeAndFollowsTheLanguage() {
        show()
        Tengwar.equivalences(TengwarMode.SPANISH).forEach { eq ->
            composeRule.onNodeWithTag("equivalence_${eq.latin}")
                .assert(hasText(eq.latin))
                .assert(hasText(eq.tengwar, substring = true))
        }
        composeRule.onNodeWithTag("equivalence_th").assertDoesNotExist()

        composeRule.onNodeWithText("Idioma: Español").performClick()
        Tengwar.equivalences(TengwarMode.ENGLISH).forEach { eq ->
            composeRule.onNodeWithTag("equivalence_${eq.latin}")
                .assert(hasText(eq.latin))
                .assert(hasText(eq.tengwar, substring = true))
        }
        composeRule.onNodeWithTag("equivalence_ñ").assertDoesNotExist()
    }

    @Test fun vowelCellsShowTheTehtaOnTheCarrierAndOnAT() {
        show()
        Tengwar.equivalences(TengwarMode.SPANISH).filter { it.kind == EquivalenceKind.VOWEL }.forEach { eq ->
            composeRule.onNodeWithTag("equivalence_${eq.latin}")
                .assert(hasText(eq.tengwar))
                .assert(hasText(Tengwar.transcribe("t${eq.latin}", TengwarMode.SPANISH)))
        }
        // "ta": tinco with the three dots on it, not on a carrier.
        assertEquals(Tengwar.transcribe("t", TengwarMode.SPANISH) + Tengwar.transcribe("a", TengwarMode.SPANISH).drop(1),
            Tengwar.transcribe("ta", TengwarMode.SPANISH))
    }

    @Test fun equivalenceLettersStayLatinInTengwarScript() {
        script = AppScript.TENGWAR
        show()
        Tengwar.equivalences(TengwarMode.SPANISH).forEach { eq ->
            composeRule.onNodeWithTag("equivalence_${eq.latin}").assert(hasText(eq.latin))
        }
    }

    private fun outputText() = composeRule.onNodeWithTag("transcriber_output").fetchSemanticsNode()
        .config[SemanticsProperties.Text].joinToString("") { it.text }

    @Test fun transcriberShowsSpanishModeLiveAndSwitchesToEnglish() {
        show()
        composeRule.onNodeWithTag("transcriber_input").performScrollTo().performTextInput("thorin")
        val es = Tengwar.transcribe("thorin", TengwarMode.SPANISH)
        val en = Tengwar.transcribe("thorin", TengwarMode.ENGLISH)
        assertNotEquals(es, en)
        assertEquals(es, outputText())
        composeRule.onNodeWithTag("transcriber_mode_en").performScrollTo().performClick()
        assertEquals(en, outputText())
        composeRule.onNodeWithTag("transcriber_mode_es").performScrollTo().performClick()
        assertEquals(es, outputText())
    }

    @Test fun transcriberDefaultsToTheAppLanguageMode() {
        language = AppLanguage.EN
        show()
        composeRule.onNodeWithTag("transcriber_input").performScrollTo().performTextInput("thorin")
        assertEquals(Tengwar.transcribe("thorin", TengwarMode.ENGLISH), outputText())
    }

    @Test fun transcriberKeepsDigitsLatin() {
        show()
        composeRule.onNodeWithTag("transcriber_input").performScrollTo().performTextInput("tolkien 2")
        assertTrue(outputText().endsWith("2"))
        assertEquals(Tengwar.transcribe("tolkien 2", TengwarMode.SPANISH), outputText())
    }

    @Test fun transcriberShowsNothingWhenEmpty() {
        show()
        composeRule.onNodeWithTag("transcriber_input").performScrollTo()
        composeRule.onNodeWithTag("transcriber_output").assertDoesNotExist()
    }

    @Test fun transcriberInputIsNeverTranscribedEvenInTengwarScript() {
        script = AppScript.TENGWAR
        show()
        composeRule.onNodeWithTag("transcriber_input").performScrollTo().performTextInput("tolkien")
        val texts = composeRule.onNodeWithTag("transcriber_input").fetchSemanticsNode()
            .config[SemanticsProperties.EditableText]
        assertEquals("tolkien", texts.text)
        assertEquals(Tengwar.transcribe("tolkien", TengwarMode.SPANISH), outputText())
    }
}
