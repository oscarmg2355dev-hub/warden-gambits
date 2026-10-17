package com.wardengambits.app.ui.help

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import com.wardengambits.app.i18n.AppLanguage
import com.wardengambits.app.i18n.AppScript
import com.wardengambits.app.i18n.ProvideLanguageAndScript
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.combo.Combos
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HelpScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun texts(node: SemanticsNodeInteraction): List<String> {
        fun collect(n: androidx.compose.ui.semantics.SemanticsNode): List<String> =
            (n.config.getOrNull(SemanticsProperties.Text)?.map { it.text } ?: emptyList()) +
                n.children.flatMap(::collect)
        return collect(node.fetchSemanticsNode())
    }

    @Test fun showsIntroAndComboRowWithKeys() {
        composeRule.setContent { WardenTheme { HelpScreen(ComboDescriber.grouped(), {}) } }
        composeRule.onNodeWithText("Cadenas:", substring = true).assertExists()
        val def = Combos.find("1212")!!
        composeRule.onNodeWithTag("help_list").performScrollToNode(hasTestTag("combo_row_1212"))
        val all = texts(composeRule.onNodeWithTag("combo_row_1212", useUnmergedTree = true))
        assertEquals(listOf("1", "2", "1", "2"), all.take(4))
        assertEquals("Lanza III", all[4])
        assertEquals("Coste ${def.cost}", all[5])
        assertEquals("3× daño de lanza por tick durante 10 ticks (+10 si encadenas)", all[6])
        assertEquals("SP3", all[7])
    }

    @Test fun showsRecoverRow() {
        composeRule.setContent { WardenTheme { HelpScreen(ComboDescriber.grouped(), {}) } }
        composeRule.onNodeWithTag("help_list").performScrollToNode(hasTestTag("combo_row_321"))
        val all = texts(composeRule.onNodeWithTag("combo_row_321", useUnmergedTree = true))
        assertEquals(listOf("3", "2", "1", "Recuperar", "Coste 0", "recupera el 10% de tu power al instante"), all)
    }

    @Test fun inEnglishTheHelpIsEnglishWithTheSameNumbers() {
        composeRule.setContent {
            ProvideLanguageAndScript(AppLanguage.EN, AppScript.LATIN) { WardenTheme { HelpScreen(ComboDescriber.grouped(), {}) } }
        }
        composeRule.onNodeWithText("Chains:", substring = true).assertExists()
        composeRule.onNodeWithText("Spear").assertExists()
        composeRule.onNodeWithTag("help_list").performScrollToNode(hasTestTag("combo_row_1212"))
        val spear = texts(composeRule.onNodeWithTag("combo_row_1212", useUnmergedTree = true))
        assertEquals(listOf("Spear III", "Cost 26", "3× spear damage per tick for 10 ticks (+10 if chained)", "SP3"), spear.drop(4))
        composeRule.onNodeWithTag("help_list").performScrollToNode(hasTestTag("combo_row_2131"))
        val tactics = texts(composeRule.onNodeWithTag("combo_row_2131", useUnmergedTree = true))
        assertEquals(listOf("Tactical Shield", "Cost 20", "immune to Daze for 60 ticks", "ET"), tactics.drop(4))
    }

    @Test fun shieldRowShowsTheShieldBadge() {
        composeRule.setContent { WardenTheme { HelpScreen(ComboDescriber.grouped(), {}) } }
        composeRule.onNodeWithTag("help_list").performScrollToNode(hasTestTag("combo_row_2132"))
        composeRule.onNode(hasTestTag("badge_shield") and hasAnyAncestor(hasTestTag("combo_row_2132")), useUnmergedTree = true).assertExists()
        val all = texts(composeRule.onNodeWithTag("combo_row_2132", useUnmergedTree = true))
        assertEquals("EM", all.last())
    }
}
