package dev.mizarc.bellclaims.partitions

import org.bukkit.Location

/**
 * Stores two integers to define a flat position in the world.
 * @property x The X-Axis position.
 * @property z The Z-Axis position.
 */
open class Position(open val x: Int, open val y: Int?, open val z: Int) {
    fun getChunk(): Position2D {
        return Position2D(x shr 4, z shr 4)
    }
}