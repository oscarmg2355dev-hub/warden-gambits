package com.wardengambits.app.ui.combat

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composer
import androidx.compose.runtime.CompositionTracer
import androidx.compose.runtime.InternalComposeTracingApi
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wardengambits.app.ui.common.EffectBadgeUi
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.PLAYER_ID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.random.Random

/** No-break space between a stat label and its number. */
private const val NBSP = ' '

/** Rival and player cards in style C (spec §3). */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es-w360dp-h640dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CombatCardsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val rival = FighterUi(
        FighterId(1), "Orco capitán", 36, 8000, 9000, 150, 300, emptyList(), isAlive = true, isTarget = true,
        attack = 4100, defense = 1200, portrait = "orc",
    )

    private val player = FighterUi(
        PLAYER_ID, "Warden", 34, 30000, 34000, 200, 300, emptyList(), isAlive = true, isTarget = false,
        attack = 3400, defense = 900, heal = 2100,
    )

    /** Decision of the user (2026-10-18): a boss portrait wears a red diagonal «ELITE» ribbon in its top-right corner. */
    @Test fun bossPortraitWearsAnEliteRibbon() {
        showRival(rival.copy(isBoss = true))
        composeRule.onNodeWithText(ELITE_LABEL).assertExists()
        composeRule.onNodeWithTag("elite_ribbon", useUnmergedTree = true).assertExists()
    }

    @Test fun commonRivalHasNoEliteRibbon() {
        showRival()
        composeRule.onNodeWithText(ELITE_LABEL).assertDoesNotExist()
    }

    /** A rival card the size of one cell of the 2×2 grid on a 360 dp wide phone. */
    private fun showRival(
        fighter: FighterUi = rival,
        numbers: List<FloatingNumber> = emptyList(),
        now: Long = 0L,
        onSelect: () -> Unit = {},
        height: Dp = 146.dp,
    ) = composeRule.setContent {
        WardenTheme {
            Box(Modifier.size(169.dp, height)) {
                RivalCard(fighter, numbers, { now }, running = false, onSelect = onSelect, modifier = Modifier.fillMaxSize().testTag("card"))
            }
        }
    }

    @Test fun rivalCardShowsNameLevelBarsAndPortrait() {
        showRival()
        composeRule.onNodeWithText("Orco capitán").assertExists()
        composeRule.onNodeWithText("Nv${NBSP}36 · ATQ${NBSP}4100 · DEF${NBSP}1200").assertExists()
        composeRule.onNodeWithText("8000/9000").assertExists()
        composeRule.onNodeWithText("150/300").assertExists()
        composeRule.onAllNodesWithContentDescription("Retrato").assertCountEquals(1)
    }

    @Test fun deadRivalShowsDefeated() {
        showRival(rival.copy(hp = 0, power = 0, isAlive = false))
        composeRule.onNodeWithText("Derrotado").assertExists()
        composeRule.onAllNodesWithContentDescription("Retrato").assertCountEquals(1)
    }

    /** Pixels of the card inside the bounds of [text] whose colour is the gold of the names. */
    private fun goldPixelsIn(text: String): Int {
        val name = composeRule.onNode(hasText(text), useUnmergedTree = true).unclippedBounds()
        // Software draw of the whole content (Robolectric native graphics); compose root coordinates start at 0,0.
        val bitmap = composeRule.runOnIdle {
            val root = composeRule.activity.findViewById<View>(android.R.id.content)
            Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888).also { root.draw(Canvas(it)) }
        }
        var gold = 0
        for (x in name.left.toInt() until name.right.toInt().coerceAtMost(bitmap.width)) {
            for (y in name.top.toInt() until name.bottom.toInt().coerceAtMost(bitmap.height)) {
                val c = bitmap.getPixel(x, y)
                val red = (c shr 16) and 0xFF
                val blue = c and 0xFF
                if (red > 150 && red - blue > 60) gold++
            }
        }
        return gold
    }

    @Test fun aliveRivalNameIsGold() {
        showRival()
        assertTrue(goldPixelsIn("Orco capitán") > 0)
    }

    /** A defeated card is desaturated (not covered by a flat block): its name keeps its shape but loses its gold. */
    @Test fun deadRivalCardIsDesaturated() {
        showRival(rival.copy(hp = 0, power = 0, isAlive = false))
        assertEquals(0, goldPixelsIn("Orco capitán"))
    }

    @Test fun tappingRivalSelectsIt() {
        var selected = 0
        showRival(onSelect = { selected++ })
        composeRule.onNodeWithTag("card").performClick()
        assertEquals(1, selected)
    }

    @Test fun playerCardShowsStats() {
        composeRule.setContent {
            WardenTheme {
                Box(Modifier.size(344.dp, 149.dp)) {
                    PlayerCard(player, emptyList(), { 0L }, running = false, modifier = Modifier.fillMaxSize())
                }
            }
        }
        composeRule.onNodeWithText("Warden${NBSP}·${NBSP}Nv${NBSP}34").assertExists()
        composeRule.onNodeWithText("ATQ${NBSP}3400 · CUR${NBSP}2100 · DEF${NBSP}900").assertExists()
        composeRule.onNodeWithText("30000/34000").assertExists()
        composeRule.onAllNodesWithContentDescription("Retrato").assertCountEquals(1)
    }

    @Test fun floatingNumbersAreDrawnOverTheCard() {
        val number = FloatingNumber(1, rival.id, "−420 SP2", CombatTextTone.DAMAGE, 20f, false, cx = 0.3f, cy = 0.5f, startMillis = 0L)
        showRival(numbers = listOf(number), now = 100L)
        composeRule.onNode(hasText("−420 SP2") and hasAnyAncestor(hasTestTag("card")), useUnmergedTree = true).assertExists()
    }

    /** Six numbers at full size (half their life: scale 1, no rise yet) spread over the 3×2 grid of the card. */
    private fun sixNumbers(target: FighterId = rival.id): List<FloatingNumber> {
        val state = FloatingNumbersState().add(
            List(6) { CombatTextLine(target, "−${1840 + it} SP2", CombatTextTone.DAMAGE, -(1840 + it)) },
            { 9000 }, 0L, Random(3), gridOf = { 3 to 2 },
        )
        return state.forTarget(target)
    }

    private fun assertNumbersBetween(numbers: List<FloatingNumber>, top: Float, card: Rect) {
        numbers.forEach { n ->
            val shown = composeRule.onNode(hasText(n.text), useUnmergedTree = true).unclippedBounds()
            assertTrue("'${n.text}' at $shown must be below $top", shown.top >= top - 0.5f)
            assertTrue("'${n.text}' at $shown must be inside $card", shown.bottom <= card.bottom + 0.5f &&
                shown.left >= card.left - 0.5f && shown.right <= card.right + 0.5f)
        }
    }

    private val twoBadges = listOf(EffectBadgeUi("SP1", true, 0.5f), EffectBadgeUi("CU2", false, 0.8f))

    /** A card tall enough for a stage of at least 40 dp below its row of icons. */
    @Test fun numbersStayInsideStage() {
        val numbers = sixNumbers()
        showRival(rival.copy(badges = twoBadges), numbers = numbers, now = FLOATING_NUMBER_LIFE_MILLIS / 2, height = 220.dp)
        composeRule.waitForIdle()
        val iconsBottom = composeRule.onAllNodes(hasTestTag("badge_pill"), useUnmergedTree = true)
            .fetchSemanticsNodes().maxOf { it.boundsInRoot.bottom }
        assertNumbersBetween(numbers, iconsBottom, composeRule.onNodeWithTag("card").unclippedBounds())
    }

    /** The icons sit right below the power bar (not centred in the space left), so a tall card has a big stage. */
    @Test fun effectIconsSitBelowThePowerBar() {
        showRival(rival.copy(badges = twoBadges), height = 220.dp)
        composeRule.waitForIdle()
        val density = composeRule.density.density
        val powerBottom = composeRule.onAllNodes(hasTestTag("gameBar"), useUnmergedTree = true)
            .fetchSemanticsNodes().maxOf { it.boundsInRoot.bottom }
        val badges = composeRule.onAllNodes(hasTestTagPrefix("badge_"), useUnmergedTree = true).fetchSemanticsNodes()
        val badgesTop = badges.minOf { it.boundsInRoot.top }
        val badgesBottom = badges.maxOf { it.boundsInRoot.bottom }
        assertTrue("badges at $badgesTop, power bar ends at $powerBottom", badgesTop - powerBottom <= (3 + 1) * density)
        val card = composeRule.onNodeWithTag("card").unclippedBounds()
        // Stage: from 4 dp below the icons to 4 dp above the card's bottom.
        val stage = (card.bottom - 4 * density) - (badgesBottom + 4 * density)
        assertTrue("stage of ${stage / density} dp", stage >= 40 * density)
    }

    /** Six numbers on a tall rival card, each in its own cell of the 3×2 grid, never overlap. */
    @Test fun sixNumbersDoNotOverlap() {
        val numbers = sixNumbers()
        showRival(rival.copy(badges = twoBadges), numbers = numbers, now = FLOATING_NUMBER_LIFE_MILLIS / 2, height = 220.dp)
        composeRule.waitForIdle()
        val bounds = numbers.map { composeRule.onNode(hasText(it.text), useUnmergedTree = true).unclippedBounds() }
        bounds.forEachIndexed { i, a ->
            bounds.drop(i + 1).forEach { b ->
                val overlap = a.left < b.right - 0.5f && b.left < a.right - 0.5f && a.top < b.bottom - 0.5f && b.top < a.bottom - 0.5f
                assertTrue("$a overlaps $b", !overlap)
            }
        }
    }

    private fun hasTestTagPrefix(prefix: String) = SemanticsMatcher("testTag starts with $prefix") {
        it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(prefix) == true
    }

    /** Below the icons of a 146 dp card there are less than 40 dp: the numbers use the lower half of the card. */
    @Test fun shortStageUsesTheLowerHalfOfTheCard() {
        val numbers = sixNumbers()
        showRival(rival.copy(badges = twoBadges), numbers = numbers, now = FLOATING_NUMBER_LIFE_MILLIS / 2)
        composeRule.waitForIdle()
        val card = composeRule.onNodeWithTag("card").unclippedBounds()
        assertNumbersBetween(numbers, card.top + card.height / 2f, card)
    }

    /** Without icons the stage starts below the bars (on a card tall enough not to fall back to its lower half). */
    @Test fun noBadgesStageStartsBelowBars() {
        val numbers = sixNumbers()
        showRival(numbers = numbers, now = FLOATING_NUMBER_LIFE_MILLIS / 2, height = 220.dp)
        composeRule.waitForIdle()
        val powerBottom = composeRule.onNode(hasText("150/300"), useUnmergedTree = true).unclippedBounds().bottom
        assertNumbersBetween(numbers, powerBottom, composeRule.onNodeWithTag("card").unclippedBounds())
    }

    @OptIn(InternalComposeTracingApi::class)
    @Test fun clockDoesNotRecomposeCards() {
        val clock = mutableLongStateOf(100L)
        val numbers = sixNumbers()
        var recompositions = 0
        // Counts the (re)compositions of the card and of its numbers layer through the compiler's trace markers.
        Composer.setTracer(object : CompositionTracer {
            override fun isTraceInProgress() = true
            override fun traceEventStart(key: Int, dirty1: Int, dirty2: Int, info: String) {
                if (info.contains("RivalCard (") || info.contains("FloatingNumbersLayer (")) recompositions++
            }
            override fun traceEventEnd() = Unit
        })
        try {
            composeRule.setContent {
                WardenTheme {
                    Box(Modifier.size(169.dp, 146.dp)) {
                        RivalCard(rival, numbers, { clock.longValue }, running = false, onSelect = {}, modifier = Modifier.fillMaxSize())
                    }
                }
            }
            composeRule.waitForIdle()
            val before = recompositions
            assertTrue("the tracer must see the first composition", before > 0)
            repeat(10) {
                composeRule.runOnIdle { clock.longValue += 50L }
                composeRule.waitForIdle()
            }
            assertEquals(before, recompositions)
        } finally {
            Composer.setTracer(null)
        }
    }

    private fun SemanticsNodeInteraction.unclippedBounds(): Rect {
        val coordinates = fetchSemanticsNode().layoutInfo.coordinates
        return coordinates.findRootCoordinates().localBoundingBoxOf(coordinates, clipBounds = false)
    }

    /** The widest late-game number at 36 sp, at its 1.25 peak and placed at the far right, still fits the card. */
    @Test fun wideNumberFitsTheCardAtItsPeak() {
        val texts = listOf("−18400 SP2", LateGameNumbers.rivalHitText)
        val numbers = texts.mapIndexed { i, text ->
            FloatingNumber(i.toLong(), rival.id, text, CombatTextTone.DAMAGE, FLOATING_NUMBER_MAX_SP, true, cx = 0.65f, cy = 0.80f, startMillis = 0L)
        }
        // 15 % of the life: the peak of the pop (scale 1.25)
        showRival(numbers = numbers, now = (FLOATING_NUMBER_LIFE_MILLIS * 0.15f).toLong())
        texts.forEach { text ->
            val card = composeRule.onNodeWithTag("card").unclippedBounds()
            val shown = composeRule.onNode(hasText(text), useUnmergedTree = true).unclippedBounds()
            val inside = shown.left >= card.left - 0.5f && shown.right <= card.right + 0.5f &&
                shown.top >= card.top - 0.5f && shown.bottom <= card.bottom + 0.5f
            assertTrue("'$text' at $shown must fit the card $card", inside)
        }
    }

    /** At the end of its rise the number is still inside the card. */
    @Test fun risingNumberStaysInsideTheCard() {
        val number = FloatingNumber(1, rival.id, "−18400 SP2", CombatTextTone.DAMAGE, FLOATING_NUMBER_MAX_SP, false, cx = 0.05f, cy = 0.15f, startMillis = 0L)
        showRival(numbers = listOf(number), now = FLOATING_NUMBER_LIFE_MILLIS - 10)
        val card = composeRule.onNodeWithTag("card").unclippedBounds()
        val shown = composeRule.onNode(hasText("−18400 SP2"), useUnmergedTree = true).unclippedBounds()
        assertTrue("$shown inside $card", shown.top >= card.top - 0.5f && shown.left >= card.left - 0.5f)
    }
}
