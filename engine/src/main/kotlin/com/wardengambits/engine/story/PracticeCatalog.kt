package com.wardengambits.engine.story

import com.wardengambits.engine.story.content.RivalContent
import com.wardengambits.engine.story.content.StoryContent

/** One fight of the practice mode: the encounter of [node], fought at the Warden's [playerLevel] (expected level). */
data class PracticeEntry(
    val node: NodeId,
    val zoneId: String,
    val level: Int,
    val rivals: List<RivalContent>,
    val isBoss: Boolean,
    val unlocked: Boolean,
    val playerLevel: Int,
)

/** Every encounter of the story in route order, with its unlock state derived from the profile's progress. */
object PracticeCatalog {
    fun entries(content: StoryContent, profile: StoryProfile): List<PracticeEntry> {
        val levels = ExpectedLevels(content)
        val run = profile.run
        val everything = run.phase == Phase.LAP_COMPLETE || profile.history.any { it.result == RunResult.COMPLETED }
        val currentZone = Route.indexOf(run.node.zoneId)
        return Route.treeZones.flatMap { zone ->
            val zoneIndex = Route.indexOf(zone.id)
            content.zone(zone.id)?.nodes.orEmpty()
                .sortedWith(compareBy({ it.level }, { it.index }))
                .mapNotNull { node ->
                    val encounter = node.encounter ?: return@mapNotNull null
                    val id = NodeId(zone.id, node.level, node.index)
                    val unlocked = everything || zoneIndex < currentZone || (
                        zoneIndex == currentZone && (
                            node.level < run.node.level || (id == run.node && run.phase == Phase.CHOOSING)
                            )
                        )
                    PracticeEntry(
                        id, zone.id, node.level, encounter.rivals,
                        isBoss = node.level == TreeShape.LEVELS, unlocked = unlocked,
                        playerLevel = levels.expectedLevel(id),
                    )
                }
        }
    }
}
