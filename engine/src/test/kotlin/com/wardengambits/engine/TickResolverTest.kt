package com.wardengambits.engine

import com.wardengambits.engine.action.CombatAction
import com.wardengambits.engine.ai.PatternBrain
import com.wardengambits.engine.ai.RivalBrain
import com.wardengambits.engine.combo.ComboDefinition
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.combo.Input
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.event.RejectReason
import com.wardengambits.engine.model.ActiveEffect
import com.wardengambits.engine.model.CombatSetup
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.FighterSpec
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.model.RivalScaling
import com.wardengambits.engine.model.RivalSpec
import com.wardengambits.engine.random.GameRandom
import com.wardengambits.engine.random.SeededRandom
import com.wardengambits.engine.rules.CombatEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Level 1 fighters with identity scaling and [TEST_TUNING]: max HP 10000, power 300, SPDMG 200, FIDMG 180, CUR 200, regen 5. */
class TickResolverTest {
    private val engine = CombatEngine(RivalScaling.IDENTITY, tuning = TEST_TUNING) { FixedRandom(99) }
    private val r1 = FighterId(1)
    private val r2 = FighterId(2)
    private val attack = Combos.find("12")!!
    private val direct = Combos.find("123")!!

    private fun started(vararg rivalLevels: Int = intArrayOf(1)): CombatState =
        engine.start(setup(1, *rivalLevels), 1L)

    private fun tick(state: CombatState) = engine.reduce(state, CombatAction.Tick)

    private fun effect(kind: EffectKind, magnitude: Int = 0, ticks: Int = 2, combo: String = kind.name) =
        ActiveEffect(kind, PLAYER_ID, combo, magnitude, ticks)

    @Test fun tickIncrements() {
        assertEquals(1, tick(started()).state.tick)
    }

    @Test fun queuedComboResolvesOnTick() {
        val state = started().withFighter(PLAYER_ID) { copy(queued = attack) }
        val result = tick(state)
        val rival = result.state.fighter(r1)
        assertEquals(9_800, rival.hp)
        assertEquals(9, rival.effects.single { it.kind == EffectKind.DOT }.remainingTicks)
        assertEquals(295, result.state.player.power)
        assertNull(result.state.player.queued)
        assertTrue(CombatEvent.EffectResolved(r1, PLAYER_ID, "12", EffectKind.DOT, -200) in result.events)
    }

    @Test fun exactPowerAllowed() {
        val state = started().withFighter(PLAYER_ID) { copy(queued = attack, power = 10) }
        val result = tick(state)
        assertTrue(result.events.any { it is CombatEvent.ComboResolved })
        assertEquals(5, result.state.player.power)
    }

    @Test fun notEnoughPowerRejected() {
        val state = started().withFighter(PLAYER_ID) { copy(queued = attack, power = 9) }
        val result = tick(state)
        assertTrue(CombatEvent.ComboRejected(PLAYER_ID, RejectReason.NOT_ENOUGH_POWER) in result.events)
        assertNull(result.state.player.queued)
        assertEquals(14, result.state.player.power)
        assertEquals(0, result.state.player.castCount)
        assertEquals(result.state.fighter(r1).stats.maxHp, result.state.fighter(r1).hp)
    }

    @Test fun dazedCannotCast() {
        val state = started().withFighter(PLAYER_ID) {
            copy(queued = attack, power = 50, effects = listOf(effect(EffectKind.DAZED, ticks = 3)))
        }
        val result = tick(state)
        assertTrue(CombatEvent.ComboRejected(PLAYER_ID, RejectReason.DAZED) in result.events)
        assertEquals(55, result.state.player.power)
        assertEquals(10_000, result.state.fighter(r1).hp)
    }

    @Test fun playerResolvesBeforeRivals() {
        val state = started()
            .withFighter(PLAYER_ID) { copy(queued = direct) }
            .withFighter(r1) { copy(hp = 5, queued = direct) }
        val result = tick(state)
        assertEquals(0, result.state.fighter(r1).hp)
        assertEquals(10_000, result.state.player.hp)
        assertEquals(1, result.events.count { it is CombatEvent.ComboResolved })
        assertNull(result.state.fighter(r1).queued)
    }

    @Test fun rivalsCastAfterPlayerInIndexOrder() {
        val state = started(1, 1)
            .withFighter(PLAYER_ID) { copy(queued = attack) }
            .withFighter(r2) { copy(queued = attack) }
            .withFighter(r1) { copy(queued = attack) }
        val resolved = tick(state).events.filterIsInstance<CombatEvent.ComboResolved>().map { it.fighter }
        assertEquals(listOf(PLAYER_ID, r1, r2), resolved)
    }

    @Test fun dotAndCotNetted() {
        val state = started().withFighter(PLAYER_ID) {
            copy(hp = 5_000, effects = listOf(effect(EffectKind.DOT, 300), effect(EffectKind.COT, 200), effect(EffectKind.DOT, 400, combo = "13")))
        }
        val result = tick(state)
        assertEquals(4_500, result.state.player.hp)
        assertEquals(listOf(-300, 200, -400), result.events.filterIsInstance<CombatEvent.EffectResolved>().map { it.amount })
    }

    @Test fun dotReducedByGuardAndMasterShieldMultiplied() {
        fun hpAfter(vararg defensive: EffectKind, magnitude: Int = 1_000): Int {
            val state = started().withFighter(PLAYER_ID) {
                copy(effects = listOf(effect(EffectKind.DOT, magnitude)) + defensive.map { effect(it, ticks = 3) })
            }
            return tick(state).state.player.hp
        }
        assertEquals(9_000, hpAfter())
        assertEquals(9_200, hpAfter(EffectKind.GUARD))
        assertEquals(9_500, hpAfter(EffectKind.SHIELD_MASTER))
        assertEquals(9_600, hpAfter(EffectKind.GUARD, EffectKind.SHIELD_MASTER))
        assertEquals(9_999, hpAfter(EffectKind.GUARD, EffectKind.SHIELD_MASTER, magnitude = 1))
        assertEquals(9_999, hpAfter(magnitude = 0))
    }

    @Test fun pendingGuardDoesNotReduceDot() {
        val state = started().withFighter(PLAYER_ID) {
            copy(effects = listOf(effect(EffectKind.DOT, 1_000), effect(EffectKind.GUARD, ticks = 3).copy(activeFrom = 2)))
        }
        assertEquals(9_000, tick(state).state.player.hp)
    }

    @Test fun dotIgnoresDefense() {
        val state = started().withFighter(PLAYER_ID) {
            copy(stats = stats.copy(defense = 5_000), effects = listOf(effect(EffectKind.DOT, 1_000)))
        }
        assertEquals(9_000, tick(state).state.player.hp)
    }

    @Test fun effectsExpire() {
        val state = started().withFighter(PLAYER_ID) { copy(effects = listOf(effect(EffectKind.DAZED, ticks = 1))) }
        val result = tick(state)
        assertTrue(result.state.player.effects.isEmpty())
        assertTrue(CombatEvent.EffectExpired(PLAYER_ID, EffectKind.DAZED) in result.events)
    }

    @Test fun vigorBoostsRegenByTwentyPercent() {
        val state = started().withFighter(PLAYER_ID) {
            copy(power = 50, effects = listOf(effect(EffectKind.VIGOR, ticks = 3)))
        }
        assertEquals(56, tick(state).state.player.power)
        val pending = started().withFighter(PLAYER_ID) {
            copy(power = 50, effects = listOf(effect(EffectKind.VIGOR, ticks = 3).copy(activeFrom = 2)))
        }
        assertEquals(55, tick(pending).state.player.power)
    }

    @Test fun retargetWhenTargetDies() {
        val state = started(1, 1)
            .withFighter(PLAYER_ID) { copy(queued = direct) }
            .withFighter(r1) { copy(hp = 5) }
        val result = tick(state)
        assertEquals(r2, result.state.targetId)
    }

    @Test fun aoeKillsAllTargetsNull() {
        val aoe = Combos.find("32")!!
        val state = started(1, 1)
            .withFighter(PLAYER_ID) { copy(queued = aoe) }
            .withFighter(r1) { copy(hp = 1) }
            .withFighter(r2) { copy(hp = 1) }
        val result = tick(state)
        assertNull(result.state.targetId)
        assertEquals(Outcome.VICTORY, result.state.outcome)
    }

    @Test fun singleTargetWithNoTargetRejected() {
        val state = started().copy(targetId = null).withFighter(PLAYER_ID) { copy(queued = attack) }
        val result = tick(state)
        assertTrue(CombatEvent.ComboRejected(PLAYER_ID, RejectReason.INVALID_TARGET) in result.events)
        assertNull(result.state.player.queued)
    }

    @Test fun victory() {
        val state = started().withFighter(PLAYER_ID) { copy(queued = direct) }.withFighter(r1) { copy(hp = 5) }
        val result = tick(state)
        assertEquals(Outcome.VICTORY, result.state.outcome)
        assertTrue(CombatEvent.CombatEnded(Outcome.VICTORY) in result.events)
    }

    @Test fun defeatWinsTie() {
        val state = started()
            .withFighter(PLAYER_ID) { copy(hp = 1, effects = listOf(effect(EffectKind.DOT, 3))) }
            .withFighter(r1) { copy(hp = 1, effects = listOf(effect(EffectKind.DOT, 3))) }
        val result = tick(state)
        assertEquals(Outcome.DEFEAT, result.state.outcome)
        assertTrue(CombatEvent.CombatEnded(Outcome.DEFEAT) in result.events)
        assertTrue(CombatEvent.CombatEnded(Outcome.VICTORY) !in result.events)
    }

    @Test fun actionsAfterEndRejected() {
        val ended = started().copy(outcome = Outcome.VICTORY)
        val expected = listOf(CombatEvent.ComboRejected(PLAYER_ID, RejectReason.COMBAT_OVER))
        val tickResult = tick(ended)
        assertEquals(ended, tickResult.state)
        assertEquals(expected, tickResult.events)
        val press = engine.reduce(ended, CombatAction.Press(PLAYER_ID, Input.SPEAR))
        assertEquals(ended, press.state)
        assertEquals(expected, press.events)
    }

    @Test fun aiQueuedComboCastSameTick() {
        val brain = object : RivalBrain {
            override fun decide(state: CombatState, self: FighterId, random: GameRandom): ComboDefinition? = attack
        }
        val state = engine.start(
            CombatSetup(FighterSpec("P", 1), listOf(RivalSpec("R", 1, brain))),
            1L,
        )
        val result = tick(state)
        assertTrue(CombatEvent.ComboQueued(r1, attack) in result.events)
        assertEquals(9_800, result.state.player.hp)
    }

    @Test fun deadFighterEffectsClearedAndSilent() {
        val state = started()
            .withFighter(PLAYER_ID) { copy(queued = direct) }
            .withFighter(r1) { copy(hp = 5, effects = listOf(ActiveEffect(EffectKind.DOT, PLAYER_ID, "12", 3, 3))) }
        val result = tick(state)
        assertTrue(result.state.fighter(r1).effects.isEmpty())
        assertTrue(result.events.none { it is CombatEvent.EffectResolved && it.target == r1 && it.kind != null })
        assertTrue(result.events.none { it is CombatEvent.EffectExpired && it.target == r1 })
    }

    @Test fun deadFromOverTimeLosesEffects() {
        val state = started().withFighter(r1) {
            copy(hp = 1, effects = listOf(ActiveEffect(EffectKind.DOT, PLAYER_ID, "12", 3, 1), effect(EffectKind.DAZED, ticks = 1)))
        }
        val result = tick(state)
        assertEquals(0, result.state.fighter(r1).hp)
        assertTrue(result.state.fighter(r1).effects.isEmpty())
        assertTrue(result.events.none { it is CombatEvent.EffectExpired && it.target == r1 })
    }

    @Test fun effectResolvedReportsActualClampedChange() {
        val healOnly = started().withFighter(PLAYER_ID) { copy(hp = 9_999, effects = listOf(effect(EffectKind.COT, 200))) }
        assertTrue(CombatEvent.EffectResolved(PLAYER_ID, PLAYER_ID, "COT", EffectKind.COT, 1) in tick(healOnly).events)

        val dotOnly = started().withFighter(r1) { copy(hp = 200, effects = listOf(effect(EffectKind.DOT, 300))) }
        assertTrue(CombatEvent.EffectResolved(r1, PLAYER_ID, "DOT", EffectKind.DOT, -200) in tick(dotOnly).events)
    }

    @Test fun rngStateCarriesOverBetweenTicks() {
        val real = CombatEngine()
        val queued = real.start(setup(1, 1), 7L).withFighter(PLAYER_ID) { copy(queued = Combos.find("31")!!) }
        assertNotEquals(7L, real.reduce(queued, CombatAction.Tick).state.rngState)

        val received = mutableListOf<Long>()
        val recording = CombatEngine { seed ->
            received += seed
            SeededRandom(seed)
        }
        val start = recording.start(setup(1, 1), 7L).withFighter(PLAYER_ID) { copy(queued = Combos.find("31")!!) }
        val first = recording.reduce(start, CombatAction.Tick).state
        val second = first.withFighter(PLAYER_ID) { copy(queued = Combos.find("31")!!) }
        recording.reduce(second, CombatAction.Tick)
        assertEquals(listOf(7L, first.rngState), received)
        assertNotEquals(7L, first.rngState)
    }

    /** Regression ("won with HP left on the rival"): VICTORY implies every rival is at 0 HP with no effects. */
    private fun assertCleanVictory(state: CombatState) {
        assertEquals(Outcome.VICTORY, state.outcome)
        state.rivals.forEach {
            assertEquals(0, it.hp, "${it.name} hp")
            assertTrue(it.effects.isEmpty(), "${it.name} effects")
        }
    }

    @Test fun rivalKilledByDotIsNotRevivedByItsOwnCotOrDrainPulse() {
        val drain = ActiveEffect(EffectKind.DRAIN, r1, "3123", 10, 20, activeFrom = -3)
        val state = started().withFighter(r1) {
            copy(
                hp = 3,
                effects = listOf(
                    effect(EffectKind.DOT, magnitude = 8, ticks = 5),
                    ActiveEffect(EffectKind.COT, r1, "21", 4, 5),
                    drain,
                ),
            )
        }
        val result = tick(state)
        assertTrue(drain.isDrainPulse(1, Combos.DRAIN_PERIOD))
        assertCleanVictory(result.state)
        assertEquals(result.state.player.stats.maxHp, result.state.player.hp)
        assertTrue(result.events.none { it is CombatEvent.EffectResolved && it.kind == EffectKind.DRAIN })
    }

    @Test fun rivalWhoseCotOuthealsTheDotSurvivesAndNoVictory() {
        val state = started().withFighter(r1) {
            copy(hp = 1, effects = listOf(effect(EffectKind.DOT, magnitude = 4, ticks = 5), ActiveEffect(EffectKind.COT, r1, "21", 6, 5)))
        }
        val result = tick(state)
        assertEquals(3, result.state.fighter(r1).hp)
        assertNull(result.state.outcome)
    }

    @Test fun twoRivalsDyingInTheSameTickByDirectHitAndDotEndCleanly() {
        val state = started(1, 1)
            .withFighter(PLAYER_ID) { copy(queued = direct) }
            .withFighter(r1) { copy(hp = 5) }
            .withFighter(r2) { copy(hp = 2, effects = listOf(effect(EffectKind.DOT, magnitude = 2, ticks = 5))) }
        val result = tick(state)
        assertCleanVictory(result.state)
        assertEquals(1, result.events.count { it is CombatEvent.CombatEnded })
    }

    private fun withPattern(state: CombatState, vararg pattern: String, every: Int = 1) =
        state.copy(brains = state.brains + (r1 to PatternBrain(pattern.toList(), every)))

    /** Rival potion (2026-10-13): a rival that cannot pay its next pattern combo drinks +50 % of its max power first. */
    @Test fun rivalDrinksPotionWhenNextComboIsUnaffordable() {
        val state = withPattern(started(), "1212").withFighter(r1) { copy(power = 20) }
        val result = tick(state)
        val potion = CombatEvent.PotionUsed(r1, 150)
        assertTrue(potion in result.events, "${result.events}")
        val queued = result.events.indexOfFirst { it is CombatEvent.ComboQueued && it.fighter == r1 }
        assertTrue(result.events.indexOf(potion) in 0 until queued, "potion before the cast: ${result.events}")
        assertTrue(result.events.any { it is CombatEvent.ComboResolved && it.fighter == r1 })
        assertEquals(20 + 150 - 26 + 5, result.state.fighter(r1).power)
    }

    @Test fun potionIsClampedToMaxPower() {
        val small = CombatEngine(RivalScaling.IDENTITY.copy(powerPct = 10), tuning = TEST_TUNING) { FixedRandom(99) }
        val state = withPattern(small.start(setup(1, 1), 1L), "1212").withFighter(r1) { copy(power = 20) }
        val result = small.reduce(state, CombatAction.Tick)
        assertTrue(CombatEvent.PotionUsed(r1, 10) in result.events, "${result.events}")
        assertEquals(30 - 26 + 5, result.state.fighter(r1).power)
    }

    @Test fun noPotionWhenAffordableOffCadenceOrDazed() {
        fun potions(state: CombatState) = tick(state).events.filterIsInstance<CombatEvent.PotionUsed>()
        val base = started().withFighter(r1) { copy(power = 20) }
        assertTrue(potions(withPattern(base, "12")).isEmpty(), "affordable")
        assertTrue(potions(withPattern(base, "1212", every = 2)).isEmpty(), "tick 1 is not its turn")
        val dazed = withPattern(base, "1212").withFighter(r1) { copy(effects = listOf(ActiveEffect(EffectKind.DAZED, r1, "", 0, 5, 0))) }
        assertTrue(potions(dazed).isEmpty(), "dazed")
    }

    @Test fun playerNeverDrinksPotions() {
        val state = started().withFighter(PLAYER_ID) { copy(power = 0, queued = Combos.find("1212")) }
        assertTrue(tick(state).events.none { it is CombatEvent.PotionUsed })
    }
}
