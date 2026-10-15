package com.luminaris.extraction.core.container

import java.util.UUID

/**
 * Data class representing a registered container marker inside a map template.
 *
 * @property id Unique identifier of this placed container instance.
 * @property categoryId Loot category identifier matching an entry in ContainerRegistry.
 * @property x Block coordinate X.
 * @property y Block coordinate Y.
 * @property z Block coordinate Z.
 */
data class PlacedContainer(
    val id: String,
    val categoryId: String,
    val x: Int,
    val y: Int,
    val z: Int
) {
    companion object {
        /**
         * Generates a unique container ID incorporating coordinates and a random suffix.
         */
        fun generateId(x: Int, y: Int, z: Int): String {
            val shortSuffix = UUID.randomUUID().toString().take(8)
            return "c_${x}_${y}_${z}_$shortSuffix"
        }
    }
}
