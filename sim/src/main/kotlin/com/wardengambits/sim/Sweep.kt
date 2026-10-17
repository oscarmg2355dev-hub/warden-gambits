package com.wardengambits.sim

import com.wardengambits.engine.combo.ComboFamily
import com.wardengambits.engine.combo.ComboTuning
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.model.RivalScaling
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.TreeShape
import com.wardengambits.engine.story.content.ModesContent
import com.wardengambits.engine.story.content.ReactionContent
import com.wardengambits.engine.story.content.StoryContent
import com.wardengambits.engine.story.content.StoryContentLoader
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/** One sweep point: the swept rival scaling plus the combo tuning (applies to every fighter). */
data class SweepCase(val scaling: RivalScaling, val tuning: ComboTuning = ComboTuning.DEFAULT)

/**
 * Grid for `./gradlew :sim:run --args="sweep attack=50,60 spear=18,20"`. Scaling keys: hp, attack, heal, def, start,
 * regen, power (max power); [ComboTuning] keys: spear, fist, cure, tactics (Daze % under Escudo táctico). Missing keys
 * keep the current value of the swept scaling ([base]) and of [ComboTuning.DEFAULT].
 */
object SweepGrid {
    private val KEYS = listOf("hp", "attack", "heal", "def", "start", "regen", "power", "spear", "fist", "cure", "tactics")

    fun parse(args: List<String>, base: RivalScaling = RivalScaling.DEFAULT): List<SweepCase> {
        val t = ComboTuning.DEFAULT
        val values = linkedMapOf(
            "hp" to listOf(base.hpPct), "attack" to listOf(base.attackPct), "heal" to listOf(base.healPct),
            "def" to listOf(base.defensePct), "start" to listOf(base.startPowerPct), "regen" to listOf(base.regenPct),
            "power" to listOf(base.powerPct), "spear" to listOf(t.spearBasePct), "fist" to listOf(t.fistBasePct),
            "cure" to listOf(t.cureBasePct), "tactics" to listOf(t.tacticsDazePct),
        )
        args.forEach { arg ->
            val (key, list) = arg.split('=', limit = 2)
            require(key in KEYS) { "Unknown sweep key '$key' (use $KEYS)" }
            values[key] = list.split(',').map { it.trim().toInt() }
        }
        return KEYS.fold(listOf(emptyMap<String, Int>())) { acc, key ->
            acc.flatMap { partial -> values.getValue(key).map { partial + (key to it) } }
        }.map { v ->
            SweepCase(
                RivalScaling(
                    v.getValue("hp"), v.getValue("attack"), v.getValue("heal"), v.getValue("def"),
                    v.getValue("start"), v.getValue("regen"), v.getValue("power"),
                ),
                ComboTuning(v.getValue("spear"), v.getValue("fist"), v.getValue("cure"), tacticsDazePct = v.getValue("tactics")),
            )
        }
    }
}

/** Which rival scaling a sweep point replaces: commons ([RivalScaling.DEFAULT]) or bosses ([RivalScaling.BOSS]). */
enum class SweepTarget {
    COMMON, BOSS;

    val base: RivalScaling get() = when (this) {
        COMMON -> RivalScaling.DEFAULT
        BOSS -> RivalScaling.BOSS
    }

    fun engine(point: RivalScaling, tuning: ComboTuning = ComboTuning.DEFAULT): CombatEngine = when (this) {
        COMMON -> CombatEngine(rivalScaling = point, bossScaling = RivalScaling.BOSS, tuning = tuning)
        BOSS -> CombatEngine(rivalScaling = RivalScaling.DEFAULT, bossScaling = point, tuning = tuning)
    }

    companion object {
        /** `sweep boss hp=...` sweeps the boss scaling; without `boss` the commons scaling. */
        fun split(args: List<String>): Pair<SweepTarget, List<String>> =
            if (args.firstOrNull() == "boss") BOSS to args.drop(1) else COMMON to args
    }
}

data class SweepPoint(
    val case: SweepCase,
    /** % of adventures with 0 deaths per skill (the old "complete the adventure" metric). */
    val completion: Map<Skill, Double>,
    val medioNormalAvg: Double,
    val medioBossAvg: Double,
    val medioNormalMin: Double,
    val medioBossMin: Double,
    val medioBossMax: Double,
    val medioBossByZone: Map<String, Double> = emptyMap(),
    /** Fights at the tick cap: MEDIO encounters + adventures of every skill. */
    val capped: Int = 0,
    /** Boss win % by zone for NOVATO, EXPERTO and MAESTRO (MEDIO is [medioBossByZone]). */
    val bossByZone: Map<Skill, Map<String, Double>> = emptyMap(),
    /** MEDIO win % of the weakest normal (non-boss) encounters, lowest first. */
    val medioWorstNormals: List<Pair<String, Double>> = emptyList(),
)

/** MEDIO encounters + adventures for every skill, same seeds as the full run; work is split over [pool]. */
fun sweepPoint(
    case: SweepCase,
    fightsPerEncounter: Int,
    adventures: Int,
    target: SweepTarget,
    pool: ExecutorService,
): SweepPoint {
    val sim = Simulator(StoryContentLoader.loadFromResources(), target.engine(case.scaling, case.tuning))
    val nodes = sim.encounterNodes()
    val rowFutures = nodes.map { pool.submit(Callable { sim.simulateEncounter(it, Skill.MEDIO, fightsPerEncounter) }) }
    val bossFutures = listOf(Skill.NOVATO, Skill.EXPERTO, Skill.MAESTRO).associateWith { skill ->
        nodes.filter { it.level == TreeShape.LEVELS }.map { pool.submit(Callable { sim.simulateEncounter(it, skill, fightsPerEncounter) }) }
    }
    val chunk = 100
    val runFutures = Skill.entries.associateWith { skill ->
        (0 until adventures step chunk).map { from ->
            pool.submit(Callable { sim.simulateAdventures(skill, minOf(chunk, adventures - from), firstSeed = 7_000_000L * (skill.ordinal + 1) + from) })
        }
    }
    val rows = rowFutures.map { it.get() }
    val runs = runFutures.mapValues { (_, futures) -> futures.flatMap { it.get() } }
    val (bosses, normals) = rows.partition { it.boss }
    return SweepPoint(
        case,
        // % of adventures without a single death: what "completed" meant before defeats stopped ending the run.
        runs.mapValues { (_, r) -> 100.0 * r.count { it.deaths == 0 } / r.size },
        normals.map { it.winPct }.average(), bosses.map { it.winPct }.average(),
        normals.minOf { it.winPct }, bosses.minOf { it.winPct }, bosses.maxOf { it.winPct },
        bosses.associate { it.zone to it.winPct },
        rows.sumOf { it.capped } + runs.values.sumOf { r -> r.sumOf { it.capped } },
        bossFutures.mapValues { (_, futures) -> futures.map { it.get() }.associate { it.zone to it.winPct } },
        normals.sortedBy { it.winPct }.take(5).map { it.node to it.winPct },
    )
}

fun runSweep(allArgs: List<String>) {
    val (target, args) = SweepTarget.split(allArgs)
    val fights = args.firstOrNull { it.startsWith("fights=") }?.substringAfter('=')?.toInt() ?: 100
    val adventures = args.firstOrNull { it.startsWith("runs=") }?.substringAfter('=')?.toInt() ?: 1000
    val grid = SweepGrid.parse(args.filterNot { it.startsWith("fights=") || it.startsWith("runs=") }, target.base)
    val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    try {
        println("target $target")
        println("hp atk heal def start regen power spear fist cure tactics | NOV% MED% EXP% MAE% sin muertes | MEDIO normal avg/min | boss avg/min/max out | capped")
        grid.forEach { case ->
            val p = sweepPoint(case, fights, adventures, target, pool)
            val s = p.case.scaling
            val t = p.case.tuning
            val out = p.medioBossByZone.values.count { it < 75.0 || it > 98.0 }
            println(
                "${s.hpPct} ${s.attackPct} ${s.healPct} ${s.defensePct} ${s.startPowerPct} ${s.regenPct} ${s.powerPct} " +
                    "${t.spearBasePct} ${t.fistBasePct} ${t.cureBasePct} ${t.tacticsDazePct} | " +
                    Skill.entries.joinToString(" ") { p.completion.getValue(it).f1() } +
                    " | ${p.medioNormalAvg.f1()}/${p.medioNormalMin.f1()} | ${p.medioBossAvg.f1()}/${p.medioBossMin.f1()}/${p.medioBossMax.f1()} $out" +
                    " | ${p.capped}",
            )
            println(
                "    " + p.medioBossByZone.entries.joinToString(" ") { (zone, medio) ->
                    "$zone=${p.bossByZone[Skill.NOVATO]?.get(zone)?.f0()}/${medio.f0()}/${p.bossByZone[Skill.EXPERTO]?.get(zone)?.f0()}" +
                        "/${p.bossByZone[Skill.MAESTRO]?.get(zone)?.f0()}"
                },
            )
            println("    peores normales MEDIO: " + p.medioWorstNormals.joinToString(" ") { (node, win) -> "$node=${win.f0()}" })
        }
    } finally {
        pool.shutdown()
    }
}

/**
 * [content] with the boss (first rival of `<zone>_7_0`) of [zoneId] at [offset] and, if given, [pattern] /
 * [everyNTicks]; [reaction] maps its HP reaction (rival-healing spec §2) and [modes] its attack and defense modes
 * (boss-modes spec §2), both identity by default.
 */
fun withBoss(
    content: StoryContent,
    zoneId: String,
    offset: Int? = null,
    pattern: List<String>? = null,
    everyNTicks: Int? = null,
    reaction: (ReactionContent?) -> ReactionContent? = { it },
    modes: (ModesContent?) -> ModesContent? = { it },
): StoryContent = StoryContent(
    content.zones.map { zone ->
        if (zone.id != zoneId) {
            zone
        } else {
            zone.copy(
                nodes = zone.nodes.map { node ->
                    val encounter = node.encounter
                    if (node.level != TreeShape.LEVELS || encounter == null) {
                        node
                    } else {
                        val boss = encounter.rivals.first()
                        val changed = boss.copy(
                            levelOffset = offset ?: boss.levelOffset,
                            pattern = pattern ?: boss.pattern,
                            everyNTicks = everyNTicks ?: boss.everyNTicks,
                            reaction = reaction(boss.reaction),
                            modes = modes(boss.modes),
                        )
                        node.copy(encounter = encounter.copy(rivals = listOf(changed) + encounter.rivals.drop(1)))
                    }
                },
            )
        }
    },
)

/** [content] with no rival HP reaction: the game before the rival-healing spec, for before/after runs. */
fun withoutReactions(content: StoryContent): StoryContent = StoryContent(
    content.zones.map { zone ->
        zone.copy(
            nodes = zone.nodes.map { node ->
                val encounter = node.encounter ?: return@map node
                node.copy(encounter = encounter.copy(rivals = encounter.rivals.map { it.copy(reaction = null) }))
            },
        )
    },
)

/**
 * Boss-modes spec §3 levers: `below` / `above` replace the defense and attack thresholds, `max` the maximum defenses and
 * `tail` (`12;121`) the damage combos that end the defense, after its shield and heals. Missing keys keep the content.
 */
fun modesOverride(options: Map<String, String>): (ModesContent?) -> ModesContent? = { current ->
    current?.let { m ->
        val protective = m.defense.indexOfLast { Combos.find(it)?.family in setOf(ComboFamily.SHIELD, ComboFamily.HEAL) } + 1
        m.copy(
            defenseBelowPct = options["below"]?.toInt() ?: m.defenseBelowPct,
            attackAbovePct = options["above"]?.toInt() ?: m.attackAbovePct,
            maxDefenses = options["max"]?.toInt() ?: m.maxDefenses,
            defense = options["tail"]?.let { m.defense.take(protective) + it.split(';') } ?: m.defense,
        )
    }
}

/**
 * `sweep offsets <zone> [2,3,...] [pattern=12;121;31] [every=2] [thresholds=45;20|none] [shield=2132|none]
 * [below=50] [above=70] [max=3] [tail=12;121] [fights=300] [levels=-1,0,1]`: boss win % per skill for each offset, with
 * the player at the expected level plus each of [levels], and the average defense entries of MEDIO and MAESTRO.
 * `thresholds` replaces the `atHpPct` of a reaction (`none` removes it), `shield` its shield; the modes keys are
 * those of [modesOverride].
 */
fun runOffsetSweep(args: List<String>) {
    val zone = args.first()
    val offsets = args.getOrNull(1)?.takeIf { !it.contains('=') }?.split(',')?.map { it.toInt() } ?: (2..12).toList()
    fun opt(key: String) = args.firstOrNull { it.startsWith("$key=") }?.substringAfter('=')
    val fights = opt("fights")?.toInt() ?: FIGHTS_PER_ENCOUNTER
    val pattern = opt("pattern")?.split(';')
    val every = opt("every")?.toInt()
    val thresholds = opt("thresholds")
    val shield = opt("shield")
    val reaction: (ReactionContent?) -> ReactionContent? = { current ->
        when (thresholds) {
            null -> current
            "none" -> null
            else -> current?.copy(atHpPct = thresholds.split(';').map { it.toInt() })
        }?.let { r -> if (shield == null) r else r.copy(shield = shield.takeIf { it != "none" }) }
    }
    val modeKeys = listOf("below", "above", "max", "tail")
    val modes = modesOverride(modeKeys.mapNotNull { key -> opt(key)?.let { key to it } }.toMap())
    val levels = opt("levels")?.split(',')?.map { it.toInt() } ?: listOf(0)
    val base = StoryContentLoader.loadFromResources()
    val node = NodeId(zone, TreeShape.LEVELS, 0)
    val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    try {
        println("zone $zone pattern=${pattern ?: "content"} every=${every ?: "content"} thresholds=${thresholds ?: "content"} shield=${shield ?: "content"} " +
                modeKeys.joinToString(" ") { "$it=${opt(it) ?: "content"}" } +
                " | per level delta: NOVATO/MEDIO/EXPERTO/MAESTRO win% (MEDIO ticks, MEDIO/MAESTRO defenses)",
        )
        offsets.forEach { offset ->
            val sim = Simulator(withBoss(base, zone, offset, pattern, every, reaction, modes))
            val cells = levels.map { delta ->
                Skill.entries.associateWith { skill -> pool.submit(Callable { sim.simulateEncounter(node, skill, fights, delta) }) }
            }.map { futures -> futures.mapValues { it.value.get() } }
            println(
                "+$offset  " + levels.zip(cells).joinToString("   ") { (delta, rows) ->
                    "L${if (delta >= 0) "+" else ""}$delta: " + Skill.entries.joinToString("/") { rows.getValue(it).winPct.f0() } +
                        " (${rows.getValue(Skill.MEDIO).avgTicks.f0()}, ${rows.getValue(Skill.MEDIO).avgDefenses.f1()}/" +
                        "${rows.getValue(Skill.MAESTRO).avgDefenses.f1()})"
                },
            )
        }
    } finally {
        pool.shutdown()
    }
}

/** [content] with the modes of the bosses of [zoneIds] mapped by [modes] and, if given, their [offset]. */
fun withBossModes(
    content: StoryContent,
    zoneIds: Collection<String>,
    modes: (ModesContent?) -> ModesContent?,
    offset: Int? = null,
): StoryContent = zoneIds.fold(content) { acc, zone -> withBoss(acc, zone, offset = offset, modes = modes) }

/**
 * `sweep modes [below=40,50] [above=60,70] [max=1,2,3] [offset=2,4] [tail=12;121] [zones=chet,moria] [fights=150]`:
 * for each grid point, the [modesOverride] (and offset) of every listed boss (default: all but Bree's tutorial boss),
 * then MEDIO and MAESTRO boss win % per zone at the expected level, with the MAESTRO average defense entries. Points
 * with `below >= above` are skipped.
 */
fun runModesSweep(args: List<String>) {
    fun opt(key: String) = args.firstOrNull { it.startsWith("$key=") }?.substringAfter('=')
    val fights = opt("fights")?.toInt() ?: 150
    val base = StoryContentLoader.loadFromResources()
    val zones = opt("zones")?.split(',') ?: base.zones.map { it.id }.filter { zone ->
        zone != "bree" && base.node(NodeId(zone, TreeShape.LEVELS, 0))?.encounter != null
    }
    val grid = listOf("below", "above", "max", "offset").fold(listOf(emptyMap<String, String>())) { acc, key ->
        val values = opt(key)?.split(',') ?: return@fold acc
        acc.flatMap { partial -> values.map { partial + (key to it) } }
    }.map { point -> opt("tail")?.let { point + ("tail" to it) } ?: point }.filter { point ->
        val below = point["below"]?.toInt() ?: return@filter true
        val above = point["above"]?.toInt() ?: return@filter true
        below < above
    }
    val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    try {
        println("zone=MEDIO/MAESTRO win% (MAESTRO defenses)")
        grid.forEach { point ->
            val sim = Simulator(withBossModes(base, zones, modesOverride(point), point["offset"]?.toInt()))
            val rows = zones.associateWith { zone ->
                listOf(Skill.MEDIO, Skill.MAESTRO).associateWith { skill ->
                    pool.submit(Callable { sim.simulateEncounter(NodeId(zone, TreeShape.LEVELS, 0), skill, fights) })
                }
            }.mapValues { (_, futures) -> futures.mapValues { it.value.get() } }
            val medio = rows.values.map { it.getValue(Skill.MEDIO).winPct }
            val maestro = rows.values.map { it.getValue(Skill.MAESTRO).winPct }
            println(
                "$point | MEDIO avg/min ${medio.average().f1()}/${medio.min().f1()} | MAESTRO avg/min/max " +
                    "${maestro.average().f1()}/${maestro.min().f1()}/${maestro.max().f1()}",
            )
            println(
                "    " + rows.entries.joinToString(" ") { (zone, r) ->
                    "$zone=${r.getValue(Skill.MEDIO).winPct.f0()}/${r.getValue(Skill.MAESTRO).winPct.f0()}" +
                        "(${r.getValue(Skill.MAESTRO).avgDefenses.f1()})"
                },
            )
        }
    } finally {
        pool.shutdown()
    }
}
