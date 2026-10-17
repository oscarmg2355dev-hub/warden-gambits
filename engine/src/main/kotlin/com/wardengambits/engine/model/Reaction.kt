package com.wardengambits.engine.model

/**
 * A rival's reaction by HP (rival-healing spec §2): when it is due to cast at or below one of [atHpPct] (percent of
 * max HP, descending, each used once per combat) it casts [shield] (if any) and then [combos], one per tick.
 */
data class Reaction(val atHpPct: List<Int>, val shield: String?, val combos: List<String>) {
    init {
        require(atHpPct.isNotEmpty() && atHpPct.size <= MAX_THRESHOLDS) { "1..$MAX_THRESHOLDS thresholds, got $atHpPct" }
        require(atHpPct.all { it in 1..100 } && atHpPct.zipWithNext().all { (a, b) -> a > b }) {
            "Thresholds must be 1..100 and strictly descending, got $atHpPct"
        }
        require(combos.isNotEmpty()) { "A reaction needs at least one combo" }
    }

    /** What the reaction casts, in order: the shield first, then the combos. */
    val sequence: List<String> get() = listOfNotNull(shield) + combos

    private companion object {
        const val MAX_THRESHOLDS = 31
    }
}
