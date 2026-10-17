package com.wardengambits.engine.combo

import com.wardengambits.engine.model.EffectKind

/** Combo catalog of spec v2 §3 (29 combos). */
object Combos {
    const val DOT_TICKS = 10
    const val DAZED_TICKS = 10
    const val DRAIN_TICKS = 30
    const val DRAIN_PERIOD = 5
    const val DAZE_CAP_PCT = 55

    /** Percent of max power restored by `321` (Recuperar). */
    const val RECOVER_PCT = 10

    private const val MULTIPLIER_TICKS = 30
    private const val SHIELD_TICKS = 60
    private val CHAIN_BONUS_TICKS = listOf(0, 5, 10, 20)
    private val DAZE_CHAIN_BONUS_PCT = listOf(0, 0, 5, 30)
    private const val DAZE_BASE_PCT = 5
    private val COSTS = listOf(10, 18, 26, 34)
    private val AREA_COSTS = listOf(15, 24, 33, 42)

    private fun tiered(
        family: ComboFamily,
        sequences: List<String>,
        costs: List<Int>,
        target: TargetKind,
        chainBonusTicks: List<Int> = CHAIN_BONUS_TICKS,
        effect: (tier: Int) -> ComboEffect,
    ): List<ComboDefinition> = sequences.mapIndexed { index, sequence ->
        val tier = index + 1
        ComboDefinition(
            sequence, family, tier, costs[index], target, effect(tier), DOT_TICKS, chainBonusTicks[index],
        )
    }

    private fun buff(sequence: String, family: ComboFamily, cost: Int, kind: EffectKind, ticks: Int) =
        ComboDefinition(sequence, family, 0, cost, TargetKind.SELF, ComboEffect.Buff(kind, ticks), ticks)

    val all: List<ComboDefinition> =
        tiered(ComboFamily.SPEAR_A, listOf("12", "121", "1212", "12121"), COSTS, TargetKind.SELECTED) {
            ComboEffect.SpearDot(it)
        } +
            tiered(ComboFamily.SPEAR_B, listOf("13", "131", "1313", "13131"), COSTS, TargetKind.SELECTED) {
                ComboEffect.SpearDot(it)
            } +
            tiered(
                ComboFamily.DAZE, listOf("31", "313", "3131", "31313"), COSTS, TargetKind.SELECTED,
                chainBonusTicks = listOf(0, 0, 0, 0),
            ) { ComboEffect.DazeChance(DAZE_BASE_PCT, DAZE_CHAIN_BONUS_PCT[it - 1]) } +
            tiered(ComboFamily.AREA, listOf("32", "323", "3232", "32323"), AREA_COSTS, TargetKind.ALL_OPPONENTS) {
                ComboEffect.AreaDot(it)
            } +
            tiered(ComboFamily.HEAL, listOf("21", "212", "2121", "21212"), COSTS, TargetKind.SELF) {
                ComboEffect.HealOverTime(it)
            } +
            listOf(
                ComboDefinition("3123", ComboFamily.DRAIN, 1, 20, TargetKind.ALL_OPPONENTS, ComboEffect.Drain(1), DRAIN_TICKS),
                ComboDefinition("31232", ComboFamily.DRAIN, 2, 30, TargetKind.ALL_OPPONENTS, ComboEffect.Drain(2), DRAIN_TICKS),
                buff("11", ComboFamily.MULTIPLIER, 15, EffectKind.FURY, MULTIPLIER_TICKS),
                buff("22", ComboFamily.MULTIPLIER, 15, EffectKind.GUARD, MULTIPLIER_TICKS),
                buff("33", ComboFamily.MULTIPLIER, 15, EffectKind.VIGOR, MULTIPLIER_TICKS),
                buff("2132", ComboFamily.SHIELD, 20, EffectKind.SHIELD_MASTER, SHIELD_TICKS),
                buff("2131", ComboFamily.SHIELD, 20, EffectKind.SHIELD_TACTICS, SHIELD_TICKS),
                ComboDefinition("123", ComboFamily.DIRECT, 0, 30, TargetKind.SELECTED, ComboEffect.DirectHit, 0),
                ComboDefinition("321", ComboFamily.RECOVER, 0, 0, TargetKind.SELF, ComboEffect.PowerRestore(RECOVER_PCT), 0),
            )

    private val bySequence: Map<String, ComboDefinition> = all.associateBy { it.sequence }

    fun find(sequence: String): ComboDefinition? = bySequence[sequence]
}
