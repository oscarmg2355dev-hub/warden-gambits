package com.wardengambits.app.ui.common

import com.wardengambits.engine.story.Route
import org.junit.Assert.assertEquals
import org.junit.Test

class ZoneColorsTest {
    @Test
    fun oneColorPerRouteZone() {
        assertEquals(Route.zones.size, ZoneColors.size)
    }
}
