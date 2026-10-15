package com.luminaris.extraction.core.item

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

/**
 * Item rarity tiers formatted with Kyori TextColor.
 */
enum class Rarity(
    val color: TextColor,
    val miniMessageTag: String
) {
    COMMON(NamedTextColor.GRAY, "<gray>"),
    UNCOMMON(NamedTextColor.GREEN, "<green>"),
    RARE(NamedTextColor.BLUE, "<blue>"),
    EPIC(NamedTextColor.DARK_PURPLE, "<dark_purple>"),
    LEGENDARY(NamedTextColor.GOLD, "<gold>");

    /**
     * Renders the rarity name as a stylized Adventure Component.
     */
    val displayName: Component
        get() = Component.text(name, color)

    companion object {
        /**
         * Parses a rarity string safely, defaulting to [COMMON].
         */
        fun fromString(name: String?): Rarity {
            if (name == null) return COMMON
            return entries.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) } ?: COMMON
        }

        /**
         * Infers an appropriate rarity tier from an item's sell price.
         */
        fun fromPrice(price: Double): Rarity = when {
            price >= 2500.0 -> LEGENDARY
            price >= 1000.0 -> EPIC
            price >= 400.0 -> RARE
            price >= 100.0 -> UNCOMMON
            else -> COMMON
        }
    }
}
