package com.wardengambits.app.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wardengambits.app.GameSession
import com.wardengambits.engine.story.Route
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.StoryProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class ZoneStatus { DONE, CURRENT, LOCKED }

data class ZoneRowUi(val id: String, val name: String, val status: ZoneStatus, val level: Int?, val total: Int?)

/** [story] is the story in the app's language: zone names follow it. */
class MapViewModel(session: GameSession, story: StateFlow<StoryEngine>) : ViewModel() {
    val rows: StateFlow<List<ZoneRowUi>> = combine(session.profile, story, ::rowsFor)
        .stateIn(viewModelScope, SharingStarted.Eagerly, rowsFor(session.profile.value, story.value))

    private fun rowsFor(profile: StoryProfile, engine: StoryEngine): List<ZoneRowUi> {
        val progress = engine.zoneProgress(profile)
        val currentIndex = Route.indexOf(progress.zone.id)
        return engine.zones().mapIndexed { index, zone ->
            when {
                index < currentIndex -> ZoneRowUi(zone.id, zone.name, ZoneStatus.DONE, null, null)
                index == currentIndex -> ZoneRowUi(zone.id, zone.name, ZoneStatus.CURRENT, progress.level, progress.total)
                else -> ZoneRowUi(zone.id, zone.name, ZoneStatus.LOCKED, null, null)
            }
        }
    }
}
