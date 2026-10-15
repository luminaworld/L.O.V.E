package com.luminaris.extraction.core.container

import net.kyori.adventure.text.Component

/**
 * Core model representing a container category classification for extraction loot containers.
 *
 * @property id Unique category identifier.
 * @property displayName Adventure component title for display across UI and dialogs.
 * @property minRolls Minimum number of item roll attempts when generating container loot.
 * @property maxRolls Maximum number of item roll attempts when generating container loot.
 */
data class ContainerCategory(
    val id: String,
    val displayName: Component,
    var minRolls: Int,
    var maxRolls: Int
)
