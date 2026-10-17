package com.wardengambits.app.ui.node

import com.wardengambits.app.GameSession
import com.wardengambits.app.InMemoryPreferencesDataStore
import com.wardengambits.app.data.SaveRepository
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.StoryProfile
import com.wardengambits.engine.story.TreeShape
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NodeViewModelTest {
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

    private fun at(node: NodeId, phase: Phase, level: Int = 3, lap: Int = 1): (StoryProfile) -> StoryProfile = { p ->
        p.copy(run = p.run.copy(node = node, path = listOf(node), phase = phase, level = level, lap = lap))
    }

    private fun vm(session: GameSession, clock: () -> Long = { 0L }) = NodeViewModel(session, MutableStateFlow(engine), clock)

    @Test fun choosingNodeActions() {
        val ui = vm(session()).node.value
        assertEquals("bree_1_0", ui.key)
        assertEquals("bree", ui.zoneId)
        assertEquals("Bree", ui.zoneName)
        assertEquals(1, ui.level)
        assertEquals(7, ui.total)
        assertEquals(engine.node(NodeId.START).title, ui.title)
        assertEquals(engine.node(NodeId.START).text, ui.text)
        val actions = ui.actions as NodeActions.Choices
        assertEquals(listOf(NodeId("bree", 2, 0), NodeId("bree", 2, 1)), actions.options.map { it.destination })
        assertEquals(
            listOf(engine.node(NodeId("bree", 2, 0)).choiceLabel, engine.node(NodeId("bree", 2, 1)).choiceLabel),
            actions.options.map { it.label },
        )
    }

    @Test fun fightActionsListRivalLevels() {
        val node = TreeShape.nodesOf("bree").first { engine.node(it).encounter != null }
        val s = session(at(node, Phase.COMBAT_PENDING))
        val setup = engine.combatSetupFor(s.profile.value)!!
        val actions = vm(s).node.value.actions as NodeActions.Fight
        assertTrue(actions.rivals.isNotEmpty())
        assertEquals(setup.rivals.map { RivalUi(it.name, it.level) }, actions.rivals)
    }

    @Test fun textsFollowTheStoryLanguage() {
        val node = TreeShape.nodesOf("bree").first { engine.node(it).encounter != null }
        val s = session(at(node, Phase.COMBAT_PENDING))
        val story = MutableStateFlow(engine)
        val vm = NodeViewModel(s, story) { 0L }
        story.value = english
        val ui = vm.node.value
        assertEquals(english.node(node).title, ui.title)
        assertEquals(english.node(node).text, ui.text)
        assertTrue(ui.title != engine.node(node).title)
        assertEquals("Bree", ui.zoneName)
        val englishRivals = english.combatSetupFor(s.profile.value)!!.rivals.map { RivalUi(it.name, it.level) }
        assertEquals(englishRivals, (ui.actions as NodeActions.Fight).rivals)
        assertTrue(englishRivals != engine.combatSetupFor(s.profile.value)!!.rivals.map { RivalUi(it.name, it.level) })
        assertEquals(s.profile.value.run.node, node) // the story itself does not change
    }

    @Test fun choiceLabelsFollowTheStoryLanguage() {
        val story = MutableStateFlow(engine)
        val vm = NodeViewModel(session(), story) { 0L }
        story.value = english
        val actions = vm.node.value.actions as NodeActions.Choices
        assertEquals(listOf(english.node(NodeId("bree", 2, 0)).choiceLabel, english.node(NodeId("bree", 2, 1)).choiceLabel), actions.options.map { it.label })
    }

    @Test fun continueAndNextLap() {
        val six = session(at(NodeId("bree", 6, 0), Phase.CHOOSING))
        assertEquals(NodeActions.Continue, vm(six).node.value.actions)
        val final = session(at(NodeId.FINAL, Phase.LAP_COMPLETE, lap = 1))
        val ui = vm(final).node.value
        assertEquals(NodeActions.NextLap(2), ui.actions)
        assertEquals(1, ui.total)
    }

    @Test fun chooseDispatches() {
        val s = session()
        val vm = vm(s)
        vm.choose(1)
        assertEquals(NodeId("bree", 2, 1), s.profile.value.run.node)
        assertEquals("bree_2_1", vm.node.value.key)
    }

    @Test fun continueOnAdvances() {
        val s = session(at(NodeId("bree", 6, 0), Phase.CHOOSING))
        vm(s).continueOn()
        assertEquals(NodeId("bree", 7, 0), s.profile.value.run.node)
    }

    @Test fun startNextLapUsesClock() {
        val s = session(at(NodeId.FINAL, Phase.LAP_COMPLETE))
        vm(s) { 4242L }.startNextLap()
        assertEquals(4242L, s.profile.value.history.single().finishedAtMillis)
        assertEquals(2, s.profile.value.run.lap)
    }
}
