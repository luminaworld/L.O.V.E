package com.luminaris.extraction.core.item

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

/**
 * Item rarity tiers formatted with Kyori TextColor.
 * Ranks ordered from lowest to highest: F, D, C, B, A, S.
 */
enum class Rarity(
    val color: TextColor,
    val miniMessageTag: String
) {
    F(NamedTextColor.GRAY, "<color:#AAAAAA>"),
    D(NamedTextColor.GREEN, "<color:#55FF55>"),
    C(NamedTextColor.AQUA, "<color:#55FFFF>"),
    B(NamedTextColor.DARK_PURPLE, "<color:#AA00AA>"),
    A(NamedTextColor.GOLD, "<color:#FFAA00>"),
    S(NamedTextColor.RED, "<color:#FF5555>");

    /**
     * Renders the rarity name as a stylized Adventure Component.
     */
    val displayName: Component
        get() = Component.text(com.luminaris.extraction.localization.TranslationManager.get("rarity.${name.lowercase()}"), color)

    companion object {
        /**
         * Parses a rarity string safely, supporting both new rank letters (F, D, C, B, A, S)
         * and backward-compatible legacy tier names (COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, MYTHIC),
         * defaulting to [F].
         */
        fun fromString(name: String?): Rarity {
            if (name == null) return F
            val clean = name.trim().uppercase()
            return when (clean) {
                "F", "COMMON" -> F
                "D", "UNCOMMON" -> D
                "C", "RARE" -> C
                "B", "EPIC" -> B
                "A", "LEGENDARY" -> A
                "S", "MYTHIC", "MYTHICAL" -> S
                else -> entries.firstOrNull { it.name.equals(clean, ignoreCase = true) } ?: F
            }
        }

        /**
         * Infers an appropriate rarity tier from an item's sell price.
         */
        fun fromPrice(price: Double): Rarity {
            val cfg = com.luminaris.extraction.LuminarisCorePlugin.instanceOrNull?.config
            val sThresh = cfg?.getDouble("economy.rarity_price_thresholds.S", 5000.0)
                ?: cfg?.getDouble("economy.rarity_price_thresholds.MYTHIC", 5000.0) ?: 5000.0
            val aThresh = cfg?.getDouble("economy.rarity_price_thresholds.A", 2500.0)
                ?: cfg?.getDouble("economy.rarity_price_thresholds.LEGENDARY", 2500.0) ?: 2500.0
            val bThresh = cfg?.getDouble("economy.rarity_price_thresholds.B", 1000.0)
                ?: cfg?.getDouble("economy.rarity_price_thresholds.EPIC", 1000.0) ?: 1000.0
            val cThresh = cfg?.getDouble("economy.rarity_price_thresholds.C", 400.0)
                ?: cfg?.getDouble("economy.rarity_price_thresholds.RARE", 400.0) ?: 400.0
            val dThresh = cfg?.getDouble("economy.rarity_price_thresholds.D", 100.0)
                ?: cfg?.getDouble("economy.rarity_price_thresholds.UNCOMMON", 100.0) ?: 100.0

            return when {
                price >= sThresh -> S
                price >= aThresh -> A
                price >= bThresh -> B
                price >= cThresh -> C
                price >= dThresh -> D
                else -> F
            }
        }
    }
}
