package com.wardengambits.engine.story.content

import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Route
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object StoryContentLoader {
    private val json = Json { ignoreUnknownKeys = false }

    /** Parses one zone file; any parse error becomes an [IllegalStateException] naming [fileName]. */
    fun parse(fileName: String, json: String): ZoneContent =
        try {
            this.json.decodeFromString<ZoneContent>(json.removePrefix("﻿"))
        } catch (e: SerializationException) {
            throw IllegalStateException("Invalid story file $fileName: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw IllegalStateException("Invalid story file $fileName: ${e.message}", e)
        }

    /** A zone file must declare the id matching its file name. */
    internal fun checkId(fileName: String, expectedId: String, zone: ZoneContent) {
        check(zone.id == expectedId) { "story/$fileName declares id '${zone.id}'" }
    }

    /** Parses one translation overlay ([ZoneTranslation]); any parse error becomes an [IllegalStateException] naming [fileName]. */
    fun parseTranslation(fileName: String, json: String): ZoneTranslation =
        try {
            this.json.decodeFromString<ZoneTranslation>(json.removePrefix("﻿"))
        } catch (e: SerializationException) {
            throw IllegalStateException("Invalid translation file $fileName: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw IllegalStateException("Invalid translation file $fileName: ${e.message}", e)
        }

    /** Applies [translation]'s texts over [zone]; anything it lacks keeps the Spanish text. */
    fun translate(zone: ZoneContent, translation: ZoneTranslation): ZoneContent =
        zone.copy(
            name = translation.name,
            nodes = zone.nodes.map { node ->
                val t = translation.nodes[NodeId(zone.id, node.level, node.index).key]
                node.copy(
                    title = t?.title ?: node.title,
                    text = t?.text ?: node.text,
                    choiceLabel = node.choiceLabel?.let { t?.choiceLabel ?: it },
                    encounter = node.encounter?.let { enc ->
                        enc.copy(rivals = enc.rivals.map { it.copy(name = translation.rivals[it.name] ?: it.name) })
                    },
                )
            },
        )

    /**
     * Reads `/<resourceDir>/<id>.json` for every route zone; a missing file is a packaging error.
     * For a [language] other than Spanish (`"es"`), each zone gets `/<resourceDir>/<language>/<id>.json` merged
     * over it when that file exists (see [loadZone]).
     */
    fun loadFromResources(resourceDir: String = "story", language: String = SPANISH): StoryContent =
        StoryContent(Route.zones.map { loadZone(resourceDir, it.id, language) })

    /**
     * Reads `/<resourceDir>/<zoneId>.json`, in or out of the route; throws if it is missing, malformed or misnamed.
     * For a [language] other than Spanish, merges `/<resourceDir>/<language>/<zoneId>.json` over it; without that
     * file the zone stays in Spanish.
     */
    fun loadZone(resourceDir: String, zoneId: String, language: String = SPANISH): ZoneContent {
        val fileName = "$zoneId.json"
        val text = readResource("/$resourceDir/$fileName")
            ?: throw IllegalStateException("Missing story file: $resourceDir/$fileName")
        val zone = parse(fileName, text).also { checkId(fileName, zoneId, it) }
        if (language == SPANISH) return zone
        return readTranslation(resourceDir, zoneId, language)?.let { translate(zone, it) } ?: zone
    }

    /**
     * [base] (the Spanish story, already parsed) with the [language] overlays of `/<resourceDir>/<language>/` merged
     * over each zone that has one: the same result as [loadFromResources] for [language], without parsing the base
     * files again.
     */
    fun translated(base: StoryContent, language: String, resourceDir: String = "story"): StoryContent {
        if (language == SPANISH) return base
        return StoryContent(base.zones.map { zone -> readTranslation(resourceDir, zone.id, language)?.let { translate(zone, it) } ?: zone })
    }

    private fun readTranslation(resourceDir: String, zoneId: String, language: String): ZoneTranslation? {
        val overlayName = "$language/$zoneId.json"
        val overlay = readResource("/$resourceDir/$overlayName") ?: return null
        val translation = parseTranslation(overlayName, overlay)
        check(translation.id == zoneId) { "$resourceDir/$overlayName declares id '${translation.id}'" }
        return translation
    }

    private fun readResource(path: String): String? =
        StoryContentLoader::class.java.getResourceAsStream(path)?.use { String(it.readBytes(), Charsets.UTF_8) }

    /** The language of the base story files, which hold the mechanics. */
    const val SPANISH = "es"
}
