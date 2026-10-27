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
        fun fromPrice(price: Double): Rarity {
            val cfg = com.luminaris.extraction.LuminarisCorePlugin.instanceOrNull?.config
            val legThresh = cfg?.getDouble("economy.rarity_price_thresholds.LEGENDARY", 2500.0) ?: 2500.0
            val epicThresh = cfg?.getDouble("economy.rarity_price_thresholds.EPIC", 1000.0) ?: 1000.0
            val rareThresh = cfg?.getDouble("economy.rarity_price_thresholds.RARE", 400.0) ?: 400.0
            val uncThresh = cfg?.getDouble("economy.rarity_price_thresholds.UNCOMMON", 100.0) ?: 100.0

            return when {
                price >= legThresh -> LEGENDARY
                price >= epicThresh -> EPIC
                price >= rareThresh -> RARE
                price >= uncThresh -> UNCOMMON
                else -> COMMON
            }
        }
    }
}
