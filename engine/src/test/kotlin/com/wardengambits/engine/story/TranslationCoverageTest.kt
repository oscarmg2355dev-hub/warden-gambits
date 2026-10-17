package com.wardengambits.engine.story

import com.wardengambits.engine.story.content.StoryContentLoader
import com.wardengambits.engine.story.content.ZoneTranslation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Languages spec §4: every English overlay file covers its whole Spanish zone, with nothing extra and nothing blank. */
class TranslationCoverageTest {
    companion object {
        /** Rival names that legitimately read the same in both languages (proper names that are not translated). */
        val IDENTICAL_RIVAL_NAMES = setOf("Crebain", "Olog-hai")
    }

    private fun translation(zoneId: String): ZoneTranslation? =
        StoryContentLoader::class.java.getResourceAsStream("/story/en/$zoneId.json")
            ?.use { String(it.readBytes(), Charsets.UTF_8) }
            ?.let { StoryContentLoader.parseTranslation("en/$zoneId.json", it) }

    private fun coverageErrors(zoneId: String, t: ZoneTranslation): List<String> {
        val zone = StoryContentLoader.loadZone("story", zoneId)
        val errors = mutableListOf<String>()
        if (t.id != zoneId) errors += "declares id '${t.id}'"
        if (t.name.isBlank()) errors += "blank zone name"
        val expectedKeys = zone.nodes.map { NodeId(zoneId, it.level, it.index).key }
        (expectedKeys - t.nodes.keys).forEach { errors += "missing node $it" }
        (t.nodes.keys - expectedKeys.toSet()).forEach { errors += "extra node $it" }
        zone.nodes.forEach { node ->
            val key = NodeId(zoneId, node.level, node.index).key
            val tn = t.nodes[key] ?: return@forEach
            if (tn.title.isBlank()) errors += "$key: blank title"
            if (tn.text.isBlank()) errors += "$key: blank text"
            when {
                node.choiceLabel != null && tn.choiceLabel == null -> errors += "$key: missing choiceLabel"
                node.choiceLabel == null && tn.choiceLabel != null -> errors += "$key: extra choiceLabel"
                tn.choiceLabel != null && tn.choiceLabel.isBlank() -> errors += "$key: blank choiceLabel"
            }
        }
        val rivalNames = zone.nodes.flatMap { n -> n.encounter?.rivals.orEmpty().map { it.name } }.toSet()
        (rivalNames - t.rivals.keys).forEach { errors += "missing rival '$it'" }
        (t.rivals.keys - rivalNames).forEach { errors += "extra rival '$it'" }
        t.rivals.filterValues { it.isBlank() }.keys.forEach { errors += "blank name for rival '$it'" }
        return errors
    }

    private fun hardeningErrors(zoneId: String, t: ZoneTranslation): List<String> {
        val zone = StoryContentLoader.loadZone("story", zoneId)
        val errors = mutableListOf<String>()
        zone.nodes.forEach { node ->
            val key = NodeId(zoneId, node.level, node.index).key
            val tn = t.nodes[key] ?: return@forEach
            if (tn.text == node.text) errors += "$key: English text equals the Spanish text"
            if (node.choiceLabel != null && tn.choiceLabel == node.choiceLabel) errors += "$key: choiceLabel equals the Spanish one"
        }
        t.rivals.forEach { (es, en) ->
            if (es == en && es !in IDENTICAL_RIVAL_NAMES) errors += "rival '$es' is not translated"
        }
        if (zoneId != Route.FINAL_ZONE_ID) {
            TreeShape.treeNodes(zoneId).forEach { id ->
                val dests = TreeShape.nextInZone(id)
                if (dests.size != 2) return@forEach
                val labels = dests.map { t.nodes[it.key]?.choiceLabel }
                if (labels[0] != null && labels[0] == labels[1]) {
                    errors += "${id.key}: both destinations share English choiceLabel '${labels[0]}'"
                }
            }
        }
        return errors
    }

    @Test
    fun everyTranslatedZoneIsFullyCovered() {
        val errors = Route.zones.flatMap { zone ->
            val t = translation(zone.id) ?: return@flatMap emptyList()
            coverageErrors(zone.id, t).map { "en/${zone.id}.json: $it" }
        }
        assertTrue(errors.isEmpty(), "Translation coverage errors:\n" + errors.joinToString("\n"))
    }

    @Test
    fun allZonesTranslated() {
        val present = Route.zones.map { it.id }.filter { translation(it) != null }
        assertEquals(Route.zones.map { it.id }.sorted(), present.sorted())
    }

    @Test
    fun translationsAreReallyEnglishAndNavigable() {
        val errors = Route.zones.flatMap { zone ->
            val t = translation(zone.id) ?: return@flatMap emptyList()
            hardeningErrors(zone.id, t).map { "en/${zone.id}.json: $it" }
        }
        assertTrue(errors.isEmpty(), "Translation quality errors:\n" + errors.joinToString("\n"))
    }
}
