package com.wardengambits.engine.story

import com.wardengambits.engine.story.content.EncounterContent
import com.wardengambits.engine.story.content.NodeContent
import com.wardengambits.engine.story.content.RivalContent
import com.wardengambits.engine.story.content.StoryContent
import com.wardengambits.engine.story.content.ZoneContent

fun rival(name: String = "Orco", offset: Int = 0, portrait: String = "bandit") = RivalContent(name, portrait, offset, listOf("12"), 1)

/** Complete valid content for all the zones of the route; encounters and base levels can be overridden. */
fun fakeContent(
    encounters: Map<String, EncounterContent> = emptyMap(),
    baseLevels: Map<String, Int> = emptyMap(),
): StoryContent = StoryContent(
    Route.zones.map { zone ->
        val zoneIndex = Route.indexOf(zone.id)
        val defaultBase = if (zone.id == Route.FINAL_ZONE_ID) 0 else 1 + 3 * zoneIndex
        ZoneContent(
            id = zone.id,
            name = zone.name,
            baseLevel = baseLevels[zone.id] ?: defaultBase,
            nodes = TreeShape.nodesOf(zone.id).map { node ->
                NodeContent(
                    level = node.level,
                    index = node.index,
                    title = "T ${node.key}",
                    text = "X ${node.key}",
                    imagePrompt = "P ${node.key}",
                    choiceLabel = if (node.level in 2..6) "C ${node.key}" else null,
                    encounter = encounters[node.key],
                )
            },
        )
    },
)
