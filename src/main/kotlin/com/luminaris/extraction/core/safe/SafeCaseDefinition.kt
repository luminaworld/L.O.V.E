package com.luminaris.extraction.core.safe

import net.kyori.adventure.text.Component
import org.bukkit.Material

/**
 * Immutable profile definition for a safe case loaded from `safe_cases.yml`.
 *
 * @property id Unique identifier of the safe container (e.g. "alpha_pouch").
 * @property displayName Rich Adventure Component representation of the container's title.
 * @property material Physical Bukkit Material representation of the item.
 * @property customModelData Optional CustomModelData identifier for custom textures or 3D models.
 * @property capacity Storage capacity in slots (e.g., 4 for 2x2, 6 for 3x2, 9 for 3x3, 12 for 4x3).
 * @property keepsOnDeath Whether this container and its contents are kept across player deaths.
 */
data class SafeCaseDefinition(
    val id: String,
    val displayName: Component,
    val material: Material = Material.BUNDLE,
    val customModelData: Int? = null,
    val capacity: Int = 9,
    val keepsOnDeath: Boolean = true
)
