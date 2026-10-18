package com.wardengambits.app.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h800dp")
class RpgBackgroundTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test fun backgroundModifierRendersWithoutCrashing() {
        composeRule.setContent { Box(Modifier.fillMaxSize().rpgBackground().testTag("bg")) }
        composeRule.onNodeWithTag("bg").assertIsDisplayed()
    }

    @Test fun tileUsesGameThemedMotifs() {
        val motifs = RpgPattern.Motif.entries.map { it.name }.toSet()
        for (themed in listOf("ONE_RING", "EYE", "MOUNT_DOOM", "WARDEN_SPEAR", "WARDEN_SHIELD", "LORIEN_LEAF", "WHITE_TREE",
            "HOBBIT_DOOR", "DURIN_STAR", "HORN", "POTION", "COMBO_KEYS")) {
            assertTrue("missing motif $themed", themed in motifs)
        }
        assertTrue("every motif is placed (${RpgPattern.motifCount})", RpgPattern.motifCount == RpgPattern.Motif.entries.size)
        assertTrue("dense like a doodle wallpaper (${RpgPattern.placementCount} placements)", RpgPattern.placementCount >= 20)
    }

    @Test fun tileIsVisibleButSubtle() {
        val px = 300
        val tile = ImageBitmap(px, px)
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(tile), Size(px.toFloat(), px.toFloat())) {
            RpgPattern.drawTile(this)
        }
        val pixels = tile.toPixelMap()
        var drawn = 0
        var maxAlpha = 0f
        for (x in 0 until px) for (y in 0 until px) {
            val a = pixels[x, y].alpha
            if (a > 0f) drawn++
            if (a > maxAlpha) maxAlpha = a
        }
        assertTrue("pattern draws plenty of line-art ($drawn px)", drawn > 2500)
        assertTrue("pattern is visible on a phone (max alpha $maxAlpha)", maxAlpha >= 0.10f)
        assertTrue("pattern stays subtle behind the UI (max alpha $maxAlpha)", maxAlpha <= 0.16f)
        assertTrue("pattern stays sparse ($drawn px)", drawn < px * px / 4)
    }
}
