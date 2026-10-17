package com.wardengambits.engine.story

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.story.content.StoryContentLoader
import java.io.File
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Empirical check (2026-10-13) that EVERY rival of EVERY encounter in the real content attacks and hurts the player.
 *
 * Each encounter node is fought for up to 30 ticks by a PASSIVE player (never casts), built with the real
 * [StoryEngine.combatSetupFor]. The player level is the sim's expected level: 1 + expected wins before the node
 * (same formula as `:sim` ExpectedLevels, replicated here because `:engine` cannot depend on `:sim`).
 *
 * Per rival: (a) at least one combo resolved; (b) negative EffectResolved on the player with source == rival within
 * 10 ticks (12 when everyNTicks >= 4); (c) never more than 8 consecutive ticks without a cast (or everyNTicks if
 * larger). The table goes to `build/reports/rival-damage.txt`.
 */
class EveryRivalDealsDamageTest {
    private val content = StoryContentLoader.loadFromResources()
    private val engine = StoryEngine(content)

    private val paths: Map<String, List<List<NodeId>>> =
        Route.treeZones.associate { it.id to paths(NodeId(it.id, 1, 0)) }

    private fun paths(from: NodeId): List<List<NodeId>> {
        if (from.level == TreeShape.LEVELS) return listOf(listOf(from))
        return TreeShape.destinations(from).flatMap { next -> paths(next).map { listOf(from) + it } }
    }

    private fun hasEncounter(id: NodeId) = content.node(id)?.encounter != null

    private fun expectedLevel(node: NodeId): Int {
        val previous = Route.treeZones.take(Route.indexOf(node.zoneId)).sumOf { z ->
            paths.getValue(z.id).map { p -> p.count(::hasEncounter) }.average()
        }
        val within = paths.getValue(node.zoneId).filter { node in it }
            .map { p -> p.count { it.level < node.level && hasEncounter(it) } }.average()
        return 1 + (previous + within).roundToInt()
    }

    private class Row(
        val name: String, val zone: String, val node: String, val firstDamage: Int?, val damageCasts: Int,
        val totalCasts: Int, val potions: Int, val maxIdle: Int, val everyN: Int, val maxTicks: Int,
    )

    private fun fight(node: NodeId): List<Row> {
        val level = expectedLevel(node)
        val run = RunState(level, 1, 1, level - 1, node, listOf(node), Phase.COMBAT_PENDING)
        val setup = requireNotNull(engine.combatSetupFor(StoryProfile(run, emptyList())))
        val combat = CombatEngine()
        var state = combat.start(setup, seed = 11L)
        val n = setup.rivals.size
        val firstDamage = arrayOfNulls<Int>(n)
        val damageCasts = IntArray(n); val totalCasts = IntArray(n); val potions = IntArray(n)
        val idle = IntArray(n); val maxIdle = IntArray(n)
        val damageCombos = Array(n) { mutableSetOf<String>() }
        var ticks = 0
        while (state.outcome == null && state.tick < 30) {
            val step = combat.reduce(state, CombatAction.Tick)
            state = step.state
            ticks = state.tick
            val casted = BooleanArray(n)
            for (e in step.events) when (e) {
                is CombatEvent.ComboResolved -> if (e.fighter != PLAYER_ID) {
                    val i = e.fighter.value - 1; totalCasts[i]++; casted[i] = true
                }
                is CombatEvent.PotionUsed -> potions[e.fighter.value - 1]++
                is CombatEvent.EffectResolved -> if (e.target == PLAYER_ID && e.source != PLAYER_ID && e.amount < 0) {
                    val i = e.source.value - 1
                    if (firstDamage[i] == null) firstDamage[i] = state.tick
                    damageCombos[i] += e.sourceCombo
                }
                else -> {}
            }
            for (i in 0 until n) { idle[i] = if (casted[i]) 0 else idle[i] + 1; maxIdle[i] = maxOf(maxIdle[i], idle[i]) }
        }
        // Damage casts: casts whose combo code ever produced damage on the player.
        val rerun = combat.start(setup, seed = 11L)
        var s = rerun
        while (s.outcome == null && s.tick < 30) {
            val step = combat.reduce(s, CombatAction.Tick); s = step.state
            step.events.filterIsInstance<CombatEvent.ComboResolved>().filter { it.fighter != PLAYER_ID }.forEach {
                if (it.combo.sequence in damageCombos[it.fighter.value - 1]) damageCasts[it.fighter.value - 1]++
            }
        }
        val rivals = requireNotNull(content.node(node)?.encounter).rivals
        return rivals.mapIndexed { i, r ->
            Row(r.name, node.zoneId, node.key, firstDamage[i], damageCasts[i], totalCasts[i], potions[i], maxIdle[i],
                r.everyNTicks, ticks)
        }
    }

    @Test
    fun everyRivalAttacksAndDealsDamage() {
        val rows = Route.treeZones.flatMap { z ->
            content.zone(z.id)!!.nodes.filter { it.encounter != null }
                .flatMap { fight(NodeId(z.id, it.level, it.index)) }
        }
        val lines = mutableListOf("name | zone | node | firstDmgTick | dmgCasts/totalCasts | potions | maxIdle | everyN")
        rows.groupBy { it.zone to it.name }.forEach { (_, g) ->
            val r = g.first()
            val fd = g.mapNotNull { it.firstDamage }.maxOrNull()
            lines += "${r.name} | ${r.zone} | ${g.size} nodes | ${fd ?: "NEVER"} (worst) | " +
                "${g.sumOf { it.damageCasts }}/${g.sumOf { it.totalCasts }} | ${g.sumOf { it.potions }} | " +
                "${g.maxOf { it.maxIdle }} | ${r.everyN}"
        }
        File("build/reports").apply { mkdirs() }.resolve("rival-damage.txt").writeText(lines.joinToString("\n"))

        val failures = rows.flatMap { r ->
            val limit = if (r.everyN >= 4) 12 else 10
            val dead = r.maxTicks < limit // fight ended early (player died): damage window not fully observable
            buildList {
                if (r.totalCasts < 1) add("${r.node} ${r.name}: no combo in ${r.maxTicks} ticks")
                val fd = r.firstDamage
                if (fd == null || fd > limit) add("${r.node} ${r.name}: first damage tick $fd > $limit")
                if (!dead && r.maxIdle > maxOf(8, r.everyN)) add("${r.node} ${r.name}: idle ${r.maxIdle} ticks")
            }
        }
        assertTrue(failures.isEmpty(), "rivals failing:\n" + failures.joinToString("\n"))
    }
}
