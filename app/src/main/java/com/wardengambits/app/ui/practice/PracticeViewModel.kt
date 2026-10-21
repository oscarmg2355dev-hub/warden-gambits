package com.wardengambits.app.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wardengambits.app.GameSession
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Route
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.StoryProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PracticeCellUi(
    val node: NodeId,
    val title: String,
    val portrait: String,
    val isBoss: Boolean,
    val rivalCount: Int,
    val playerLevel: Int,
    val unlocked: Boolean,
)

data class PracticeZoneUi(val zoneId: String, val name: String, val cells: List<PracticeCellUi>)

/** The practice grid: every encounter of the story grouped by zone, in the story's current language. */
class PracticeViewModel(session: GameSession, story: StateFlow<StoryEngine>) : ViewModel() {
    val zones: StateFlow<List<PracticeZoneUi>> = combine(session.profile, story, ::zonesFor)
        .stateIn(viewModelScope, SharingStarted.Eagerly, zonesFor(session.profile.value, story.value))

    /** Index in [zones] of the zone where the story currently is (the grid opens there). */
    val currentZoneIndex: Int = run {
        // Rest zones and the final one have no encounters: open at the next grid zone, or the last one.
        val current = Route.indexOf(story.value.zoneProgress(session.profile.value).zone.id)
        zones.value.indexOfFirst { Route.indexOf(it.zoneId) >= current }.takeIf { it >= 0 } ?: zones.value.lastIndex.coerceAtLeast(0)
    }

    private fun zonesFor(profile: StoryProfile, engine: StoryEngine): List<PracticeZoneUi> =
        engine.practiceEntries(profile).groupBy { it.zoneId }.map { (zoneId, entries) ->
            PracticeZoneUi(
                zoneId = zoneId,
                name = engine.zone(zoneId).name,
                cells = entries.map {
                    PracticeCellUi(
                        node = it.node,
                        title = engine.node(it.node).title,
                        portrait = it.rivals.first().portrait,
                        isBoss = it.isBoss,
                        rivalCount = it.rivals.size,
                        playerLevel = it.playerLevel,
                        unlocked = it.unlocked,
                    )
                },
            )
        }
}
