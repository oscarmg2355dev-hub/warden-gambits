package com.wardengambits.app.ui.node

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wardengambits.app.GameSession
import com.wardengambits.engine.story.Choice
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.StoryAction
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.StoryProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** A rival of the coming fight, as listed on the node. */
data class RivalUi(val name: String, val level: Int)

sealed interface NodeActions {
    data class Fight(val rivals: List<RivalUi>) : NodeActions
    data class Choices(val options: List<Choice>) : NodeActions
    data object Continue : NodeActions
    data class NextLap(val nextLap: Int) : NodeActions
}

data class NodeUi(
    val key: String,
    val zoneId: String,
    val zoneName: String,
    val level: Int,
    val total: Int,
    val title: String,
    val text: String,
    val actions: NodeActions,
)

/** [story] is the story in the app's language (texts and names only: the mechanics are the same in every language). */
class NodeViewModel(
    private val session: GameSession,
    story: StateFlow<StoryEngine>,
    private val clock: () -> Long,
) : ViewModel() {
    val node: StateFlow<NodeUi> = combine(session.profile, story, ::uiFor)
        .stateIn(viewModelScope, SharingStarted.Eagerly, uiFor(session.profile.value, story.value))

    fun choose(index: Int) {
        session.dispatch(StoryAction.Choose(index))
    }

    fun continueOn() {
        session.dispatch(StoryAction.Continue)
    }

    fun startNextLap() {
        session.dispatch(StoryAction.StartNextLap(clock()))
    }

    private fun uiFor(profile: StoryProfile, engine: StoryEngine): NodeUi {
        val run = profile.run
        val content = engine.node(run.node)
        val progress = engine.zoneProgress(profile)
        return NodeUi(
            key = run.node.key,
            zoneId = progress.zone.id,
            zoneName = progress.zone.name,
            level = progress.level,
            total = progress.total,
            title = content.title,
            text = content.text,
            actions = actionsFor(profile, engine),
        )
    }

    private fun actionsFor(profile: StoryProfile, engine: StoryEngine): NodeActions = when (profile.run.phase) {
        Phase.COMBAT_PENDING -> NodeActions.Fight(engine.combatSetupFor(profile)?.rivals.orEmpty().map { RivalUi(it.name, it.level) })
        Phase.CHOOSING -> {
            val options = engine.options(profile)
            if (options.size == 2) NodeActions.Choices(options) else NodeActions.Continue
        }
        Phase.LAP_COMPLETE -> NodeActions.NextLap(profile.run.lap + 1)
    }
}
