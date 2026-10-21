package com.wardengambits.app.ui.practice

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.wardengambits.app.i18n.AppLanguage
import com.wardengambits.app.i18n.AppScript
import com.wardengambits.app.i18n.ProvideLanguageAndScript
import com.wardengambits.app.i18n.tengwar.Tengwar
import com.wardengambits.app.i18n.tengwar.TengwarMode
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.story.NodeId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PracticeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val open = NodeId("bree", 1, 0)
    private val group = NodeId("bree", 2, 0)
    private val boss = NodeId("bree", 7, 0)
    private val locked = NodeId("chet", 1, 0)

    private val zones = listOf(
        PracticeZoneUi(
            "bree", "Bree",
            listOf(
                PracticeCellUi(open, "Posada", "bandit", false, 1, 3, true),
                PracticeCellUi(group, "Callejón", "bandit", false, 4, 4, true),
                PracticeCellUi(boss, "Jefe de Bree", "boss_bree", true, 1, 5, true),
            ),
        ),
        PracticeZoneUi("chet", "Bosque de Chet", listOf(PracticeCellUi(locked, "Secreto", "bandit", false, 1, 6, false))),
    )

    private fun show(fights: MutableList<NodeId> = mutableListOf(), language: AppLanguage = AppLanguage.ES, script: AppScript = AppScript.LATIN) {
        composeRule.setContent {
            ProvideLanguageAndScript(language, script) {
                WardenTheme { PracticeScreen(zones, 0, onFight = { fights += it }, onBack = {}) }
            }
        }
    }

    @Test fun showsZoneHeadersAndUnlockedCell() {
        show()
        composeRule.onNodeWithText("Bree").assertIsDisplayed()
        composeRule.onNodeWithText("Posada").assertIsDisplayed()
        composeRule.onNodeWithText("Nv 3").assertIsDisplayed()
        composeRule.onNodeWithText("×4").assertIsDisplayed()
    }

    @Test fun lockedCellHidesTitleAndIgnoresTaps() {
        val fights = mutableListOf<NodeId>()
        show(fights)
        composeRule.onNodeWithTag("practice_grid").performScrollToNode(hasTestTag("practice_cell_chet_1_0"))
        composeRule.onNodeWithText("Bosque de Chet").assertExists()
        composeRule.onNodeWithText("Bloqueado").assertExists()
        composeRule.onNodeWithText("Secreto").assertDoesNotExist()
        composeRule.onNodeWithTag("practice_cell_chet_1_0").performClick()
        assertEquals(emptyList<NodeId>(), fights)
    }

    @Test fun tappingUnlockedCellFights() {
        val fights = mutableListOf<NodeId>()
        show(fights)
        composeRule.onNodeWithText("Posada").performClick()
        assertEquals(listOf(open), fights)
    }

    private fun colorOf(text: String): androidx.compose.ui.graphics.Color {
        val results = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        val node = composeRule.onNodeWithText(text, useUnmergedTree = true).fetchSemanticsNode()
        composeRule.runOnUiThread {
            node.config[androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        }
        return results.first().layoutInput.style.color
    }

    @Test fun cellTextsAreNotDefaultBlack() {
        show()
        assertEquals(com.wardengambits.app.ui.theme.WardenColors.Text, colorOf("Posada"))
        composeRule.onNodeWithTag("practice_grid").performScrollToNode(hasTestTag("practice_cell_chet_1_0"))
        val locked = colorOf("Bloqueado")
        assertTrue(locked.alpha > 0f)
        assertNotEquals(androidx.compose.ui.graphics.Color.Black, locked)
        assertTrue(locked.red > 0.5f)
    }

    @Test fun lockedCellIsDisabledForAccessibility() {
        show()
        composeRule.onNodeWithTag("practice_grid").performScrollToNode(hasTestTag("practice_cell_chet_1_0"))
        composeRule.onNodeWithTag("practice_cell_chet_1_0").assertIsNotEnabled()
        composeRule.onNodeWithTag("practice_cell_bree_1_0").assertIsEnabled()
    }

    @Test fun bossWearsEliteRibbon() {
        show()
        composeRule.onNodeWithTag("elite_ribbon", useUnmergedTree = true).assertExists()
    }

    @Test fun worksInEnglish() {
        show(language = AppLanguage.EN)
        composeRule.onNodeWithText("Lv 3").assertIsDisplayed()
    }

    @Test fun worksInTengwar() {
        show(script = AppScript.TENGWAR)
        composeRule.onNodeWithTag("practice_cell_bree_1_0").assertIsDisplayed()
        composeRule.onNodeWithText("Posada").assertDoesNotExist()
        composeRule.onNodeWithText(Tengwar.transcribe("Posada", TengwarMode.SPANISH)).assertIsDisplayed()
        composeRule.onNodeWithTag("practice_grid").performScrollToNode(hasTestTag("practice_cell_chet_1_0"))
        composeRule.onNodeWithText("Bloqueado").assertDoesNotExist()
        composeRule.onNodeWithText(Tengwar.transcribe("Bloqueado", TengwarMode.SPANISH)).assertExists()
    }
}
