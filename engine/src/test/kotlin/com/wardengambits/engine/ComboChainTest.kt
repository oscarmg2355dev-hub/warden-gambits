package com.wardengambits.engine

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.combo.ChainRules
import com.wardengambits.engine.combo.ComboChain
import com.wardengambits.engine.combo.ComboFamily
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.event.RejectReason
import com.wardengambits.engine.model.ActiveEffect
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.RivalScaling
import com.wardengambits.engine.rules.CombatEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComboChainTest {
    /** Folds [sequences] through [ChainRules.next], returning the chained flags and the final chain. */
    private fun run(vararg sequences: String, from: ComboChain = ComboChain()): Pair<List<Boolean>, ComboChain> {
        var chain = from
        val flags = sequences.map { seq ->
            val (next, chained) = ChainRules.next(chain, Combos.find(seq)!!)
            chain = next
            chained
        }
        return flags to chain
    }

    @Test fun fullChainAdvances() {
        val (flags, chain) = run("12", "121", "1212", "12121")
        assertEquals(listOf(false, true, true, true), flags)
        assertEquals(ComboChain(ComboFamily.SPEAR_A, 4), chain)
    }

    @Test fun tierFourRepeatedKeepsBonus() {
        val (flags, chain) = run("12", "121", "1212", "12121", "12121", "12121")
        assertEquals(listOf(false, true, true, true, true, true), flags)
        assertEquals(ComboChain(ComboFamily.SPEAR_A, 4), chain)
    }

    @Test fun tierFourWithoutChainIsNotChained() {
        val (flags, chain) = run("12121", "12121")
        assertEquals(listOf(false, false), flags)
        assertEquals(ComboChain(), chain)
    }

    @Test fun skippingATierBreaksTheChain() {
        val (flags, chain) = run("12", "1212", "121")
        assertEquals(listOf(false, false, false), flags)
        assertEquals(ComboChain(), chain)
    }

    @Test fun repeatingANonFourTierBreaks() {
        val (flags, chain) = run("12", "121", "121", "1212")
        assertEquals(listOf(false, true, false, false), flags)
        assertEquals(ComboChain(), chain)
    }

    @Test fun untieredComboBreaks() {
        listOf("11", "22", "33", "2132", "2131", "123", "321").forEach { breaker ->
            val (flags, chain) = run("12", breaker, "121")
            assertEquals(listOf(false, false, false), flags, breaker)
            assertEquals(ComboChain(), chain, breaker)
        }
    }

    @Test fun otherFamilyBreaks() {
        val (flags, chain) = run("12", "13", "121")
        assertEquals(listOf(false, false, false), flags)
        assertEquals(ComboChain(), chain)
        val (afterHeal, _) = run("12", "21", "121")
        assertEquals(listOf(false, false, false), afterHeal)
    }

    @Test fun otherFamilyTierOneStartsItsOwnChain() {
        val (flags, chain) = run("12", "13", "131")
        assertEquals(listOf(false, false, true), flags)
        assertEquals(ComboChain(ComboFamily.SPEAR_B, 2), chain)
    }

    @Test fun tierOneRestartsAChain() {
        val (flags, chain) = run("12", "121", "12", "121")
        assertEquals(listOf(false, true, false, true), flags)
        assertEquals(ComboChain(ComboFamily.SPEAR_A, 2), chain)
    }

    @Test fun drainChain() {
        assertEquals(listOf(false, true), run("3123", "31232").first)
        assertEquals(listOf(false), run("31232").first)
    }

    // Engine level: chain lives on the fighter and is only touched by resolved casts.

    private val engine = CombatEngine(RivalScaling.IDENTITY) { FixedRandom(99) }

    private fun cast(state: CombatState, seq: String) = engine.reduce(
        engine.reduce(engine.press(state, PLAYER_ID, seq), CombatAction.Execute(PLAYER_ID)).state,
        CombatAction.Tick,
    )

    @Test fun resolvedCastsUpdateFighterChainAndEvent() {
        val first = cast(engine.start(setup(1, 1), 1L), "12")
        assertEquals(ComboChain(ComboFamily.SPEAR_A, 1), first.state.player.chain)
        assertTrue(CombatEvent.ComboResolved(PLAYER_ID, Combos.find("12")!!, chained = false) in first.events)
        val second = cast(first.state, "121")
        assertEquals(ComboChain(ComboFamily.SPEAR_A, 2), second.state.player.chain)
        assertTrue(CombatEvent.ComboResolved(PLAYER_ID, Combos.find("121")!!, chained = true) in second.events)
    }

    @Test fun rejectedForPowerLeavesChainUnchanged() {
        val first = cast(engine.start(setup(1, 1), 1L), "12")
        val broke = first.state.withFighter(PLAYER_ID) { copy(power = 0) }
        val rejected = cast(broke, "13")
        assertTrue(CombatEvent.ComboRejected(PLAYER_ID, RejectReason.NOT_ENOUGH_POWER) in rejected.events)
        assertEquals(ComboChain(ComboFamily.SPEAR_A, 1), rejected.state.player.chain)
        val next = cast(rejected.state.withFighter(PLAYER_ID) { copy(power = 100) }, "121")
        assertTrue(CombatEvent.ComboResolved(PLAYER_ID, Combos.find("121")!!, chained = true) in next.events)
    }

    @Test fun rejectedWhileDazedLeavesChainUnchanged() {
        val first = cast(engine.start(setup(1, 1), 1L), "12")
        val dazed = first.state.withFighter(PLAYER_ID) {
            copy(effects = listOf(ActiveEffect(EffectKind.DAZED, PLAYER_ID, "", 0, 3, activeFrom = 1)))
        }
        val rejected = cast(dazed, "13")
        assertTrue(CombatEvent.ComboRejected(PLAYER_ID, RejectReason.DAZED) in rejected.events)
        assertEquals(ComboChain(ComboFamily.SPEAR_A, 1), rejected.state.player.chain)
    }
}
