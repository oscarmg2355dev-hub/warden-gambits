package com.wardengambits.app.ui.node

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.wardengambits.app.ui.common.NodeImage
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.story.Choice
import com.wardengambits.engine.story.NodeId
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NodeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun ui(actions: NodeActions) =
        NodeUi("nope_1_0", "bree", "Bree", 1, 7, "Titulo X", "Texto largo del nodo", actions)

    @Test fun nodeScreenRendersAndClicks() {
        var chosen = -1
        var fights = 0
        val choices = NodeActions.Choices(
            listOf(Choice(NodeId("bree", 2, 0), "Ir al norte"), Choice(NodeId("bree", 2, 1), "Ir al sur")),
        )
        composeRule.setContent {
            WardenTheme { NodeScreen(ui(choices), { fights++ }, { chosen = it }, {}, {}, {}) }
        }
        composeRule.onNodeWithText("Titulo X").assertIsDisplayed()
        composeRule.onNodeWithText("Texto largo del nodo").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Ir al norte").assertIsDisplayed()
        composeRule.onNodeWithText("Ir al sur").performClick()
        assertEquals(1, chosen)
        assertEquals(0, fights)
    }

    @Test fun fightShowsButtonAndRivals() {
        var fights = 0
        composeRule.setContent {
            WardenTheme {
                NodeScreen(ui(NodeActions.Fight(listOf(RivalUi("Orco", 3)))), { fights++ }, {}, {}, {}, {})
            }
        }
        composeRule.onNodeWithText("Orco · Nv 3").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("¡Luchar!").performClick()
        assertEquals(1, fights)
    }

    @Test fun continueAndNextLapButtons() {
        var continues = 0
        var laps = 0
        composeRule.setContent {
            WardenTheme { NodeScreen(ui(NodeActions.Continue), {}, {}, { continues++ }, { laps++ }, {}) }
        }
        composeRule.onNodeWithText("Continuar").performClick()
        assertEquals(1, continues)
    }

    @Test fun nextLapLabel() {
        var laps = 0
        composeRule.setContent {
            WardenTheme { NodeScreen(ui(NodeActions.NextLap(2)), {}, {}, {}, { laps++ }, {}) }
        }
        composeRule.onNodeWithText("Emprender la vuelta 2").performClick()
        assertEquals(1, laps)
    }

    @Test fun missingImageShowsPlaceholder() {
        composeRule.setContent {
            WardenTheme { NodeImage("nope_1_0", "bree", "Bree") }
        }
        composeRule.onNodeWithText("Bree").assertIsDisplayed()
    }

    @Test fun lothlorienFirstNodeHasBundledImage() {
        composeRule.setContent {
            WardenTheme { NodeImage("lothlorien_1_0", "lothlorien", "Lothlórien", contentDescription = "Caras Galadhon") }
        }
        composeRule.onNodeWithContentDescription("Caras Galadhon").assertIsDisplayed()
        composeRule.onNodeWithText("Lothlórien").assertDoesNotExist()
    }

    @Test fun orodruinFinalShowsGradientPlaceholder() {
        composeRule.setContent {
            WardenTheme { NodeImage("orodruin_final", "orodruin", "Monte del Destino") }
        }
        composeRule.onNodeWithText("Monte del Destino").assertIsDisplayed()
    }

    @Test fun bundledImageReplacesPlaceholder() {
        composeRule.setContent {
            WardenTheme { NodeImage("bree_1_0", "bree", "Bree", contentDescription = "Las puertas de Bree") }
        }
        composeRule.onNodeWithContentDescription("Las puertas de Bree").assertIsDisplayed()
        composeRule.onNodeWithText("Bree").assertDoesNotExist()
    }
}
