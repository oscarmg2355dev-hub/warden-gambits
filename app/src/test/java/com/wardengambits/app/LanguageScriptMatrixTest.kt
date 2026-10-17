package com.wardengambits.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.wardengambits.app.i18n.AppLanguage
import com.wardengambits.app.i18n.AppScript
import com.wardengambits.app.i18n.ProvideLanguageAndScript
import com.wardengambits.app.i18n.tengwar.Tengwar
import com.wardengambits.app.ui.character.CharacterScreen
import com.wardengambits.app.ui.character.CharacterUi
import com.wardengambits.app.ui.character.HistoryRowUi
import com.wardengambits.app.ui.character.HistoryScreen
import com.wardengambits.app.ui.combat.CombatScreen
import com.wardengambits.app.ui.combat.CombatUiState
import com.wardengambits.app.ui.combat.FighterUi
import com.wardengambits.app.ui.common.EffectBadgeUi
import com.wardengambits.app.ui.help.ComboDescriber
import com.wardengambits.app.ui.help.HelpScreen
import com.wardengambits.app.ui.menu.MenuScreen
import com.wardengambits.app.ui.node.NodeActions
import com.wardengambits.app.ui.node.NodeScreen
import com.wardengambits.app.ui.node.NodeUi
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.Stats
import com.wardengambits.engine.story.Choice
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.RunResult
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The four language x script combinations over the main screens: English shows the translated text,
 * tengwar shows the transcription of that text, and numbers / combo codes stay Latin.
 * Each test composes its screen once and switches language and script through state.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h1600dp")
class LanguageScriptMatrixTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var language by mutableStateOf(AppLanguage.ES)
    private var script by mutableStateOf(AppScript.LATIN)

    private val combos = listOf(
        AppLanguage.ES to AppScript.LATIN, AppLanguage.EN to AppScript.LATIN,
        AppLanguage.ES to AppScript.TENGWAR, AppLanguage.EN to AppScript.TENGWAR,
    )

    private fun setScreen(content: @Composable () -> Unit) = composeRule.setContent {
        ProvideLanguageAndScript(language, script) { WardenTheme { content() } }
    }

    /** What the screen must show in the current combination, from the Spanish and English source texts. */
    private class Written(val lang: AppLanguage, val scr: AppScript) {
        fun of(text: String) = if (scr == AppScript.LATIN) text else Tengwar.transcribe(text, lang.tengwarMode)
        fun of(es: String, en: String) = of(if (lang == AppLanguage.ES) es else en)
    }

    private fun eachCombo(check: Written.() -> Unit) {
        combos.forEach { (lang, scr) ->
            language = lang
            script = scr
            composeRule.waitForIdle()
            Written(lang, scr).check()
        }
    }

    private fun texts(node: SemanticsNode): List<String> =
        (node.config.getOrNull(SemanticsProperties.Text)?.map { it.text } ?: emptyList()) + node.children.flatMap(::texts)

    @Test fun menu() {
        setScreen { MenuScreen(onStory = {}, onCharacter = {}, onHelp = {}, onNewAdventure = {}) }
        eachCombo {
            listOf(
                "Modo Historia" to "Story Mode", "Personaje" to "Character", "Ayuda de combos" to "Combo Help",
                "Nueva aventura" to "New Adventure", "Acerca de" to "About",
            ).forEach { (es, en) -> composeRule.onNodeWithText(of(es, en)).performScrollTo().assertIsDisplayed() }
            // Options stays Latin in every combination so the player can always switch back.
            val options = if (lang == AppLanguage.ES) "Opciones" else "Options"
            composeRule.onNodeWithText(options).performScrollTo().assertIsDisplayed()
        }
    }

    @Test fun node() {
        val fight = NodeActions.Fight(emptyList())
        var current: NodeActions by mutableStateOf(fight)
        setScreen {
            NodeScreen(NodeUi("nope_1_0", "bree", "Bree", 1, 7, "Titulo X", "Texto del nodo", current), {}, {}, {}, {}, {})
        }
        eachCombo {
            current = fight
            composeRule.waitForIdle()
            composeRule.onNodeWithText(of("Titulo X")).assertIsDisplayed()
            composeRule.onNodeWithText(of("¡Luchar!", "Fight!")).assertIsDisplayed()
            current = NodeActions.Continue
            composeRule.waitForIdle()
            composeRule.onNodeWithText(of("Continuar", "Continue")).assertIsDisplayed()
            current = NodeActions.Choices(listOf(Choice(NodeId("bree", 2, 0), "Ir al norte")))
            composeRule.waitForIdle()
            composeRule.onNodeWithText(of("Ir al norte")).assertIsDisplayed()
        }
    }

    @Test fun characterSheet() {
        val ui = CharacterUi(
            level = 79, lap = 3, wins = 7, deaths = 2, zoneName = "Bosque de Chet", progress = "3/7",
            stats = Stats(maxHp = 166_000, maxPower = 300, powerRegen = 5, attack = 16_600, heal = 15_400, defense = 8_000),
            history = emptyList(), completedLaps = 2,
        )
        setScreen { CharacterScreen(ui = ui, onBack = {}, onHistory = {}) }
        eachCombo {
            composeRule.onNodeWithText(of("Atributos", "Attributes")).performScrollTo().assertIsDisplayed()
            composeRule.onNodeWithText(of("El viaje", "The Journey")).performScrollTo().assertIsDisplayed()
            composeRule.onNodeWithText(of("Guardián de Bree", "Warden of Bree")).assertIsDisplayed()
            // Label transcribed, the number after it stays Latin.
            composeRule.onNodeWithText(of("Muertes: ", "Deaths: ") + "2").performScrollTo().assertIsDisplayed()
            composeRule.onNode(hasText(of("Ataque", "Attack")) and hasText("16 600")).performScrollTo().assertIsDisplayed()
        }
    }

    @Test fun history() {
        val rows = listOf(HistoryRowUi(2, RunResult.COMPLETED, "Monte del Destino", 72, 35, "16/10/2026 10:00", deaths = 5, completed = true))
        setScreen { HistoryScreen(history = rows, onBack = {}) }
        eachCombo {
            composeRule.onNodeWithText(of("Crónica de vueltas", "Chronicle of Laps")).assertIsDisplayed()
            composeRule.onNodeWithText("16/10/2026 10:00").assertIsDisplayed() // a date is never transcribed
        }
    }

    @Test fun help() {
        setScreen { HelpScreen(ComboDescriber.grouped(), {}) }
        eachCombo {
            composeRule.onNodeWithTag("help_list").performScrollToNode(hasTestTag("combo_row_1212"))
            val row = texts(composeRule.onNodeWithTag("combo_row_1212", useUnmergedTree = true).fetchSemanticsNode())
            assertEquals(listOf("1", "2", "1", "2"), row.take(4)) // keys stay Latin
            assertEquals(of("Lanza III", "Spear III"), row[4])
            assertEquals(of("Coste ", "Cost ") + Combos.find("1212")!!.cost, row[5])
            assertEquals("SP3", row.last()) // combo code stays Latin
        }
    }

    @Test
    @Config(qualifiers = "w360dp-h640dp")
    fun combat() {
        fun rival(id: Int, target: Boolean) =
            FighterUi(FighterId(id), "Orco", 2, 40, 50, 10, 20, listOf(EffectBadgeUi("SP1", true, 0.5f)), true, target)
        val player = FighterUi(PLAYER_ID, "Warden", 3, 80, 100, 30, 60, emptyList(), true, false)
        val state = CombatUiState(
            zoneName = "Bree", levelLabel = "3/7", tick = 5, paused = false, rivals = listOf(rival(1, true), rival(2, false)),
            player = player, buffer = "", bufferMatch = null, outcome = null, levelBefore = 3, winsBefore = 2, inputsEnabled = true,
        )
        setScreen { CombatScreen(state, {}, {}, {}, {}, {}, {}, {}) }
        eachCombo {
            composeRule.onNodeWithText(of("Pausa", "Pause")).assertIsDisplayed()
            composeRule.onNodeWithText(of("3/7  ·  Tick ") + "5").assertIsDisplayed() // the tick number stays Latin
            composeRule.onNodeWithText(of("Warden · Nv 3", "Warden · Lv 3")).assertExists()
        }
    }
}
