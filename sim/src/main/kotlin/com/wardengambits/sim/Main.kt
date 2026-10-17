package com.wardengambits.sim

import com.wardengambits.engine.combo.ComboTuning
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.random.SeededRandom
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.Route
import com.wardengambits.engine.story.RunState
import com.wardengambits.engine.story.StoryAction
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.StoryProfile
import com.wardengambits.engine.story.TreeShape
import com.wardengambits.engine.story.content.StoryContent
import com.wardengambits.engine.story.content.StoryContentLoader
import java.io.File
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.Executors

const val FIGHTS_PER_ENCOUNTER = 300
const val ADVENTURES_PER_SKILL = 2000

/** An adventure that dies this many times is given up ("abandonada"). */
const val MAX_DEATHS_PER_ADVENTURE = 200

data class EncounterRow(
    val zone: String,
    val node: String,
    val rivals: String,
    val boss: Boolean,
    val expectedLevel: Int,
    val skill: Skill,
    val winPct: Double,
    val avgTicks: Double,
    val avgHpLeft: Double,
    val capped: Int,
    /** Average times the boss entered its defense per fight (boss-modes spec §2). */
    val avgDefenses: Double = 0.0,
)

data class AdventureStats(
    val skill: Skill,
    val runs: Int,
    val reached: Map<String, Int>,
    val completed: Int,
    val capped: Int,
    /** Total deaths per node key / zone id over all [runs]. */
    val deathsByNode: Map<String, Int>,
    val entryLevelSum: Map<String, Long>,
    val abandoned: Int,
    /** Runs that never lost a fight: the old "completed" metric (a defeat used to end the adventure). */
    val deathless: Int,
    val deaths: Int,
    val deathsByZone: Map<String, Int>,
    /** Abandoned runs counted by the node where they died the most (where they got stuck). */
    val abandonedAt: Map<String, Int> = emptyMap(),
)


class Simulator(private val content: StoryContent, private val engine: CombatEngine = CombatEngine()) {
    private val story = StoryEngine(content)
    private val levels = ExpectedLevels(content)

    fun encounterNodes(): List<NodeId> = Route.treeZones.flatMap { TreeShape.nodesOf(it.id) }.filter(levels::hasEncounter)

    /** [levelDelta] moves the player away from the expected level (robustness checks). */
    fun simulateEncounter(node: NodeId, skill: Skill, fights: Int = FIGHTS_PER_ENCOUNTER, levelDelta: Int = 0): EncounterRow {
        val level = maxOf(1, levels.expectedLevel(node) + levelDelta)
        val profile = StoryProfile(RunState(level, 1, 1, level - 1, node, listOf(node), Phase.COMBAT_PENDING), emptyList())
        val setup = requireNotNull(story.combatSetupFor(profile))
        val results = (0 until fights).map { i ->
            FightRunner.fight(setup, skill, seed = node.key.hashCode().toLong() * 1_000_003L + skill.ordinal * 100_000L + i, engine = engine)
        }
        return EncounterRow(
            zone = node.zoneId,
            node = node.key,
            rivals = setup.rivals.joinToString("+") { "${it.name} L${it.level}" },
            boss = node.level == TreeShape.LEVELS,
            expectedLevel = level,
            skill = skill,
            winPct = 100.0 * results.count { it.victory } / fights,
            avgTicks = results.map { it.ticks }.average(),
            avgHpLeft = results.map { it.hpLeft }.average(),
            capped = results.count { it.capped },
            avgDefenses = results.map { it.bossDefenses }.average(),
        )
    }

    /**
     * One full adventure: uniform random choices and the real story engine. A defeat sends the Warden back to the
     * previous node (the policy chooses again, so it may re-enter the same fight); [MAX_DEATHS_PER_ADVENTURE]
     * deaths abandon it.
     */
    private fun adventure(skill: Skill, seed: Long): AdventureRunSummary {
        val rng = SeededRandom(seed)
        var profile = story.newGame()
        val reached = mutableListOf(profile.run.node.zoneId)
        val entry = mutableMapOf(profile.run.node.zoneId to profile.run.level)
        var capped = 0
        val deaths = mutableMapOf<String, Int>()
        while (true) {
            val run = profile.run
            if (run.node.zoneId != reached.last()) {
                reached += run.node.zoneId
                entry[run.node.zoneId] = run.level
            }
            when (run.phase) {
                Phase.LAP_COMPLETE -> return AdventureRunSummary(reached, entry, true, false, deaths, capped)
                Phase.CHOOSING -> {
                    val options = story.options(profile)
                    val action = if (options.size == 2) StoryAction.Choose(rng.nextInt(2)) else StoryAction.Continue
                    profile = story.reduce(profile, action).profile
                }
                Phase.COMBAT_PENDING -> {
                    val setup = requireNotNull(story.combatSetupFor(profile))
                    val fightSeed = rng.nextInt(Int.MAX_VALUE).toLong() shl 16 or rng.nextInt(65_536).toLong()
                    val result = FightRunner.fight(setup, skill, fightSeed, engine)
                    if (result.capped) capped++
                    val outcome = if (result.victory) Outcome.VICTORY else Outcome.DEFEAT
                    if (!result.victory) {
                        deaths.merge(run.node.key, 1, Int::plus)
                        if (deaths.values.sum() >= MAX_DEATHS_PER_ADVENTURE) {
                            return AdventureRunSummary(reached, entry, false, true, deaths, capped)
                        }
                    }
                    profile = story.reduce(profile, StoryAction.CombatFinished(outcome, 0L)).profile
                }
            }
        }
    }

    fun simulateAdventures(skill: Skill, runs: Int, firstSeed: Long): List<AdventureRunSummary> =
        (0 until runs).map { i -> adventure(skill, firstSeed + i) }

}

data class AdventureRunSummary(
    val reachedZones: List<String>,
    val entryLevels: Map<String, Int>,
    val completed: Boolean,
    /** Gave up after [MAX_DEATHS_PER_ADVENTURE] deaths. */
    val abandoned: Boolean,
    /** Deaths per node key. */
    val deathsByNode: Map<String, Int>,
    val capped: Int,
) {
    val deaths: Int get() = deathsByNode.values.sum()
    val deathsByZone: Map<String, Int> get() = zoneTotals(deathsByNode)
}

/** Node-key counts summed by zone id (keys are `<zone>_<level>_<index>`). */
fun zoneTotals(byNode: Map<String, Int>): Map<String, Int> =
    byNode.entries.groupBy({ it.key.substringBefore('_') }, { it.value }).mapValues { it.value.sum() }

fun aggregate(skill: Skill, runs: List<AdventureRunSummary>): AdventureStats {
    val reached = Route.zones.associate { z -> z.id to runs.count { z.id in it.reachedZones } }
    val entryLevelSum = Route.zones.associate { z -> z.id to runs.sumOf { (it.entryLevels[z.id] ?: 0).toLong() } }
    val deathsByNode = runs.flatMap { it.deathsByNode.entries }.groupBy({ it.key }, { it.value }).mapValues { it.value.sum() }
    return AdventureStats(
        skill = skill,
        runs = runs.size,
        reached = reached,
        completed = runs.count { it.completed },
        capped = runs.sumOf { it.capped },
        deathsByNode = deathsByNode,
        entryLevelSum = entryLevelSum,
        abandoned = runs.count { it.abandoned },
        deathless = runs.count { it.deaths == 0 },
        deaths = runs.sumOf { it.deaths },
        deathsByZone = zoneTotals(deathsByNode),
        abandonedAt = runs.filter { it.abandoned }
            .groupingBy { run -> run.deathsByNode.maxBy { it.value }.key }.eachCount(),
    )
}

internal fun Double.f1() = String.format(Locale.ROOT, "%.1f", this)

internal fun Double.f2() = String.format(Locale.ROOT, "%.2f", this)

internal fun Double.f0() = String.format(Locale.ROOT, "%.0f", this)

private fun csv(s: String) = if (s.contains(',') || s.contains('"')) "\"" + s.replace("\"", "\"\"") + "\"" else s

fun main(args: Array<String>) {
    if (args.firstOrNull() == "sweep") {
        when (args.getOrNull(1)) {
            "offsets" -> runOffsetSweep(args.drop(2))
            "modes" -> runModesSweep(args.drop(2))
            else -> runSweep(args.drop(1))
        }
        return
    }
    val start = System.currentTimeMillis()
    // --fixed-roll: ComboTuning.FIXED (every pulse at 100 %, the engine before the damage roll) for before/after runs.
    val fixedRoll = "--fixed-roll" in args
    // --no-reactions: rivals without their HP reaction (the game before the rival-healing spec) for before/after runs.
    val noReactions = "--no-reactions" in args
    val rest = args.filter { it != "--fixed-roll" && it != "--no-reactions" }
    val outDir = File(rest.firstOrNull() ?: "build/reports").apply { mkdirs() }
    val loaded = StoryContentLoader.loadFromResources()
    val content = if (noReactions) withoutReactions(loaded) else loaded
    val sim = Simulator(content, if (fixedRoll) CombatEngine(tuning = ComboTuning.FIXED) else CombatEngine())
    if (fixedRoll) println("Tirada fija (ComboTuning.FIXED)")
    if (noReactions) println("Rivales sin reacciones por vida")
    val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    try {
        val nodes = sim.encounterNodes()
        val encounterTasks = Skill.entries.flatMap { skill -> nodes.map { node -> Callable { sim.simulateEncounter(node, skill) } } }
        val chunk = 100
        val adventureTasks = Skill.entries.flatMap { skill ->
            (0 until ADVENTURES_PER_SKILL step chunk).map { from ->
                Callable { skill to sim.simulateAdventures(skill, chunk, firstSeed = 7_000_000L * (skill.ordinal + 1) + from) }
            }
        }
        val encounterFutures = encounterTasks.map(pool::submit)
        val adventureFutures = adventureTasks.map(pool::submit)
        val rows = encounterFutures.map { it.get() }
        val adventures = Skill.entries.associateWith { skill ->
            aggregate(skill, adventureFutures.map { it.get() }.filter { it.first == skill }.flatMap { it.second })
        }
        writeCsvs(outDir, rows, adventures)
        print(summaryReport(rows, adventures))
        println("\nCSV: ${outDir.absolutePath}  (${(System.currentTimeMillis() - start) / 1000}s)")
    } finally {
        pool.shutdown()
    }
}

internal fun writeCsvs(outDir: File, rows: List<EncounterRow>, adventures: Map<Skill, AdventureStats>) {
    File(outDir, "encounters.csv").printWriter().use { w ->
        w.println("zone,node,rivals,expectedLevel,skill,winPct,avgTicks,avgHpLeft,avgDefenses")
        rows.forEach { r ->
            w.println(
                "${r.zone},${r.node},${csv(r.rivals)},${r.expectedLevel},${r.skill},${r.winPct.f1()},${r.avgTicks.f1()}," +
                    "${r.avgHpLeft.f1()},${r.avgDefenses.f2()}",
            )
        }
    }
    File(outDir, "adventures.csv").printWriter().use { w ->
        w.println("skill,zone,reachedPct,entryLevel,deathsPerAdventure")
        adventures.values.forEach { a ->
            Route.zones.forEach { z ->
                val n = a.reached.getValue(z.id)
                val level = if (n > 0) a.entryLevelSum.getValue(z.id).toDouble() / n else 0.0
                val deaths = (a.deathsByZone[z.id] ?: 0).toDouble() / a.runs
                w.println("${a.skill},${z.id},${(100.0 * n / a.runs).f1()},${level.f1()},${deaths.f2()}")
            }
        }
    }
}

/** The console report: adventures, zone reach and deaths, deadliest nodes and encounters, per skill. */
internal fun summaryReport(rows: List<EncounterRow>, adventures: Map<Skill, AdventureStats>): String = buildString {
    appendLine("=== Aventuras completas (${ADVENTURES_PER_SKILL} por perfil; morir vuelve al nodo anterior) ===")
    adventures.values.forEach { a ->
        appendLine(
            "${a.skill.name.padEnd(8)} completan: ${(100.0 * a.completed / a.runs).f1()}%" +
                "   sin muertes: ${(100.0 * a.deathless / a.runs).f1()}%" +
                "   muertes por aventura: ${(a.deaths.toDouble() / a.runs).f2()}" +
                "   abandonadas (${MAX_DEATHS_PER_ADVENTURE} muertes): ${(100.0 * a.abandoned / a.runs).f1()}%" +
                "   combates al tope de ${FightRunner.MAX_TICKS} ticks: ${a.capped}",
        )
        if (a.abandoned in 1 until a.runs) {
            appendLine("         abandonadas, atascadas en: " + a.abandonedAt.entries.sortedByDescending { it.value }.joinToString(" ") { "${it.key}=${it.value}" })
        }
    }
    appendLine("\n% que llega a cada zona (nivel medio al entrar)")
    appendLine("zona".padEnd(12) + Skill.entries.joinToString("") { it.name.padStart(20) })
    Route.zones.forEach { z ->
        val cells = Skill.entries.joinToString("") { s ->
            val a = adventures.getValue(s)
            val n = a.reached.getValue(z.id)
            val lvl = if (n > 0) a.entryLevelSum.getValue(z.id).toDouble() / n else 0.0
            "${(100.0 * n / a.runs).f1()}% (L${lvl.f1()})".padStart(20)
        }
        appendLine(z.id.padEnd(12) + cells)
    }

    appendLine("\nMuertes por zona (media por aventura)")
    appendLine("zona".padEnd(12) + Skill.entries.joinToString("") { it.name.padStart(12) })
    Route.zones.forEach { z ->
        appendLine(
            z.id.padEnd(12) + Skill.entries.joinToString("") { s ->
                val a = adventures.getValue(s)
                ((a.deathsByZone[z.id] ?: 0).toDouble() / a.runs).f2().padStart(12)
            },
        )
    }

    Skill.entries.forEach { s ->
        val a = adventures.getValue(s)
        appendLine("\nNodos con más muertes ($s, top 15, media por aventura)")
        a.deathsByNode.entries.sortedByDescending { it.value }.take(15).forEach { (node, n) ->
            appendLine("  ${node.padEnd(16)} ${(n.toDouble() / a.runs).f2()}")
        }
    }

    val medio = rows.filter { it.skill == Skill.MEDIO }
    appendLine("\n=== Encuentros (${FIGHTS_PER_ENCOUNTER} combates, nivel esperado) — MEDIO top 15 más letales ===")
    medio.sortedBy { it.winPct }.take(15).forEach { r ->
        appendLine("  ${r.node.padEnd(16)} L${r.expectedLevel.toString().padEnd(3)} win ${r.winPct.f1().padStart(5)}%  ticks ${r.avgTicks.f1()}  hp ${r.avgHpLeft.f1()}  ${r.rivals}")
    }
    appendLine("\nPor zona: % victoria jefe / media combates normales / mínimo normal")
    appendLine("zona".padEnd(12) + Skill.entries.joinToString("") { it.name.padStart(26) })
    Route.treeZones.forEach { z ->
        val cells = Skill.entries.joinToString("") { s ->
            val zr = rows.filter { it.skill == s && it.zone == z.id }
            if (zr.isEmpty()) {
                "-".padStart(26)
            } else {
                val boss = zr.firstOrNull { it.boss }?.winPct
                val normal = zr.filter { !it.boss }
                "${boss?.f1() ?: "-"} / ${normal.map { it.winPct }.average().f1()} / ${normal.minOf { it.winPct }.f1()}".padStart(26)
            }
        }
        appendLine(z.id.padEnd(12) + cells)
    }
    appendLine("\nJefes: ticks medios / entradas medias en defensa por combate")
    appendLine("zona".padEnd(12) + Skill.entries.joinToString("") { it.name.padStart(16) })
    Route.treeZones.forEach { z ->
        val cells = Skill.entries.joinToString("") { s ->
            val boss = rows.firstOrNull { it.skill == s && it.zone == z.id && it.boss }
            (boss?.let { "${it.avgTicks.f1()} / ${it.avgDefenses.f2()}" } ?: "-").padStart(16)
        }
        appendLine(z.id.padEnd(12) + cells)
    }
    Skill.entries.forEach { s ->
        val bosses = rows.filter { it.skill == s && it.boss }
        if (bosses.isNotEmpty()) {
            appendLine(
                "${s.name.padEnd(8)} media de jefes: ticks ${bosses.map { it.avgTicks }.average().f1()}, " +
                    "entradas en defensa ${bosses.map { it.avgDefenses }.average().f2()}",
            )
        }
    }
    val capped = rows.sumOf { it.capped }
    val cappedAt = rows.filter { it.capped > 0 }.joinToString(" ") { "${it.skill}:${it.node}=${it.capped}" }
    appendLine("\nCombates de encuentros al tope de ticks: $capped" + if (cappedAt.isEmpty()) "" else " ($cappedAt)")
}
