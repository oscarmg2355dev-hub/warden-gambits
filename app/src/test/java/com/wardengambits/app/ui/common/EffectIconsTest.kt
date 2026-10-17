package com.wardengambits.app.ui.common

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.unit.dp
import com.wardengambits.app.ui.theme.BadgeColors
import com.wardengambits.app.ui.theme.WardenTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EffectIconsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val all = (1..10).map { EffectBadgeUi("SP$it", it % 2 == 0, it / 10f) }

    private fun shown(): Int = all.count { badge ->
        composeRule.onAllNodesWithText(badge.code).fetchSemanticsNodes().isNotEmpty()
    }

    @Test fun overflowShowsPlusN() {
        composeRule.setContent {
            WardenTheme { Box(Modifier.size(100.dp, 30.dp)) { EffectIcons(all, modifier = Modifier.fillMaxSize()) } }
        }
        val visible = shown()
        val hidden = all.size - visible
        assertTrue("expected some badges hidden, visible=$visible", hidden > 0)
        composeRule.onAllNodesWithText("+$hidden").assertCountEquals(1)
    }

    @Test fun noPlusWhenEverythingFits() {
        composeRule.setContent {
            WardenTheme { Box(Modifier.size(400.dp, 200.dp)) { EffectIcons(all, modifier = Modifier.fillMaxSize()) } }
        }
        assertEquals(all.size, shown())
        composeRule.onAllNodes(hasText("+", substring = true)).assertCountEquals(0)
    }

    @Test fun marginIsBudgetedPerBadge() {
        val widths = listOf(10, 10, 10)
        // 10 + 1 + 10 + 1 + 10 = 32: one row without margin
        assertEquals(3, visibleBadgeCount(widths, { 5 }, maxWidth = 32, lines = 1, gap = 1))
        // with 2 px margin each badge is 12 and "+N" is 7: 12+1+12+1+12 = 38 and 12+1+12+1+7 = 33 overflow,
        // so only one badge plus "+2" (12+1+7 = 20) fits
        assertEquals(1, visibleBadgeCount(widths, { 5 }, maxWidth = 32, lines = 1, gap = 1, margin = 2))
    }

    @Test fun noRowMeansNothingShown() {
        assertEquals(0, visibleBadgeCount(listOf(10), { 5 }, maxWidth = 100, lines = 0, gap = 1))
    }

    @Test fun areaShorterThanOneRowDrawsNothing() {
        composeRule.setContent {
            WardenTheme { Box(Modifier.size(140.dp, 4.dp)) { EffectIcons(all, modifier = Modifier.fillMaxSize()) } }
        }
        assertEquals(0, shown())
        composeRule.onAllNodes(hasText("+", substring = true)).assertCountEquals(0)
    }

    @Test fun badgesWithBarsShareARowInASmallArea() {
        val three = listOf(EffectBadgeUi("SP1", true, 0.5f), EffectBadgeUi("SP2", true, 0.5f), EffectBadgeUi("FU", false, 0.5f))
        composeRule.setContent {
            WardenTheme { Box(Modifier.size(200.dp, 60.dp)) { EffectIcons(three, modifier = Modifier.fillMaxSize()) } }
        }
        val tops = three.map { composeRule.onAllNodesWithText(it.code).onFirst().getUnclippedBoundsInRoot().top }
        assertEquals("all badges in one row: $tops", 1, tops.distinct().size)
    }

    @Test fun plusNIsInsideTheAreaWhenManyBadges() {
        val many = (1..40).map { EffectBadgeUi("SP$it", it % 2 == 0, 0.5f) }
        composeRule.setContent {
            WardenTheme { Box(Modifier.size(200.dp, 60.dp)) { EffectIcons(many, modifier = Modifier.fillMaxSize()) } }
        }
        val hidden = many.size - many.count { b -> composeRule.onAllNodesWithText(b.code).fetchSemanticsNodes().isNotEmpty() }
        assertTrue("expected hidden badges", hidden > 0)
        val b = composeRule.onAllNodesWithText("+$hidden").onFirst().getUnclippedBoundsInRoot()
        assertTrue("+N inside area: $b", b.right.value <= 200f && b.bottom.value <= 60f && b.top.value >= 0f)
    }

    private val shaped = listOf(
        EffectBadgeUi("SP2", true, 0.5f, BadgeShape.PILL, BadgeColors.SpearA2, BadgeColors.DarkText),
        EffectBadgeUi("EM", false, 0.5f, BadgeShape.SHIELD, BadgeColors.ShieldMaster, BadgeColors.DarkText),
        EffectBadgeUi("FU", false, 0.5f, BadgeShape.DIAMOND, BadgeColors.Fury, BadgeColors.DarkText),
        EffectBadgeUi("RV1", false, 0.5f, BadgeShape.DROP, BadgeColors.Drain1, BadgeColors.DarkText),
        EffectBadgeUi("DZ", true, 0.5f, BadgeShape.STAR, BadgeColors.Dazed, BadgeColors.DarkText),
    )

    @Test fun everyShapeComposesWithItsCode() {
        composeRule.setContent {
            WardenTheme { Box(Modifier.size(400.dp, 200.dp)) { EffectIcons(shaped, modifier = Modifier.fillMaxSize()) } }
        }
        shaped.forEach { b ->
            composeRule.onAllNodesWithText(b.code).assertCountEquals(1)
            val tag = "badge_${b.shape.name.lowercase()}"
            composeRule.onAllNodes(androidx.compose.ui.test.hasTestTag(tag)).assertCountEquals(1)
            shapeWidth(b.shape)?.let { composeRule.onNodeWithTag(tag).assertWidthIsEqualTo(it) }
        }
    }

    @Test fun plusNIsRightWithMixedShapes() {
        val many = (1..30).map { shaped[it % shaped.size].copy(code = "${shaped[it % shaped.size].code.take(2)}$it") }
        composeRule.setContent {
            WardenTheme { Box(Modifier.size(200.dp, 60.dp)) { EffectIcons(many, modifier = Modifier.fillMaxSize()) } }
        }
        val hidden = many.size - many.count { b -> composeRule.onAllNodesWithText(b.code).fetchSemanticsNodes().isNotEmpty() }
        assertTrue("expected hidden badges", hidden > 0)
        val b = composeRule.onAllNodesWithText("+$hidden").onFirst().getUnclippedBoundsInRoot()
        assertTrue("+N inside area: $b", b.right.value <= 200f && b.bottom.value <= 60f && b.top.value >= 0f)
        many.filter { composeRule.onAllNodesWithText(it.code).fetchSemanticsNodes().isNotEmpty() }.forEach {
            val r = composeRule.onAllNodesWithText(it.code).onFirst().getUnclippedBoundsInRoot()
            assertTrue("${it.code} inside area: $r", r.right.value <= 200.5f && r.bottom.value <= 60.5f)
        }
    }

    @Test fun badgeShowsCodeAt26dp() {
        val sp2 = EffectBadgeUi("SP2", true, 0.5f, BadgeShape.PILL, BadgeColors.SpearA2)
        composeRule.setContent {
            WardenTheme { Box(Modifier.size(200.dp, 60.dp)) { EffectIcons(listOf(sp2), modifier = Modifier.fillMaxSize()) } }
        }
        composeRule.onAllNodesWithText("SP2").assertCountEquals(1)
        composeRule.onNodeWithTag("badge_pill").assertHeightIsEqualTo(26.dp)
    }

    private val animated = listOf(
        EffectBadgeUi("SP2", true, 0.2f, BadgeShape.PILL, BadgeColors.SpearA2),
        EffectBadgeUi("DZ", true, 0.5f, BadgeShape.STAR, BadgeColors.Dazed),
    )

    private fun motions(): List<BadgeMotion> =
        composeRule.onAllNodes(SemanticsMatcher.keyIsDefined(BadgeMotionKey)).fetchSemanticsNodes().map { it.config[BadgeMotionKey]() }

    private fun motionsOverOneSecond(running: Boolean): Pair<List<BadgeMotion>, List<BadgeMotion>> {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            WardenTheme { Box(Modifier.size(200.dp, 60.dp)) { EffectIcons(animated, running, Modifier.fillMaxSize()) } }
        }
        composeRule.mainClock.advanceTimeBy(100)
        val before = motions()
        composeRule.mainClock.advanceTimeBy(1_000)
        return before to motions()
    }

    @Test fun pausedBadgesDoNotAnimate() {
        val (before, after) = motionsOverOneSecond(running = false)
        assertEquals(animated.size, before.size)
        assertEquals(before, after)
        before.forEach { assertEquals("settled while paused", 1f, it.scale, 0.001f) }
    }

    @Test fun runningBadgesAnimate() {
        val (before, after) = motionsOverOneSecond(running = true)
        assertEquals(animated.size, before.size)
        assertNotEquals(before[1].rotation, after[1].rotation)
    }
}
