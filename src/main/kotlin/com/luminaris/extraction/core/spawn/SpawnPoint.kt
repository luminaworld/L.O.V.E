package com.luminaris.extraction.core.spawn

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import java.util.UUID
import kotlin.math.sqrt

/**
 * Data structure representing a player spawn point within an extraction map.
 *
 * @property id Unique spawn point identifier.
 * @property x World coordinate X.
 * @property y World coordinate Y.
 * @property z World coordinate Z.
 * @property yaw Camera yaw angle.
 * @property pitch Camera pitch angle.
 * @property name Optional human-readable identifier or callsign.
 */
data class SpawnPoint(
    val id: String = UUID.randomUUID().toString().take(8),
    val x: Double = 0.0,
    val y: Double = 64.0,
    val z: Double = 0.0,
    val yaw: Float = 0.0f,
    val pitch: Float = 0.0f,
    val name: String = id
) {
    /**
     * Backward-compatible secondary constructor matching (id, displayName, x, y, z, yaw, pitch).
     */
    constructor(
        id: String,
        displayName: String,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float
    ) : this(
        id = id,
        x = x,
        y = y,
        z = z,
        yaw = yaw,
        pitch = pitch,
        name = displayName
    )

    /**
     * Calculates 3D Euclidean distance to another spawn point: sqrt((Δx)^2 + (Δy)^2 + (Δz)^2).
     */
    fun distance(other: SpawnPoint): Double = sqrt(distanceSquared(other))

    /**
     * Calculates squared 3D Euclidean distance to another spawn point: (Δx)^2 + (Δy)^2 + (Δz)^2.
     */
    fun distanceSquared(other: SpawnPoint): Double {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return dx * dx + dy * dy + dz * dz
    }

    /**
     * Calculates 2D horizontal (XZ) Euclidean distance to another spawn point: sqrt((Δx)^2 + (Δz)^2).
     */
    fun horizontalDistance(other: SpawnPoint): Double {
        val dx = x - other.x
        val dz = z - other.z
        return sqrt(dx * dx + dz * dz)
    }

    /**
     * Converts coordinates into a Bukkit Vector.
     */
    fun toVector(): Vector = Vector(x, y, z)

    /**
     * Converts this spawn point into a Bukkit Location within the specified world.
     */
    fun toLocation(world: World): Location = Location(world, x, y, z, yaw, pitch)
}
