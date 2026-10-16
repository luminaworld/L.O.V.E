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
    COMMON(NamedTextColor.GRAY, "<color:#AAAAAA>"),
    UNCOMMON(NamedTextColor.GREEN, "<color:#55FF55>"),
    RARE(NamedTextColor.BLUE, "<color:#55FFFF>"),
    EPIC(NamedTextColor.DARK_PURPLE, "<color:#AA00AA>"),
    LEGENDARY(NamedTextColor.GOLD, "<color:#FFAA00>");

    /**
     * Renders the rarity name as a stylized Adventure Component.
     */
    val displayName: Component
        get() = Component.text(com.luminaris.extraction.localization.TranslationManager.get("rarity.${name.lowercase()}"), color)

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
