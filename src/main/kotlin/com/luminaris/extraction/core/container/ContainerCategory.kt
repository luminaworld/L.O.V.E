package com.luminaris.extraction.core.container

import com.luminaris.extraction.core.item.Rarity
import net.kyori.adventure.text.Component

/**
 * Core model representing a container category classification for extraction loot containers.
 *
 * @property id Unique category identifier.
 * @property displayName Adventure component title for display across UI and dialogs.
 * @property minRolls Minimum number of item roll attempts when generating container loot.
 * @property maxRolls Maximum number of item roll attempts when generating container loot.
 * @property rarityWeights Relative weight / chance for each [Rarity] tier to drop in this container.
 */
data class ContainerCategory(
    val id: String,
    val displayName: Component,
    var minRolls: Int,
    var maxRolls: Int,
    val rarityWeights: MutableMap<Rarity, Double> = defaultRarityWeights(id)
) {
    /**
     * Retrieves the configured drop weight for a given [Rarity].
     */
    fun getRarityWeight(rarity: Rarity): Double = rarityWeights[rarity] ?: 0.0

    /**
     * Updates the drop weight for a given [Rarity].
     */
    fun setRarityWeight(rarity: Rarity, weight: Double) {
        rarityWeights[rarity] = maxOf(0.0, weight)
    }

    /**
     * Calculates the drop probability percentage (0.0% - 100.0%) for a given [Rarity].
     */
    fun getRarityPercentage(rarity: Rarity): Double {
        val total = rarityWeights.values.sum()
        if (total <= 0.0) return 0.0
        val weight = rarityWeights[rarity] ?: 0.0
        return (weight / total) * 100.0
    }

    companion object {
        /**
         * Generates sensible default rarity weights based on the container category identifier.
         */
        fun defaultRarityWeights(categoryId: String = ""): MutableMap<Rarity, Double> {
            val lower = categoryId.lowercase()
            return when {
                lower.contains("legendary") || lower.contains("safe") -> mutableMapOf(
                    Rarity.COMMON to 0.0,
                    Rarity.UNCOMMON to 5.0,
                    Rarity.RARE to 25.0,
                    Rarity.EPIC to 45.0,
                    Rarity.LEGENDARY to 25.0
                )
                lower.contains("epic") || lower.contains("airdrop") -> mutableMapOf(
                    Rarity.COMMON to 5.0,
                    Rarity.UNCOMMON to 20.0,
                    Rarity.RARE to 40.0,
                    Rarity.EPIC to 25.0,
                    Rarity.LEGENDARY to 10.0
                )
                lower.contains("rare") -> mutableMapOf(
                    Rarity.COMMON to 15.0,
                    Rarity.UNCOMMON to 35.0,
                    Rarity.RARE to 35.0,
                    Rarity.EPIC to 15.0,
                    Rarity.LEGENDARY to 0.0
                )
                lower.contains("uncommon") -> mutableMapOf(
                    Rarity.COMMON to 40.0,
                    Rarity.UNCOMMON to 40.0,
                    Rarity.RARE to 18.0,
                    Rarity.EPIC to 2.0,
                    Rarity.LEGENDARY to 0.0
                )
                else -> mutableMapOf(
                    Rarity.COMMON to 70.0,
                    Rarity.UNCOMMON to 25.0,
                    Rarity.RARE to 5.0,
                    Rarity.EPIC to 0.0,
                    Rarity.LEGENDARY to 0.0
                )
            }
        }
    }
}

