package com.wardengambits.engine.story

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.story.content.StoryContentLoader
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Regression of the bug report from a real fight (2026-10-13): eregion_3_0, two Crebain Lv 20 against a passive
 * Warden Lv 18. The rivals must hurt the player (DOT EffectResolved with negative amounts, HP going down) and never
 * stall for lack of power. The tick-by-tick trace is written to `build/reports/trace/eregion_3_0.txt`.
 */
class EregionCrebainTraceTest {
    private val content = StoryContentLoader.loadFromResources()

    private fun setup(): CombatSetup {
        val zone = requireNotNull(content.zone("eregion"))
        val rivals = requireNotNull(content.node(NodeId("eregion", 3, 0))?.encounter).rivals
        return CombatSetup(
            FighterSpec("Warden", 18),
            rivals.map { RivalSpec(it.name, zone.baseLevel + it.levelOffset, PatternBrain(it.pattern, it.everyNTicks)) },
        )
    }

    private data class Trace(val lines: List<String>, val events: List<Pair<Int, CombatEvent>>, val hpByTick: List<Int>)

    private fun run(ticks: Int): Trace {
        val engine = CombatEngine()
        var state = engine.start(setup(), seed = 7L)
        val lines = mutableListOf<String>()
        val events = mutableListOf<Pair<Int, CombatEvent>>()
        val hp = mutableListOf(state.player.hp)
        lines += "rivals: " + state.rivals.joinToString { "${it.name} L${it.level} hp=${it.hp} pow=${it.power}" } +
            " | player L${state.player.level} hp=${state.player.hp}"
        while (state.outcome == null && state.tick < ticks) {
            val step = engine.reduce(state, CombatAction.Tick)
            state = step.state
            step.events.forEach { events += state.tick to it }
            hp += state.player.hp
            val p = state.player
            val toPlayer = step.events.filterIsInstance<CombatEvent.EffectResolved>().filter { it.target == PLAYER_ID }
            val rivalActs = step.events.filter {
                (it is CombatEvent.ComboResolved && it.fighter != PLAYER_ID) ||
                    (it is CombatEvent.ComboRejected && it.fighter != PLAYER_ID) ||
                    it::class.simpleName == "PotionUsed"
            }
            lines += "t=${state.tick} hp=${p.hp} dazed=${p.isDazed()} chance=${p.dazeChancePct(state.tick)}% " +
                "effects=${p.effects.map { "${it.kind}:${it.sourceCombo}" }} " +
                "rivalPow=${state.rivals.map { it.power }} acts=$rivalActs toPlayer=${toPlayer.map { "${it.amount} ${it.kind} ${it.sourceCombo}" }}"
        }
        File("build/reports/trace").apply { mkdirs() }.resolve("eregion_3_0.txt").writeText(lines.joinToString("\n"))
        return Trace(lines, events, hp)
    }

    @Test
    fun crebainHurtThePlayerAndNeverStall() {
        val trace = run(ticks = 260)
        val dots = trace.events.map { it.second }.filterIsInstance<CombatEvent.EffectResolved>()
            .filter { it.target == PLAYER_ID && it.kind == EffectKind.DOT }
        assertTrue(dots.isNotEmpty() && dots.all { it.amount < 0 }, "rival DOTs must land on the player: ${trace.lines.take(12)}")
        assertTrue(trace.hpByTick.last() < trace.hpByTick.first(), "player HP must drop")
        val ticks = trace.hpByTick.size - 1
        val casts = trace.events.map { it.second }.filterIsInstance<CombatEvent.ComboResolved>()
            .filter { it.fighter != PLAYER_ID }.groupingBy { it.fighter }.eachCount()
        // everyNTicks 2: one cast every 2 ticks, never skipped for lack of power.
        assertTrue(casts.size == 2 && casts.values.all { it >= ticks / 2 - 1 }, "rivals must cast on every turn: $casts in $ticks ticks")
    }
}
