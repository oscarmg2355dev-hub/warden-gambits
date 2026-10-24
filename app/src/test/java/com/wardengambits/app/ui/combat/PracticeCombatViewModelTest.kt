package com.wardengambits.app.ui.combat

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.wardengambits.app.GameSession
import com.wardengambits.app.InMemoryPreferencesDataStore
import com.wardengambits.app.R
import com.wardengambits.app.data.SaveRepository
import com.wardengambits.app.ui.theme.WardenTheme
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.combo.Input
import com.wardengambits.engine.model.Difficulty
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.StoryProfile
import com.wardengambits.engine.story.TreeShape
import com.wardengambits.engine.story.content.StoryContentLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es-w360dp-h640dp")
class PracticeCombatViewModelTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val engine = StoryEngine(StoryContentLoader.loadFromResources())
    private val node: NodeId = TreeShape.nodesOf("bree").first { engine.node(it).encounter?.rivals?.size == 1 }

    @After fun tearDown() = Dispatchers.resetMain()

    private suspend fun TestScope.session(): GameSession {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repo = SaveRepository(InMemoryPreferencesDataStore(), engine) { }
        val session = GameSession(engine, repo, backgroundScope) { 0L }
        val p = engine.newGame()
        val parent = TreeShape.nodesOf(node.zoneId).firstOrNull { node in TreeShape.destinations(it) }
        val seeded: StoryProfile = p.copy(
            run = p.run.copy(node = node, path = listOfNotNull(parent, node), phase = Phase.COMBAT_PENDING, level = 5),
        )
        assertTrue(engine.isValid(seeded))
        repo.save(seeded)
        session.load()
        return session
    }

    private fun practice() = CombatViewModel(
        PracticeCombatSource(node, engine, Difficulty.EYE), MutableStateFlow(engine), CombatEngine(), { 1234L },
    )

    private fun TestScope.tick(n: Int = 1) {
        advanceTimeBy(1000L * n)
        runCurrent()
    }

    private fun TestScope.fightUntilOver(vm: CombatViewModel) {
        repeat(800) {
            if (vm.state.value.outcome != null) return
            vm.press(Input.SPEAR)
            vm.press(Input.SHIELD)
            vm.execute()
            tick()
        }
    }

    @Test fun practiceFinishDoesNotReport() = runTest {
        val s = session()
        val before = s.profile.value
        val vm = practice()
        assertTrue(vm.isPractice)
        vm.setActive(true)
        fightUntilOver(vm)
        assertTrue(vm.state.value.outcome != null)
        vm.finish()
        assertEquals(before, s.profile.value)
    }

    @Test fun practiceAbandonDoesNotReport() = runTest {
        val s = session()
        val before = s.profile.value
        val vm = practice()
        vm.setActive(true)
        tick(2)
        vm.abandon()
        assertEquals(before, s.profile.value)
    }

    @Test fun retryStartsAFreshCombat() = runTest {
        session()
        val vm = practice()
        vm.setActive(true)
        val start = vm.state.value
        repeat(2) {
            fightUntilOver(vm)
            assertTrue(vm.state.value.outcome != null)
            vm.retry()
            val ui = vm.state.value
            assertNull(ui.outcome)
            assertEquals(0, ui.tick)
            assertEquals(start.player.hp, ui.player.hp)
            assertEquals(start.player.power, ui.player.power)
            assertEquals(start.rivals.map { it.hp to it.power }, ui.rivals.map { it.hp to it.power })
            assertFalse(ui.paused)
        }
        tick(2)
        assertEquals(2, vm.state.value.tick)
        vm.setActive(false)
    }

    @Test fun storyCombatStillReports() = runTest {
        val s = session()
        val strong = s.profile.value.run.let { s.profile.value.copy(run = it.copy(level = 60)) }
        val repo = SaveRepository(InMemoryPreferencesDataStore(), engine) { }
        repo.save(strong)
        val session = GameSession(engine, repo, backgroundScope) { 0L }
        session.load()
        val vm = CombatViewModel(StoryCombatSource(session, engine, Difficulty.EYE), MutableStateFlow(engine), CombatEngine(), { 1234L })
        assertFalse(vm.isPractice)
        vm.setActive(true)
        fightUntilOver(vm)
        assertEquals(Outcome.VICTORY, vm.state.value.outcome)
        vm.finish()
        assertEquals(61, session.profile.value.run.level)
    }

    private fun showOverlay(isPractice: Boolean, outcome: Outcome? = Outcome.VICTORY) {
        val ui = CombatUiState(
            zoneName = "Bree", levelLabel = "1/7", tick = 5, paused = false, rivals = emptyList(),
            player = FighterUi(FighterId(0), "Warden", 3, 80, 100, 30, 60, emptyList(), true, false),
            buffer = "", bufferMatch = Combos.find(""), outcome = outcome, levelBefore = 3, winsBefore = 2,
        )
        composeRule.setContent {
            WardenTheme {
                CombatScreen(ui, {}, {}, {}, {}, {}, {}, {}, isPractice = isPractice)
            }
        }
    }

    @Test fun practiceAbandonDialogShowsPracticeMessage() {
        showOverlay(isPractice = true, outcome = null)
        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        val practice = composeRule.activity.getString(R.string.combat_practice_abandon_message)
        val story = composeRule.activity.getString(R.string.combat_abandon_message)
        composeRule.onNodeWithText(practice).assertIsDisplayed()
        composeRule.onAllNodesWithText(story).assertCountEquals(0)
    }

    @Test fun storyAbandonDialogKeepsStoryMessage() {
        showOverlay(isPractice = false, outcome = null)
        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        val practice = composeRule.activity.getString(R.string.combat_practice_abandon_message)
        val story = composeRule.activity.getString(R.string.combat_abandon_message)
        composeRule.onNodeWithText(story).assertIsDisplayed()
        composeRule.onAllNodesWithText(practice).assertCountEquals(0)
    }

    @Test fun practiceOverlayShowsRetryAndBack() {
        showOverlay(isPractice = true)
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.combat_practice_retry)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.combat_practice_back)).assertIsDisplayed()
    }

    @Test fun storyOverlayKeepsContinueButton() {
        showOverlay(isPractice = false)
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.combat_continue)).assertIsDisplayed()
    }
}
