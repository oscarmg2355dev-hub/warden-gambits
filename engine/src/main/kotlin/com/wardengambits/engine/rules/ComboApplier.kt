package com.wardengambits.engine.rules

import com.wardengambits.engine.combo.ComboDefinition
import com.wardengambits.engine.combo.ComboEffect
import com.wardengambits.engine.combo.ComboTuning
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.Fighter
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.random.GameRandom

/**
 * Applies the effect of a resolved combo (spec v2 §3, §5, §7). Validation, power payment and the chain update are the
 * caller's job; [chained] says whether the chain bonus applies. [random] is only consumed by the direct hit's pulse
 * roll (one per alive target).
 */
internal object ComboApplier {
    fun apply(
        state: CombatState,
        casterId: FighterId,
        targets: List<FighterId>,
        combo: ComboDefinition,
        chained: Boolean,
        tuning: ComboTuning,
        random: GameRandom,
    ): StepResult = Run(state, casterId, targets, combo, chained, tuning, random).execute()

    private class Run(
        var state: CombatState,
        val casterId: FighterId,
        val targets: List<FighterId>,
        val combo: ComboDefinition,
        val chained: Boolean,
        val tuning: ComboTuning,
        val random: GameRandom,
    ) {
        val events = mutableListOf<CombatEvent>()
        val caster: Fighter get() = state.fighter(casterId)
        val duration: Int get() = combo.baseTicks + if (chained) combo.chainBonusTicks else 0
        val aliveTargets: List<FighterId> get() = targets.filter { state.fighter(it).isAlive }
        val spearUnit: Int get() = DamageFormula.spearUnit(caster.stats.attack, tuning)
        val cureUnit: Int get() = DamageFormula.cureUnit(caster.stats.heal, tuning)
        val fury: Boolean get() = caster.isActive(EffectKind.FURY, state.tick)

        fun execute(): StepResult {
            update(casterId) { copy(castCount = castCount + 1) }
            events += CombatEvent.ComboResolved(casterId, combo, chained)
            when (val effect = combo.effect) {
                is ComboEffect.SpearDot -> {
                    val magnitude = DamageFormula.spearDot(effect.multiplier, spearUnit, fury)
                    aliveTargets.forEach { put(it, EffectKind.DOT, magnitude) }
                }
                is ComboEffect.AreaDot -> {
                    val magnitude = DamageFormula.areaDot(effect.multiplier, DamageFormula.fistUnit(caster.stats.attack, tuning))
                    aliveTargets.forEach { put(it, EffectKind.DOT, magnitude) }
                }
                is ComboEffect.DazeChance -> {
                    val magnitude = effect.basePct + if (chained) effect.chainBonusPct else 0
                    // A Dazed target gets no chance at all, so Aturdir cannot pile up a re-Daze while it is Dazed.
                    // An active Escudo táctico with full immunity (tacticsDazePct = 0) blocks it too, so no chance
                    // gained under the shield rolls once it expires; with a partial reduction it scales the roll
                    // instead (TickResolver.rollDaze).
                    aliveTargets.filterNot { state.fighter(it).isDazed() || immune(state.fighter(it)) }
                        .forEach { put(it, EffectKind.DAZE_CHANCE, magnitude) }
                }
                is ComboEffect.HealOverTime -> put(casterId, EffectKind.COT, effect.multiplier * cureUnit)
                is ComboEffect.Drain -> put(casterId, EffectKind.DRAIN, cureUnit * if (chained) effect.chainMultiplier else 1)
                is ComboEffect.Buff -> put(casterId, effect.kind, 0)
                ComboEffect.DirectHit -> aliveTargets.forEach { directHit(it) }
                is ComboEffect.PowerRestore -> update(casterId) {
                    copy(power = minOf(stats.maxPower, power + stats.maxPower * effect.pct / 100))
                }
            }
            return StepResult(state, events)
        }

        private fun immune(target: Fighter): Boolean =
            tuning.tacticsDazePct == 0 && target.isActive(EffectKind.SHIELD_TACTICS, state.tick)

        private fun update(id: FighterId, change: Fighter.() -> Fighter) {
            state = state.copy(fighters = state.fighters.map { if (it.id == id) it.change() else it })
        }

        private fun put(id: FighterId, kind: EffectKind, magnitude: Int) {
            val fresh = EffectRules.fresh(kind, casterId, combo.sequence, magnitude, duration, state.tick)
            val (fighter, stored) = EffectRules.put(state.fighter(id), fresh, state.tick)
            update(id) { fighter }
            events += CombatEvent.EffectApplied(id, kind, stored.remainingTicks)
        }

        private fun directHit(targetId: FighterId) {
            val target = state.fighter(targetId)
            val amount = DamageFormula.directHit(
                spearUnit,
                fury,
                target.stats.defense,
                target.isActive(EffectKind.GUARD, state.tick),
                target.isActive(EffectKind.SHIELD_MASTER, state.tick),
            )
            val roll = PulseRoll.roll(random, tuning)
            val dealt = minOf(PulseRoll.scale(amount, roll), target.hp)
            update(targetId) { copy(hp = hp - dealt) }
            events += CombatEvent.EffectResolved(targetId, casterId, combo.sequence, null, -dealt, roll)
            if (!state.fighter(targetId).isAlive) {
                update(targetId) { copy(effects = emptyList()) }
                events += CombatEvent.FighterDefeated(targetId)
            }
        }
    }
}
