package com.wardengambits.engine

import com.wardengambits.engine.combo.ComboTuning
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.ActiveEffect
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.RivalScaling
import com.wardengambits.engine.rules.CombatEngine
import com.wardengambits.engine.rules.ComboApplier
import com.wardengambits.engine.rules.StepResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Level 1 (HP 10000, attack 1000, heal 800, defense 200): SPDMG 200, FIDMG 180, CUR 200 with [TEST_TUNING]. */
class ComboApplierTest {
    private val rival = FighterId(1)
    private val tick = 5

    private fun start(vararg rivalLevels: Int): CombatState =
        CombatEngine(RivalScaling.IDENTITY).start(setup(1, *rivalLevels), 1L).copy(tick = tick)

    private fun cast(
        state: CombatState,
        sequence: String,
        chained: Boolean = false,
        targets: List<FighterId> = listOf(rival),
    ): StepResult = ComboApplier.apply(state, PLAYER_ID, targets, Combos.find(sequence)!!, chained, TEST_TUNING, FixedRandom(0))

    private fun self(state: CombatState, sequence: String, chained: Boolean = false) =
        cast(state, sequence, chained, listOf(PLAYER_ID))

    private fun CombatState.effects(id: FighterId, kind: EffectKind) = fighter(id).effects.filter { it.kind == kind }

    private fun withEffects(state: CombatState, id: FighterId, vararg effects: ActiveEffect) =
        state.withFighter(id) { copy(effects = effects.toList()) }

    private fun buff(kind: EffectKind, combo: String, owner: FighterId = PLAYER_ID) =
        ActiveEffect(kind, owner, combo, 0, 10, activeFrom = 1)

    @Test fun spearDotMagnitudeAndEvents() {
        val result = cast(start(1), "121")
        assertEquals(listOf(ActiveEffect(EffectKind.DOT, PLAYER_ID, "121", 400, 10, tick)), result.state.fighter(rival).effects)
        assertEquals(10_000, result.state.fighter(rival).hp, "no immediate damage")
        assertEquals(1, result.state.player.castCount)
        assertEquals(
            listOf(
                CombatEvent.ComboResolved(PLAYER_ID, Combos.find("121")!!, false),
                CombatEvent.EffectApplied(rival, EffectKind.DOT, 10),
            ),
            result.events,
        )
    }

    @Test fun spearTiers() {
        assertEquals(200, cast(start(1), "12").state.effects(rival, EffectKind.DOT).single().magnitude)
        assertEquals(600, cast(start(1), "1212").state.effects(rival, EffectKind.DOT).single().magnitude)
        assertEquals(800, cast(start(1), "13131").state.effects(rival, EffectKind.DOT).single().magnitude)
    }

    @Test fun furyBoostsSpearOnly() {
        val fury = withEffects(start(1, 1), PLAYER_ID, buff(EffectKind.FURY, "11"))
        assertEquals(960, cast(fury, "12121").state.effects(rival, EffectKind.DOT).single().magnitude)
        val area = cast(fury, "32323", targets = listOf(rival, FighterId(2)))
        assertEquals(720, area.state.effects(rival, EffectKind.DOT).single().magnitude)
        val pendingFury = withEffects(start(1), PLAYER_ID, buff(EffectKind.FURY, "11").copy(activeFrom = tick + 1))
        assertEquals(800, cast(pendingFury, "12121").state.effects(rival, EffectKind.DOT).single().magnitude)
    }

    @Test fun areaDotOnAllAliveOpponents() {
        val state = start(1, 1, 1).withFighter(FighterId(2)) { copy(hp = 0) }
        val result = cast(state, "323", targets = listOf(FighterId(1), FighterId(3)))
        assertEquals(360, result.state.effects(FighterId(1), EffectKind.DOT).single().magnitude)
        assertEquals(360, result.state.effects(FighterId(3), EffectKind.DOT).single().magnitude)
        assertTrue(result.state.fighter(FighterId(2)).effects.isEmpty())
    }

    @Test fun durationsWithAndWithoutChain() {
        fun dotTicks(seq: String, chained: Boolean) = cast(start(1), seq, chained).state.effects(rival, EffectKind.DOT).single().remainingTicks
        assertEquals(10, dotTicks("12", false))
        assertEquals(10, dotTicks("121", false))
        assertEquals(15, dotTicks("121", true))
        assertEquals(20, dotTicks("1212", true))
        assertEquals(30, dotTicks("12121", true))
        assertEquals(15, cast(start(1, 1), "323", true, listOf(rival)).state.effects(rival, EffectKind.DOT).single().remainingTicks)
        assertEquals(20, self(start(1), "2121", true).state.effects(PLAYER_ID, EffectKind.COT).single().remainingTicks)
    }

    @Test fun healOverTime() {
        val result = self(start(1), "2121")
        assertEquals(listOf(ActiveEffect(EffectKind.COT, PLAYER_ID, "2121", 600, 10, tick)), result.state.player.effects)
        assertEquals(800, self(start(1), "21212").state.effects(PLAYER_ID, EffectKind.COT).single().magnitude)
    }

    @Test fun recastSameComboRestartsEffect() {
        val fury = withEffects(start(1), PLAYER_ID, buff(EffectKind.FURY, "11"))
        val first = cast(fury, "12").state
        assertEquals(240, first.effects(rival, EffectKind.DOT).single().magnitude)
        val decayed = first.withFighter(rival) { copy(effects = effects.map { it.copy(remainingTicks = 2) }) }
        val noFury = decayed.withFighter(PLAYER_ID) { copy(effects = emptyList()) }
        val again = cast(noFury, "12121").state
        val recast = cast(again, "12121", chained = true).state
        assertEquals(2, recast.effects(rival, EffectKind.DOT).size)
        val dot12121 = recast.effects(rival, EffectKind.DOT).single { it.sourceCombo == "12121" }
        assertEquals(30, dot12121.remainingTicks)
        assertEquals(tick, dot12121.activeFrom)
        val refreshed12 = cast(decayed, "12").state.effects(rival, EffectKind.DOT).single()
        assertEquals(10, refreshed12.remainingTicks)
        assertEquals(240, refreshed12.magnitude, "the player still has Furia")
        assertEquals(10, refreshed12.totalTicks)
        assertEquals(30, dot12121.totalTicks)
    }

    @Test fun recastRestartsMagnitude() {
        val furyState = withEffects(start(1), PLAYER_ID, buff(EffectKind.FURY, "11"))
        val strong = cast(furyState, "12121").state
        assertEquals(960, strong.effects(rival, EffectKind.DOT).single().magnitude)
        val weak = cast(strong.withFighter(PLAYER_ID) { copy(effects = emptyList()) }, "12121").state
        assertEquals(800, weak.effects(rival, EffectKind.DOT).single().magnitude)
    }

    @Test fun differentCombosStack() {
        val state = cast(cast(start(1), "12").state, "13").state
        assertEquals(listOf("12", "13"), state.effects(rival, EffectKind.DOT).map { it.sourceCombo })
        val tiers = cast(cast(start(1), "12").state, "121").state
        assertEquals(2, tiers.effects(rival, EffectKind.DOT).size)
    }

    @Test fun totalTicksIsTheCreationDurationAndRestartsOnRefresh() {
        val fury = self(start(1), "11").state.player.effects.single()
        assertEquals(30, fury.totalTicks)
        val aged = self(start(1), "11").state.withFighter(PLAYER_ID) { copy(effects = effects.map { it.copy(remainingTicks = 4) }) }
        val refreshed = self(aged.copy(tick = tick + 5), "11").state.player.effects.single()
        assertEquals(30, refreshed.totalTicks)
        assertEquals(60, self(start(1), "2132").state.player.effects.single().totalTicks)
    }

    @Test fun buffsActiveFromNextTick() {
        val fury = self(start(1), "11").state.player.effects.single()
        assertEquals(ActiveEffect(EffectKind.FURY, PLAYER_ID, "11", 0, 30, tick + 1), fury)
        val shield = self(start(1), "2132").state.player.effects.single()
        assertEquals(ActiveEffect(EffectKind.SHIELD_MASTER, PLAYER_ID, "2132", 0, 60, tick + 1), shield)
    }

    @Test fun refreshedActiveBuffKeepsActivation() {
        val active = withEffects(start(1), PLAYER_ID, buff(EffectKind.FURY, "11"))
        val fury = self(active, "11").state.player.effects.single()
        assertEquals(1, fury.activeFrom)
        assertEquals(31, fury.remainingTicks, "decremented this same tick, so active through t+30")
        val pending = withEffects(start(1), PLAYER_ID, buff(EffectKind.FURY, "11").copy(activeFrom = tick + 1, remainingTicks = 3))
        val stillPending = self(pending, "11").state.player.effects.single()
        assertEquals(tick + 1, stillPending.activeFrom)
        assertEquals(30, stillPending.remainingTicks)
    }

    @Test fun multipliersAreExclusive() {
        val state = withEffects(start(1), PLAYER_ID, buff(EffectKind.GUARD, "22"), buff(EffectKind.VIGOR, "33"))
        assertEquals(listOf(EffectKind.FURY), self(state, "11").state.player.effects.map { it.kind })
        assertEquals(listOf(EffectKind.VIGOR), self(withEffects(start(1), PLAYER_ID, buff(EffectKind.FURY, "11")), "33").state.player.effects.map { it.kind })
    }

    @Test fun shieldsAreExclusiveAndKeepMultipliers() {
        val state = withEffects(start(1), PLAYER_ID, buff(EffectKind.SHIELD_TACTICS, "2131"), buff(EffectKind.GUARD, "22"))
        assertEquals(
            setOf(EffectKind.GUARD, EffectKind.SHIELD_MASTER),
            self(state, "2132").state.player.effects.map { it.kind }.toSet(),
        )
    }

    @Test fun directHit() {
        val result = cast(start(1), "123")
        assertEquals(8_200, result.state.fighter(rival).hp)
        assertTrue(result.state.fighter(rival).effects.isEmpty())
        assertEquals(
            listOf(
                CombatEvent.ComboResolved(PLAYER_ID, Combos.find("123")!!, false),
                CombatEvent.EffectResolved(rival, PLAYER_ID, "123", null, -1_800),
            ),
            result.events,
        )
    }

    @Test fun directHitWithFuryAndReductions() {
        val fury = withEffects(start(1), PLAYER_ID, buff(EffectKind.FURY, "11"))
        assertEquals(7_800, cast(fury, "123").state.fighter(rival).hp)
        val guard = withEffects(start(1), rival, buff(EffectKind.GUARD, "22", rival))
        assertEquals(8_560, cast(guard, "123").state.fighter(rival).hp)
        val both = withEffects(start(1), rival, buff(EffectKind.GUARD, "22", rival), buff(EffectKind.SHIELD_MASTER, "2132", rival))
        assertEquals(9_280, cast(both, "123").state.fighter(rival).hp)
        val armored = start(1).withFighter(rival) { copy(stats = stats.copy(defense = 5_000)) }
        assertEquals(9_999, cast(armored, "123").state.fighter(rival).hp)
    }

    @Test fun lethalDirectHit() {
        val state = start(1).withFighter(rival) {
            copy(hp = 5, effects = listOf(ActiveEffect(EffectKind.DOT, PLAYER_ID, "12", 2, 3)))
        }
        val result = cast(state, "123")
        assertEquals(0, result.state.fighter(rival).hp)
        assertTrue(result.state.fighter(rival).effects.isEmpty())
        assertTrue(CombatEvent.EffectResolved(rival, PLAYER_ID, "123", null, -5) in result.events)
        assertTrue(CombatEvent.FighterDefeated(rival) in result.events)
    }

    @Test fun dazeChanceMagnitudes() {
        fun chance(seq: String, chained: Boolean) =
            cast(start(1), seq, chained).state.effects(rival, EffectKind.DAZE_CHANCE).single()
        assertEquals(ActiveEffect(EffectKind.DAZE_CHANCE, PLAYER_ID, "31", 5, 10, tick), chance("31", false))
        assertEquals(5, chance("313", true).magnitude)
        assertEquals(5, chance("3131", false).magnitude)
        assertEquals(10, chance("3131", true).magnitude)
        assertEquals(5, chance("31313", false).magnitude)
        assertEquals(35, chance("31313", true).magnitude)
        assertEquals(10, chance("31313", true).remainingTicks)
    }

    /**
     * Full immunity (tacticsDazePct = 0, descripcion.md «impide entrar en modo daze»): Aturdir stores no chance on a
     * target whose Escudo táctico is active, so nothing is left to roll once the shield expires.
     */
    @Test fun tacticalShieldBlocksDazeChanceUnderImmunity() {
        assertEquals(0, TEST_TUNING.tacticsDazePct)
        val shielded = withEffects(start(1), rival, buff(EffectKind.SHIELD_TACTICS, "2131", rival))
        val result = cast(shielded, "3131", chained = true)
        assertTrue(result.state.effects(rival, EffectKind.DAZE_CHANCE).isEmpty())
        assertTrue(result.events.none { it is CombatEvent.EffectApplied })
    }

    /** A shield cast this tick is not active yet (buffs act from the next tick): the chance is still stored. */
    @Test fun pendingTacticalShieldDoesNotBlockDazeChance() {
        val pending = withEffects(start(1), rival, ActiveEffect(EffectKind.SHIELD_TACTICS, rival, "2131", 0, 10, tick + 1))
        assertEquals(1, cast(pending, "31").state.effects(rival, EffectKind.DAZE_CHANCE).size)
    }

    /** With a partial reduction (tacticsDazePct > 0) the chance is stored and the roll is scaled (DazeRollTest). */
    @Test fun tacticalShieldStoresDazeChanceWhenItOnlyScales() {
        val shielded = withEffects(start(1), rival, buff(EffectKind.SHIELD_TACTICS, "2131", rival))
        val partial = TEST_TUNING.copy(tacticsDazePct = 50)
        val result = ComboApplier.apply(shielded, PLAYER_ID, listOf(rival), Combos.find("31")!!, false, partial, FixedRandom(0))
        assertEquals(1, result.state.effects(rival, EffectKind.DAZE_CHANCE).size)
    }

    /** Daze loop fix (2026-10-13): Aturdir does not pile up chance on a target that is already Dazed. */
    @Test fun dazeChanceNotAppliedToDazedTarget() {
        listOf(tick, tick + 1).forEach { activeFrom ->
            val dazed = withEffects(start(1), rival, ActiveEffect(EffectKind.DAZED, rival, "", 0, 10, activeFrom))
            val result = cast(dazed, "3131", chained = true)
            assertTrue(result.state.effects(rival, EffectKind.DAZE_CHANCE).isEmpty(), "dazed from $activeFrom")
            assertTrue(result.events.none { it is CombatEvent.EffectApplied })
        }
    }

    @Test fun drainEffectOnCaster() {
        val drain = cast(start(1), "3123").state.player.effects.single()
        assertEquals(ActiveEffect(EffectKind.DRAIN, PLAYER_ID, "3123", 200, 30, tick + 1), drain)
        assertEquals(400, cast(start(1), "31232", chained = true).state.player.effects.single().magnitude)
        assertEquals(200, cast(start(1), "31232", chained = false).state.player.effects.single().magnitude)
        assertTrue(cast(start(1), "3123").state.fighter(rival).effects.isEmpty())
    }

    @Test fun tuningDrivesUnits() {
        val tuning = ComboTuning(spearBasePct = 50, fistBasePct = 30, cureBasePct = 100)
        fun apply(seq: String, targets: List<FighterId>) =
            ComboApplier.apply(start(1), PLAYER_ID, targets, Combos.find(seq)!!, false, tuning, FixedRandom(0)).state
        assertEquals(500, apply("12", listOf(rival)).effects(rival, EffectKind.DOT).single().magnitude)
        assertEquals(300, apply("32", listOf(rival)).effects(rival, EffectKind.DOT).single().magnitude)
        assertEquals(800, apply("21", listOf(PLAYER_ID)).effects(PLAYER_ID, EffectKind.COT).single().magnitude)
    }

    @Test fun recoverRestoresTenPercentOfMaxPowerClamped() {
        val low = start(1).withFighter(PLAYER_ID) { copy(power = 100) }
        val result = self(low, "321")
        assertEquals(130, result.state.player.power, "10 % of max power 300")
        assertTrue(result.state.player.effects.isEmpty(), "no lasting effect")
        assertEquals(listOf(CombatEvent.ComboResolved(PLAYER_ID, Combos.find("321")!!, false)), result.events)
        val nearlyFull = start(1).withFighter(PLAYER_ID) { copy(power = 290) }
        assertEquals(300, self(nearlyFull, "321").state.player.power, "clamped to max power")
    }
}
