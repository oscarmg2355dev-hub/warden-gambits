package com.wardengambits.engine.random

interface GameRandom {
    /** Returns a value in `0 until bound`. */
    fun nextInt(bound: Int): Int

    /** Current internal state; `SeededRandom(state)` continues the same sequence. */
    val state: Long
}

/** SplitMix64 deterministic generator. */
class SeededRandom(seed: Long) : GameRandom {
    override var state: Long = seed
        private set

    override fun nextInt(bound: Int): Int {
        state += GOLDEN_GAMMA
        var mix = state
        mix = (mix xor (mix ushr 30)) * MIX_1
        mix = (mix xor (mix ushr 27)) * MIX_2
        mix = mix xor (mix ushr 31)
        return ((mix ushr 1) % bound).toInt()
    }

    private companion object {
        const val GOLDEN_GAMMA = -0x61c8864680b583ebL // 0x9E3779B97F4A7C15
        const val MIX_1 = -0x40a7b892e31b1a47L // 0xBF58476D1CE4E5B9
        const val MIX_2 = -0x6b2fb644ecceee15L // 0x94D049BB133111EB
    }
}
