package com.wardengambits.engine

import com.wardengambits.engine.combo.ComboDefinition
import com.wardengambits.engine.combo.ComboEffect
import com.wardengambits.engine.combo.ComboFamily
import com.wardengambits.engine.combo.ComboFamily.AREA
import com.wardengambits.engine.combo.ComboFamily.DAZE
import com.wardengambits.engine.combo.ComboFamily.DIRECT
import com.wardengambits.engine.combo.ComboFamily.DRAIN
import com.wardengambits.engine.combo.ComboFamily.HEAL
import com.wardengambits.engine.combo.ComboFamily.MULTIPLIER
import com.wardengambits.engine.combo.ComboFamily.RECOVER
import com.wardengambits.engine.combo.ComboFamily.SHIELD
import com.wardengambits.engine.combo.ComboFamily.SPEAR_A
import com.wardengambits.engine.combo.ComboFamily.SPEAR_B
import com.wardengambits.engine.combo.Combos
import com.wardengambits.engine.combo.TargetKind
import com.wardengambits.engine.combo.TargetKind.ALL_OPPONENTS
import com.wardengambits.engine.combo.TargetKind.SELECTED
import com.wardengambits.engine.combo.TargetKind.SELF
import com.wardengambits.engine.event.RejectReason
import com.wardengambits.engine.model.EffectKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ComboCatalogTest {
    private val bonus = listOf(0, 5, 10, 20)

    /** One tiered family of spec v2 §3: four sequences, tier n = index + 1. */
    private fun tiered(
        family: ComboFamily,
        sequences: List<String>,
        costs: List<Int>,
        target: TargetKind,
        effect: (Int) -> ComboEffect,
        chainBonus: (Int) -> Int = { bonus[it - 1] },
    ): List<ComboDefinition> = sequences.mapIndexed { index, sequence ->
        val tier = index + 1
        ComboDefinition(sequence, family, tier, costs[index], target, effect(tier), 10, chainBonus(tier))
    }

    private val expected: List<ComboDefinition> =
        tiered(SPEAR_A, listOf("12", "121", "1212", "12121"), listOf(10, 18, 26, 34), SELECTED, { ComboEffect.SpearDot(it) }) +
            tiered(SPEAR_B, listOf("13", "131", "1313", "13131"), listOf(10, 18, 26, 34), SELECTED, { ComboEffect.SpearDot(it) }) +
            tiered(
                DAZE, listOf("31", "313", "3131", "31313"), listOf(10, 18, 26, 34), SELECTED,
                { ComboEffect.DazeChance(5, listOf(0, 0, 5, 30)[it - 1]) }, { 0 },
            ) +
            tiered(AREA, listOf("32", "323", "3232", "32323"), listOf(15, 24, 33, 42), ALL_OPPONENTS, { ComboEffect.AreaDot(it) }) +
            tiered(HEAL, listOf("21", "212", "2121", "21212"), listOf(10, 18, 26, 34), SELF, { ComboEffect.HealOverTime(it) }) +
            listOf(
                ComboDefinition("3123", DRAIN, 1, 20, ALL_OPPONENTS, ComboEffect.Drain(1), 30),
                ComboDefinition("31232", DRAIN, 2, 30, ALL_OPPONENTS, ComboEffect.Drain(2), 30),
                ComboDefinition("11", MULTIPLIER, 0, 15, SELF, ComboEffect.Buff(EffectKind.FURY, 30), 30),
                ComboDefinition("22", MULTIPLIER, 0, 15, SELF, ComboEffect.Buff(EffectKind.GUARD, 30), 30),
                ComboDefinition("33", MULTIPLIER, 0, 15, SELF, ComboEffect.Buff(EffectKind.VIGOR, 30), 30),
                ComboDefinition("2132", SHIELD, 0, 20, SELF, ComboEffect.Buff(EffectKind.SHIELD_MASTER, 60), 60),
                ComboDefinition("2131", SHIELD, 0, 20, SELF, ComboEffect.Buff(EffectKind.SHIELD_TACTICS, 60), 60),
                ComboDefinition("123", DIRECT, 0, 30, SELECTED, ComboEffect.DirectHit, 0),
                ComboDefinition("321", RECOVER, 0, 0, SELF, ComboEffect.PowerRestore(10), 0),
            )

    @Test fun has29Combos() = assertEquals(29, Combos.all.size)

    @Test fun sequencesAreUnique() =
        assertEquals(Combos.all.size, Combos.all.map { it.sequence }.toSet().size)

    @Test fun sequencesAreValid() = Combos.all.forEach {
        assertTrue(it.sequence.length in 2..5 && it.sequence.all { c -> c in "123" }, it.sequence)
    }

    @Test fun everyRowOfSpecTable() {
        assertEquals(expected.map { it.sequence }.toSet(), Combos.all.map { it.sequence }.toSet())
        expected.forEach { assertEquals(it, Combos.find(it.sequence), it.sequence) }
    }

    @Test fun sample() = assertEquals(
        ComboDefinition("1313", SPEAR_B, 3, 26, SELECTED, ComboEffect.SpearDot(3), 10, 10),
        Combos.find("1313"),
    )

    @Test fun constants() {
        assertEquals(10, Combos.DOT_TICKS)
        assertEquals(10, Combos.DAZED_TICKS)
        assertEquals(30, Combos.DRAIN_TICKS)
        assertEquals(5, Combos.DRAIN_PERIOD)
        assertEquals(55, Combos.DAZE_CAP_PCT)
    }

    @Test fun removedCombosAreGone() {
        listOf("23", "232", "2323", "23232", "111", "").forEach { assertNull(Combos.find(it), it) }
        assertTrue(RejectReason.entries.none { it.name == "FEARED" })
        assertTrue(EffectKind.entries.none { it.name == "FEARED" })
    }
}
