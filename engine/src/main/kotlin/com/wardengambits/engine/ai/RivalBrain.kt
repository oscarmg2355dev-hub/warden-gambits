package com.wardengambits.engine.ai

import com.wardengambits.engine.combo.ComboDefinition
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.model.BossMode
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.Fighter
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.Reaction
import com.wardengambits.engine.random.GameRandom

interface RivalBrain {
    fun decide(state: CombatState, self: FighterId, random: GameRandom): ComboDefinition?

    /** The combo this brain is due to cast this tick whatever its power (null when it is not its turn). */
    fun nextCast(state: CombatState, self: FighterId): ComboDefinition? = null

    /** Updates the brain's own bookkeeping in [self] at the start of [tick], before [nextCast] and [decide]. */
    fun prepare(self: Fighter, tick: Int): Fighter = self

    /** Updates the brain's own bookkeeping in [self] after one of its casts resolved. */
    fun afterCast(self: Fighter): Fighter = self
}

/**
 * Casts [pattern] in a cycle every [everyNTicks] ticks. With a [reaction] (rival-healing spec §2), when it is due to
 * cast at or below an unused HP threshold it marks every crossed threshold as used and casts the reaction's sequence
 * instead, one combo every tick so the chain keeps its bonus; being Dazed cancels the rest. The pattern then resumes
 * where it was left. With [modes] instead (boss-modes spec §2; never both) [pattern] is the attack pattern, cast
 * every [everyNTicks] ticks while attacking, and the defense pattern is cast every tick while defending. All the
 * bookkeeping lives in the [Fighter], so decisions depend only on the state.
 */
data class PatternBrain(
    val pattern: List<String>,
    val everyNTicks: Int = 1,
    val reaction: Reaction? = null,
    val modes: BossModes? = null,
) : RivalBrain {
    private val reactionSequence: List<String> = reaction?.sequence.orEmpty()

    init {
        require(pattern.isNotEmpty() && pattern.all { Combos.find(it) != null })
        require(everyNTicks > 0)
        require(reactionSequence.all { Combos.find(it) != null }) { "Unknown combo in reaction $reaction" }
        require(reaction == null || modes == null) { "A brain has either a reaction or modes, not both" }
    }

    override fun decide(state: CombatState, self: FighterId, random: GameRandom): ComboDefinition? =
        nextCast(state, self)?.takeIf { state.fighter(self).power >= it.cost }

    override fun nextCast(state: CombatState, self: FighterId): ComboDefinition? {
        val fighter = state.fighter(self)
        if (modes != null) {
            if (fighter.bossMode == BossMode.DEFENSE) {
                // A Dazed defender would only queue a combo to have it rejected every tick: it stays quiet instead.
                if (fighter.isActive(EffectKind.DAZED, state.tick)) return null
                return Combos.find(modes.defense[fighter.modeCasts % modes.defense.size])
            }
            if (state.tick % everyNTicks != 0) return null
            return Combos.find(pattern[fighter.modeCasts % pattern.size])
        }
        if (fighter.reactionStep >= 0) return Combos.find(reactionSequence[fighter.reactionStep])
        if (state.tick % everyNTicks != 0) return null
        return Combos.find(pattern[(fighter.castCount - fighter.reactionCasts) % pattern.size])
    }

    override fun prepare(self: Fighter, tick: Int): Fighter {
        if (modes != null) return prepareModes(self, tick, modes)
        val reaction = reaction ?: return self
        if (self.reactionStep >= 0) return if (self.isDazed()) self.copy(reactionStep = -1) else self
        if (tick % everyNTicks != 0 || self.isDazed()) return self
        val crossed = reaction.atHpPct.indices.fold(0) { mask, index ->
            val unused = self.reactionUsed and (1 shl index) == 0
            if (unused && self.hp * PERCENT <= reaction.atHpPct[index] * self.stats.maxHp) mask or (1 shl index) else mask
        }
        return if (crossed == 0) self else self.copy(reactionUsed = self.reactionUsed or crossed, reactionStep = 0)
    }

    /**
     * Boss-modes spec §2: on a due attack tick at or below [BossModes.defenseBelowPct] (with defenses left) it enters
     * defense from the start of its pattern; while defending, at or above [BossModes.attackAbovePct] it returns to
     * attack from the start of its pattern. A Dazed defender skips what is left before the final damage combos. The last
     * allowed defense (defenses == maxDefenses) runs one full cycle of the defense pattern (a Daze skip counts as reaching
     * the damage tail) and then returns to attack whatever the HP.
     */
    private fun prepareModes(self: Fighter, tick: Int, modes: BossModes): Fighter {
        val switched = when (self.bossMode) {
            BossMode.ATTACK -> {
                val due = tick % everyNTicks == 0 && self.defenses < modes.maxDefenses
                if (due && self.hp * PERCENT <= modes.defenseBelowPct * self.stats.maxHp) {
                    self.copy(bossMode = BossMode.DEFENSE, defenses = self.defenses + 1, modeCasts = 0)
                } else {
                    self
                }
            }
            BossMode.DEFENSE -> {
                // The last allowed defense runs its pattern once and ends whatever the HP, so it cannot loop forever.
                val lastCycleDone = self.defenses >= modes.maxDefenses && self.modeCasts >= modes.defense.size
                if (lastCycleDone || self.hp * PERCENT >= modes.attackAbovePct * self.stats.maxHp) {
                    self.copy(bossMode = BossMode.ATTACK, modeCasts = 0)
                } else {
                    self
                }
            }
        }
        val skipHeals = switched.bossMode == BossMode.DEFENSE && switched.isDazed() &&
            modes.dazedRestart < modes.defense.size && switched.modeCasts % modes.defense.size < modes.dazedRestart
        return if (skipHeals) switched.copy(modeCasts = modes.dazedRestart) else switched
    }

    override fun afterCast(self: Fighter): Fighter {
        if (modes != null) return self.copy(modeCasts = self.modeCasts + 1)
        if (self.reactionStep < 0) return self
        val next = self.reactionStep + 1
        return self.copy(
            reactionStep = if (next < reactionSequence.size) next else -1,
            reactionCasts = self.reactionCasts + 1,
        )
    }

    private companion object {
        const val PERCENT = 100
    }
}
