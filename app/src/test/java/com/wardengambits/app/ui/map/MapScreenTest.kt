package com.wardengambits.app.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wardengambits.app.ui.map.world.WorldGen
import com.wardengambits.app.ui.map.world.WorldMap
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.story.Route
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MapScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun rowsWithCurrent(current: Int, level: Int = 2, total: Int = 7) = Route.zones.mapIndexed { i, z ->
        when {
            i < current -> ZoneRowUi(z.id, z.name, ZoneStatus.DONE, null, null)
            i == current -> ZoneRowUi(z.id, z.name, ZoneStatus.CURRENT, level, total)
            else -> ZoneRowUi(z.id, z.name, ZoneStatus.LOCKED, null, null)
        }
    }

    private fun show(rows: List<ZoneRowUi>, onContinue: () -> Unit = {}, onBack: () -> Unit = {}) {
        composeRule.setContent { WardenTheme { MapScreen(rows = rows, onContinue = onContinue, onBack = onBack) } }
        awaitWorld()
    }

    private fun awaitWorld() {
        composeRule.waitUntil(20_000) { composeRule.onAllNodesWithTag(MAP_WORLD_TAG).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun mapShowsStopsAndContinue() {
        show(rowsWithCurrent(0))
        Route.zones.forEach { composeRule.onNodeWithText(it.name).assertExists() }
        composeRule.onNodeWithText("Continuar ▶").assertIsDisplayed()
        composeRule.onNodeWithTag(CURRENT_MARKER_TAG).assertIsDisplayed()
    }

    @Test
    fun loadingTextWhileGenerating() {
        val release = CountDownLatch(1)
        val slow = object : WorldSource {
            override fun load(zoneIds: List<String>, rows: Int): WorldMap {
                release.await(20, TimeUnit.SECONDS)
                return WorldGen.generate(zoneIds, rows)
            }
        }
        composeRule.setContent {
            WardenTheme { MapScreen(rows = rowsWithCurrent(0), onContinue = {}, onBack = {}, worldSource = slow) }
        }
        composeRule.onNodeWithText("Dibujando el mapa…").assertIsDisplayed()
        composeRule.onNodeWithText("Continuar ▶").assertDoesNotExist()
        release.countDown()
        awaitWorld()
        composeRule.onNodeWithText("Dibujando el mapa…").assertDoesNotExist()
        composeRule.onNodeWithText("Continuar ▶").assertIsDisplayed()
    }

    @Test
    fun showsAllZoneNamesAndTheHeader() {
        show(rowsWithCurrent(2))
        Route.zones.forEach { composeRule.onNodeWithText(it.name).assertExists() }
        composeRule.onNodeWithText("El camino a Mordor").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Volver").assertIsDisplayed()
    }

    @Test
    fun currentZoneShowsCartoucheAndContinue() {
        var continues = 0
        show(rowsWithCurrent(2), onContinue = { continues++ })
        composeRule.onNodeWithText("Pantanos de Moscagua").assertIsDisplayed()
        composeRule.onNodeWithText("2/7").assertIsDisplayed()
        composeRule.onNodeWithText("Continuar ▶").assertIsDisplayed().performClick()
        assertEquals(1, continues)
    }

    @Test
    fun tappingTheCurrentMarkerContinues() {
        var continues = 0
        show(rowsWithCurrent(2), onContinue = { continues++ })
        composeRule.onNodeWithTag(CURRENT_MARKER_TAG).performClick()
        assertEquals(1, continues)
    }

    @Test
    fun initialScrollCentersTheCurrentZone() {
        show(rowsWithCurrent(10, level = 5))
        composeRule.onNodeWithText("Puertas de Durin").assertIsDisplayed()
        composeRule.onNodeWithText("5/7").assertIsDisplayed()
        composeRule.onNodeWithText("Bree").assertIsNotDisplayed()
    }

    @Test
    fun finalZoneShowsOneOfOne() {
        show(rowsWithCurrent(14, level = 1, total = 1))
        composeRule.onNodeWithText("Lothlórien").assertIsDisplayed()
        composeRule.onNodeWithText("1/1").assertIsDisplayed()
    }

    @Test
    fun backCallsOnBack() {
        var backs = 0
        show(rowsWithCurrent(0), onBack = { backs++ })
        composeRule.onNodeWithContentDescription("Volver").performClick()
        assertEquals(1, backs)
    }

    @Test
    fun cartoucheStaysInsideAShortMap() {
        composeRule.setContent {
            WardenTheme {
                Box(Modifier.size(360.dp, 260.dp)) { MapScreen(rows = rowsWithCurrent(0), onContinue = {}, onBack = {}) }
            }
        }
        awaitWorld()
        val bounds = composeRule.onNodeWithText("Continuar ▶").getUnclippedBoundsInRoot()
        assertTrue("Continuar bottom ${bounds.bottom} must be inside 260.dp", bounds.bottom <= 260.dp)
        composeRule.onNodeWithText("Continuar ▶").assertIsDisplayed()
    }
}

@RunWith(RobolectricTestRunner::class)
class ZoneMarkerSemanticsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun markersExposeZoneAndState() {
        composeRule.setContent {
            WardenTheme {
                androidx.compose.foundation.layout.Column {
                    ZoneMarker("Alfa", ZoneStatus.DONE)
                    ZoneMarker("Beta", ZoneStatus.CURRENT)
                    ZoneMarker("Gamma", ZoneStatus.LOCKED)
                }
            }
        }
        composeRule.onNodeWithContentDescription("Alfa, completada").assertExists()
        composeRule.onNodeWithContentDescription("Beta, actual").assertExists()
        composeRule.onNodeWithContentDescription("Gamma, bloqueada").assertExists()
    }
}
