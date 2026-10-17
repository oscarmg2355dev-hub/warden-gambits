package com.wardengambits.app

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.wardengambits.app.data.SaveRepository
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.RunRecord
import com.wardengambits.engine.story.RunResult
import com.wardengambits.engine.story.StoryAction
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.StoryEvent
import com.wardengambits.engine.story.content.StoryContentLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameSessionTest {

    private val engine = StoryEngine(StoryContentLoader.loadFromResources())

    private fun dataStore(): DataStore<Preferences> = InMemoryPreferencesDataStore()


    private fun TestScope.session(ds: DataStore<Preferences>): Pair<GameSession, SaveRepository> {
        val repo = SaveRepository(ds, engine) { }
        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        val session = GameSession(engine, repo, scope) { 42L }
        return session to repo
    }

    @Test fun dispatchUpdatesFlowAndSaves() = runTest {
        val (session, repo) = session(dataStore())
        session.load()
        session.dispatch(StoryAction.Choose(0))
        assertEquals(NodeId("bree", 2, 0), session.profile.value.run.node)
        advanceUntilIdle()
        assertEquals(session.profile.value, repo.load())
    }

    @Test fun rejectedActionDoesNotSave() = runTest {
        val (session, repo) = session(dataStore())
        session.load()
        val step = session.dispatch(StoryAction.Continue)
        advanceUntilIdle()
        assertTrue(step.events.single() is StoryEvent.StoryRejected)
        assertEquals(engine.newGame(), session.profile.value)
        assertEquals(engine.newGame(), repo.load())
    }

    @Test fun newAdventureKeepsHistory() = runTest {
        val (session, repo) = session(dataStore())
        val fresh = engine.newGame()
        val record = RunRecord(1, NodeId.START, 3, 2, RunResult.DEFEATED, 7L)
        repo.save(fresh.copy(history = listOf(record)))
        session.load()
        session.dispatch(StoryAction.Choose(0))
        session.newAdventure()
        advanceUntilIdle()
        assertEquals(fresh.run, session.profile.value.run)
        assertEquals(listOf(record), session.profile.value.history)
        assertEquals(session.profile.value, repo.load())
    }

    @Test fun killedMidCombatRestartsCombat() = runTest {
        val ds = dataStore()
        val (session, _) = session(ds)
        session.load()
        var guard = 0
        while (session.profile.value.run.phase != Phase.COMBAT_PENDING) {
            check(guard++ < 50) { "no combat reached" }
            val twoWay = engine.options(session.profile.value).size == 2
            session.dispatch(if (twoWay) StoryAction.Choose(0) else StoryAction.Continue)
        }
        advanceUntilIdle()
        val (revived, _) = session(ds)
        revived.load()
        assertEquals(Phase.COMBAT_PENDING, revived.profile.value.run.phase)
        assertEquals(session.profile.value, revived.profile.value)
    }

    @Test fun quickDispatchesSaveTheFinalProfile() = runTest {
        val (session, repo) = session(dataStore())
        session.load()
        session.dispatch(StoryAction.Choose(0))
        session.dispatch(StoryAction.Choose(0))
        advanceUntilIdle()
        assertEquals(3, session.profile.value.run.node.level)
        assertEquals(session.profile.value, repo.load())
    }

    @Test fun saveFailureDoesNotCrashAndKeepsInMemoryProfile() = runTest {
        val warnings = mutableListOf<String>()
        val repo = SaveRepository(FailingPreferencesDataStore(), engine) { warnings += it }
        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        val session = GameSession(engine, repo, scope) { 42L }
        session.dispatch(StoryAction.Choose(0))
        advanceUntilIdle()
        assertEquals(NodeId("bree", 2, 0), session.profile.value.run.node)
        assertEquals(1, warnings.size)
        // the writer survived: a later dispatch still works
        session.dispatch(StoryAction.Choose(0))
        advanceUntilIdle()
        assertEquals(2, warnings.size)
    }

    @Test fun loadDoesNotClobberDispatchMadeWhileLoading() = runTest {
        val gate = CompletableDeferred<Unit>()
        val slow = object : DataStore<Preferences> by InMemoryPreferencesDataStore() {
            override val data = flow<Preferences> { gate.await(); emit(emptyPreferences()) }
        }
        val (session, _) = session(slow)
        val loading = launch { session.load() }
        runCurrent()
        session.dispatch(StoryAction.Choose(0))
        gate.complete(Unit)
        loading.join()
        assertEquals(NodeId("bree", 2, 0), session.profile.value.run.node)
    }

    @Test fun loadedFlipsAfterLoad() = runTest {
        val (session, _) = session(dataStore())
        assertEquals(false, session.loaded.value)
        session.load()
        assertEquals(true, session.loaded.value)
    }
}
