package com.wardengambits.engine.rules

import com.wardengambits.engine.combo.ChainRules
import com.wardengambits.engine.combo.ComboDefinition
import com.wardengambits.engine.combo.ComboTuning
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.combo.TargetKind
import com.wardengambits.engine.event.CombatEvent
import com.wardengambits.engine.event.RejectReason
import com.wardengambits.engine.model.CombatState
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.Fighter
import com.wardengambits.engine.model.FighterId
import com.wardengambits.engine.model.Outcome
import com.wardengambits.engine.model.PLAYER_ID
import com.wardengambits.engine.random.GameRandom

/** How the HP change of one fighter's over-time effects in a tick is split among those effects. */
internal object OverTimeReport {
    /**
     * Shown amounts for [raw] (COT positive, DOT negative and already reduced), in the same order, given the HP
     * [before] and [after] the net change. A survivor's DOTs show their full damage and its COTs share, in order,
     * the heal that actually landed (so everything adds up to the HP change; a capped COT can show +0). For a
     * fighter that ends at 0 the effects are walked in order and the ones after the lethal one are not shown (the
     * returned list stops at the lethal effect, whose damage is clamped to the HP it removed).
     */
    fun amounts(before: Int, after: Int, maxHp: Int, raw: List<Int>): List<Int> {
        if (after > 0) {
            var heal = (after - before - raw.filter { it < 0 }.sum()).coerceAtLeast(0)
            return raw.map { amount ->
                if (amount < 0) {
                    amount
                } else {
                    val shown = minOf(amount, heal)
                    heal -= shown
                    shown
                }
            }
        }
        var hp = before
        val shown = mutableListOf<Int>()
        for (amount in raw) {
            if (hp <= 0) break
            val applied = if (amount < 0) maxOf(amount, -hp) else minOf(amount, maxHp - hp).coerceAtLeast(0)
            shown += applied
            hp += applied
        }
        return shown
    }
}

/**
 * Resolves one combat tick (spec v2 §6): AI, casts, over-time effects and drain pulses, Daze rolls, expiry,
 * power regen, end check.
 */
internal object TickResolver {
    private const val ROLL_BOUND = 100
    private const val FULL_PCT = 100

    /** Rival potion (2026-10-13): percent of max power restored when a rival cannot pay its due combo. */
    const val POTION_PCT = 50

    /** DAZED is not cast by a combo; it is keyed by this empty sequence. */
    private const val DAZED_SOURCE = ""

    fun resolve(state: CombatState, random: GameRandom, tuning: ComboTuning): StepResult {
        val run = Run(state.copy(tick = state.tick + 1), random, tuning)
        return run.execute()
    }

    private class Run(var state: CombatState, val random: GameRandom, val tuning: ComboTuning) {
        val events = mutableListOf<CombatEvent>()
        val tick: Int get() = state.tick

        fun execute(): StepResult {
            runAi()
            resolveCasts()
            resolveOverTime()
            resolveDrains()
            rollDaze()
            expireEffects()
            regenPower()
            checkEnd()
            return StepResult(state, events)
        }

        private fun update(id: FighterId, change: Fighter.() -> Fighter) {
            state = state.copy(fighters = state.fighters.map { if (it.id == id) it.change() else it })
        }

        private fun reject(id: FighterId, reason: RejectReason) {
            events += CombatEvent.ComboRejected(id, reason)
        }

        /**
         * Each alive rival without a queued combo first lets its brain update its bookkeeping (reactions), then drinks
         * a potion if it is its turn, it is not Dazed and it cannot pay the combo it is due to cast (+[POTION_PCT] % of
         * its max power, clamped, unlimited uses), then its brain decides. Rivals only read and change their own fighter, so the order does not matter.
         */
        private fun runAi() {
            state.rivals.filter { it.isAlive && it.queued == null }.forEach { rival ->
                val brain = state.brains.getValue(rival.id)
                update(rival.id) { brain.prepare(this, tick) }
                drinkPotionIfNeeded(rival.id, brain.nextCast(state, rival.id))
                val combo = brain.decide(state, rival.id, random)
                if (combo != null) {
                    update(rival.id) { copy(queued = combo) }
                    events += CombatEvent.ComboQueued(rival.id, combo)
                }
            }
        }

        private fun drinkPotionIfNeeded(id: FighterId, due: ComboDefinition?) {
            val rival = state.fighter(id)
            if (due == null || rival.power >= due.cost || rival.isActive(EffectKind.DAZED, tick)) return
            val amount = minOf(rival.stats.maxPower * POTION_PCT / FULL_PCT, rival.stats.maxPower - rival.power)
            update(id) { copy(power = power + amount) }
            events += CombatEvent.PotionUsed(id, amount)
        }

        private fun resolveCasts() {
            state.fighters.forEach { initial ->
                val fighter = state.fighter(initial.id)
                val combo = fighter.queued ?: return@forEach
                update(fighter.id) { copy(queued = null) }
                if (fighter.isAlive) cast(fighter, combo)
            }
        }

        private fun cast(fighter: Fighter, combo: ComboDefinition) {
            val rejection = when {
                fighter.isActive(EffectKind.DAZED, tick) -> RejectReason.DAZED
                fighter.power < combo.cost -> RejectReason.NOT_ENOUGH_POWER
                fighter.id == PLAYER_ID && combo.target == TargetKind.SELECTED && state.targetId == null ->
                    RejectReason.INVALID_TARGET
                else -> null
            }
            if (rejection != null) {
                reject(fighter.id, rejection)
                return
            }
            val (chain, chained) = ChainRules.next(fighter.chain, combo)
            update(fighter.id) { copy(power = power - combo.cost, chain = chain) }
            val result = ComboApplier.apply(state, fighter.id, targetsFor(fighter, combo), combo, chained, tuning, random)
            state = result.state
            events += result.events
            state.brains[fighter.id]?.let { brain -> update(fighter.id) { brain.afterCast(this) } }
            retarget()
        }

        private fun opponentsOf(id: FighterId): List<Fighter> =
            (if (id == PLAYER_ID) state.rivals else listOf(state.player)).filter { it.isAlive }

        private fun targetsFor(caster: Fighter, combo: ComboDefinition): List<FighterId> = when (combo.target) {
            TargetKind.SELF -> listOf(caster.id)
            TargetKind.ALL_OPPONENTS -> opponentsOf(caster.id).map { it.id }
            TargetKind.SELECTED ->
                if (caster.id == PLAYER_ID) listOfNotNull(state.targetId) else opponentsOf(caster.id).map { it.id }
        }

        private fun retarget() {
            val target = state.targetId ?: return
            if (state.fighter(target).isAlive) return
            state = state.copy(targetId = state.rivals.firstOrNull { it.isAlive }?.id)
        }

        private fun defeatIfDead(id: FighterId) {
            if (state.fighter(id).isAlive) return
            update(id) { copy(effects = emptyList()) }
            events += CombatEvent.FighterDefeated(id)
        }

        /**
         * DOT and COT of every alive fighter, by index. The HP rule is the net sum of the tick (all effects at once,
         * clamped to 0..max HP); each effect is reported with its own [CombatEvent.EffectResolved] in list order.
         * Each effect rolls once, in list order, and the roll scales its magnitude before Guardia and Escudo maestro.
         */
        private fun resolveOverTime() {
            state.fighters.filter { it.isAlive }.forEach { initial ->
                val overTime = initial.effects.filter {
                    (it.kind == EffectKind.DOT || it.kind == EffectKind.COT) && it.isActive(tick)
                }
                if (overTime.isEmpty()) return@forEach
                val guard = initial.isActive(EffectKind.GUARD, tick)
                val masterShield = initial.isActive(EffectKind.SHIELD_MASTER, tick)
                val rolls = overTime.map { PulseRoll.roll(random, tuning) }
                val amounts = overTime.zip(rolls) { effect, roll ->
                    val magnitude = PulseRoll.scale(effect.magnitude, roll)
                    if (effect.kind == EffectKind.COT) magnitude else -DamageFormula.received(magnitude, guard, masterShield)
                }
                update(initial.id) { copy(hp = (hp + amounts.sum()).coerceIn(0, stats.maxHp)) }
                val shown = OverTimeReport.amounts(initial.hp, state.fighter(initial.id).hp, initial.stats.maxHp, amounts)
                shown.forEachIndexed { index, amount ->
                    val effect = overTime[index]
                    events += CombatEvent.EffectResolved(
                        initial.id, effect.sourceId, effect.sourceCombo, effect.kind, amount, rolls[index],
                    )
                }
                defeatIfDead(initial.id)
            }
            retarget()
        }

        /**
         * Drain pulses: each rolls once per alive opponent (in [opponentsOf] order), takes up to the rolled magnitude
         * from it and heals the caster the total actually taken.
         */
        private fun resolveDrains() {
            state.fighters.forEach { initial ->
                val pulses = state.fighter(initial.id).effects.filter {
                    it.isActive(tick) && it.isDrainPulse(tick, Combos.DRAIN_PERIOD)
                }
                pulses.forEach { pulse ->
                    if (!state.fighter(initial.id).isAlive) return@forEach
                    fun resolved(target: FighterId, amount: Int, rollPct: Int) = CombatEvent.EffectResolved(
                        target, initial.id, pulse.sourceCombo, EffectKind.DRAIN, amount, rollPct,
                    )
                    val taken = opponentsOf(initial.id).sumOf { opponent ->
                        val roll = PulseRoll.roll(random, tuning)
                        val amount = minOf(PulseRoll.scale(pulse.magnitude, roll), opponent.hp)
                        if (amount > 0) {
                            update(opponent.id) { copy(hp = hp - amount) }
                            events += resolved(opponent.id, -amount, roll)
                            defeatIfDead(opponent.id)
                        }
                        amount
                    }
                    if (taken > 0) {
                        val before = state.fighter(initial.id).hp
                        update(initial.id) { copy(hp = minOf(stats.maxHp, hp + taken)) }
                        events += resolved(initial.id, state.fighter(initial.id).hp - before, FULL_PCT)
                    }
                }
            }
            retarget()
        }

        /**
         * One roll per alive, non-dazed fighter, by index, only when its chance is > 0. Escudo táctico scales the
         * chance to [ComboTuning.tacticsDazePct] percent (0, the default, means no roll at all: full immunity).
         */
        private fun rollDaze() {
            state.fighters.forEach { initial ->
                val fighter = state.fighter(initial.id)
                if (!fighter.isAlive || fighter.has(EffectKind.DAZED)) return@forEach
                val base = fighter.dazeChancePct(tick)
                val tactics = fighter.isActive(EffectKind.SHIELD_TACTICS, tick)
                val chance = if (tactics) base * tuning.tacticsDazePct / FULL_PCT else base
                if (chance <= 0 || random.nextInt(ROLL_BOUND) >= chance) return@forEach
                val dazed = EffectRules.fresh(EffectKind.DAZED, fighter.id, DAZED_SOURCE, 0, Combos.DAZED_TICKS, tick)
                update(fighter.id) {
                    EffectRules.put(copy(effects = effects.filter { it.kind != EffectKind.DAZE_CHANCE }), dazed, tick).first
                }
                events += CombatEvent.EffectApplied(fighter.id, EffectKind.DAZED, Combos.DAZED_TICKS)
            }
        }

        private fun expireEffects() {
            state.fighters.forEach { initial ->
                val decremented = initial.effects.map {
                    if (it.isActive(tick)) it.copy(remainingTicks = it.remainingTicks - 1) else it
                }
                val (expired, kept) = decremented.partition { it.remainingTicks <= 0 }
                update(initial.id) { copy(effects = kept) }
                expired.forEach { events += CombatEvent.EffectExpired(initial.id, it.kind) }
            }
        }

        private fun regenPower() {
            state.fighters.filter { it.isAlive }.forEach { fighter ->
                val pct = if (fighter.isActive(EffectKind.VIGOR, tick)) DamageFormula.VIGOR_REGEN_PCT else FULL_PCT
                update(fighter.id) { copy(power = minOf(stats.maxPower, power + stats.powerRegen * pct / FULL_PCT)) }
            }
        }

        private fun checkEnd() {
            val outcome = when {
                !state.player.isAlive -> Outcome.DEFEAT
                state.rivals.none { it.isAlive } -> Outcome.VICTORY
                else -> return
            }
            state = state.copy(outcome = outcome)
            events += CombatEvent.CombatEnded(outcome)
        }
    }
}
