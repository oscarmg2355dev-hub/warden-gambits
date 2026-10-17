package com.wardengambits.engine.combo

/** A fighter's current combo chain (spec v2 §4). `family == null` means no chain (never started or broken). */
data class ComboChain(val family: ComboFamily? = null, val tier: Int = 0)

object ChainRules {
    private const val MAX_TIER = 4

    /** Chain after [combo] resolves successfully, and whether [combo] was applied chained (with bonus). */
    fun next(chain: ComboChain, combo: ComboDefinition): Pair<ComboChain, Boolean> {
        val reached = ComboChain(combo.family, combo.tier)
        val sameFamily = chain.family != null && chain.family == combo.family
        return when {
            combo.tier >= 2 && sameFamily && chain.tier == combo.tier - 1 -> reached to true
            combo.tier == MAX_TIER && sameFamily && chain.tier == MAX_TIER -> reached to true
            combo.tier == 1 -> reached to false
            else -> ComboChain() to false
        }
    }
}
