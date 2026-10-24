package com.wardengambits.app.ui.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wardengambits.app.GameSession
import com.wardengambits.engine.model.Difficulty
import com.wardengambits.engine.model.LevelStats
import com.wardengambits.engine.model.Stats
import com.wardengambits.engine.story.RunResult
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.StoryProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class HistoryRowUi(
    val lap: Int,
    val result: RunResult,
    val place: String,
    val level: Int,
    val wins: Int,
    val date: String,
    val deaths: Int = 0,
    /** The lap reached the end of the story (shown with a green seal). */
    val completed: Boolean = false,
    /** Easiest difficulty of the lap; null in laps recorded before difficulties existed. */
    val difficulty: Difficulty? = null,
)

data class CharacterUi(
    val level: Int,
    val lap: Int,
    val wins: Int,
    val deaths: Int,
    val zoneName: String,
    val progress: String,
    val stats: Stats,
    val history: List<HistoryRowUi>,
    /** History records of laps that reached the end. */
    val completedLaps: Int = 0,
)

/** [story] is the story in the app's language: zone names follow it. */
class CharacterViewModel(
    session: GameSession,
    story: StateFlow<StoryEngine>,
    zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(zone)

    val ui: StateFlow<CharacterUi> = combine(session.profile, story, ::uiFor)
        .stateIn(viewModelScope, SharingStarted.Eagerly, uiFor(session.profile.value, story.value))

    private fun uiFor(profile: StoryProfile, engine: StoryEngine): CharacterUi {
        val run = profile.run
        val progress = engine.zoneProgress(profile)
        return CharacterUi(
            level = run.level,
            lap = run.lap,
            wins = run.wins,
            deaths = run.deaths,
            zoneName = progress.zone.name,
            progress = "${progress.level}/${progress.total}",
            stats = LevelStats.forLevel(run.level),
            history = profile.history.asReversed().map { record ->
                HistoryRowUi(
                    lap = record.lap,
                    result = record.result,
                    place = engine.zone(record.node.zoneId).name,
                    level = record.level,
                    wins = record.wins,
                    date = formatter.format(Instant.ofEpochMilli(record.finishedAtMillis)),
                    deaths = record.deaths,
                    completed = record.result == RunResult.COMPLETED,
                    difficulty = record.difficulty,
                )
            },
            completedLaps = profile.history.count { it.result == RunResult.COMPLETED },
        )
    }
}
