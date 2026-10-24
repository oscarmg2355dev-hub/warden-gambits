package com.wardengambits.engine.tutorial

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.event.RejectReason
import com.wardengambits.engine.press
import com.wardengambits.engine.rules.CombatEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TutorialTest {
    private val lessons = TutorialLessons.load()
    private fun lesson(id: String) = lessons.first { it.id == id }
    private fun resolved(seq: String, who: FighterId = PLAYER_ID) =
        CombatEvent.ComboResolved(who, Combos.find(seq)!!, chained = false)

    @Test fun lessonsMatchTheSpec() {
        val expected = mapOf(
            "attack" to (listOf("11", "12", "121", "1212", "12121") to 1),
            "defense" to (listOf("2132", "22", "21", "212", "2121", "21212") to 1),
            "daze" to (listOf("2131", "33", "31", "313", "3131", "31313") to 1),
            "drain" to (listOf("22", "3123", "31232") to 3),
            "finisher" to (listOf("11", "123", "13", "131", "1313", "13131") to 1),
        )
        assertEquals(expected.keys.toList(), lessons.map { it.id })
        for ((id, spec) in expected) {
            val l = lesson(id)
            assertEquals(spec.first, l.steps, id)
            assertEquals(spec.second, l.rivals.size, id)
            l.steps.forEach { assertNotNull(Combos.find(it), "$id: $it") }
            assertTrue(l.rivals.all { it.portrait == "dummy" })
        }
    }

    @Test fun advancesOnRequestedCombo() {
        val l = lesson("attack")
        val p = TutorialProgress("attack").advance(l, listOf(resolved("11")))
        assertEquals(1, p.step)
        assertEquals(0, p.misses)
    }

    @Test fun otherComboCountsMiss() {
        val l = lesson("attack")
        val p = TutorialProgress("attack").advance(l, listOf(resolved("12")))
        assertEquals(0, p.step)
        assertEquals(1, p.misses)
    }

    @Test fun rivalCombosIgnored() {
        val l = lesson("attack")
        val p = TutorialProgress("attack").advance(l, listOf(resolved("11", FighterId(1)), resolved("12", FighterId(1))))
        assertEquals(TutorialProgress("attack"), p)
    }

    @Test fun rejectedComboDoesNotAdvance() {
        val l = lesson("attack")
        val events = listOf(
            CombatEvent.ComboRejected(PLAYER_ID, RejectReason.NOT_ENOUGH_POWER),
            CombatEvent.ComboQueued(PLAYER_ID, Combos.find("11")!!),
        )
        assertEquals(TutorialProgress("attack"), TutorialProgress("attack").advance(l, events))
    }

    @Test fun unknownSequenceCountsMissOnlyForPlayer() {
        val l = lesson("attack")
        val own = TutorialProgress("attack")
            .advance(l, listOf(CombatEvent.ComboRejected(PLAYER_ID, RejectReason.UNKNOWN_SEQUENCE)))
        assertEquals(TutorialProgress("attack", misses = 1), own)
        val rival = TutorialProgress("attack")
            .advance(l, listOf(CombatEvent.ComboRejected(FighterId(1), RejectReason.UNKNOWN_SEQUENCE)))
        assertEquals(TutorialProgress("attack"), rival)
    }

    @Test fun recoverComboIsNotAMiss() {
        val l = lesson("attack")
        assertEquals(TutorialProgress("attack"), TutorialProgress("attack").advance(l, listOf(resolved("321"))))
    }

    @Test fun completesAfterLastStep() {
        val l = lesson("attack")
        val p = TutorialProgress("attack").advance(l, l.steps.map { resolved(it) })
        assertEquals(5, p.step)
        assertTrue(p.completed)
    }

    @Test fun setupHasFloorAndLevelOne() {
        for (l in lessons) {
            val s = TutorialLessons.setup(l)
            assertEquals(10, s.hpFloorPct)
            assertEquals(1, s.player.level)
            assertTrue(s.rivals.all { it.level == 1 && !it.isBoss })
            assertEquals(l.rivals.size, s.rivals.size)
        }
    }

    @Test fun fullLessonPlayableWithRealEngine() {
        val engine = CombatEngine()
        for (l in lessons) {
            var state = engine.start(TutorialLessons.setup(l), 7L)
            var progress = TutorialProgress(l.id)
            var ticks = 0
            for (seq in l.steps) {
                val combo = Combos.find(seq)!!
                var cast = false
                while (!progress.completed && progress.step < l.steps.indexOf(seq) + 1 && ticks < 2000) {
                    if (!cast && state.player.power >= combo.cost && state.player.queued == null && state.player.isAlive) {
                        state = engine.press(state, PLAYER_ID, combo.sequence)
                        val ex = engine.reduce(state, CombatAction.Execute(PLAYER_ID))
                        state = ex.state
                        progress = progress.advance(l, ex.events)
                        cast = state.player.queued != null || progress.step > l.steps.indexOf(seq)
                    }
                    if (progress.step > l.steps.indexOf(seq)) break
                    val t = engine.reduce(state, CombatAction.Tick)
                    state = t.state
                    ticks++
                    progress = progress.advance(l, t.events)
                    assertFalse(t.events.any { it is CombatEvent.CombatEnded }, "${l.id} ended")
                }
            }
            assertTrue(progress.completed, "${l.id}: step ${progress.step}, misses ${progress.misses}")
            assertEquals(0, progress.misses, l.id)
            assertNull(state.outcome, l.id)
        }
    }
}
