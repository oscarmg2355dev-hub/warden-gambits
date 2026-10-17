package com.wardengambits.app.ui.ending

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.wardengambits.app.ui.theme.WardenTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EndingScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ui = EndingUi(
        key = "orodruin_final", zoneId = "orodruin", zoneName = "Monte del Destino",
        title = "El Bosque Dorado", text = "Epílogo de la aventura",
        lap = 1, nextLap = 2, level = 42, levelsGained = 41, wins = 40, deaths = 5,
    )

    @Test fun showsTitleImageEpilogueAndStats() {
        composeRule.setContent { WardenTheme { EndingScreen(ui, {}, {}) } }
        composeRule.onNodeWithText("¡Has completado la aventura!").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("El Bosque Dorado").assertIsDisplayed()
        composeRule.onNodeWithText("Epílogo de la aventura").performScrollTo().assertIsDisplayed()
        listOf("Vuelta 1 completada", "Nivel 42 (+41 en esta vuelta)", "Victorias: 40", "Muertes: 5").forEach {
            composeRule.onNodeWithText(it).performScrollTo().assertIsDisplayed()
        }
    }

    @Test fun buttonsCallBack() {
        var laps = 0
        var menus = 0
        composeRule.setContent { WardenTheme { EndingScreen(ui, { laps++ }, { menus++ }) } }
        composeRule.onNodeWithText("Emprender la vuelta 2").performClick()
        composeRule.onNodeWithText("Volver al menú").performClick()
        assertEquals(1, laps)
        assertEquals(1, menus)
    }
}
