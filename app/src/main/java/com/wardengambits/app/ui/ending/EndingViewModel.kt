package com.wardengambits.app.ui.ending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wardengambits.app.GameSession
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.StoryAction
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.StoryProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** The completed lap shown on the ending screen: the final node plus the lap summary. */
data class EndingUi(
    val key: String,
    val zoneId: String,
    val zoneName: String,
    val title: String,
    val text: String,
    val lap: Int,
    val nextLap: Int,
    val level: Int,
    val levelsGained: Int,
    val wins: Int,
    val deaths: Int,
)

/** [story] is the story in the app's language: the final node's texts and the zone name follow it. */
class EndingViewModel(
    private val session: GameSession,
    story: StateFlow<StoryEngine>,
    private val clock: () -> Long,
) : ViewModel() {
    /** Null when the run is not in [Phase.LAP_COMPLETE] (there is no ending to show). */
    val ui: StateFlow<EndingUi?> = combine(session.profile, story, ::uiFor)
        .stateIn(viewModelScope, SharingStarted.Eagerly, uiFor(session.profile.value, story.value))

    fun startNextLap() {
        session.dispatch(StoryAction.StartNextLap(clock()))
    }

    private fun uiFor(profile: StoryProfile, engine: StoryEngine): EndingUi? {
        val run = profile.run
        if (run.phase != Phase.LAP_COMPLETE) return null
        val content = engine.node(NodeId.FINAL)
        val zone = engine.zoneProgress(profile).zone
        return EndingUi(
            key = NodeId.FINAL.key,
            zoneId = zone.id,
            zoneName = zone.name,
            title = content.title,
            text = content.text,
            lap = run.lap,
            nextLap = run.lap + 1,
            level = run.level,
            levelsGained = run.level - run.lapStartLevel,
            wins = run.wins,
            deaths = run.deaths,
        )
    }
}
