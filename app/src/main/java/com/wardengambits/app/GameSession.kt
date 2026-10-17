package com.wardengambits.app

import com.wardengambits.app.data.SaveRepository
import com.wardengambits.engine.story.StoryAction
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.StoryProfile
import com.wardengambits.engine.story.StoryStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Holds the live [StoryProfile]; dispatch is main-thread, updates synchronously and saves in [scope].
 * Saves go through one writer coroutine fed by a conflated channel, so they are ordered and the latest wins.
 */
class GameSession(
    private val engine: StoryEngine,
    private val repository: SaveRepository,
    scope: CoroutineScope,
    @Suppress("unused") private val clock: () -> Long,
) {
    private val state = MutableStateFlow(engine.newGame())
    val profile: StateFlow<StoryProfile> = state.asStateFlow()

    private val loadedState = MutableStateFlow(false)

    /** True once [load] has finished (stored save or new game). */
    val loaded: StateFlow<Boolean> = loadedState.asStateFlow()

    private val pendingSaves = Channel<StoryProfile>(Channel.CONFLATED)

    /** Bumped on every profile change, so [load] can tell whether anything happened while it suspended. */
    private var revision = 0

    init {
        scope.launch {
            for (next in pendingSaves) repository.save(next) // save() never throws on I/O errors
        }
    }

    suspend fun load() {
        val seen = revision
        try {
            val stored = repository.load()
            if (revision == seen) state.value = stored
        } finally {
            loadedState.value = true
        }
    }

    fun dispatch(action: StoryAction): StoryStep {
        val step = engine.reduce(state.value, action)
        if (step.profile != state.value) update(step.profile)
        return step
    }

    /** `newGame()` run, keeping the current history. */
    fun newAdventure() {
        update(engine.newGame().copy(history = state.value.history))
    }

    private fun update(next: StoryProfile) {
        revision++
        state.value = next
        pendingSaves.trySend(next)
    }
}
