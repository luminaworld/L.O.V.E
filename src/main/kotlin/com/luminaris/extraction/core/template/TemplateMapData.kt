package com.luminaris.extraction.core.template

import com.luminaris.extraction.core.container.PlacedContainer

/**
 * Data model holding all template map attributes, registered container markers,
 * spawn points, and extraction evacuation zones for a template map.
 *
 * Persisted to `templates/<template_id>/map_data.yml`.
 *
 * @property templateId Unique identifier for the map template (e.g., "factory").
 * @property displayName Human-readable or MiniMessage formatted display name.
 * @property containers Mutable list of container loot markers placed on the map.
 * @property spawns Mutable list of spawn points configured on the map.
 * @property extractions Mutable list of extraction zone boundaries.
 */
data class TemplateMapData(
    val templateId: String,
    var displayName: String = templateId,
    val containers: MutableList<PlacedContainer> = mutableListOf(),
    val spawns: MutableList<SpawnPoint> = mutableListOf(),
    val extractions: MutableList<ExtractionZoneData> = mutableListOf()
)

/**
 * Type alias to support canonical TemplateData referencing.
 */
typealias TemplateData = TemplateMapData
