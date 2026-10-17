package com.wardengambits.app.ui.map.world

/**
 * Presentation-only keyframes: how much of each terrain field every zone has. The world generator
 * interpolates them between zone centres, so the map changes continuously along the route.
 */
internal object ZoneKeys {
    const val FARM = 0
    const val MEADOW = 1
    const val FOREST = 2
    const val MARSH = 3
    const val HILLS = 4
    const val ROCK = 5
    const val SNOW = 6
    const val MOUNT = 7
    const val UNDER = 8
    const val GOLDEN = 9
    const val HOLLY = 10
    const val VALLEY = 11
    const val BIGRIVER = 12
    const val CLIFFS = 13
    const val HILLRUINS = 14
    const val GREYROCK = 15
    const val DEADMARSH = 16
    const val HERBS = 17
    const val MORGUL = 18
    const val ASH = 19
    const val LAVA = 20

    /** Number of biome fields (the ones with a colour ramp and sprites). */
    const val BIOMES = 21

    /** Extra non-biome channel: base elevation. */
    const val ELEV = 21
    const val CHANNELS = 22

    private fun keys(vararg pairs: Pair<Int, Float>): FloatArray =
        FloatArray(CHANNELS).also { a -> for ((k, v) in pairs) a[k] = v }

    private val TABLE: Map<String, FloatArray> = mapOf(
        "bree" to keys(FARM to 1f, MEADOW to 0.8f),
        "chet" to keys(FOREST to 1f, MEADOW to 0.2f),
        "moscagua" to keys(MARSH to 1f, FOREST to 0.2f),
        "cima" to keys(HILLS to 1f, ROCK to 0.4f, MEADOW to 0.3f, ELEV to 0.45f),
        "puente" to keys(MEADOW to 0.6f, HILLS to 0.5f, ELEV to 0.2f),
        "quebradas" to keys(FOREST to 0.55f, ROCK to 0.7f, HILLS to 0.6f, ELEV to 0.45f),
        "bruinen" to keys(FOREST to 0.5f, MEADOW to 0.4f, ELEV to 0.25f),
        "rivendel" to keys(VALLEY to 1f, FOREST to 0.5f, ELEV to 0.35f),
        "eregion" to keys(HOLLY to 1f, HILLS to 0.5f, MEADOW to 0.3f, ELEV to 0.3f),
        "caradhras" to keys(SNOW to 1f, MOUNT to 1f, ROCK to 0.6f, ELEV to 1f),
        "durin" to keys(MOUNT to 0.8f, ROCK to 1f, SNOW to 0.2f, ELEV to 0.8f),
        "moria" to keys(UNDER to 1f, MOUNT to 0.6f, ELEV to 0.7f),
        "sombrio" to keys(ROCK to 0.7f, MOUNT to 0.45f, MEADOW to 0.35f, ELEV to 0.45f),
        "nimrodel" to keys(FOREST to 0.85f, GOLDEN to 0.35f, MEADOW to 0.25f, ELEV to 0.15f),
        "lothlorien" to keys(GOLDEN to 1f, FOREST to 0.5f, ELEV to 0.05f),
        "anduin" to keys(BIGRIVER to 1f, FOREST to 0.45f, GOLDEN to 0.3f, MEADOW to 0.25f, ELEV to 0.05f),
        "argonath" to keys(CLIFFS to 1f, ROCK to 0.5f, BIGRIVER to 0.35f, ELEV to 0.45f),
        "amonhen" to keys(HILLRUINS to 1f, FOREST to 0.45f, HILLS to 0.35f, ELEV to 0.3f),
        "emynmuil" to keys(GREYROCK to 1f, ROCK to 0.5f, HILLS to 0.3f, ELEV to 0.5f),
        "cienagas" to keys(DEADMARSH to 1f, MARSH to 0.25f),
        "ithilien" to keys(HERBS to 1f, FOREST to 0.35f, MEADOW to 0.25f, ELEV to 0.2f),
        "cirithungol" to keys(MORGUL to 1f, MOUNT to 0.5f, ROCK to 0.3f, ELEV to 0.6f),
        "gorgoroth" to keys(ASH to 1f, LAVA to 0.4f, ROCK to 0.25f, ELEV to 0.3f),
        "orodruin" to keys(ASH to 0.7f, LAVA to 1f, ELEV to 0.35f),
    )

    private val FALLBACK = keys(MEADOW to 1f)

    /** The [CHANNELS] weights of a zone; unknown zones are plain meadow. */
    fun weights(zoneId: String): FloatArray = (TABLE[zoneId] ?: FALLBACK).copyOf()
}
