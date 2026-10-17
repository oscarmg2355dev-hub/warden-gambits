package com.wardengambits.engine

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.event.RejectReason
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.RivalScaling
import com.wardengambits.engine.random.GameRandom
import com.wardengambits.engine.rules.CombatEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EffectTimingTest {
    private val rival = FighterId(1)

    private fun engine(random: () -> GameRandom = { FixedRandom(99) }) = CombatEngine(RivalScaling.IDENTITY, tuning = TEST_TUNING) { random() }

    private val engine = engine()

    private fun started(): CombatState = engine.start(setup(1, 1), 1L)

    /** Queues each (id, seq) pair, then runs one tick. */
    private fun step(state: CombatState, vararg casts: Pair<FighterId, String>, engine: CombatEngine = this.engine) =
        engine.reduce(
            casts.fold(state) { current, (id, seq) ->
                engine.reduce(engine.press(current, id, seq), CombatAction.Execute(id)).state
            },
            CombatAction.Tick,
        )

    private fun rejectedDazed(events: List<CombatEvent>, id: FighterId) =
        CombatEvent.ComboRejected(id, RejectReason.DAZED) in events

    private fun resolved(events: List<CombatEvent>, id: FighterId) =
        events.any { it is CombatEvent.ComboResolved && it.fighter == id }

    private fun dazeBlocksExactlyTenCasts(caster: FighterId, victim: FighterId) {
        val alwaysDaze = engine { FixedRandom(0) }
        var r = step(alwaysDaze.start(setup(1, 1), 1L), caster to "31", victim to "21", engine = alwaysDaze)
        assertTrue(resolved(r.events, victim), "the victim still casts on the tick it gets dazed")
        assertTrue(CombatEvent.EffectApplied(victim, EffectKind.DAZED, 10) in r.events)
        repeat(10) {
            r = step(r.state.withFighter(victim) { copy(power = 100) }, victim to "21", engine = alwaysDaze)
            assertTrue(rejectedDazed(r.events, victim), "blocked cast ${it + 1}")
        }
        r = step(r.state.withFighter(victim) { copy(power = 100) }, victim to "21", engine = alwaysDaze)
        assertTrue(resolved(r.events, victim))
    }

    @Test fun playerDazeBlocksRivalExactlyTenCasts() = dazeBlocksExactlyTenCasts(PLAYER_ID, rival)

    @Test fun rivalDazeBlocksPlayerExactlyTenCasts() = dazeBlocksExactlyTenCasts(rival, PLAYER_ID)

    @Test fun furyActiveExactlyThirtyTicks() {
        var r = step(started(), PLAYER_ID to "11")
        val fury = r.state.player.effects.single { it.kind == EffectKind.FURY }
        assertEquals(2, fury.activeFrom)
        assertEquals(30, fury.remainingTicks)
        assertFalse(r.state.player.isActive(EffectKind.FURY, 1))
        repeat(29) { r = step(r.state) }
        assertEquals(30, r.state.tick)
        assertEquals(1, r.state.player.effects.single { it.kind == EffectKind.FURY }.remainingTicks)
        r = step(r.state)
        assertFalse(r.state.player.has(EffectKind.FURY))
    }

    @Test fun shieldActiveExactlySixtyTicks() {
        var r = step(started(), PLAYER_ID to "2131")
        repeat(60) {
            assertTrue(r.state.player.has(EffectKind.SHIELD_TACTICS))
            r = step(r.state)
        }
        assertEquals(61, r.state.tick)
        assertFalse(r.state.player.has(EffectKind.SHIELD_TACTICS))
    }

    @Test fun dotHitsImmediatelyForTenTicks() {
        var r = step(started(), PLAYER_ID to "12")
        assertEquals(9_800, r.state.fighter(rival).hp)
        repeat(9) { r = step(r.state) }
        assertEquals(8_000, r.state.fighter(rival).hp)
        assertFalse(r.state.fighter(rival).has(EffectKind.DOT))
        r = step(r.state)
        assertEquals(8_000, r.state.fighter(rival).hp)
    }

    @Test fun recastDotRestartsFromTheRecastTick() {
        var r = step(started(), PLAYER_ID to "12")
        repeat(2) { r = step(r.state) }
        r = step(r.state, PLAYER_ID to "12")
        assertEquals(4, r.state.tick)
        assertEquals(1, r.state.fighter(rival).effects.count { it.kind == EffectKind.DOT })
        repeat(9) { r = step(r.state) }
        assertEquals(13, r.state.tick)
        assertEquals(7_400, r.state.fighter(rival).hp)
        assertFalse(r.state.fighter(rival).has(EffectKind.DOT))
    }

    @Test fun refreshExtendsActiveBuffToTPlusN() {
        var r = step(started(), PLAYER_ID to "11")
        repeat(2) { r = step(r.state) }
        r = step(r.state, PLAYER_ID to "11")
        assertEquals(4, r.state.tick)
        assertEquals(2, r.state.player.effects.single { it.kind == EffectKind.FURY }.activeFrom)
        repeat(29) { r = step(r.state) }
        assertEquals(33, r.state.tick)
        assertEquals(1, r.state.player.effects.single { it.kind == EffectKind.FURY }.remainingTicks)
        r = step(r.state)
        assertEquals(34, r.state.tick)
        assertFalse(r.state.player.has(EffectKind.FURY))
    }

    @Test fun furyAppliesFromNextTick() {
        // Fury cast on tick 1 is not active yet for a spear cast on the same tick, but is on tick 2.
        val sameTick = step(started(), PLAYER_ID to "11")
        val r = step(sameTick.state.withFighter(PLAYER_ID) { copy(power = 100) }, PLAYER_ID to "12121")
        assertEquals(960, r.state.fighter(rival).effects.single { it.kind == EffectKind.DOT }.magnitude)
    }
}
