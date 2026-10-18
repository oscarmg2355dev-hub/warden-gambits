package com.wardengambits.engine.story

import com.wardengambits.engine.combo.ComboFamily
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.story.content.ModesContent
import com.wardengambits.engine.story.content.NodeContent
import com.wardengambits.engine.story.content.ReactionContent
import com.wardengambits.engine.story.content.RivalContent
import com.wardengambits.engine.story.content.ZoneContent

data class BeastEntry(val name: String, val pattern: List<String>, val everyNTicks: Int, val portrait: String = "")

/** Bestiary of the content spec §3 (combat v2 patterns, spec 2026-10-12 §8) and the commons per zone (§3 "Zonas" / §4 "Comunes"). */
object Bestiary {
    val common: Map<String, BeastEntry> = mapOf(
        "bandido" to BeastEntry("Bandido del camino", listOf("12", "121"), 2, "bandit"),
        "lobo" to BeastEntry("Lobo hambriento", listOf("12", "121", "1212"), 2, "wolf"),
        "moscardones" to BeastEntry("Moscardones de Moscagua", listOf("32", "323", "13"), 2, "swarm"),
        "espia" to BeastEntry("Espía dunlendino", listOf("13", "131", "31"), 2, "dunlending"),
        "huargo" to BeastEntry("Huargo", listOf("12", "121", "1212", "12121"), 2, "warg"),
        "crebain" to BeastEntry("Crebain", listOf("12", "121", "1212", "31"), 2, "crebain"),
        "loboSaruman" to BeastEntry("Lobo de Saruman", listOf("13", "131", "1313"), 2, "wolf_saruman"),
        "trasgo" to BeastEntry("Trasgo de Moria", listOf("13", "131"), 1, "goblin"),
        "orcoCapitan" to BeastEntry("Orco capitán", listOf("11", "12", "121", "1212"), 1, "orc"),
        "trolCavernas" to BeastEntry("Trol de las cavernas", listOf("2132", "12", "121", "1212", "12121"), 2, "troll"),
        "orcoMontanas" to BeastEntry("Orco de las Montañas Nubladas", listOf("11", "12", "121", "1212", "22"), 2, "orc"),
        "arquero" to BeastEntry("Arquero orco", listOf("32", "323"), 1, "orc_archer"),
        // Road to Mordor (spec 2026-10-16 §3), tuned in tuning.md «Libro II: hasta el Monte del Destino»: the commons
        // that came in groups of 3–4 attack every 3 ticks (a −4/−5 offset weakens less at level 40+).
        "huargoAnduin" to BeastEntry("Huargo del Anduin", listOf("12", "121", "1212", "12121"), 3, "warg"),
        "uruk" to BeastEntry("Uruk-hai", listOf("11", "12", "121", "1212", "12121"), 3, "uruk"),
        "orcoMordor" to BeastEntry("Orco de Mordor", listOf("13", "131", "1313", "11"), 3, "mordor_orc"),
        "loboRocas" to BeastEntry("Lobo de las rocas", listOf("12", "121", "1212", "12121", "31"), 3, "wolf"),
        "fuegoFatuo" to BeastEntry("Fuego fatuo", listOf("32", "323", "3232", "31"), 3, "wisp"),
        "espectroCienaga" to BeastEntry("Espectro de la ciénaga", listOf("3123", "31232", "13", "31"), 2, "marsh_wraith"),
        // Rival healing (tuning.md «Rivales que se curan y MAESTRO»): the healer of ithilien_4_1 made it a wall.
        "guerreroHaradrim" to BeastEntry("Guerrero haradrim", listOf("11", "12", "121", "1212", "123"), 3, "haradrim"),
        "arqueroHaradrim" to BeastEntry("Arquero haradrim", listOf("32", "323", "3232"), 3, "haradrim"),
        "orcoMorgul" to BeastEntry("Orco de Morgul", listOf("13", "131", "1313", "31"), 3, "mordor_orc"),
        "arana" to BeastEntry("Araña", listOf("13", "131", "31", "313"), 2, "spider"),
        "urukNegro" to BeastEntry("Uruk negro", listOf("11", "12", "121", "1212", "12121", "22"), 2, "uruk"),
        "ologHai" to BeastEntry("Olog-hai", listOf("2132", "13", "131", "1313", "13131"), 2, "troll"),
    )

    /**
     * zoneId to the boss of that zone. The pattern is the attack pattern (boss-modes spec §1): no shields or heals, which
     * the boss casts in its defense mode ([ContentRules.bossModes]).
     */
    val bosses: Map<String, BeastEntry> = mapOf(
        "bree" to BeastEntry("Cabecilla de los bandidos", listOf("12", "121", "11", "31"), 4),
        "chet" to BeastEntry("Lobo gris alfa", listOf("12", "121", "1212", "12121", "31"), 1),
        "moscagua" to BeastEntry("Reina del enjambre", listOf("32", "323", "3232", "13", "131", "1313", "31"), 1),
        "cima" to BeastEntry("Espectro del Anillo", listOf("32", "323", "3232", "12", "121", "31"), 1),
        "puente" to BeastEntry("Jefe de la manada de huargos", listOf("12", "121", "1212", "13", "131", "31", "11"), 1),
        "quebradas" to BeastEntry("Trol de piedra", listOf("12", "121", "1212", "13", "131", "31"), 1),
        "bruinen" to BeastEntry("Jinete Negro", listOf("32", "323", "3232", "13", "131", "3123", "31232", "31"), 1),
        "eregion" to BeastEntry("Capitán dunlendino", listOf("12", "121", "1212", "13", "131", "31", "123"), 1),
        "caradhras" to BeastEntry("Furia del Caradhras", listOf("32", "323", "3232", "12", "121", "1212", "31", "123"), 2),
        "durin" to BeastEntry("Guardián del Agua", listOf("13", "131", "1313", "12", "121", "1212", "31", "123"), 1),
        "moria" to BeastEntry("Sombra de Durin", listOf("32", "323", "3232", "13", "131", "31", "123"), 1),
        "sombrio" to BeastEntry("Capitán orco de las Montañas Nubladas", listOf("12", "121", "1212", "13", "131", "31", "123"), 1),
        "nimrodel" to BeastEntry("Jefe de los arqueros orcos", listOf("32", "323", "3232", "12", "121", "31"), 1),
        // Road to Mordor (spec 2026-10-16 §3), tuned in tuning.md «Libro II: hasta el Monte del Destino». Since «Escudo
        // táctico: inmunidad total» (2026-10-17) most bosses run two damage chains and a single Aturdir.
        "anduin" to BeastEntry("Cabecilla de los saqueadores del río", listOf("12", "121", "1212", "13", "131", "31", "32", "323", "123"), 2),
        "argonath" to BeastEntry("Capitán uruk de la Mano Blanca", listOf("12", "121", "1212", "13", "131", "1313", "31", "123"), 1),
        "amonhen" to BeastEntry("Caudillo uruk-hai", listOf("13", "131", "1313", "12", "121", "1212", "31", "123"), 1),
        "emynmuil" to BeastEntry("Trol de las colinas", listOf("12", "121", "1212", "32", "323", "3232", "31", "123"), 1),
        "cienagas" to BeastEntry("Señor de los ahogados", listOf("3123", "31232", "13", "131", "1313", "31", "32", "323", "123"), 1),
        "ithilien" to BeastEntry("Olifante de guerra", listOf("32", "323", "3232", "12", "121", "123", "31"), 1),
        "cirithungol" to BeastEntry("Ella-Laraña", listOf("13", "131", "1313", "12", "121", "1212", "31", "123"), 1),
        "gorgoroth" to BeastEntry("Nazgûl en su bestia alada", listOf("32", "323", "3232", "3123", "31232", "31", "123"), 1),
    )

    /** zoneId to the keys of [common] allowed in that zone. */
    val zoneCommons: Map<String, Set<String>> = mapOf(
        "bree" to setOf("bandido", "lobo"),
        "chet" to setOf("lobo", "bandido"),
        "moscagua" to setOf("moscardones", "espia"),
        "cima" to setOf("espia", "lobo"),
        "puente" to setOf("huargo", "espia"),
        "quebradas" to setOf("huargo", "orcoMontanas"),
        "bruinen" to setOf("huargo", "espia"),
        "rivendel" to emptySet(),
        "eregion" to setOf("crebain", "loboSaruman", "espia"),
        "caradhras" to setOf("loboSaruman", "crebain"),
        "durin" to setOf("loboSaruman", "trasgo"),
        "moria" to setOf("trasgo", "orcoCapitan", "trolCavernas"),
        "sombrio" to setOf("orcoMontanas", "arquero", "trasgo"),
        "nimrodel" to setOf("arquero", "orcoMontanas"),
        "lothlorien" to emptySet(),
        "anduin" to setOf("arquero", "huargoAnduin"),
        "argonath" to setOf("uruk", "huargo"),
        "amonhen" to setOf("uruk", "crebain"),
        "emynmuil" to setOf("loboRocas", "orcoMordor"),
        "cienagas" to setOf("fuegoFatuo", "espectroCienaga"),
        "ithilien" to setOf("guerreroHaradrim", "arqueroHaradrim"),
        "cirithungol" to setOf("orcoMorgul", "arana"),
        "gorgoroth" to setOf("urukNegro", "ologHai", "orcoMordor"),
    )
}

/** Content rules of the content spec §5, applied to one zone. */
object ContentRules {
    /** Portrait keys of the 18 common rival types (combat-cards spec §2; the last 6, road to Mordor spec §3). */
    val COMMON_PORTRAITS: Set<String> = setOf(
        "bandit", "wolf", "wolf_saruman", "warg", "warg_rider", "dunlending",
        "crebain", "swarm", "goblin", "orc", "orc_archer", "troll",
        "uruk", "mordor_orc", "wisp", "marsh_wraith", "haradrim", "spider",
    )

    /** Portrait key of the boss of [zoneId]. */
    fun bossPortrait(zoneId: String) = "boss_$zoneId"

    private val PORTRAIT_FORMAT = Regex("[a-z_]+")

    /** Rival-healing spec §3: portraits of the humanoid commons, the only commons that may heal. */
    val HUMANOID_PORTRAITS: Set<String> = setOf("bandit", "dunlending", "goblin", "orc", "orc_archer", "uruk", "mordor_orc", "haradrim")

    /** Rival-healing spec §3: reaction of the first humanoid of each common encounter with humanoids. */
    val HEALER_REACTION = ReactionContent(atHpPct = listOf(30), combos = listOf("21212"))

    /** Spec v2 §3: families whose combos hurt opponents (Lanza, Lanza cruzada, Puño en área, Robo de vida, Golpe directo). */
    val DAMAGE_FAMILIES: Set<ComboFamily> =
        setOf(ComboFamily.SPEAR_A, ComboFamily.SPEAR_B, ComboFamily.AREA, ComboFamily.DRAIN, ComboFamily.DIRECT)

    fun isDamage(sequence: String) = Combos.find(sequence)?.family in DAMAGE_FAMILIES

    /** Boss-modes spec §1: shields and heals, which leave the attack pattern of a boss for its defense. */
    fun isShieldOrHeal(sequence: String) = Combos.find(sequence)?.family in setOf(ComboFamily.SHIELD, ComboFamily.HEAL)

    /**
     * Boss-modes spec §1: the defense of every boss but Bree's opens with Escudo maestro (`2132`, never Escudo táctico,
     * which gives Daze immunity and would void the counterplay of stunning the boss to cut its heals) and the full
     * Cura chain.
     */
    val BOSS_DEFENSE_OPENING: List<String> = listOf("2132", "21", "212", "2121", "21212")

    /** Boss-modes spec §1: Bree's tutorial boss defends without shield, up to Cura II. */
    val BREE_DEFENSE_OPENING: List<String> = listOf("21", "212")

    /**
     * Boss-modes spec §3 re-balance (tuning.md «Jefes con modo ataque y defensa» and «Escudo táctico: inmunidad total»):
     * zoneId to the `defenseBelowPct` and `maxDefenses` of the bosses that do not use the spec values (50 and 3;
     * `attackAbovePct` is always 70). Since the tactical-shield immunity re-balance every boss but Bree's is listed.
     */
    val BOSS_MODE_TUNING: Map<String, Pair<Int, Int>> = mapOf(
        "chet" to (10 to 1),
        "moscagua" to (10 to 1),
        "cima" to (15 to 1),
        "puente" to (50 to 1),
        "quebradas" to (20 to 2),
        "bruinen" to (15 to 1),
        "eregion" to (15 to 1),
        "caradhras" to (15 to 1),
        "durin" to (15 to 1),
        "moria" to (15 to 1),
        "sombrio" to (20 to 2),
        "nimrodel" to (20 to 1),
        "anduin" to (15 to 1),
        "argonath" to (15 to 1),
        "amonhen" to (15 to 1),
        "emynmuil" to (10 to 1),
        "cienagas" to (15 to 1),
        "ithilien" to (15 to 1),
        "cirithungol" to (15 to 1),
        "gorgoroth" to (15 to 1),
    )

    /**
     * Boss-modes spec §1: the modes of the boss of [zoneId]: the defense opening followed by the first two damage combos
     * of its attack pattern (Bree's: 40/60, one defense and a single damage combo), with the thresholds and defenses of
     * [BOSS_MODE_TUNING].
     */
    fun bossModes(zoneId: String): ModesContent {
        val damage = Bestiary.bosses.getValue(zoneId).pattern.filter(::isDamage)
        if (zoneId == "bree") {
            return ModesContent(defenseBelowPct = 40, attackAbovePct = 60, maxDefenses = 1, defense = BREE_DEFENSE_OPENING + damage.take(1))
        }
        val (below, max) = BOSS_MODE_TUNING[zoneId] ?: (50 to 3)
        return ModesContent(defenseBelowPct = below, attackAbovePct = 70, maxDefenses = max, defense = BOSS_DEFENSE_OPENING + damage.take(2))
    }

    /**
     * Boss-modes spec §1–§2: structural errors of [modes]: valid thresholds, at least one defense, known combos, and a
     * defense that ends with a damage combo, where a Dazed boss jumps to.
     */
    fun modesErrors(modes: ModesContent): List<String> = buildList {
        if (modes.defenseBelowPct !in 1 until modes.attackAbovePct || modes.attackAbovePct > 100) {
            add("modes thresholds ${modes.defenseBelowPct}/${modes.attackAbovePct} need 0 < defense < attack <= 100")
        }
        if (modes.maxDefenses < 1) add("modes maxDefenses ${modes.maxDefenses} must be >= 1")
        modes.defense.filter { Combos.find(it) == null }.forEach { add("modes defense has unknown combo $it") }
        if (modes.defense.lastOrNull()?.let(::isDamage) != true) add("modes defense ${modes.defense} must end with a damage combo")
    }

    fun isHumanoid(rival: RivalContent) = rival.portrait in HUMANOID_PORTRAITS

    /** Rival-healing spec §3: expected reaction of each rival of a level 2–6 encounter (only its first humanoid heals). */
    fun expectedCommonReactions(rivals: List<RivalContent>): List<ReactionContent?> {
        val healer = rivals.indexOfFirst(::isHumanoid)
        return rivals.indices.map { if (it == healer) HEALER_REACTION else null }
    }

    /** §5: allowed levelOffset of every rival of a level 2–6 encounter, by number of rivals. */
    val GROUP_OFFSETS: Map<Int, Set<Int>> = mapOf(1 to setOf(0, 1), 2 to setOf(-2), 3 to setOf(-4), 4 to setOf(-5))

    /** §5: levelOffset of the zone boss, tuned per boss. */
    val BOSS_OFFSETS: IntRange = 2..15

    const val PROMPT_SUFFIX = ", painterly dark fantasy illustration, Middle-earth, muted earthy palette, 2D game card art, no text"

    /** §4: 1 + 3 * i, where i is the route index minus the rest zones before it (each rest zone shifts the curve). */
    fun expectedBaseLevel(zoneId: String): Int {
        if (zoneId == Route.FINAL_ZONE_ID) return 0
        val index = Route.indexOf(zoneId)
        val restBefore = Route.zones.take(index).count { it.id in Route.REST_ZONES }
        return 1 + 3 * (index - restBefore)
    }

    /** Set of "number of nodes with encounter" over every path from (zone,1,0) to (zone,7,0). */
    fun combatCounts(zone: ZoneContent): Set<Int> {
        val byPosition = zone.nodes.associateBy { it.level to it.index }
        fun counts(id: NodeId): Set<Int> {
            val own = if (byPosition[id.level to id.index]?.encounter != null) 1 else 0
            if (id.level == TreeShape.LEVELS) return setOf(own)
            return TreeShape.nextInZone(id).flatMap { counts(it) }.map { it + own }.toSet()
        }
        return counts(NodeId(zone.id, 1, 0))
    }

    private fun nodeKey(zoneId: String, node: NodeContent) = "${zoneId}_${node.level}_${node.index}"

    /**
     * Rules of §5 for one zone. [baseLevel] and [isFinal] default to the zone's place in the route; pending zones (not
     * yet in the route) pass them explicitly. Rest zones are those of [Route.REST_ZONES].
     */
    fun errors(
        zone: ZoneContent,
        baseLevel: Int = expectedBaseLevel(zone.id),
        isFinal: Boolean = zone.id == Route.FINAL_ZONE_ID,
    ): List<String> {
        val errors = mutableListOf<String>()
        val id = zone.id
        if (zone.baseLevel != baseLevel) {
            errors += "$id: baseLevel ${zone.baseLevel} should be $baseLevel"
        }
        if (isFinal) {
            if (zone.nodes.size != 1) errors += "$id: must have a single node, has ${zone.nodes.size}"
            zone.nodes.forEach { node ->
                val key = "${id}_final"
                if (node.encounter != null) errors += "$key: the final node cannot have an encounter"
                textErrors(key, node, 100..160, errors)
            }
            return errors
        }
        zone.nodes.forEach { node ->
            val key = nodeKey(id, node)
            textErrors(key, node, 40..160, errors)
            encounterErrors(id, key, node, errors)
        }
        val counts = combatCounts(zone)
        val allowed = if (id in Route.REST_ZONES) setOf(0) else setOf(2, 3, 4)
        if (!allowed.containsAll(counts)) {
            errors += "$id: paths have combat counts $counts, expected within $allowed"
        }
        return errors
    }

    private fun textErrors(key: String, node: NodeContent, range: IntRange, errors: MutableList<String>) {
        val words = node.text.trim().split(Regex("\\s+")).size
        if (words !in range) errors += "$key: text has $words words, expected $range"
        if (!node.imagePrompt.endsWith(PROMPT_SUFFIX)) errors += "$key: imagePrompt must end with the standard suffix"
    }

    private fun matches(rival: RivalContent, entry: BeastEntry) =
        rival.pattern == entry.pattern && rival.everyNTicks == entry.everyNTicks

    private fun portraitErrors(key: String, rivals: List<RivalContent>, bossZone: String?, errors: MutableList<String>) {
        rivals.forEachIndexed { i, rival ->
            val expected = if (bossZone != null && i == 0) bossPortrait(bossZone) else null
            when {
                !PORTRAIT_FORMAT.matches(rival.portrait) -> errors += "$key: rival $i portrait '${rival.portrait}' must match [a-z_]+"
                expected != null && rival.portrait != expected -> errors += "$key: boss portrait must be $expected"
                expected == null && rival.portrait !in COMMON_PORTRAITS -> errors += "$key: rival $i portrait '${rival.portrait}' is not a common type"
            }
        }
    }

    private fun isCommonOf(zoneId: String, rival: RivalContent): Boolean =
        Bestiary.zoneCommons[zoneId].orEmpty().any { matches(rival, Bestiary.common.getValue(it)) }

    private fun encounterErrors(zoneId: String, key: String, node: NodeContent, errors: MutableList<String>) {
        val rivals = node.encounter?.rivals
        when {
            node.level == 1 -> if (rivals != null) errors += "$key: level 1 cannot have an encounter"
            node.level == TreeShape.LEVELS -> bossErrors(zoneId, key, rivals, errors)
            rivals != null -> commonErrors(zoneId, key, rivals, errors)
        }
    }

    private fun bossErrors(zoneId: String, key: String, rivals: List<RivalContent>?, errors: MutableList<String>) {
        if (zoneId in Route.REST_ZONES) {
            if (rivals != null) errors += "$key: $zoneId is a rest zone and cannot have encounters"
            return
        }
        if (rivals.isNullOrEmpty()) {
            errors += "$key: level 7 needs an encounter with the zone boss"
            return
        }
        portraitErrors(key, rivals, zoneId, errors)
        val boss = Bestiary.bosses.getValue(zoneId)
        val first = rivals.first()
        if (!matches(first, boss) || first.levelOffset !in BOSS_OFFSETS) {
            errors += "$key: first rival must be the boss ${boss.name} with offset in +${BOSS_OFFSETS.first}..+${BOSS_OFFSETS.last}"
        }
        // Decision of the user (2026-10-18): every boss fights alone.
        if (rivals.size > 1) errors += "$key: the boss fights alone, found ${rivals.size - 1} extra rivals"
        if (first.reaction != null) errors += "$key: the boss cannot have a reaction, it heals in its defense mode"
        first.pattern.filter(::isShieldOrHeal).forEach { errors += "$key: boss attack pattern cannot shield or heal: $it" }
        val modes = first.modes
        if (modes == null) {
            errors += "$key: the boss needs modes"
        } else {
            modesErrors(modes).forEach { errors += "$key: $it" }
            if (modes != bossModes(zoneId)) errors += "$key: boss modes $modes should be ${bossModes(zoneId)}"
        }
    }

    private fun commonErrors(zoneId: String, key: String, rivals: List<RivalContent>, errors: MutableList<String>) {
        portraitErrors(key, rivals, null, errors)
        if (rivals.size !in 1..4) errors += "$key: encounter needs 1 to 4 rivals, has ${rivals.size}"
        val allowedOffsets = GROUP_OFFSETS[rivals.size] ?: emptySet()
        rivals.forEachIndexed { i, rival ->
            if (!isCommonOf(zoneId, rival)) errors += "$key: rival $i is not a common of $zoneId"
            if (rival.modes != null) errors += "$key: rival $i cannot have modes, only bosses"
            if (rival.levelOffset !in allowedOffsets) {
                errors += "$key: rival $i offset ${rival.levelOffset} should be in $allowedOffsets for ${rivals.size} rivals"
            }
        }
        rivals.zip(expectedCommonReactions(rivals)).forEachIndexed { i, (rival, expected) ->
            if (rival.reaction != expected) errors += "$key: rival $i reaction ${rival.reaction} should be $expected"
        }
    }
}
