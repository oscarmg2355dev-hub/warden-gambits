package com.wardengambits.engine.model

import com.wardengambits.engine.combo.ComboChain
import com.wardengambits.engine.combo.ComboDefinition
import com.wardengambits.engine.combo.Combos

enum class Side { PLAYER, RIVAL }

/** A boss's mode (boss-modes spec §2); rivals without modes stay in [ATTACK]. */
enum class BossMode { ATTACK, DEFENSE }

data class Fighter(
    val id: FighterId,
    val name: String,
    val level: Int,
    val side: Side,
    val stats: Stats,
    val hp: Int,
    val power: Int,
    val buffer: String = "",
    val queued: ComboDefinition? = null,
    val effects: List<ActiveEffect> = emptyList(),
    val castCount: Int = 0,
    /** Zone boss (from [RivalSpec.isBoss]); available to the UI. */
    val isBoss: Boolean = false,
    /** Portrait key for the UI: the rival's from content, `warden` for the player. */
    val portrait: String = "warden",
    /** Combo chain (spec v2 §4); only resolved casts change it. */
    val chain: ComboChain = ComboChain(),
    /** HP thresholds of the brain's reaction already used, one bit per threshold index (rival-healing spec §2). */
    val reactionUsed: Int = 0,
    /** Index of the next combo of the reaction in progress, or -1 when there is none. */
    val reactionStep: Int = -1,
    /** Casts made inside reactions, so the pattern resumes where it was left ([castCount] counts every cast). */
    val reactionCasts: Int = 0,
    /** Current mode of a brain with boss modes (boss-modes spec §2). */
    val bossMode: BossMode = BossMode.ATTACK,
    /** Times this fighter has entered [BossMode.DEFENSE] in this combat. */
    val defenses: Int = 0,
    /** Casts made since the last mode change, the index into the current mode's pattern. */
    val modeCasts: Int = 0,
) {
    val isAlive: Boolean get() = hp > 0

    fun has(kind: EffectKind): Boolean = effects.any { it.kind == kind }

    /** True while a DAZED effect is present (even if pending): the fighter cannot act. */
    fun isDazed(): Boolean = has(EffectKind.DAZED)

    fun isActive(kind: EffectKind, tick: Int): Boolean = effects.any { it.kind == kind && it.isActive(tick) }

    /** Chance (percent) of being Dazed this tick: sum of the active DAZE_CHANCE magnitudes, capped at [Combos.DAZE_CAP_PCT]. */
    fun dazeChancePct(tick: Int): Int = minOf(
        Combos.DAZE_CAP_PCT,
        effects.filter { it.kind == EffectKind.DAZE_CHANCE && it.isActive(tick) }.sumOf { it.magnitude },
    )
}
