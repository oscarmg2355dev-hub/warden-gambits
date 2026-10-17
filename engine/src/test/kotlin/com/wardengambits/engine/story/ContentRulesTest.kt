package com.wardengambits.engine.story

import com.wardengambits.engine.combo.ComboFamily
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.story.content.EncounterContent
import com.wardengambits.engine.story.content.ModesContent
import com.wardengambits.engine.story.content.NodeContent
import com.wardengambits.engine.story.content.RivalContent
import com.wardengambits.engine.story.content.ZoneContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ContentRulesTest {
    private fun words(n: Int) = List(n) { "palabra" }.joinToString(" ")

    private fun prompt(key: String) = "Scene of $key" + ContentRules.PROMPT_SUFFIX

    private fun fromBestiary(key: String, offset: Int): RivalContent {
        val entry = Bestiary.common.getValue(key)
        return RivalContent(entry.name, entry.portrait, offset, entry.pattern, entry.everyNTicks)
    }

    /** A humanoid common carrying the healer reaction (the first humanoid of its encounter). */
    private fun healer(key: String, offset: Int) = fromBestiary(key, offset).copy(reaction = ContentRules.HEALER_REACTION)

    private fun boss(zoneId: String, offset: Int = 2): RivalContent {
        val entry = Bestiary.bosses.getValue(zoneId)
        return RivalContent(
            entry.name, ContentRules.bossPortrait(zoneId), offset, entry.pattern, entry.everyNTicks, modes = ContentRules.bossModes(zoneId),
        )
    }

    private fun encounter(vararg rivals: RivalContent) = EncounterContent(rivals.toList())

    /** Valid bree zone: boss at level 7, one bandit at every level-3 node. */
    private fun validBree(): ZoneContent = zoneWith(
        "bree",
        buildMap {
            put("bree_7_0", encounter(boss("bree")))
            (0 until 4).forEach { put("bree_3_$it", encounter(healer("bandido", 0))) }
        },
    )

    private fun zoneWith(
        id: String,
        encounters: Map<String, EncounterContent>,
        words: Int = 60,
        baseLevel: Int = ContentRules.expectedBaseLevel(id),
    ): ZoneContent {
        val base = fakeContent(encounters, mapOf(id to baseLevel)).zone(id)!!
        return base.copy(
            nodes = base.nodes.map {
                val key = NodeId(id, it.level, it.index).key
                it.copy(text = words(words), imagePrompt = prompt(key))
            },
        )
    }

    private fun ZoneContent.mapNode(level: Int, index: Int, transform: (NodeContent) -> NodeContent) =
        copy(nodes = nodes.map { if (it.level == level && it.index == index) transform(it) else it })

    private fun ZoneContent.withEncounter(level: Int, index: Int, encounter: EncounterContent?) =
        mapNode(level, index) { it.copy(encounter = encounter) }

    private fun assertErrorFor(key: String, zone: ZoneContent) {
        val errors = ContentRules.errors(zone)
        assertTrue(errors.any { it.startsWith(key) }, "expected an error for $key, got $errors")
    }

    @Test
    fun bestiaryTables() {
        assertEquals(24, Bestiary.common.size)
        assertEquals(21, Bestiary.bosses.size)
        assertEquals(18, ContentRules.COMMON_PORTRAITS.size)
        assertEquals(setOf("trasgo", "orcoCapitan", "trolCavernas"), Bestiary.zoneCommons["moria"])
        Route.REST_ZONES.forEach { assertEquals(emptySet(), Bestiary.zoneCommons[it], it) }
    }

    /**
     * Combat v2 (spec 2026-10-12 §8): commons rewritten for the new catalog, running real chains. Damage chains for
     * everyone (2026-10-13): Moscardones, Espía and Crebain now chain damage too. Rival healing (2026-10-16): the bandit
     * lost its Cura I; humanoids heal through their reaction instead.
     */
    private val v2Commons = mapOf(
        "bandido" to BeastEntry("Bandido del camino", listOf("12", "121"), 2),
        "lobo" to BeastEntry("Lobo hambriento", listOf("12", "121", "1212"), 2),
        "moscardones" to BeastEntry("Moscardones de Moscagua", listOf("32", "323", "13"), 2),
        "espia" to BeastEntry("Espía dunlendino", listOf("13", "131", "31"), 2),
        "huargo" to BeastEntry("Huargo", listOf("12", "121", "1212", "12121"), 2),
        "crebain" to BeastEntry("Crebain", listOf("12", "121", "1212", "31"), 2),
        "loboSaruman" to BeastEntry("Lobo de Saruman", listOf("13", "131", "1313"), 2),
        "trasgo" to BeastEntry("Trasgo de Moria", listOf("13", "131"), 1),
        "orcoCapitan" to BeastEntry("Orco capitán", listOf("11", "12", "121", "1212"), 1),
        "trolCavernas" to BeastEntry("Trol de las cavernas", listOf("2132", "12", "121", "1212", "12121"), 2),
        "orcoMontanas" to BeastEntry("Orco de las Montañas Nubladas", listOf("11", "12", "121", "1212", "22"), 2),
        "arquero" to BeastEntry("Arquero orco", listOf("32", "323"), 1),
    )

    /**
     * Combat v2 (spec 2026-10-12 §8): bosses open with damage, mostly damage, at most one heal. «Más enemigos y
     * dificultad +» (2026-10-13): the Lobo gris alfa trades «33» for a second Aturdir tier; the Jinete Negro and the Jefe
     * de los arqueros orcos stun twice per cycle (the latter without its Cura). Rival healing (2026-10-16): the Lobo gris
     * alfa, the Espectro del Anillo, the Furia del Caradhras and the Guardián del Agua lose Aturdir tiers and the Capitán
     * orco gains a Golpe directo. Boss modes (2026-10-17): the Capitán dunlendino and the Jefe de los arqueros orcos
     * lose their Escudo táctico; only the defense mode shields now (with Escudo maestro). Escudo táctico immunity
     * (2026-10-17): most bosses add a second damage chain and drop their extra Aturdir, Furia or Guardia.
     */
    private val v2Bosses = mapOf(
        "bree" to BeastEntry("Cabecilla de los bandidos", listOf("12", "121", "11", "31"), 4),
        "chet" to BeastEntry("Lobo gris alfa", listOf("12", "121", "1212", "12121", "31"), 1),
        "moscagua" to BeastEntry("Reina del enjambre", listOf("32", "323", "3232", "13", "131", "1313", "31"), 1),
        "cima" to BeastEntry("Espectro del Anillo", listOf("32", "323", "3232", "12", "121", "31"), 1),
        "puente" to BeastEntry("Jefe de la manada de huargos", listOf("12", "121", "1212", "13", "131", "31", "11"), 1),
        "quebradas" to BeastEntry("Trol de piedra", listOf("12", "121", "1212", "13", "131", "31"), 1),
        "bruinen" to BeastEntry("Jinete Negro", listOf("32", "323", "3232", "13", "131", "3123", "31232", "31"), 1),
        "eregion" to BeastEntry("Capitán dunlendino", listOf("12", "121", "1212", "13", "131", "31", "123"), 1),
        "caradhras" to BeastEntry("Furia del Caradhras", listOf("32", "323", "3232", "12", "121", "1212", "31", "123"), 1),
        "durin" to BeastEntry("Guardián del Agua", listOf("13", "131", "1313", "12", "121", "1212", "31", "123"), 1),
        "moria" to BeastEntry("Sombra de Durin", listOf("32", "323", "3232", "13", "131", "31", "123"), 1),
        "sombrio" to BeastEntry("Capitán orco de las Montañas Nubladas", listOf("12", "121", "1212", "13", "131", "31", "123"), 1),
        "nimrodel" to BeastEntry("Jefe de los arqueros orcos", listOf("32", "323", "3232", "12", "121", "31"), 1),
    )

    @Test
    fun v2CommonPatterns() {
        assertEquals(v2Commons, Bestiary.common.filterKeys { it in v2Commons }.mapValues { it.value.copy(portrait = "") })
    }

    @Test
    fun v2BossPatterns() {
        assertEquals(v2Bosses, Bestiary.bosses.filterKeys { it in v2Bosses })
    }

    @Test
    fun patternsUseOnlyV2Combos() {
        (Bestiary.common.values + Bestiary.bosses.values).forEach { entry ->
            entry.pattern.forEach { assertTrue(Combos.find(it) != null, "${entry.name}: unknown combo $it") }
        }
    }

    /** Spec v2 §4/§8: a tier n > 1 combo is always cast right after tier n - 1 of its family (cyclic order), so it chains. */
    @Test
    fun tieredCombosFormRealChains() {
        (Bestiary.common.values + Bestiary.bosses.values).forEach { entry ->
            val combos = entry.pattern.map { requireNotNull(Combos.find(it)) { "unknown combo $it" } }
            combos.forEachIndexed { i, combo ->
                if (combo.tier >= 2) {
                    val previous = combos[(i - 1 + combos.size) % combos.size]
                    assertTrue(
                        previous.family == combo.family && previous.tier == combo.tier - 1,
                        "${entry.name}: ${combo.sequence} must follow tier ${combo.tier - 1} of ${combo.family}: ${entry.pattern}",
                    )
                }
            }
        }
    }

    @Test
    fun bossesOpenWithDamageAndMostlyDamage() {
        fun isDamage(seq: String) = requireNotNull(Combos.find(seq)) { "unknown combo $seq" }
            .family in DAMAGE_FAMILIES
        Bestiary.bosses.forEach { (zone, entry) ->
            val pattern = entry.pattern
            assertTrue(isDamage(pattern.first()), "$zone must open with a damage combo: $pattern")
            assertTrue(2 * pattern.count(::isDamage) >= pattern.size, "$zone: at least half damage casts: $pattern")
            assertTrue(pattern.count { Combos.find(it)?.family == ComboFamily.HEAL } <= 1, "$zone: at most one self-heal: $pattern")
        }
    }

    /** 2026-10-13: every rival hurts: at least half of its casts are damage and it runs at least one damage chain. */
    @Test
    fun everyRivalIsMostlyDamageWithADamageChain() {
        (Bestiary.common.values + Bestiary.bosses.values).forEach { entry ->
            val combos = entry.pattern.map { requireNotNull(Combos.find(it)) { "unknown combo $it" } }
            val damage = combos.count { it.family in DAMAGE_FAMILIES }
            assertTrue(2 * damage >= combos.size, "${entry.name}: at least half damage casts: ${entry.pattern}")
            val chain = combos.indices.any { i ->
                val combo = combos[i]
                val previous = combos[(i - 1 + combos.size) % combos.size]
                combo.family in DAMAGE_FAMILIES && combo.tier >= 2 &&
                    previous.family == combo.family && previous.tier == combo.tier - 1
            }
            assertTrue(chain, "${entry.name}: needs a damage chain (tier n after tier n - 1): ${entry.pattern}")
        }
    }

    /** Road to Mordor (spec 2026-10-16 §3): the new commons and the 8 new bosses, with their portraits. */
    private val newCommonKeys = mapOf(
        "uruk" to "uruk", "orcoMordor" to "mordor_orc", "loboRocas" to "wolf", "fuegoFatuo" to "wisp",
        "espectroCienaga" to "marsh_wraith", "guerreroHaradrim" to "haradrim", "arqueroHaradrim" to "haradrim",
        "orcoMorgul" to "mordor_orc", "arana" to "spider", "urukNegro" to "uruk", "ologHai" to "troll",
        "huargoAnduin" to "warg",
    )
    private val newBossZones = listOf("anduin", "argonath", "amonhen", "emynmuil", "cienagas", "ithilien", "cirithungol", "gorgoroth")

    @Test
    fun newBestiaryRulesHold() {
        fun isDamage(seq: String) = requireNotNull(Combos.find(seq)) { "unknown combo $seq" }.family in DAMAGE_FAMILIES
        fun hasDamageChain(pattern: List<String>): Boolean {
            val combos = pattern.map { requireNotNull(Combos.find(it)) }
            return combos.indices.any { i ->
                val previous = combos[(i - 1 + combos.size) % combos.size]
                combos[i].family in DAMAGE_FAMILIES && combos[i].tier >= 2 &&
                    previous.family == combos[i].family && previous.tier == combos[i].tier - 1
            }
        }
        val commons = newCommonKeys.map { (key, portrait) ->
            val entry = assertNotNull(Bestiary.common[key], "missing common $key")
            assertEquals(portrait, entry.portrait, key)
            assertTrue(portrait in ContentRules.COMMON_PORTRAITS, "$key: portrait $portrait")
            entry
        }
        val bosses = newBossZones.map { zone ->
            val entry = assertNotNull(Bestiary.bosses[zone], "missing boss of $zone")
            assertTrue("31" in entry.pattern, "$zone boss needs an Aturdir (31): ${entry.pattern}")
            assertTrue(isDamage(entry.pattern.first()), "$zone boss must open with damage: ${entry.pattern}")
            entry
        }
        (commons + bosses).forEach { entry ->
            assertTrue(entry.everyNTicks >= 1, entry.name)
            assertTrue(2 * entry.pattern.count(::isDamage) >= entry.pattern.size, "${entry.name}: at least half damage: ${entry.pattern}")
            assertTrue(hasDamageChain(entry.pattern), "${entry.name}: needs a damage chain: ${entry.pattern}")
        }
        // The existing commons reused on the road to Mordor.
        listOf("arquero", "huargo", "crebain").forEach { assertNotNull(Bestiary.common[it]) }
        assertEquals(
            setOf("uruk", "mordor_orc", "wisp", "marsh_wraith", "haradrim", "spider"),
            ContentRules.COMMON_PORTRAITS - setOf(
                "bandit", "wolf", "wolf_saruman", "warg", "warg_rider", "dunlending",
                "crebain", "swarm", "goblin", "orc", "orc_archer", "troll",
            ),
        )
        newBossZones.forEach { zone ->
            val commonsOfZone = assertNotNull(Bestiary.zoneCommons[zone], "missing commons of $zone")
            assertTrue(commonsOfZone.isNotEmpty() && commonsOfZone.all { it in Bestiary.common }, "$zone: $commonsOfZone")
        }
    }

    /** The new Lothlórien is a rest tree (no combats) with the baseLevel of the global constraints. */
    @Test
    fun restTreeOutsideTheRoute() {
        val base = zoneWith("rivendel", emptyMap())
        val lothlorien = base.copy(id = "lothlorien", name = "Lothlórien", baseLevel = 40)
        assertEquals(emptyList(), ContentRules.errors(lothlorien, baseLevel = 40, isFinal = false))
        val withCombat = lothlorien.withEncounter(3, 0, encounter(rival()))
        assertTrue(ContentRules.errors(withCombat, baseLevel = 40, isFinal = false).any { it.startsWith("lothlorien_3_0") })
    }

    @Test
    fun baseLevels() {
        assertEquals(1, ContentRules.expectedBaseLevel("bree"))
        assertEquals(22, ContentRules.expectedBaseLevel("rivendel"))
        assertEquals(22, ContentRules.expectedBaseLevel("eregion"))
        assertEquals(37, ContentRules.expectedBaseLevel("nimrodel"))
        assertEquals(40, ContentRules.expectedBaseLevel("lothlorien"))
        assertEquals(40, ContentRules.expectedBaseLevel("anduin"))
        assertEquals(61, ContentRules.expectedBaseLevel("gorgoroth"))
        assertEquals(0, ContentRules.expectedBaseLevel("orodruin"))
    }

    @Test
    fun combatCountsNoEncounters() = assertEquals(setOf(0), ContentRules.combatCounts(fakeContent().zone("bree")!!))

    @Test
    fun combatCountsMixed() {
        val zone = fakeContent(mapOf("bree_7_0" to encounter(rival()), "bree_4_0" to encounter(rival()))).zone("bree")!!
        // bree_4_0 is reached only from some level-3 nodes, so paths have either 1 or 2 combats.
        assertEquals(setOf(1, 2), ContentRules.combatCounts(zone))
    }

    @Test
    fun validZonePasses() = assertEquals(emptyList(), ContentRules.errors(validBree()))

    @Test
    fun detectsEachRule() {
        val zone = validBree()
        val bandit = healer("bandido", 0)
        // 1: encounter on level 1
        assertErrorFor("bree_1_0", zone.withEncounter(1, 0, encounter(bandit)))
        // 2: level 7 without boss
        assertErrorFor("bree_7_0", zone.withEncounter(7, 0, null))
        assertErrorFor("bree_7_0", zone.withEncounter(7, 0, encounter(bandit.copy(levelOffset = 2))))
        // 2: boss not first
        assertErrorFor("bree_7_0", zone.withEncounter(7, 0, encounter(fromBestiary("bandido", -2), boss("bree"))))
        // 3: single rival with offset 2
        assertErrorFor("bree_3_0", zone.withEncounter(3, 0, encounter(bandit.copy(levelOffset = 2))))
        // 3: two rivals with offset 0
        assertErrorFor("bree_3_0", zone.withEncounter(3, 0, encounter(bandit, bandit)))
        // 5: unknown pattern
        assertErrorFor("bree_3_0", zone.withEncounter(3, 0, encounter(bandit.copy(pattern = listOf("12", "22")))))
        // 5: common not of the zone
        assertErrorFor("bree_3_0", zone.withEncounter(3, 0, encounter(fromBestiary("huargo", 0))))
        // 4: path with too many combats
        val crowded = (2..6).fold(zone) { acc, level ->
            (0 until TreeShape.size(level)).fold(acc) { z, i -> z.withEncounter(level, i, encounter(bandit)) }
        }
        assertErrorFor("bree:", crowded)
        // 4: path with too few combats
        assertErrorFor("bree:", zone.withEncounter(3, 0, null))
        // 6: baseLevel
        assertErrorFor("bree:", zone.copy(baseLevel = 2))
        // 7: prompt suffix and word count
        assertErrorFor("bree_2_0", zone.mapNode(2, 0) { it.copy(imagePrompt = "Scene without suffix") })
        assertErrorFor("bree_2_1", zone.mapNode(2, 1) { it.copy(text = words(20)) })
    }

    @Test
    fun rivendelZeroCombatsAllowed() {
        val valid = zoneWith("rivendel", emptyMap())
        assertEquals(22, valid.baseLevel)
        assertEquals(emptyList(), ContentRules.errors(valid))
        assertErrorFor("rivendel_3_0", valid.withEncounter(3, 0, encounter(rival())))
        assertErrorFor("rivendel_7_0", valid.withEncounter(7, 0, encounter(boss("bree"))))
    }

    @Test
    fun finalZoneRules() {
        val valid = zoneWith("orodruin", emptyMap(), words = 120)
        assertEquals(emptyList(), ContentRules.errors(valid))
        assertErrorFor("orodruin_final", zoneWith("orodruin", emptyMap(), words = 80))
        assertErrorFor("orodruin_final", valid.withEncounter(1, 0, encounter(rival())))
    }

    @Test
    fun groupOffsets() {
        val zone = validBree()
        fun bandits(n: Int, offset: Int) =
            encounter(*Array(n) { if (it == 0) healer("bandido", offset) else fromBestiary("bandido", offset) })
        // Valid: 1 rival 0/+1, 2 rivals -2, 3 rivals -4, 4 rivals -5.
        listOf(1 to 0, 1 to 1, 2 to -2, 3 to -4, 4 to -5).forEach { (n, offset) ->
            assertEquals(emptyList(), ContentRules.errors(zone.withEncounter(3, 0, bandits(n, offset))), "$n rivals at $offset")
        }
        // Old offsets are rejected.
        listOf(2 to -1, 3 to -2, 4 to -3, 3 to -3, 4 to -4).forEach { (n, offset) ->
            assertErrorFor("bree_3_0", zone.withEncounter(3, 0, bandits(n, offset)))
        }
        // Boss escorts: -4.
        val escorted = zone.withEncounter(7, 0, encounter(boss("bree"), fromBestiary("lobo", -4), fromBestiary("bandido", -4)))
        assertEquals(emptyList(), ContentRules.errors(escorted))
        assertErrorFor("bree_7_0", zone.withEncounter(7, 0, encounter(boss("bree"), fromBestiary("lobo", -2))))
    }

    /** Rival-healing spec §3: reactions are mandatory for the boss and the first humanoid, forbidden for everyone else. */
    @Test
    fun detectsReactionRules() {
        val zone = validBree()
        val wolf = fromBestiary("lobo", -2)
        // Valid: the first humanoid heals even behind a beast; Bree's boss with its own reaction and a quiet escort.
        assertEquals(emptyList(), ContentRules.errors(zone.withEncounter(3, 0, encounter(wolf, healer("bandido", -2)))))
        assertEquals(emptyList(), ContentRules.errors(zone.withEncounter(7, 0, encounter(boss("bree"), fromBestiary("bandido", -4)))))
        // Boss with a reaction (it heals in its defense mode now).
        assertErrorFor("bree_7_0", zone.withEncounter(7, 0, encounter(boss("bree").copy(reaction = ContentRules.HEALER_REACTION))))
        // Escort that heals.
        assertErrorFor("bree_7_0", zone.withEncounter(7, 0, encounter(boss("bree"), healer("bandido", -4))))
        // Humanoid encounter without healer, two healers, healer that is not the first humanoid, beast that heals.
        assertErrorFor("bree_3_0", zone.withEncounter(3, 0, encounter(fromBestiary("bandido", 0))))
        assertErrorFor("bree_3_0", zone.withEncounter(3, 0, encounter(healer("bandido", -2), healer("bandido", -2))))
        assertErrorFor("bree_3_0", zone.withEncounter(3, 0, encounter(fromBestiary("bandido", -2), healer("bandido", -2))))
        assertErrorFor("bree_3_0", zone.withEncounter(3, 0, encounter(fromBestiary("lobo", 0).copy(reaction = ContentRules.HEALER_REACTION))))
    }

    /** Rival-healing spec §3: the combos of the healer reaction exist and heal. */
    @Test
    fun reactionCombosFormChain() {
        val reaction = ContentRules.HEALER_REACTION
        assertEquals(null, reaction.shield)
        val combos = reaction.combos.map { assertNotNull(Combos.find(it), "unknown combo $it in $reaction") }
        assertTrue(combos.all { it.family == ComboFamily.HEAL }, "reactions heal: $reaction")
        assertTrue(reaction.atHpPct.all { it in 1..99 }, "$reaction")
    }

    /** Boss-modes spec §1: attack patterns keep the boss rules without shields or heals, which go to the defense. */
    @Test
    fun attackPatternsHaveNoShieldsOrHeals() {
        Bestiary.bosses.forEach { (zone, entry) ->
            val pattern = entry.pattern
            assertTrue(pattern.none(ContentRules::isShieldOrHeal), "$zone attack pattern shields or heals: $pattern")
            assertTrue(2 * pattern.count(ContentRules::isDamage) >= pattern.size, "$zone: at least half damage: $pattern")
            assertTrue("31" in pattern, "$zone needs an Aturdir (31): $pattern")
            val combos = pattern.map { requireNotNull(Combos.find(it)) }
            val chain = combos.indices.any { i ->
                val previous = combos[(i - 1 + combos.size) % combos.size]
                combos[i].family in DAMAGE_FAMILIES && combos[i].tier >= 2 &&
                    previous.family == combos[i].family && previous.tier == combos[i].tier - 1
            }
            assertTrue(chain, "$zone needs a damage chain: $pattern")
        }
    }

    /**
     * Boss-modes spec §1: the defense is Escudo maestro, the full Cura chain and the first two damage combos of the
     * attack pattern; Bree's, 40/60, one defense, Cura I–II and one damage combo. Every defense ends with damage. The
     * thresholds and defenses are 50/70/3 but for the re-balanced bosses of [TUNED_MODES].
     */
    /** tuning.md «Jefes con modo ataque y defensa» / «Escudo táctico: inmunidad total»: defenseBelowPct and maxDefenses. */
    private val TUNED_MODES: Map<String, Pair<Int, Int>> = mapOf(
        "chet" to (10 to 1), "moscagua" to (10 to 1), "cima" to (15 to 1), "puente" to (50 to 1),
        "quebradas" to (20 to 2), "bruinen" to (15 to 1), "eregion" to (15 to 1), "caradhras" to (15 to 1),
        "durin" to (15 to 1), "moria" to (15 to 1), "sombrio" to (20 to 2), "nimrodel" to (20 to 1),
        "anduin" to (15 to 1), "argonath" to (15 to 1), "amonhen" to (15 to 1), "emynmuil" to (10 to 1),
        "cienagas" to (15 to 1), "ithilien" to (15 to 1), "cirithungol" to (15 to 1), "gorgoroth" to (15 to 1),
    )

    @Test
    fun defenseFollowsRule() {
        assertEquals(ModesContent(40, 60, 1, listOf("21", "212", "12")), ContentRules.bossModes("bree"))
        assertEquals(
            ModesContent(15, 70, 1, listOf("2132", "21", "212", "2121", "21212", "3123", "31232")),
            ContentRules.bossModes("cienagas"),
        )
        assertEquals(listOf("2132", "21", "212", "2121", "21212", "12", "121"), ContentRules.bossModes("eregion").defense)
        assertEquals(listOf("2132", "21", "212", "2121", "21212", "13", "131"), ContentRules.bossModes("amonhen").defense)
        // Re-balance (tuning.md «Jefes con modo ataque y defensa»): some bosses defend later and fewer times.
        assertEquals(ModesContent(10, 70, 1, listOf("2132", "21", "212", "2121", "21212", "12", "121")), ContentRules.bossModes("chet"))
        assertEquals(Pair(20, 2), ContentRules.bossModes("sombrio").let { it.defenseBelowPct to it.maxDefenses })
        (Bestiary.bosses.keys - "bree").forEach { zone ->
            val modes = ContentRules.bossModes(zone)
            val (below, max) = TUNED_MODES[zone] ?: (50 to 3)
            assertEquals(Triple(below, 70, max), Triple(modes.defenseBelowPct, modes.attackAbovePct, modes.maxDefenses), zone)
            val damage = Bestiary.bosses.getValue(zone).pattern.filter(ContentRules::isDamage).take(2)
            assertEquals(listOf("2132", "21", "212", "2121", "21212") + damage, modes.defense, zone)
        }
        Bestiary.bosses.keys.forEach { zone ->
            val modes = ContentRules.bossModes(zone)
            assertEquals(emptyList(), ContentRules.modesErrors(modes), zone)
            assertTrue(ContentRules.isDamage(modes.defense.last()), "$zone defense must end with damage: ${modes.defense}")
        }
    }

    /** Boss-modes spec §1: modes are mandatory and exact for the boss and forbidden for everyone else. */
    @Test
    fun detectsModesRules() {
        val zone = validBree()
        fun withBossModes(modes: ModesContent?) = zone.withEncounter(7, 0, encounter(boss("bree").copy(modes = modes)))
        assertErrorFor("bree_7_0", withBossModes(null))
        assertErrorFor("bree_7_0", withBossModes(ContentRules.bossModes("chet")))
        // A defense that does not end with damage leaves a Dazed boss nowhere to jump.
        val noDamageEnd = ContentRules.bossModes("bree").copy(defense = listOf("21", "212"))
        assertTrue(ContentRules.modesErrors(noDamageEnd).any { "end with a damage combo" in it })
        assertErrorFor("bree_7_0", withBossModes(noDamageEnd))
        assertTrue(ContentRules.modesErrors(ContentRules.bossModes("bree").copy(attackAbovePct = 40)).isNotEmpty())
        assertTrue(ContentRules.modesErrors(ContentRules.bossModes("bree").copy(maxDefenses = 0)).isNotEmpty())
        assertTrue(ContentRules.modesErrors(ContentRules.bossModes("bree").copy(defense = listOf("99", "12"))).isNotEmpty())
        // Shield or heal in the attack pattern.
        val shielded = boss("bree").copy(pattern = listOf("12", "121", "11", "31", "2132"))
        assertErrorFor("bree_7_0", zone.withEncounter(7, 0, encounter(shielded)))
        // Escort or common with modes.
        val withModes = fromBestiary("lobo", -4).copy(modes = ContentRules.bossModes("bree"))
        assertErrorFor("bree_7_0", zone.withEncounter(7, 0, encounter(boss("bree"), withModes)))
        assertErrorFor("bree_3_0", zone.withEncounter(3, 0, encounter(withModes.copy(levelOffset = 0))))
    }

    @Test
    fun bossOffsetRange() {
        val zone = validBree()
        // §5: the boss offset may be tuned per boss within +2..+15 (rival-healing re-balance).
        (2..15).forEach { offset ->
            assertEquals(emptyList(), ContentRules.errors(zone.withEncounter(7, 0, encounter(boss("bree", offset)))), "boss at +$offset")
        }
        listOf(1, 0, 16, 17).forEach { offset ->
            assertErrorFor("bree_7_0", zone.withEncounter(7, 0, encounter(boss("bree", offset))))
        }
    }
}

/** Families whose combos hurt opponents (spec v2 §3). */
private val DAMAGE_FAMILIES = ContentRules.DAMAGE_FAMILIES
