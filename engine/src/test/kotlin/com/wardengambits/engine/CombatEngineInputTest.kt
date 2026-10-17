package com.wardengambits.engine

import com.wardengambits.engine.action.CombatAction.ClearBuffer
import com.wardengambits.engine.action.CombatAction.Execute
import com.wardengambits.engine.action.CombatAction.Press
import com.wardengambits.engine.action.CombatAction.SelectTarget
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.combo.Input
import com.wardengambits.engine.event.CombatEvent.ComboQueued
import com.wardengambits.engine.event.CombatEvent.ComboRejected
import com.wardengambits.engine.event.RejectReason.BUFFER_FULL
import com.wardengambits.engine.event.RejectReason.FIGHTER_DEFEATED
import com.wardengambits.engine.event.RejectReason.INVALID_TARGET
import com.wardengambits.engine.event.RejectReason.UNKNOWN_SEQUENCE
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.RivalScaling
import com.wardengambits.engine.rules.CombatEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CombatEngineInputTest {
    private val engine = CombatEngine(RivalScaling.IDENTITY)
    private val start = engine.start(setup(1, 1), seed = 1)
    private val start2Rivals = engine.start(setup(1, 1, 1), seed = 1)

    @Test
    fun startFillsHpAndPower() {
        val s = engine.start(setup(1, 1, 5), seed = 1)
        assertEquals(listOf(10_000, 10_000, 18_000), s.fighters.map { it.hp })
        assertEquals(listOf(300, 300, 300), s.fighters.map { it.power })
        assertEquals(FighterId(1), s.targetId)
        assertEquals(0, s.tick)
        assertEquals(1L, s.rngState)
    }

    @Test
    fun pressAppendsToBuffer() = assertEquals("12", engine.press(start, PLAYER_ID, "12").player.buffer)

    @Test
    fun sixthPressRejected() {
        val r = engine.reduce(engine.press(start, PLAYER_ID, "12121"), Press(PLAYER_ID, Input.SPEAR))
        assertEquals("12121", r.state.player.buffer)
        assertEquals(listOf(ComboRejected(PLAYER_ID, BUFFER_FULL)), r.events)
    }

    @Test
    fun executeKnownQueuesAndClears() {
        val r = engine.reduce(engine.press(start, PLAYER_ID, "121"), Execute(PLAYER_ID))
        assertEquals(Combos.find("121"), r.state.player.queued)
        assertEquals("", r.state.player.buffer)
        assertEquals(300, r.state.player.power)
        assertEquals(listOf(ComboQueued(PLAYER_ID, Combos.find("121")!!)), r.events)
    }

    @Test
    fun executeUnknownRejectsAndClears() {
        val r = engine.reduce(engine.press(start, PLAYER_ID, "111"), Execute(PLAYER_ID))
        assertNull(r.state.player.queued)
        assertEquals("", r.state.player.buffer)
        assertEquals(listOf(ComboRejected(PLAYER_ID, UNKNOWN_SEQUENCE)), r.events)
    }

    @Test
    fun executeEmptyBufferRejected() =
        assertEquals(listOf(ComboRejected(PLAYER_ID, UNKNOWN_SEQUENCE)), engine.reduce(start, Execute(PLAYER_ID)).events)

    @Test
    fun executeReplacesQueued() {
        val first = engine.reduce(engine.press(start, PLAYER_ID, "12"), Execute(PLAYER_ID)).state
        val second = engine.reduce(engine.press(first, PLAYER_ID, "21"), Execute(PLAYER_ID)).state
        assertEquals(Combos.find("21"), second.player.queued)
    }

    @Test
    fun clearBufferEmpties() {
        val r = engine.reduce(engine.press(start, PLAYER_ID, "12"), ClearBuffer(PLAYER_ID))
        assertEquals("", r.state.player.buffer)
        assertTrue(r.events.isEmpty())
    }

    @Test
    fun clearBufferOnEmptyIsNoop() {
        val r = engine.reduce(start, ClearBuffer(PLAYER_ID))
        assertEquals(start, r.state)
        assertTrue(r.events.isEmpty())
    }

    @Test
    fun selectTargetAliveRival() =
        assertEquals(FighterId(2), engine.reduce(start2Rivals, SelectTarget(FighterId(2))).state.targetId)

    @Test
    fun selectTargetInvalid() {
        val deadRival = start2Rivals.withFighter(FighterId(2)) { copy(hp = 0) }
        for (target in listOf(PLAYER_ID, FighterId(9), FighterId(2))) {
            val r = engine.reduce(deadRival, SelectTarget(target))
            assertEquals(deadRival, r.state)
            assertEquals(listOf(ComboRejected(PLAYER_ID, INVALID_TARGET)), r.events)
        }
    }

    @Test
    fun pressOnDefeatedFighterRejected() {
        val dead = start.withFighter(FighterId(1)) { copy(hp = 0) }
        val r = engine.reduce(dead, Press(FighterId(1), Input.SPEAR))
        assertEquals(dead, r.state)
        assertEquals(listOf(ComboRejected(FighterId(1), FIGHTER_DEFEATED)), r.events)
    }

    @Test
    fun unknownFighterTreatedAsDefeated() {
        val r = engine.reduce(start, Press(FighterId(9), Input.SPEAR))
        assertEquals(start, r.state)
        assertEquals(listOf(ComboRejected(FighterId(9), FIGHTER_DEFEATED)), r.events)
    }
}
