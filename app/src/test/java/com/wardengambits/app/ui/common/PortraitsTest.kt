package com.wardengambits.app.ui.common

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PortraitsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun everySpriteIs16x16WithKnownColors() {
        val keys = PortraitSprites.TYPES + "warden"
        assertEquals(19, keys.size)
        keys.forEach { key ->
            val sprite = PortraitSprites.spriteFor(key)
            assertEquals("$key rows", 16, sprite.rows.size)
            sprite.rows.forEachIndexed { i, row ->
                assertEquals("$key row $i width", 16, row.length)
                row.filter { it != '.' }.forEach { c ->
                    assertTrue("$key row $i: '$c' not in palette", c in sprite.palette)
                }
            }
        }
    }

    @Test
    fun typesIncludeTheRoadToMordorBestiary() {
        assertEquals(18, PortraitSprites.TYPES.size)
        val newTypes = setOf("uruk", "mordor_orc", "wisp", "marsh_wraith", "haradrim", "spider")
        assertTrue(PortraitSprites.TYPES.containsAll(newTypes))
    }

    @Test
    fun roadToMordorBossesMapToBaseType() {
        val expected = mapOf(
            "boss_anduin" to "orc_archer",
            "boss_argonath" to "uruk",
            "boss_amonhen" to "uruk",
            "boss_emynmuil" to "troll",
            "boss_cienagas" to "marsh_wraith",
            "boss_ithilien" to "haradrim",
            "boss_cirithungol" to "spider",
            "boss_gorgoroth" to "marsh_wraith",
        )
        expected.forEach { (boss, base) -> assertEquals(boss, base, PortraitSprites.baseType(boss)) }
    }

    @Test
    fun bossesMapToBaseType() {
        assertEquals("wolf", PortraitSprites.baseType("boss_chet"))
        assertEquals("orc_archer", PortraitSprites.baseType("boss_nimrodel"))
        assertEquals("orc", PortraitSprites.baseType("orc"))
        assertEquals("warden", PortraitSprites.baseType("zzz"))
    }

    @Test
    fun portraitFallsBackToSprite() {
        composeRule.setContent { Portrait("orc", 44.dp) }
        composeRule.onNodeWithContentDescription("Retrato").assertIsDisplayed()
    }
}
