package com.luminaris.extraction.core.extraction

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

/**
 * Runtime model representing an evacuation extraction zone within an instance world.
 *
 * @property id Unique extraction zone identifier.
 * @property displayName Formatted Adventure component label for user interfaces and actionbars.
 * @property boundingBox 3D spatial boundary within the instance world.
 * @property type Extraction operational criteria classification.
 * @property requiredItemId Optional custom item ID or key item required to extract.
 * @property timeStartSeconds Elapsed match timestamp when the zone becomes active (in seconds).
 * @property timeEndSeconds Elapsed match timestamp when the zone closes (in seconds).
 * @property countdownSeconds Time in seconds a player must remain in the zone to extract.
 * @property allowedTeamIds Set of permitted squad/team IDs for team-specific zones.
 */
data class ExtractionZone(
    val id: String,
    val displayName: Component,
    val boundingBox: BoundingBox,
    val type: ExtractionType,
    val requiredItemId: String? = null,
    val timeStartSeconds: Long = 0L,
    val timeEndSeconds: Long = Long.MAX_VALUE,
    val countdownSeconds: Int = 10,
    val allowedTeamIds: MutableSet<String> = mutableSetOf()
) {
    /**
     * Checks if a Bukkit Vector coordinate is within this extraction zone's 3D boundary.
     */
    fun contains(vector: Vector): Boolean = boundingBox.contains(vector)

    /**
     * Checks if a Bukkit Location is within this extraction zone's 3D boundary.
     */
    fun contains(location: Location): Boolean = boundingBox.contains(location.toVector())

    /**
     * Checks if 3D coordinates (x, y, z) are within this extraction zone's 3D boundary.
     */
    fun contains(x: Double, y: Double, z: Double): Boolean = boundingBox.contains(x, y, z)
}
