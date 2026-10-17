package com.wardengambits.app.ui.ending

import com.wardengambits.app.GameSession
import com.wardengambits.app.InMemoryPreferencesDataStore
import com.wardengambits.app.data.SaveRepository
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.StoryProfile
import com.wardengambits.engine.story.content.StoryContentLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EndingViewModelTest {
    private val engine = StoryEngine(StoryContentLoader.loadFromResources())
    private val english = StoryEngine(StoryContentLoader.loadFromResources(language = "en"))
    private val dispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun session(seed: ((StoryProfile) -> StoryProfile)? = null): GameSession {
        val repo = SaveRepository(InMemoryPreferencesDataStore(), engine) { }
        val session = GameSession(engine, repo, CoroutineScope(SupervisorJob() + dispatcher)) { 0L }
        if (seed != null) runBlocking {
            repo.save(seed(engine.newGame()))
            session.load()
        }
        return session
    }

    private val finished: (StoryProfile) -> StoryProfile = { p ->
        p.copy(
            run = p.run.copy(
                node = NodeId.FINAL, path = listOf(NodeId.FINAL), phase = Phase.LAP_COMPLETE,
                level = 99, lapStartLevel = 1, lap = 1, wins = 98, deaths = 3,
            ),
        )
    }

    @Test fun lapCompleteShowsFinalNodeAndStats() {
        val ui = EndingViewModel(session(finished), MutableStateFlow(engine)) { 0L }.ui.value!!
        val final = engine.node(NodeId.FINAL)
        assertEquals("orodruin_final", ui.key)
        assertEquals("orodruin", ui.zoneId)
        assertEquals("Monte del Destino", ui.zoneName)
        assertEquals(final.title, ui.title)
        assertEquals(final.text, ui.text)
        assertEquals(1, ui.lap)
        assertEquals(2, ui.nextLap)
        assertEquals(99, ui.level)
        assertEquals(98, ui.levelsGained)
        assertEquals(98, ui.wins)
        assertEquals(3, ui.deaths)
    }

    @Test fun followsTheStoryLanguage() {
        val story = MutableStateFlow(engine)
        val vm = EndingViewModel(session(finished), story) { 0L }
        story.value = english
        val ui = vm.ui.value!!
        assertEquals("Mount Doom", ui.zoneName)
        assertEquals(english.node(NodeId.FINAL).title, ui.title)
        assertEquals(english.node(NodeId.FINAL).text, ui.text)
    }

    @Test fun notLapCompleteHasNoEnding() {
        assertNull(EndingViewModel(session(), MutableStateFlow(engine)) { 0L }.ui.value)
    }

    @Test fun startNextLapRecordsTheLapAndLeavesTheEnding() {
        val s = session(finished)
        val vm = EndingViewModel(s, MutableStateFlow(engine)) { 777L }
        assertEquals(1, vm.ui.value!!.lap)
        vm.startNextLap()
        assertNull(vm.ui.value)
        val profile = s.profile.value
        assertEquals(2, profile.run.lap)
        assertEquals(NodeId.START, profile.run.node)
        assertEquals(777L, profile.history.single().finishedAtMillis)
        assertEquals(3, profile.history.single().deaths)
    }
}
