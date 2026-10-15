package com.luminaris.extraction.core.template

import com.luminaris.extraction.core.extraction.ExtractionType
import com.luminaris.extraction.core.extraction.ExtractionZone
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.util.BoundingBox
import java.util.UUID

/**
 * Data structure representing an extraction evacuation zone within a map template.
 * Persisted to `templates/<template_id>/map_data.yml`.
 *
 * @property id Unique extraction zone identifier.
 * @property name Human-readable display label (supports MiniMessage).
 * @property minX Minimum bounding coordinate X.
 * @property minY Minimum bounding coordinate Y.
 * @property minZ Minimum bounding coordinate Z.
 * @property maxX Maximum bounding coordinate X.
 * @property maxY Maximum bounding coordinate Y.
 * @property maxZ Maximum bounding coordinate Z.
 * @property countdownSeconds Extraction hold duration required to extract.
 * @property type Extraction operational rule classification (e.g. ALWAYS_OPEN, TEAM_SPECIFIC).
 * @property requiredItemId Optional custom item ID or key item required to extract.
 * @property timeStartSeconds Elapsed match timestamp when the zone becomes active.
 * @property timeEndSeconds Elapsed match timestamp when the zone closes.
 * @property allowedTeamIds List of permitted squad/team IDs for team-specific zones.
 */
data class ExtractionZoneData(
    val id: String = UUID.randomUUID().toString().take(8),
    val name: String = "Extraction Zone",
    val minX: Double = 0.0,
    val minY: Double = 0.0,
    val minZ: Double = 0.0,
    val maxX: Double = 0.0,
    val maxY: Double = 0.0,
    val maxZ: Double = 0.0,
    val countdownSeconds: Int = 10,
    val type: String = "ALWAYS_OPEN",
    val requiredItemId: String? = null,
    val timeStartSeconds: Long = 0L,
    val timeEndSeconds: Long = Long.MAX_VALUE,
    val allowedTeamIds: List<String> = emptyList()
) {
    /**
     * Converts this serialized template definition into an active runtime [ExtractionZone].
     */
    fun toExtractionZone(): ExtractionZone {
        val mm = MiniMessage.miniMessage()
        val display = runCatching { mm.deserialize(name) }.getOrElse { Component.text(name) }
        val box = BoundingBox(
            minOf(minX, maxX),
            minOf(minY, maxY),
            minOf(minZ, maxZ),
            maxOf(minX, maxX),
            maxOf(minY, maxY),
            maxOf(minZ, maxZ)
        )
        val parsedType = runCatching { ExtractionType.valueOf(type.uppercase()) }.getOrDefault(ExtractionType.ALWAYS_OPEN)
        return ExtractionZone(
            id = id,
            displayName = display,
            boundingBox = box,
            type = parsedType,
            requiredItemId = requiredItemId,
            timeStartSeconds = timeStartSeconds,
            timeEndSeconds = timeEndSeconds,
            countdownSeconds = countdownSeconds,
            allowedTeamIds = allowedTeamIds.toMutableSet()
        )
    }
}
