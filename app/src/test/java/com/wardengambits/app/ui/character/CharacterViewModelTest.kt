package com.wardengambits.app.ui.character

import com.wardengambits.app.GameSession
import com.wardengambits.app.InMemoryPreferencesDataStore
import com.wardengambits.app.data.SaveRepository
import com.wardengambits.engine.model.LevelStats
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.RunRecord
import com.wardengambits.engine.story.RunResult
import com.wardengambits.engine.story.StoryEngine
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
import org.junit.Before
import org.junit.Test
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterViewModelTest {
    private val engine = StoryEngine(StoryContentLoader.loadFromResources())
    private val dispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun characterUi() {
        val repo = SaveRepository(InMemoryPreferencesDataStore(), engine) { }
        val session = GameSession(engine, repo, CoroutineScope(SupervisorJob() + dispatcher)) { 0L }
        val node = NodeId("chet", 3, 0)
        val base = engine.newGame()
        val old = RunRecord(1, node, 4, 3, RunResult.DEFEATED, 0L)
        val recent = RunRecord(2, NodeId.FINAL, 9, 20, RunResult.COMPLETED, 86_400_000L, deaths = 6)
        runBlocking {
            repo.save(
                base.copy(
                    run = base.run.copy(level = 5, lap = 2, wins = 7, deaths = 4, node = node, path = listOf(node), phase = Phase.CHOOSING),
                    history = listOf(old, recent),
                ),
            )
            session.load()
        }
        val ui = CharacterViewModel(session, MutableStateFlow(engine), ZoneId.of("UTC")).ui.value
        assertEquals(5, ui.level)
        assertEquals(2, ui.lap)
        assertEquals(7, ui.wins)
        assertEquals(4, ui.deaths)
        assertEquals("Bosque de Chet", ui.zoneName)
        assertEquals("3/7", ui.progress)
        assertEquals(LevelStats.forLevel(5), ui.stats)
        assertEquals(
            listOf(
                HistoryRowUi(2, RunResult.COMPLETED, "Monte del Destino", 9, 20, "02/01/1970 00:00", deaths = 6, completed = true),
                HistoryRowUi(1, RunResult.DEFEATED, "Bosque de Chet", 4, 3, "01/01/1970 00:00"),
            ),
            ui.history,
        )
        assertEquals(1, ui.completedLaps)
    }

    @Test fun zoneNamesFollowTheStoryLanguage() {
        val repo = SaveRepository(InMemoryPreferencesDataStore(), engine) { }
        val session = GameSession(engine, repo, CoroutineScope(SupervisorJob() + dispatcher)) { 0L }
        val node = NodeId("chet", 3, 0)
        val base = engine.newGame()
        runBlocking {
            repo.save(
                base.copy(
                    run = base.run.copy(node = node, path = listOf(node), phase = Phase.CHOOSING),
                    history = listOf(RunRecord(1, NodeId.FINAL, 9, 20, RunResult.COMPLETED, 0L)),
                ),
            )
            session.load()
        }
        val story = MutableStateFlow(engine)
        val vm = CharacterViewModel(session, story, ZoneId.of("UTC"))
        story.value = StoryEngine(StoryContentLoader.loadFromResources(language = "en"))
        assertEquals("The Chetwood", vm.ui.value.zoneName)
        assertEquals(listOf("Mount Doom"), vm.ui.value.history.map { it.place })
    }

    /** Road to Mordor: records of laps finished at the old final node (Lothlórien 1/0) keep their place name. */
    @Test fun oldHistoryRecordsStillRender() {
        val repo = SaveRepository(InMemoryPreferencesDataStore(), engine) { }
        val session = GameSession(engine, repo, CoroutineScope(SupervisorJob() + dispatcher)) { 0L }
        val base = engine.newGame()
        val old = RunRecord(1, NodeId("lothlorien", 1, 0), 30, 28, RunResult.COMPLETED, 0L, deaths = 2)
        runBlocking {
            repo.save(base.copy(history = listOf(old)))
            session.load()
        }
        val ui = CharacterViewModel(session, MutableStateFlow(engine), ZoneId.of("UTC")).ui.value
        assertEquals(listOf(HistoryRowUi(1, RunResult.COMPLETED, "Lothlórien", 30, 28, "01/01/1970 00:00", deaths = 2, completed = true)), ui.history)
        assertEquals(1, ui.completedLaps)
    }
}
