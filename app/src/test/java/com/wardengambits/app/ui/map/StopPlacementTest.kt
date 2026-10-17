package com.wardengambits.app.ui.map

import com.wardengambits.app.ui.map.world.Tile
import org.junit.Assert.assertEquals
import org.junit.Test

class StopPlacementTest {
    @Test fun labelGoesBelowTheMarkerWhenItFits() {
        val p = stopPlacement(centerY = 100, marker = 60, gap = 4, label = 120, mapHeight = 400)
        assertEquals(StopPlacement(markerTop = 70, labelTop = 134), p)
    }

    @Test fun labelGoesAboveTheMarkerWhenBelowWouldClip() {
        val p = stopPlacement(centerY = 250, marker = 60, gap = 4, label = 120, mapHeight = 300)
        assertEquals(StopPlacement(markerTop = 220, labelTop = 96), p)
    }

    @Test fun wholeStopIsClampedInsideWhenNeitherSideFits() {
        val p = stopPlacement(centerY = 60, marker = 60, gap = 4, label = 120, mapHeight = 160)
        // stack = 184 > 160: marker pinned to the top, label pulled up over it so it is not clipped.
        assertEquals(StopPlacement(markerTop = 0, labelTop = 40), p)
        val q = stopPlacement(centerY = 100, marker = 60, gap = 4, label = 120, mapHeight = 200)
        // stack = 184 fits only if moved up to 16.
        assertEquals(StopPlacement(markerTop = 16, labelTop = 80), q)
    }

    @Test fun stopCenterFollowsTheRoadTileAtTheNewScale() {
        assertEquals(4, TILE_DP)
        assertEquals(224, ZONE_WIDTH_DP)
        // Tile row 30 is centred at (30 + 0.5) × 4 dp.
        assertEquals(122f, stopCenterYDp(Tile(x = 28, y = 30)), 0f)
    }
}
