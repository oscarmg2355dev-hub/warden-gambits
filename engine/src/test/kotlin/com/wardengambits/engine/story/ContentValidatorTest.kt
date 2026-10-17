package com.wardengambits.engine.story

import com.wardengambits.engine.story.content.ContentValidator
import com.wardengambits.engine.story.content.EncounterContent
import com.wardengambits.engine.story.content.NodeContent
import com.wardengambits.engine.story.content.StoryContent
import com.wardengambits.engine.story.content.ZoneContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContentValidatorTest {
    private fun errors(content: StoryContent) = ContentValidator.validate(content)

    private fun StoryContent.mapZone(id: String, transform: (ZoneContent) -> ZoneContent) =
        StoryContent(zones.map { if (it.id == id) transform(it) else it })

    private fun StoryContent.mapNode(zoneId: String, level: Int, index: Int, transform: (NodeContent) -> NodeContent) =
        mapZone(zoneId) { z ->
            z.copy(nodes = z.nodes.map { if (it.level == level && it.index == index) transform(it) else it })
        }

    private fun withEncounter(key: String, encounter: EncounterContent) =
        fakeContent(encounters = mapOf(key to encounter))

    @Test fun fakeContentIsValid() = assertEquals(emptyList(), ContentValidator.validate(fakeContent()))

    /** A zone outside the current route (pending content) is validated on its own, as a tree or as a final zone. */
    @Test
    fun validateZoneOutsideTheRoute() {
        val tree = fakeContent().zone("bree")!!.copy(id = "anduin", name = "Río Anduin", baseLevel = 40)
        assertEquals(emptyList(), ContentValidator.validateZone(tree, isFinal = false))
        val missing = tree.copy(nodes = tree.nodes.filterNot { it.level == 4 && it.index == 5 })
        assertTrue(ContentValidator.validateZone(missing, isFinal = false).any { it.startsWith("anduin_4_5") })
        // The old final zone becomes a tree: its node (1,0) is an ordinary first node.
        val rest = tree.copy(id = "lothlorien", name = "Lothlórien")
        assertEquals(emptyList(), ContentValidator.validateZone(rest, isFinal = false))

        val final = fakeContent().zone(Route.FINAL_ZONE_ID)!!.copy(id = "orodruin", baseLevel = 0)
        assertEquals(emptyList(), ContentValidator.validateZone(final, isFinal = true))
        val withEncounter = final.copy(nodes = final.nodes.map { it.copy(encounter = EncounterContent(listOf(rival()))) })
        assertTrue(ContentValidator.validateZone(withEncounter, isFinal = true).any { it.startsWith("orodruin_final") })
        assertTrue(ContentValidator.validateZone(tree, isFinal = true).any { "outside the tree shape" in it })
    }

    @Test
    fun missingZone() {
        val content = StoryContent(fakeContent().zones.filter { it.id != "moria" })
        assertTrue(errors(content).any { "moria" in it })
    }

    @Test
    fun extraZone() {
        val base = fakeContent()
        val content = StoryContent(base.zones + base.zones.first().copy(id = "mordor"))
        assertTrue(errors(content).any { "mordor" in it })
    }

    @Test
    fun zoneOrderDoesNotMatter() {
        assertEquals(emptyList(), errors(StoryContent(fakeContent().zones.reversed())))
    }

    @Test
    fun wrongZoneName() {
        val content = fakeContent().mapZone("bree") { it.copy(name = "Breee") }
        assertTrue(errors(content).any { "bree" in it })
    }

    @Test
    fun missingNode() {
        val content = fakeContent().mapZone("bree") { z ->
            z.copy(nodes = z.nodes.filterNot { it.level == 4 && it.index == 5 })
        }
        assertTrue(errors(content).any { "bree_4_5" in it })
    }

    @Test
    fun duplicateNode() {
        val content = fakeContent().mapZone("bree") { z ->
            z.copy(nodes = z.nodes + z.nodes.first { it.level == 2 && it.index == 0 })
        }
        assertTrue(errors(content).any { "bree_2_0" in it })
    }

    @Test
    fun nodeOutsideShape() {
        val content = fakeContent().mapZone("bree") { z ->
            z.copy(nodes = z.nodes + z.nodes.first().copy(level = 8, index = 0))
        }
        assertTrue(errors(content).any { "bree_8_0" in it })
    }

    @Test
    fun emptyText() {
        val content = fakeContent().mapNode("bree", 1, 0) { it.copy(title = "", text = " ", imagePrompt = "\n") }
        val result = errors(content)
        assertEquals(3, result.count { "bree_1_0" in it })
        assertEquals(3, result.size)
    }

    @Test
    fun missingChoiceLabel() {
        val content = fakeContent()
            .mapNode("bree", 3, 2) { it.copy(choiceLabel = null) }
            .mapNode("bree", 7, 0) { it.copy(choiceLabel = null) }
        val result = errors(content)
        assertTrue(result.any { "bree_3_2" in it })
        assertTrue(result.none { "bree_7_0" in it })
    }

    @Test
    fun blankChoiceLabelCountsAsMissing() {
        val content = fakeContent().mapNode("bree", 2, 1) { it.copy(choiceLabel = "  ") }
        assertTrue(errors(content).any { "bree_2_1" in it })
    }

    @Test
    fun encounterRivalCount() {
        assertTrue(errors(withEncounter("bree_1_0", EncounterContent(emptyList()))).any { "bree_1_0" in it })
        assertTrue(errors(withEncounter("bree_1_0", EncounterContent(List(5) { rival() }))).any { "bree_1_0" in it })
        assertEquals(emptyList(), errors(withEncounter("bree_1_0", EncounterContent(List(4) { rival() }))))
    }

    @Test
    fun badPattern() {
        val empty = EncounterContent(listOf(rival().copy(pattern = emptyList())))
        val unknown = EncounterContent(listOf(rival().copy(pattern = listOf("999"))))
        val ticks = EncounterContent(listOf(rival().copy(everyNTicks = 0)))
        assertTrue(errors(withEncounter("bree_1_0", empty)).any { "bree_1_0" in it })
        assertTrue(errors(withEncounter("bree_1_0", unknown)).any { "bree_1_0" in it })
        assertTrue(errors(withEncounter("bree_1_0", ticks)).any { "bree_1_0" in it })
    }

    @Test
    fun removedV1CombosAreRejected() {
        listOf("23", "232", "2323").forEach { removed ->
            val content = withEncounter("bree_1_0", EncounterContent(listOf(rival().copy(pattern = listOf("12", removed)))))
            assertTrue(errors(content).any { "bree_1_0" in it && "'$removed'" in it }, "combo $removed must be rejected")
        }
    }

    @Test
    fun blankRivalName() {
        val content = withEncounter("bree_1_0", EncounterContent(listOf(rival(name = " "))))
        assertTrue(errors(content).any { "bree_1_0" in it && "name" in it })
    }

    @Test
    fun baseLevelBelowOne() {
        val content = fakeContent(baseLevels = mapOf("chet" to 0))
        assertTrue(errors(content).any { "chet" in it && "baseLevel" in it })
        assertEquals(emptyList(), errors(fakeContent(baseLevels = mapOf("orodruin" to 0))))
    }

    @Test
    fun duplicateChoiceLabels() {
        val source = TreeShape.nodesOf("bree").first { TreeShape.destinations(it).size == 2 }
        val (a, b) = TreeShape.destinations(source)
        val label = fakeContent().node(a)!!.choiceLabel
        val content = fakeContent().mapNode("bree", b.level, b.index) { it.copy(choiceLabel = label) }
        assertTrue(errors(content).any { source.key in it })
    }

    @Test
    fun finalNodeEncounterRejected() {
        val content = withEncounter("orodruin_final", EncounterContent(listOf(rival())))
        assertTrue(errors(content).any { "orodruin_final" in it })
    }

    @Test
    fun fakeContentBaseLevels() {
        val zones = fakeContent().zones.associateBy { it.id }
        assertEquals(1, zones.getValue("bree").baseLevel)
        assertEquals(4, zones.getValue("chet").baseLevel)
        assertEquals(0, zones.getValue("orodruin").baseLevel)
        assertEquals(1, zones.getValue("orodruin").nodes.size)
        assertEquals(22, zones.getValue("lothlorien").nodes.size)
    }
}
