package com.wardengambits.app.ui.combat

import com.wardengambits.app.GameSession
import com.wardengambits.app.InMemoryPreferencesDataStore
import com.wardengambits.app.data.SaveRepository
import com.wardengambits.engine.combo.ComboChain
import com.wardengambits.engine.combo.ComboFamily
import com.wardengambits.engine.combo.ComboTuning
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.combo.Input
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.event.RejectReason
import com.wardengambits.engine.model.Difficulty
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.random.GameRandom
import com.wardengambits.engine.random.SeededRandom
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.story.NodeId
import com.wardengambits.engine.story.Phase
import com.wardengambits.engine.story.Route
import com.wardengambits.engine.story.StoryEngine
import com.wardengambits.engine.story.StoryProfile
import com.wardengambits.engine.story.TreeShape
import com.wardengambits.engine.story.content.StoryContentLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CombatViewModelTest {
    private val engine = StoryEngine(StoryContentLoader.loadFromResources())

    /** One weak rival in Bree. */
    private val weakNode = TreeShape.nodesOf("bree").first { engine.node(it).encounter?.rivals?.size == 1 }

    /** At least two rivals. */
    private val pairNode = TreeShape.nodesOf("bree").first { (engine.node(it).encounter?.rivals?.size ?: 0) >= 2 }

    /** A late-zone fight far above a level-1 Warden. */
    private val lateNode = TreeShape.nodesOf("nimrodel").first { engine.node(it).encounter != null }

    @After fun tearDown() = Dispatchers.resetMain()

    /** The first node of the same zone that leads to [node]; null for level-1 nodes. */
    private fun parentOf(node: NodeId): NodeId? =
        TreeShape.nodesOf(node.zoneId).firstOrNull { node in TreeShape.destinations(it) }

    private suspend fun TestScope.session(
        node: NodeId,
        level: Int,
        phase: Phase = Phase.COMBAT_PENDING,
        path: List<NodeId> = listOfNotNull(parentOf(node), node),
    ): GameSession {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repo = SaveRepository(InMemoryPreferencesDataStore(), engine) { }
        val session = GameSession(engine, repo, backgroundScope) { 0L }
        val p = engine.newGame()
        val seeded: StoryProfile = p.copy(run = p.run.copy(node = node, path = path, phase = phase, level = level))
        assertTrue(engine.isValid(seeded))
        repo.save(seeded)
        session.load()
        assertEquals(seeded, session.profile.value)
        return session
    }

    private fun vm(session: GameSession, clock: () -> Long = { 1234L }) =
        CombatViewModel(StoryCombatSource(session, engine, Difficulty.EYE), MutableStateFlow(engine), CombatEngine(), clock)

    private fun TestScope.tick(n: Int = 1) {
        advanceTimeBy(1000L * n)
        runCurrent()
    }

    /** Presses "12" + execute every tick until the combat ends. */
    private fun TestScope.fightUntilOver(vm: CombatViewModel) {
        repeat(500) {
            if (vm.state.value.outcome != null) return
            vm.press(Input.SPEAR)
            vm.press(Input.SHIELD)
            vm.execute()
            tick()
        }
    }

    @Test fun executedComboIsQueuedUntilNextTick() = runTest {
        val vm = vm(session(weakNode, 3))
        vm.setActive(true)
        assertNull(vm.state.value.player.queued)
        vm.press(Input.SPEAR)
        vm.press(Input.SHIELD)
        vm.execute()
        assertEquals(Combos.find("12"), vm.state.value.player.queued)
        tick()
        assertNull(vm.state.value.player.queued)
        vm.setActive(false)
    }

    @Test fun ticksEverySecondWhileActive() = runTest {
        val vm = vm(session(weakNode, 3))
        vm.setActive(true)
        advanceTimeBy(3000)
        runCurrent()
        assertEquals(3, vm.state.value.tick)
        vm.setActive(false)
    }

    @Test fun noTicksWhenInactiveOrPaused() = runTest {
        val vm = vm(session(weakNode, 3))
        vm.setActive(false)
        tick(3)
        assertEquals(0, vm.state.value.tick)

        vm.setActive(true)
        vm.togglePause()
        assertTrue(vm.state.value.paused)
        tick(3)
        assertEquals(0, vm.state.value.tick)

        vm.togglePause()
        assertFalse(vm.state.value.paused)
        tick()
        assertEquals(1, vm.state.value.tick)

        // Pausing mid-interval: after resuming, the next tick is a full interval later.
        advanceTimeBy(500)
        runCurrent()
        vm.togglePause()
        runCurrent()
        vm.togglePause()
        runCurrent()
        advanceTimeBy(999)
        runCurrent()
        assertEquals(1, vm.state.value.tick)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(2, vm.state.value.tick)

        // Toggling active/pause repeatedly never starts concurrent loops.
        repeat(5) {
            vm.setActive(false)
            runCurrent()
            vm.setActive(true)
            runCurrent()
            vm.togglePause()
            runCurrent()
        }
        assertFalse(vm.state.value.paused)
        tick(3)
        assertEquals(5, vm.state.value.tick)
        vm.setActive(false)
    }

    @Test fun backgroundLatchesPause() = runTest {
        val vm = vm(session(weakNode, 3))
        assertFalse(vm.state.value.paused)
        vm.setActive(true)
        tick()
        assertEquals(1, vm.state.value.tick)

        vm.setActive(false) // background
        assertTrue(vm.state.value.paused)
        tick(3)
        assertEquals(1, vm.state.value.tick)

        vm.setActive(true) // foreground: still paused
        assertTrue(vm.state.value.paused)
        tick(3)
        assertEquals(1, vm.state.value.tick)

        vm.togglePause()
        assertFalse(vm.state.value.paused)
        tick()
        assertEquals(2, vm.state.value.tick)
        vm.setActive(false)
    }

    /** Daze roll succeeds only on the first roll; afterwards never (with [ComboTuning.FIXED] only Daze rolls consume it). */
    private class OneDazeRandom : GameRandom {
        private var calls = 0
        override fun nextInt(bound: Int) = if (calls++ == 0) 0 else bound - 1
        override val state = 0L
    }

    private fun bossNode() = TreeShape.nodesOf("bree").first { id ->
        engine.node(id).encounter?.rivals?.any { "31" in it.pattern } == true
    }

    @Test fun inputsDisabledWhileDazedAndBackAfterItExpires() = runTest {
        val vm = CombatViewModel(StoryCombatSource(session(bossNode(), 10), engine, Difficulty.EYE), MutableStateFlow(engine), CombatEngine(tuning = ComboTuning.FIXED) { OneDazeRandom() }, { 1234L })
        vm.setActive(true)
        assertTrue(vm.state.value.inputsEnabled)
        var guard = 0
        while ("DZ" !in vm.state.value.player.badges.map { it.code } && guard++ < 40) tick()
        assertTrue("player never dazed", "DZ" in vm.state.value.player.badges.map { it.code })
        assertFalse(vm.state.value.inputsEnabled)

        val seen = mutableListOf<CombatEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.collect { seen += it } }
        val before = vm.state.value
        vm.press(Input.SPEAR)
        vm.execute()
        vm.clearBuffer()
        assertEquals(before, vm.state.value)
        assertTrue(seen.isEmpty())

        guard = 0
        while (!vm.state.value.inputsEnabled && vm.state.value.outcome == null && guard++ < 30) tick()
        assertEquals(null, vm.state.value.outcome)
        assertTrue(vm.state.value.inputsEnabled)
        vm.press(Input.SPEAR)
        assertEquals("1", vm.state.value.buffer)
        vm.setActive(false)
    }

    @Test fun dazedTicksLeftCountsDownTheActiveDaze() = runTest {
        val vm = CombatViewModel(StoryCombatSource(session(bossNode(), 10), engine, Difficulty.EYE), MutableStateFlow(engine), CombatEngine(tuning = ComboTuning.FIXED) { OneDazeRandom() }, { 1234L })
        vm.setActive(true)
        assertEquals(0, vm.state.value.player.dazedTicksLeft)
        var guard = 0
        while (vm.state.value.player.dazedTicksLeft == 0 && guard++ < 40) tick()
        val first = vm.state.value.player.dazedTicksLeft
        assertTrue("never dazed: $first", first in 1..Combos.DAZED_TICKS)
        assertTrue("DZ" in vm.state.value.player.badges.map { it.code })
        if (first > 1) {
            tick()
            assertEquals(first - 1, vm.state.value.player.dazedTicksLeft)
        }
        guard = 0
        while (vm.state.value.player.dazedTicksLeft > 0 && guard++ < 30) tick()
        assertEquals(0, vm.state.value.player.dazedTicksLeft)
        assertEquals(null, vm.state.value.outcome)
        assertTrue(vm.state.value.inputsEnabled)
        vm.setActive(false)
    }

    @Test fun pendingDazeCountsDownWithoutGap() = runTest {
        val vm = CombatViewModel(StoryCombatSource(session(bossNode(), 10), engine, Difficulty.EYE), MutableStateFlow(engine), CombatEngine(tuning = ComboTuning.FIXED) { OneDazeRandom() }, { 1234L })
        vm.setActive(true)
        var guard = 0
        while ("DZ" !in vm.state.value.player.badges.map { it.code } && guard++ < 40) tick()
        // The tick the roll succeeds the Daze is still pending, but the keys are already off: the count shows.
        assertFalse(vm.state.value.inputsEnabled)
        val first = vm.state.value.player.dazedTicksLeft
        assertTrue("pending: $first", first > 0)
        // One less every tick until the keys come back exactly when it reaches 0.
        var expected = first
        while (expected > 0) {
            assertEquals(expected, vm.state.value.player.dazedTicksLeft)
            assertFalse(vm.state.value.inputsEnabled)
            tick()
            expected--
        }
        assertEquals(0, vm.state.value.player.dazedTicksLeft)
        assertTrue(vm.state.value.inputsEnabled)
        vm.setActive(false)
    }

    @Test fun inputsDisabledWhilePausedOrOver() = runTest {
        val vm = vm(session(pairNode, 3))
        assertTrue(vm.state.value.inputsEnabled)
        vm.togglePause()
        assertFalse(vm.state.value.inputsEnabled)
    }

    @Test fun inputsIgnoredWhilePaused() = runTest {
        val vm = vm(session(pairNode, 3))
        val seen = mutableListOf<CombatEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.collect { seen += it } }
        vm.togglePause()
        val before = vm.state.value
        vm.press(Input.SPEAR)
        vm.execute()
        vm.clearBuffer()
        vm.selectTarget(FighterId(2))
        assertEquals(before, vm.state.value)
        assertTrue(seen.isEmpty())

        vm.togglePause()
        vm.press(Input.SPEAR)
        assertEquals("1", vm.state.value.buffer)
    }

    @Test fun chainContinuesAfterTierOneResolves() = runTest {
        val vm = vm(session(weakNode, 3))
        vm.setActive(true)
        vm.press(Input.SPEAR)
        vm.press(Input.SHIELD)
        vm.execute()
        tick() // "12" resolves: chain Lanza tier 1
        assertEquals(ComboChain(ComboFamily.SPEAR_A, 1), vm.state.value.playerChain)
        vm.press(Input.SPEAR)
        vm.press(Input.SHIELD)
        vm.press(Input.SPEAR)
        assertTrue(vm.state.value.chainContinues)
        vm.setActive(false)
    }

    @Test fun pendingEffectsHiddenOverTimeShown() = runTest {
        val vm = vm(session(weakNode, 3))
        vm.setActive(true)
        vm.press(Input.SPEAR)
        vm.press(Input.SHIELD)
        vm.execute()
        tick() // "12" lands: DOT on the rival
        assertTrue("SP1" in vm.state.value.rivals.single().badges.map { it.code })
        vm.press(Input.SPEAR)
        vm.press(Input.SPEAR)
        vm.execute()
        tick() // "11" lands: FURY pending until the next tick
        assertFalse("FU" in vm.state.value.player.badges.map { it.code })
        assertTrue("SP1" in vm.state.value.rivals.single().badges.map { it.code })
        tick()
        assertTrue("FU" in vm.state.value.player.badges.map { it.code })
        vm.setActive(false)
    }

    @Test fun dazedShownFromTheRollTick() = runTest {
        val alwaysZero = object : GameRandom {
            override fun nextInt(bound: Int) = 0
            override val state = 0L
        }
        val vm = CombatViewModel(StoryCombatSource(session(weakNode, 3), engine, Difficulty.EYE), MutableStateFlow(engine), CombatEngine { alwaysZero }, { 1234L })
        vm.setActive(true)
        vm.press(Input.FIST)
        vm.press(Input.SPEAR)
        vm.execute()
        tick() // "31" lands, the chance acts at once, the roll succeeds: the chance is removed and DAZED applied
        val codes = vm.state.value.rivals.single().badges.map { it.code }
        assertFalse("AT1" in codes)
        assertTrue("badges: $codes", "DZ" in codes)
        vm.setActive(false)
    }

    @Test fun seedComesFromClock() = runTest {
        val s = session(weakNode, 3)
        fun firstTickSeed(now: Long): Long {
            val seeds = mutableListOf<Long>()
            val vm = CombatViewModel(StoryCombatSource(s, engine, Difficulty.EYE), MutableStateFlow(engine), CombatEngine { seed -> seeds += seed; SeededRandom(seed) }, { now })
            vm.setActive(true)
            tick()
            vm.setActive(false)
            return seeds.first()
        }
        assertEquals(111L, firstTickSeed(111L))
        assertEquals(222L, firstTickSeed(222L))
    }

    @Test fun rivalAndZoneNamesFollowTheStoryLanguage() = runTest {
        val english = StoryEngine(StoryContentLoader.loadFromResources(language = "en"))
        val s = session(pairNode, 3)
        val story = MutableStateFlow(engine)
        val vm = CombatViewModel(StoryCombatSource(s, engine, Difficulty.EYE), story, CombatEngine(), { 1234L })
        val spanishNames = engine.combatSetupFor(s.profile.value)!!.rivals.map { it.name }
        assertEquals(spanishNames, vm.state.value.rivals.map { it.name })
        vm.setActive(true)
        tick()
        val hp = vm.state.value.rivals.map { it.hp }
        story.value = english
        runCurrent()
        val englishNames = english.combatSetupFor(s.profile.value)!!.rivals.map { it.name }
        assertTrue(englishNames != spanishNames)
        assertEquals(englishNames, vm.state.value.rivals.map { it.name })
        assertEquals(hp, vm.state.value.rivals.map { it.hp }) // same fight, only the names change
        assertEquals("Bree", vm.state.value.zoneName)
        tick()
        assertEquals(englishNames, vm.state.value.rivals.map { it.name })
        vm.setActive(false)
    }

    @Test fun inputsReachEngine() = runTest {
        val vm = vm(session(weakNode, 3))
        vm.press(Input.SPEAR)
        vm.press(Input.SHIELD)
        assertEquals("12", vm.state.value.buffer)
        assertEquals(Combos.find("12"), vm.state.value.bufferMatch)
        assertFalse(vm.state.value.chainContinues)
        vm.execute()
        assertEquals("", vm.state.value.buffer)
        assertNull(vm.state.value.bufferMatch)
        val before = vm.state.value.rivals.single().hp
        vm.setActive(true)
        tick()
        assertTrue(vm.state.value.rivals.single().hp < before)
        vm.press(Input.FIST)
        vm.clearBuffer()
        assertEquals("", vm.state.value.buffer)
        vm.setActive(false)
    }

    @Test fun selectTargetMovesMarker() = runTest {
        val vm = vm(session(pairNode, 3))
        assertEquals(listOf(true, false), vm.state.value.rivals.map { it.isTarget })
        vm.selectTarget(FighterId(2))
        assertEquals(listOf(false, true), vm.state.value.rivals.map { it.isTarget })
    }

    @Test fun headerAndStartState() = runTest {
        val vm = vm(session(weakNode, 3))
        val s = vm.state.value
        assertFalse(vm.invalid)
        assertEquals("Bree", s.zoneName)
        assertEquals("${weakNode.level}/7", s.levelLabel)
        assertEquals(3, s.levelBefore)
        assertEquals(0, s.winsBefore)
        assertEquals(3, s.player.level)
        assertEquals(s.player.maxHp, s.player.hp)
        assertNull(s.outcome)
    }

    @Test fun fightersExposeStatsForTheCards() = runTest {
        val vm = vm(session(weakNode, 3))
        val p = vm.state.value.player
        val stats = com.wardengambits.engine.model.LevelStats.forLevel(3)
        assertEquals(stats.attack, p.attack)
        assertEquals(stats.heal, p.heal)
        assertEquals(stats.defense, p.defense)
        assertFalse(p.isBoss)
        val rival = vm.state.value.rivals.single()
        assertTrue(rival.attack > 0)
        assertTrue(rival.defense >= 0)
    }

    @Test fun invalidSetupNeverTicks() = runTest {
        val vm = vm(session(NodeId.START, 3, Phase.CHOOSING))
        assertTrue(vm.invalid)
        vm.setActive(true)
        tick(3)
        assertEquals(0, vm.state.value.tick)
        assertNull(vm.state.value.outcome)
    }

    @Test fun victoryFinishLevelsUp() = runTest {
        val s = session(weakNode, 60)
        val vm = vm(s)
        vm.setActive(true)
        fightUntilOver(vm)
        assertEquals(Outcome.VICTORY, vm.state.value.outcome)
        vm.finish()
        assertEquals(Phase.CHOOSING, s.profile.value.run.phase)
        assertEquals(61, s.profile.value.run.level)
        vm.finish()
        assertEquals(61, s.profile.value.run.level)
        assertEquals(60, vm.state.value.levelBefore)
    }

    @Test fun defeatFinishReturnsToPreviousNode() = runTest {
        val s = session(lateNode, 1)
        val previous = s.profile.value.run.path.first()
        val vm = vm(s, clock = { 777L })
        vm.setActive(true)
        repeat(500) { if (vm.state.value.outcome == null) tick() }
        assertEquals(Outcome.DEFEAT, vm.state.value.outcome)
        vm.finish()
        val run = s.profile.value.run
        assertEquals(previous, run.node)
        assertEquals(Phase.CHOOSING, run.phase)
        assertEquals(1, run.level)
        assertEquals(1, run.deaths)
        assertTrue(s.profile.value.history.isEmpty())
        vm.finish()
        assertEquals(1, s.profile.value.run.deaths)
    }

    @Test fun abandonCountsAsDefeat() = runTest {
        val s = session(weakNode, 5)
        val previous = s.profile.value.run.path.first()
        val vm = vm(s)
        vm.setActive(true)
        tick(2)
        assertEquals(2, vm.state.value.tick)
        vm.abandon()
        assertEquals(previous, s.profile.value.run.node)
        assertEquals(Phase.CHOOSING, s.profile.value.run.phase)
        assertEquals(5, s.profile.value.run.level)
        assertEquals(1, s.profile.value.run.deaths)
        assertTrue(s.profile.value.history.isEmpty())
        tick(3)
        assertEquals(2, vm.state.value.tick)
        vm.abandon()
        vm.finish()
        assertEquals(1, s.profile.value.run.deaths)
    }

    @Test fun abandonAfterVictoryKeepsVictory() = runTest {
        val s = session(weakNode, 60)
        val vm = vm(s)
        vm.setActive(true)
        fightUntilOver(vm)
        assertEquals(Outcome.VICTORY, vm.state.value.outcome)
        vm.abandon()
        assertEquals(Phase.CHOOSING, s.profile.value.run.phase)
        assertEquals(61, s.profile.value.run.level)
        assertTrue(s.profile.value.history.isEmpty())
        vm.finish()
        assertEquals(61, s.profile.value.run.level)
    }

    @Test fun ticksStopAfterOutcome() = runTest {
        val vm = vm(session(weakNode, 60))
        vm.setActive(true)
        fightUntilOver(vm)
        assertNotNull(vm.state.value.outcome)
        val seen = mutableListOf<CombatEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.collect { seen += it } }
        val tickAtEnd = vm.state.value.tick
        tick(5)
        vm.press(Input.SPEAR)
        vm.execute()
        vm.clearBuffer()
        vm.selectTarget(FighterId(1))
        assertEquals(tickAtEnd, vm.state.value.tick)
        assertFalse(seen.any { it == CombatEvent.ComboRejected(PLAYER_ID, RejectReason.COMBAT_OVER) })
        assertTrue(seen.isEmpty())
    }

    @Test fun eventsAreEmitted() = runTest {
        val vm = vm(session(weakNode, 3))
        val seen = mutableListOf<CombatEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.collect { seen += it } }
        vm.execute() // empty buffer -> rejected
        assertTrue(seen.any { it is CombatEvent.ComboRejected })
        vm.press(Input.SPEAR)
        vm.press(Input.SHIELD)
        vm.execute()
        assertTrue(seen.any { it is CombatEvent.ComboQueued })
        vm.setActive(true)
        tick()
        assertTrue(seen.any { it is CombatEvent.ComboResolved })
        vm.setActive(false)
    }

    /**
     * Bug report 2026-10-13 (eregion_3_0, two Crebain against a Warden Lv 18): the rivals' damage reaches the player
     * as negative EffectResolved events, and those become red numbers over the player's card.
     */
    @Test fun rivalDamageShowsRedOverThePlayerCard() = runTest {
        val node = NodeId("eregion", 3, 0)
        val vm = vm(session(node, 18))
        val seen = mutableListOf<CombatEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.collect { seen += it } }
        val hpBefore = vm.state.value.player.hp
        vm.setActive(true)
        tick(12)
        vm.setActive(false)
        val hits = seen.filterIsInstance<CombatEvent.EffectResolved>().filter { it.target == PLAYER_ID && it.amount < 0 }
        assertTrue("rival damage on the player: $seen", hits.isNotEmpty())
        assertTrue(vm.state.value.player.hp < hpBefore)
        val numbers = FloatingNumbersState().add(seen.mapNotNull(::combatTextFor), { 1000 }, 0L, kotlin.random.Random(1)).forTarget(PLAYER_ID)
        assertTrue("red player numbers: $numbers", numbers.any { it.tone == CombatTextTone.DAMAGE && it.text.startsWith("−") })
        hits.forEach { assertEquals(CombatTextTone.DAMAGE, combatTextFor(it)?.tone) }
    }

    /** The cards show the engine's portrait key: the content's for the rivals, "warden" for the player. */
    @Test fun viewModelMapsPortrait() = runTest {
        val node = Route.zones.flatMap { TreeShape.nodesOf(it.id) }
            .first { engine.node(it).encounter?.rivals?.firstOrNull()?.portrait == "goblin" }
        val vm = vm(session(node, 10))
        assertEquals("goblin", vm.state.value.rivals[0].portrait)
        assertEquals("warden", vm.state.value.player.portrait)
    }

    /** Regression ("won with HP left on the rival"): every emitted state with VICTORY shows all rivals at 0 HP. */
    @Test fun victoryStateNeverShowsRivalHp() = runTest {
        val vm = vm(session(pairNode, 60))
        val seen = mutableListOf<CombatUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect { seen += it } }
        vm.setActive(true)
        fightUntilOver(vm)
        assertEquals(Outcome.VICTORY, vm.state.value.outcome)
        val victories = seen.filter { it.outcome == Outcome.VICTORY }
        assertTrue(victories.isNotEmpty())
        victories.forEach { ui ->
            assertTrue(ui.rivals.size >= 2)
            ui.rivals.forEach {
                assertEquals(0, it.hp)
                assertFalse(it.isAlive)
                assertTrue(it.badges.isEmpty())
                assertEquals(0, it.power)
            }
        }
        val lastLive = seen.last { it.outcome == null }
        assertEquals(lastLive.tick + 1, victories.first().tick)
    }
}
