package com.wardengambits.engine

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.event.CombatEvent.EffectResolved
import com.wardengambits.engine.model.ActiveEffect
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.RivalScaling
import com.wardengambits.engine.rules.CombatEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Per-effect HP events (combat text strip spec §1). Level 1 fighters with identity scaling, max HP 10000. */
class EffectResolvedTest {
    private val engine = CombatEngine(RivalScaling.IDENTITY, tuning = TEST_TUNING) { FixedRandom(99) }
    private val r1 = FighterId(1)
    private val r2 = FighterId(2)

    private fun started(vararg rivalLevels: Int = intArrayOf(1)): CombatState = engine.start(setup(1, *rivalLevels), 1L)

    private fun tick(state: CombatState) = engine.reduce(state, CombatAction.Tick)

    private fun resolved(state: CombatState) = tick(state).events.filterIsInstance<EffectResolved>()

    private fun dot(magnitude: Int, combo: String = "12", source: FighterId = r1) =
        ActiveEffect(EffectKind.DOT, source, combo, magnitude, 5)

    private fun cot(magnitude: Int, combo: String = "21", source: FighterId = PLAYER_ID) =
        ActiveEffect(EffectKind.COT, source, combo, magnitude, 5)

    private fun buff(kind: EffectKind) = ActiveEffect(kind, PLAYER_ID, kind.name, 0, 3)

    @Test fun twoDotsAndACotGiveThreeEventsInListOrder() {
        val state = started().withFighter(PLAYER_ID) {
            copy(hp = 5_000, effects = listOf(dot(300), cot(200), dot(400, "13")))
        }
        assertEquals(
            listOf(
                EffectResolved(PLAYER_ID, r1, "12", EffectKind.DOT, -300),
                EffectResolved(PLAYER_ID, PLAYER_ID, "21", EffectKind.COT, 200),
                EffectResolved(PLAYER_ID, r1, "13", EffectKind.DOT, -400),
            ),
            resolved(state),
        )
    }

    @Test fun guardAndMasterShieldReduceEachDot() {
        val state = started().withFighter(PLAYER_ID) {
            copy(effects = listOf(dot(1_000), dot(1, "13"), buff(EffectKind.GUARD), buff(EffectKind.SHIELD_MASTER)))
        }
        val result = tick(state)
        assertEquals(listOf(-400, -1), result.events.filterIsInstance<EffectResolved>().map { it.amount })
        assertEquals(9_599, result.state.player.hp)
    }

    @Test fun cappedCotShowsTheRealHeal() {
        val nearlyFull = started().withFighter(PLAYER_ID) { copy(hp = 9_999, effects = listOf(cot(200), cot(300, "212"))) }
        assertEquals(listOf(1, 0), resolved(nearlyFull).map { it.amount })

        val full = started().withFighter(PLAYER_ID) { copy(effects = listOf(cot(200))) }
        assertEquals(listOf(EffectResolved(PLAYER_ID, PLAYER_ID, "21", EffectKind.COT, 0)), resolved(full))
    }

    @Test fun shownAmountsAddUpToTheHpChangeWhenTheFighterSurvives() {
        val state = started().withFighter(PLAYER_ID) { copy(effects = listOf(cot(500), dot(300))) }
        val result = tick(state)
        assertEquals(listOf(300, -300), result.events.filterIsInstance<EffectResolved>().map { it.amount })
        assertEquals(10_000, result.state.player.hp)
    }

    @Test fun effectsAfterTheLethalOneAreNotShown() {
        val state = started().withFighter(r1) {
            copy(hp = 300, effects = listOf(dot(800, source = PLAYER_ID), cot(400, source = r1)))
        }
        val result = tick(state)
        assertEquals(0, result.state.fighter(r1).hp)
        assertEquals(listOf(EffectResolved(r1, PLAYER_ID, "12", EffectKind.DOT, -300)), result.events.filterIsInstance<EffectResolved>())
        assertTrue(CombatEvent.FighterDefeated(r1) in result.events)
    }

    @Test fun lethalDotShowsOnlyTheHpItRemoved() {
        val state = started().withFighter(r1) { copy(hp = 300, effects = listOf(dot(800, source = PLAYER_ID))) }
        assertEquals(listOf(-300), resolved(state).map { it.amount })
    }

    @Test fun shownAmountsAddUpToTheHpChangeWhenAHealIsCappedAtMaxHpWithADotActive() {
        val state = started().withFighter(PLAYER_ID) { copy(hp = 9_900, effects = listOf(cot(500), dot(300))) }
        val result = tick(state)
        assertEquals(10_000, result.state.player.hp)
        assertEquals(listOf(400, -300), result.events.filterIsInstance<EffectResolved>().map { it.amount })
    }

    @Test fun drainCasterAtFullHpStillGetsAZeroEvent() {
        var state = engine.start(setup(1, 1), 1L)
        state = engine.reduce(engine.press(state, PLAYER_ID, "3123"), CombatAction.Execute(PLAYER_ID)).state
        var r = tick(state)
        repeat(5) { r = tick(r.state) }
        assertTrue(EffectResolved(PLAYER_ID, PLAYER_ID, "3123", EffectKind.DRAIN, 0) in r.events)
    }

    @Test fun drainPulseEmitsOnePerRivalAndOneForTheCaster() {
        var state = engine.start(setup(1, 1, 1), 1L)
            .withFighter(PLAYER_ID) { copy(hp = 5_000) }
        state = engine.reduce(engine.press(state, PLAYER_ID, "3123"), CombatAction.Execute(PLAYER_ID)).state
        var r = tick(state)
        repeat(5) { r = tick(r.state) }
        assertEquals(6, r.state.tick)
        assertEquals(
            listOf(
                EffectResolved(r1, PLAYER_ID, "3123", EffectKind.DRAIN, -200),
                EffectResolved(r2, PLAYER_ID, "3123", EffectKind.DRAIN, -200),
                EffectResolved(PLAYER_ID, PLAYER_ID, "3123", EffectKind.DRAIN, 400),
            ),
            r.events.filterIsInstance<EffectResolved>(),
        )
    }

    @Test fun directHitEmitsItsEventWithoutKind() {
        val state = started().withFighter(PLAYER_ID) { copy(queued = Combos.find("123")) }
        val result = tick(state)
        assertTrue(EffectResolved(r1, PLAYER_ID, "123", null, -1_800) in result.events)
    }

    @Test fun orderIsFighterIndexThenEffectList() {
        val state = started(1, 1)
            .withFighter(PLAYER_ID) { copy(hp = 5_000, effects = listOf(cot(200), dot(100, source = r2))) }
            .withFighter(r1) { copy(effects = listOf(dot(200, "13", PLAYER_ID), dot(300, "12", PLAYER_ID))) }
            .withFighter(r2) { copy(effects = listOf(dot(400, "32", PLAYER_ID))) }
        assertEquals(
            listOf(PLAYER_ID to 200, PLAYER_ID to -100, r1 to -200, r1 to -300, r2 to -400),
            resolved(state).map { it.target to it.amount },
        )
    }
}
