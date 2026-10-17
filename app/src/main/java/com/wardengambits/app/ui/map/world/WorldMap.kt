package com.wardengambits.app.ui.map.world

/** A tile of the world map (column [x], row [y]). */
data class Tile(val x: Int, val y: Int)

/**
 * The generated world: [pixels] holds one ARGB colour per tile (row-major, `y * width + x`) and
 * [water] and [lava] the indices of the water and lava tiles. The tile lists anchor the ambient
 * animation (chimney smoke, torches, mallorn sparkles, marsh [wisps], volcanic smoke from the
 * [vents]) and the zone stops on the road.
 */
class WorldMap(
    val width: Int,
    val height: Int,
    val pixels: IntArray,
    val water: IntArray,
    val chimneys: List<Tile>,
    val torches: List<Tile>,
    val mallorns: List<Tile>,
    val stops: List<Tile>,
    internal val spriteAnchors: List<Tile> = emptyList(),
    val lava: IntArray = IntArray(0),
    val wisps: List<Tile> = emptyList(),
    val vents: List<Tile> = emptyList(),
)
