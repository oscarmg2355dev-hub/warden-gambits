package com.wardengambits.app.ui.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.wardengambits.app.ui.theme.BadgeColors
import com.wardengambits.engine.combo.ComboFamily
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.model.ActiveEffect
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.PLAYER_ID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EffectBadgesTest {
    private val rival = FighterId(1)

    private fun fx(kind: EffectKind, combo: String, remaining: Int, total: Int = remaining, from: Int = 0, magnitude: Int = 0) =
        ActiveEffect(kind, PLAYER_ID, combo, magnitude, remaining, from, total)

    @Test fun codeFollowsTheSourceCombo() {
        fun code(kind: EffectKind, combo: String) = badgeFor(fx(kind, combo, 5)).code
        assertEquals("SP1", code(EffectKind.DOT, "12"))
        assertEquals("SP2", code(EffectKind.DOT, "121"))
        assertEquals("LC4", code(EffectKind.DOT, "13131"))
        assertEquals("AT3", code(EffectKind.DAZE_CHANCE, "3131"))
        assertEquals("PA2", code(EffectKind.DOT, "323"))
        assertEquals("CU4", code(EffectKind.COT, "21212"))
        assertEquals("RV1", code(EffectKind.DRAIN, "3123"))
        assertEquals("RV2", code(EffectKind.DRAIN, "31232"))
        assertEquals("FU", code(EffectKind.FURY, "11"))
        assertEquals("GU", code(EffectKind.GUARD, "22"))
        assertEquals("BR", code(EffectKind.VIGOR, "33"))
        assertEquals("EM", code(EffectKind.SHIELD_MASTER, "2132"))
        assertEquals("ET", code(EffectKind.SHIELD_TACTICS, "2131"))
        assertEquals("DZ", code(EffectKind.DAZED, ""))
    }

    @Test fun debuffsAreDotDazeChanceAndDazed() {
        assertEquals(true, badgeFor(fx(EffectKind.DOT, "12", 5)).debuff)
        assertEquals(true, badgeFor(fx(EffectKind.DAZE_CHANCE, "31", 5)).debuff)
        assertEquals(true, badgeFor(fx(EffectKind.DAZED, "", 5)).debuff)
        assertEquals(false, badgeFor(fx(EffectKind.COT, "21", 5)).debuff)
        assertEquals(false, badgeFor(fx(EffectKind.FURY, "11", 5)).debuff)
    }

    @Test fun fractionIsRemainingOverTotalClamped() {
        assertEquals(0.25f, badgeFor(fx(EffectKind.DOT, "12", 5, total = 20)).remainingFraction, 0.001f)
        assertEquals(1f, badgeFor(fx(EffectKind.FURY, "11", 31, total = 30)).remainingFraction, 0.001f)
        assertEquals(0f, badgeFor(fx(EffectKind.DOT, "12", 0, total = 10)).remainingFraction, 0.001f)
        assertEquals(0f, badgeFor(fx(EffectKind.DOT, "12", 3, total = 0)).remainingFraction, 0.001f)
    }

    @Test fun oneBadgePerInstance() {
        val badges = badgesFor(listOf(fx(EffectKind.DOT, "12", 5), fx(EffectKind.DOT, "121", 7), fx(EffectKind.DOT, "13", 9)), tick = 3)
        assertEquals(listOf("SP1", "SP2", "LC1"), badges.map { it.code })
    }

    @Test fun debuffsFirstThenBuffsEachByRemainingAscending() {
        val badges = badgesFor(
            listOf(
                fx(EffectKind.FURY, "11", 20),
                fx(EffectKind.DOT, "121", 9),
                fx(EffectKind.COT, "21", 4),
                fx(EffectKind.DAZED, "", 3),
                fx(EffectKind.DOT, "12", 6),
            ),
            tick = 3,
        )
        assertEquals(listOf("DZ", "SP1", "SP2", "CU1", "FU"), badges.map { it.code })
    }

    @Test fun pendingBuffsAreHiddenButOverTimeEffectsShown() {
        val badges = badgesFor(
            listOf(fx(EffectKind.FURY, "11", 30, from = 4), fx(EffectKind.DOT, "12", 10, from = 4), fx(EffectKind.GUARD, "22", 30, from = 3)),
            tick = 3,
        )
        assertEquals(listOf("SP1", "GU"), badges.map { it.code })
    }

    @Test fun summaryBadgeOnlyWithSeveralDazeChances() {
        val one = badgesFor(listOf(fx(EffectKind.DAZE_CHANCE, "31", 5, magnitude = 5)), tick = 3, dazeChancePct = 5)
        assertEquals(listOf("AT1"), one.map { it.code })
        val two = badgesFor(
            listOf(fx(EffectKind.DAZE_CHANCE, "31", 5, magnitude = 5), fx(EffectKind.DAZE_CHANCE, "3131", 8, magnitude = 10)),
            tick = 3, dazeChancePct = 15,
        )
        assertEquals(listOf("AT 15%", "AT1", "AT3"), two.map { it.code })
    }

    private fun look(kind: EffectKind, combo: String) = badgeFor(fx(kind, combo, 5))

    private fun contrast(a: Color, b: Color): Float {
        val (hi, lo) = listOf(a.luminance(), b.luminance()).sortedDescending()
        return (hi + 0.05f) / (lo + 0.05f)
    }

    @Test fun tieredFamiliesArePillsToned() {
        val cases = mapOf(
            EffectKind.DOT to listOf("12", "121", "1212", "12121"),
            EffectKind.COT to listOf("21", "212", "2121", "21212"),
        )
        cases.forEach { (kind, seqs) -> seqs.forEach { assertEquals(BadgeShape.PILL, look(kind, it).shape) } }
        assertEquals(
            listOf(BadgeColors.SpearA1, BadgeColors.SpearA2, BadgeColors.SpearA3, BadgeColors.SpearA4),
            listOf("12", "121", "1212", "12121").map { look(EffectKind.DOT, it).color },
        )
        assertEquals(BadgeColors.SpearB2, look(EffectKind.DOT, "131").color)
        assertEquals(BadgeColors.Daze3, look(EffectKind.DAZE_CHANCE, "3131").color)
        assertEquals(BadgeColors.Area4, look(EffectKind.DOT, "32323").color)
        assertEquals(BadgeColors.Heal1, look(EffectKind.COT, "21").color)
    }

    @Test fun tierTonesGoFromLightToDarkAndAreAllDistinct() {
        val families = listOf(ComboFamily.SPEAR_A, ComboFamily.SPEAR_B, ComboFamily.DAZE, ComboFamily.AREA, ComboFamily.HEAL)
        families.forEach { family ->
            val tones = tierTones(family)
            assertEquals(4, tones.size)
            val lum = tones.map { it.luminance() }
            assertTrue("$family light to dark: $lum", lum.zipWithNext().all { (a, b) -> a > b })
        }
        val all = families.flatMap(::tierTones)
        assertEquals(all.size, all.distinct().size)
    }

    @Test fun shieldsMultipliersDrainAndDazedHaveTheirOwnShapes() {
        assertEquals(BadgeShape.SHIELD to BadgeColors.ShieldMaster, look(EffectKind.SHIELD_MASTER, "2132").let { it.shape to it.color })
        assertEquals(BadgeShape.SHIELD to BadgeColors.ShieldTactics, look(EffectKind.SHIELD_TACTICS, "2131").let { it.shape to it.color })
        assertEquals(BadgeShape.DIAMOND to BadgeColors.Fury, look(EffectKind.FURY, "11").let { it.shape to it.color })
        assertEquals(BadgeShape.DIAMOND to BadgeColors.Guard, look(EffectKind.GUARD, "22").let { it.shape to it.color })
        assertEquals(BadgeShape.DIAMOND to BadgeColors.Vigor, look(EffectKind.VIGOR, "33").let { it.shape to it.color })
        assertEquals(BadgeShape.DROP to BadgeColors.Drain1, look(EffectKind.DRAIN, "3123").let { it.shape to it.color })
        assertEquals(BadgeShape.DROP to BadgeColors.Drain2, look(EffectKind.DRAIN, "31232").let { it.shape to it.color })
        assertTrue(BadgeColors.Drain1.luminance() > BadgeColors.Drain2.luminance())
        assertEquals(BadgeShape.STAR to BadgeColors.Dazed, look(EffectKind.DAZED, "").let { it.shape to it.color })
    }

    @Test fun summaryIsANeutralPill() {
        val two = badgesFor(
            listOf(fx(EffectKind.DAZE_CHANCE, "31", 5, magnitude = 5), fx(EffectKind.DAZE_CHANCE, "3131", 8, magnitude = 10)),
            tick = 3, dazeChancePct = 15,
        )
        assertEquals(BadgeShape.PILL, two[0].shape)
        assertEquals(BadgeColors.Neutral, two[0].color)
    }

    @Test fun textContrastsWithEveryBadgeColour() {
        val badges = Combos.all.mapNotNull(::comboBadge) + look(EffectKind.DAZED, "") +
            badgesFor(listOf(fx(EffectKind.DAZE_CHANCE, "31", 5), fx(EffectKind.DAZE_CHANCE, "313", 5)), tick = 0, dazeChancePct = 10).take(1)
        badges.forEach { assertTrue("${it.code}: ${contrast(it.color, it.contentColor)}", contrast(it.color, it.contentColor) >= 4.5f) }
    }

    @Test fun comboBadgeShowsTheEffectAComboLeaves() {
        assertEquals("SP1" to BadgeShape.PILL, comboBadge(Combos.find("12")!!)!!.let { it.code to it.shape })
        assertEquals("EM" to BadgeShape.SHIELD, comboBadge(Combos.find("2132")!!)!!.let { it.code to it.shape })
        assertEquals("BR" to BadgeShape.DIAMOND, comboBadge(Combos.find("33")!!)!!.let { it.code to it.shape })
        assertEquals("RV2" to BadgeShape.DROP, comboBadge(Combos.find("31232")!!)!!.let { it.code to it.shape })
        assertNull(comboBadge(Combos.find("123")!!))
        assertNull(comboBadge(Combos.find("321")!!))
    }

    @Test fun tierFromCode() {
        assertEquals(3, badgeTier("SP3"))
        assertEquals(1, badgeTier("CU1"))
        assertEquals(0, badgeTier("EM"))
        assertEquals(2, badgeTier("RV2"))
    }

    @Test fun rimColorsByTierAndDebuff() {
        val silver = Color(0xFFC8CCD4) to Color(0xFF6A6E78)
        val gold = Color(0xFFF4D98A) to Color(0xFF8A6420)
        (0..2).forEach { assertEquals("tier $it", silver, rimColors(it, debuff = false)) }
        (3..4).forEach { assertEquals("tier $it", gold, rimColors(it, debuff = false)) }
        (0..4).forEach { tier ->
            val (light, dark) = rimColors(tier, debuff = true)
            assertNotEquals(silver, light to dark)
            assertNotEquals(gold, light to dark)
            listOf(light, dark).forEach { assertTrue("reddish: $it", it.red > it.green && it.red > it.blue) }
            assertTrue(light.luminance() > dark.luminance())
        }
    }

    @Test fun ringIsProportionalToRemainingFraction() {
        assertEquals(50f, ringLength(100f, 0.5f), 0.001f)
        assertEquals(25f, ringLength(100f, 0.25f), 0.001f)
        assertEquals(100f, ringLength(100f, 1.5f), 0.001f)
        assertEquals(0f, ringLength(100f, SUMMARY_FRACTION), 0.001f)
    }

    @Test fun popGoesFromZeroUpTo1_2AndBackTo1In300ms() {
        assertEquals(0f, popScale(0), 0.001f)
        assertEquals(1.2f, (0L..300L).maxOf { popScale(it) }, 0.001f)
        assertEquals(1f, popScale(300), 0.001f)
        assertEquals(1f, popScale(5_000), 0.001f)
    }

    @Test fun heartbeatGoesUpTo1_12AndBackIn200ms() {
        assertEquals(1f, beatScale(0), 0.001f)
        assertEquals(1.12f, (0L..200L).maxOf { beatScale(it) }, 0.001f)
        assertEquals(1f, beatScale(200), 0.001f)
        assertEquals(1f, beatScale(-1), 0.001f)
    }

    @Test fun dazeCountdownPulsesUpTo1_15AndBackIn250ms() {
        assertEquals(1f, countdownPulseScale(0), 0.001f)
        assertEquals(1.15f, countdownPulseScale(125), 0.001f)
        assertEquals(1f, countdownPulseScale(250), 0.001f)
        assertEquals(1f, countdownPulseScale(-1), 0.001f)
    }

    @Test fun blinkOnlyWhenLittleIsLeft() {
        val low = (0L..2_000L step 10).map { blinkAlpha(it, 0.25f) }
        assertEquals(0.6f, low.min(), 0.01f)
        assertEquals(1f, low.max(), 0.01f)
        assertTrue((0L..2_000L step 10).all { blinkAlpha(it, 0.26f) == 1f })
        assertTrue((0L..2_000L step 10).all { blinkAlpha(it, SUMMARY_FRACTION) == 1f })
    }

    @Test fun dazedStarTurnsOnceEvery4s() {
        assertEquals(0f, starRotation(0), 0.001f)
        assertEquals(90f, starRotation(1_000), 0.001f)
        assertEquals(0f, starRotation(4_000), 0.001f)
    }

    @Test fun shineSweepsOnAppearingAndEvery3s() {
        assertEquals(0f, shineProgress(0), 0.001f)
        assertTrue(shineProgress(1_500) < 0f)
        assertEquals(0f, shineProgress(3_000), 0.001f)
        assertTrue(shineProgress(3_300) in 0f..1f)
    }
}
