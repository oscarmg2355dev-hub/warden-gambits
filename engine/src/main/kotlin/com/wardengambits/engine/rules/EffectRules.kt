package com.wardengambits.engine.rules

import com.wardengambits.engine.model.ActiveEffect
import com.wardengambits.engine.model.EffectKind
import com.wardengambits.engine.model.Fighter
import com.wardengambits.engine.model.FighterId

/** Effect identity, exclusion and activation rules of spec v2 §5. */
internal object EffectRules {
    /** DOT, COT and DAZE_CHANCE act on the tick they are applied; the rest from the next tick. */
    fun startsImmediately(kind: EffectKind): Boolean = when (kind) {
        EffectKind.DOT, EffectKind.COT, EffectKind.DAZE_CHANCE -> true
        EffectKind.DAZED, EffectKind.FURY, EffectKind.GUARD, EffectKind.VIGOR,
        EffectKind.SHIELD_MASTER, EffectKind.SHIELD_TACTICS, EffectKind.DRAIN,
        -> false
    }

    /** Kinds removed from a fighter when [kind] is applied to it. */
    fun excludedBy(kind: EffectKind): Set<EffectKind> = when (kind) {
        EffectKind.FURY -> setOf(EffectKind.GUARD, EffectKind.VIGOR)
        EffectKind.GUARD -> setOf(EffectKind.FURY, EffectKind.VIGOR)
        EffectKind.VIGOR -> setOf(EffectKind.FURY, EffectKind.GUARD)
        EffectKind.SHIELD_MASTER -> setOf(EffectKind.SHIELD_TACTICS)
        EffectKind.SHIELD_TACTICS -> setOf(EffectKind.SHIELD_MASTER)
        EffectKind.DOT, EffectKind.COT, EffectKind.DAZE_CHANCE, EffectKind.DAZED, EffectKind.DRAIN -> emptySet()
    }

    /**
     * Applies [effect] (built as fresh, with its `activeFrom`) to [fighter] on [tick]. An existing effect with the
     * same identity (kind, source, combo) restarts with the new magnitude and duration instead of duplicating; an
     * already-active one keeps its activation (it is decremented this same tick, so it gets one extra tick to last
     * through t+N). A recast DRAIN restarts its pulse schedule. Returns the fighter and the stored effect.
     */
    fun put(fighter: Fighter, effect: ActiveEffect, tick: Int): Pair<Fighter, ActiveEffect> {
        val excluded = excludedBy(effect.kind)
        val kept = fighter.effects.filter { it.kind !in excluded }
        val existing = kept.firstOrNull { it.sameIdentity(effect) }
        val stored = when {
            existing == null || effect.kind == EffectKind.DRAIN -> effect
            else -> {
                val extra = if (!startsImmediately(effect.kind) && existing.isActive(tick)) 1 else 0
                effect.copy(remainingTicks = effect.remainingTicks + extra, activeFrom = minOf(existing.activeFrom, effect.activeFrom))
            }
        }
        val effects = if (existing == null) kept + stored else kept.map { if (it === existing) stored else it }
        return fighter.copy(effects = effects) to stored
    }

    fun fresh(kind: EffectKind, sourceId: FighterId, sourceCombo: String, magnitude: Int, ticks: Int, tick: Int) =
        ActiveEffect(kind, sourceId, sourceCombo, magnitude, ticks, if (startsImmediately(kind)) tick else tick + 1, ticks)

    private fun ActiveEffect.sameIdentity(other: ActiveEffect): Boolean =
        kind == other.kind && sourceId == other.sourceId && sourceCombo == other.sourceCombo
}
