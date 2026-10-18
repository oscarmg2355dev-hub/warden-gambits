package com.wardengambits.engine.story

import com.wardengambits.engine.story.content.ContentValidator
import com.wardengambits.engine.story.content.RivalContent
import com.wardengambits.engine.story.content.StoryContent
import com.wardengambits.engine.story.content.StoryContentLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RealContentTest {
    private val WRITTEN_ZONES = listOf("bree", "chet", "moscagua", "cima", "puente", "quebradas", "bruinen", "rivendel", "eregion", "caradhras", "durin", "moria", "sombrio", "nimrodel", "lothlorien", "anduin", "argonath", "amonhen", "emynmuil", "cienagas", "ithilien", "cirithungol", "gorgoroth", "orodruin")

    @Test
    fun writtenZonesAreValid() {
        WRITTEN_ZONES.forEach { id ->
            val stream = requireNotNull(RealContentTest::class.java.getResourceAsStream("/story/$id.json")) {
                "Missing story file: story/$id.json"
            }
            val text = stream.use { String(it.readBytes(), Charsets.UTF_8) }
            val zone = StoryContentLoader.parse("$id.json", text)
            assertEquals(id, zone.id)
            val errors = ContentValidator.validate(StoryContent(listOf(zone)))
                .filter { it.startsWith("$id:") || it.startsWith("${id}_") } + ContentRules.errors(zone)
            assertTrue(errors.isEmpty(), "Content errors in $id:\n" + errors.joinToString("\n"))
        }
    }

    /** Content spec §5 («Más enemigos y dificultad +»): 4-rival groups from the third zone on; Bree stays a gentle tutorial. */
    @Test
    fun groupSizesPerZone() {
        val content = StoryContentLoader.loadFromResources()
        fun sizes(zoneId: String) = requireNotNull(content.zone(zoneId)).nodes
            .filter { it.level < TreeShape.LEVELS }
            .mapNotNull { it.encounter?.rivals?.size }
        assertTrue(sizes("bree").all { it <= 2 }, "bree must keep groups of at most 2 rivals: ${sizes("bree")}")
        val late = setOf("caradhras", "durin", "moria", "sombrio", "nimrodel")
        Route.treeZones.map { it.id }.drop(2).filter { it !in Route.REST_ZONES }.forEach { id ->
            val fours = sizes(id).count { it == 4 }
            val min = if (id in late) 2 else 1
            assertTrue(fours >= min, "$id needs at least $min encounters of 4 rivals, has $fours")
        }
    }

    @Test
    fun restZonesHaveNoEncounters() {
        val content = StoryContentLoader.loadFromResources()
        val rest = Route.REST_ZONES.filter { id -> Route.zones.any { it.id == id } }
        assertTrue(rest.isNotEmpty())
        rest.forEach { id ->
            val withEncounter = requireNotNull(content.zone(id)).nodes.filter { it.encounter != null }
            assertTrue(withEncounter.isEmpty(), "$id is a rest zone but has encounters at ${withEncounter.map { it.level to it.index }}")
        }
    }

    @Test
    fun allContentLoadsAndValidates() {
        val errors = ContentValidator.validate(StoryContentLoader.loadFromResources())
        assertTrue(errors.isEmpty(), "Content errors:\n" + errors.joinToString("\n"))
        assertEquals(Route.zones.map { it.id }, WRITTEN_ZONES)
        // 23 trees of 22 nodes plus the single final node.
        assertEquals(507, StoryContentLoader.loadFromResources().zones.sumOf { it.nodes.size })
    }

    /** Every rival of the real content with its zone and whether it is the boss (first rival of a level-7 node). */
    private data class Placed(val zoneId: String, val isBoss: Boolean, val portrait: String)

    private fun placedRivals(): List<Placed> = StoryContentLoader.loadFromResources().zones.flatMap { zone ->
        zone.nodes.flatMap { node ->
            node.encounter?.rivals.orEmpty().mapIndexed { i, r ->
                Placed(zone.id, node.level == TreeShape.LEVELS && i == 0, r.portrait)
            }
        }
    }

    @Test
    fun everyRivalHasKnownPortrait() {
        val bad = placedRivals().filter { !it.isBoss && it.portrait !in ContentRules.COMMON_PORTRAITS }
        assertTrue(bad.isEmpty(), "Rivals with unknown portrait: $bad")
    }

    @Test
    fun everyBossHasItsZonePortrait() {
        val bosses = placedRivals().filter { it.isBoss }
        assertEquals(combatZones().size, bosses.size)
        bosses.forEach { assertEquals(ContentRules.bossPortrait(it.zoneId), it.portrait, "boss of ${it.zoneId}") }
    }

    @Test
    fun portraitsCoverAllTypes() {
        val used = placedRivals().map { it.portrait }.toSet()
        val bosses = combatZones().map { ContentRules.bossPortrait(it) }
        assertEquals(21, bosses.size)
        val expected = ContentRules.COMMON_PORTRAITS + bosses
        assertTrue(used.containsAll(expected), "missing: ${expected - used}")
    }

    private fun combatZones() = Route.treeZones.map { it.id }.filter { it !in Route.REST_ZONES }

    /** Every encounter of the real content with its zone and node level. */
    private data class PlacedEncounter(val zoneId: String, val level: Int, val rivals: List<RivalContent>)

    private fun encounters(): List<PlacedEncounter> = StoryContentLoader.loadFromResources().zones.flatMap { zone ->
        zone.nodes.mapNotNull { node -> node.encounter?.let { PlacedEncounter(zone.id, node.level, it.rivals) } }
    }

    /** Decision of the user (2026-10-18): every zone boss fights alone, without escorts. */
    @Test
    fun everyBossFightsAlone() {
        val bosses = encounters().filter { it.level == TreeShape.LEVELS }
        assertEquals(21, bosses.size)
        bosses.forEach { assertEquals(1, it.rivals.size, "boss of ${it.zoneId} has escorts: ${it.rivals.map { r -> r.name }}") }
    }

    /** Boss-modes spec §1: the 21 bosses carry their attack and defense modes (Bree's, its own tutorial version). */
    @Test
    fun everyBossHasModes() {
        val bosses = encounters().filter { it.level == TreeShape.LEVELS }
        assertEquals(21, bosses.size)
        bosses.forEach { assertEquals(ContentRules.bossModes(it.zoneId), it.rivals.first().modes, "boss of ${it.zoneId}") }
    }

    /** Boss-modes spec §1: `modes` replaces the HP reaction of the bosses, and their pattern is the attack pattern. */
    @Test
    fun noBossHasReaction() {
        val bosses = encounters().filter { it.level == TreeShape.LEVELS }.map { it.zoneId to it.rivals.first() }
        bosses.forEach { (zoneId, boss) ->
            assertNull(boss.reaction, "boss of $zoneId")
            assertTrue(boss.pattern.none(ContentRules::isShieldOrHeal), "boss of $zoneId shields or heals: ${boss.pattern}")
        }
    }

    /** Boss-modes spec §1: the commons keep their healer reactions and never carry modes. */
    @Test
    fun commonsUnchanged() {
        var commons = 0
        encounters().forEach { (zoneId, level, rivals) ->
            val placed = if (level == TreeShape.LEVELS) rivals.drop(1) else rivals
            placed.forEach { assertNull(it.modes, "$zoneId level $level: ${it.name} cannot have modes") }
            if (level != TreeShape.LEVELS) {
                assertEquals(ContentRules.expectedCommonReactions(rivals), rivals.map { it.reaction }, "$zoneId level $level")
            }
            commons += placed.size
        }
        val healers = encounters().flatMap { it.rivals }.count { it.reaction == ContentRules.HEALER_REACTION }
        assertTrue(commons > 0 && healers > 0)
    }

    /** Rival-healing spec §3: one healer per group: the first humanoid of a common encounter, never a boss escort. */
    @Test
    fun oneHealerPerHumanoidEncounter() {
        var healers = 0
        encounters().forEach { (zoneId, level, rivals) ->
            val reactions = rivals.map { it.reaction }
            if (level == TreeShape.LEVELS) {
                assertTrue(reactions.drop(1).all { it == null }, "$zoneId boss escorts cannot react: $reactions")
            } else {
                assertEquals(ContentRules.expectedCommonReactions(rivals), reactions, "$zoneId level $level: ${rivals.map { it.name }}")
                healers += reactions.count { it != null }
            }
        }
        assertTrue(healers > 0)
    }

    @Test
    fun beastsNeverReact() {
        val reacting = encounters().flatMap { e ->
            e.rivals.filterIndexed { i, r -> !(e.level == TreeShape.LEVELS && i == 0) && !ContentRules.isHumanoid(r) && r.reaction != null }
        }
        assertTrue(reacting.isEmpty(), "beasts and wraiths cannot react: ${reacting.map { it.name }}")
    }

    /** Rival-healing spec §3: the Cura I («21») of the Bree and Chet bandits left their pattern; it is only a reaction now. */
    @Test
    fun banditsLostTheirCureOne() {
        val withCure = encounters().flatMap { it.rivals }.filter { "21" in it.pattern }
        assertTrue(withCure.isEmpty(), "patterns with «21»: ${withCure.map { it.name to it.pattern }}")
    }
}
