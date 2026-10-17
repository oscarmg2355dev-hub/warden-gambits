package com.wardengambits.app.ui.map.world

import kotlin.math.floor

/** Deterministic hash and value noise; the same shape as the approved JavaScript prototype. */
internal object WorldNoise {
    fun hash(a: Int, b: Int, c: Int): Int {
        var h = a * 374761393 + b * 668265263 + c * 2147483647
        h = (h xor (h ushr 13)) * 1274126177
        return h xor (h ushr 16)
    }

    /** Uniform value in [0, 1). */
    fun h01(a: Int, b: Int, c: Int): Float = (hash(a, b, c) ushr 8) / 16777216f

    fun smooth(t: Float): Float = t * t * (3 - 2 * t)

    fun valueNoise(x: Float, y: Float, seed: Int): Float {
        val xi = floor(x).toInt()
        val yi = floor(y).toInt()
        val xf = smooth(x - xi)
        val yf = smooth(y - yi)
        val a = h01(xi, yi, seed)
        val b = h01(xi + 1, yi, seed)
        val c = h01(xi, yi + 1, seed)
        val d = h01(xi + 1, yi + 1, seed)
        return a + (b - a) * xf + (c - a) * yf + (a - b - c + d) * xf * yf
    }

    fun fbm(x: Float, y: Float, seed: Int, octaves: Int = 4): Float {
        var v = 0f
        var amp = 0.5f
        var freq = 1f
        var norm = 0f
        for (i in 0 until octaves) {
            v += amp * valueNoise(x * freq, y * freq, seed + i * 17)
            norm += amp
            amp *= 0.5f
            freq *= 2.03f
        }
        return v / norm
    }
}
