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

    /**
     * Adjusts the percentage of a target rarity tier by [delta] (+1, -1, +5, -5, etc.)
     * and automatically balances all other rarity tiers proportionally, guaranteeing that
     * the sum across all rarities equals exactly 100.0%.
     */
    fun adjustRarityPercentage(targetRarity: Rarity, delta: Double) {
        val currentPcts = mutableMapOf<Rarity, Double>()
        val currentTotal = rarityWeights.values.sum()
        for (r in Rarity.entries) {
            val currentW = rarityWeights[r] ?: 0.0
            currentPcts[r] = if (currentTotal > 0.0) (currentW / currentTotal) * 100.0 else 0.0
        }
        if (currentPcts.values.sum() <= 0.0) {
            currentPcts[Rarity.COMMON] = 100.0
        }

        val oldTargetPct = currentPcts[targetRarity] ?: 0.0
        val newTargetPct = (oldTargetPct + delta).coerceIn(0.0, 100.0)
        currentPcts[targetRarity] = newTargetPct

        val targetDelta = newTargetPct - oldTargetPct
        if (kotlin.math.abs(targetDelta) < 0.0001) return

        val otherRarities = Rarity.entries.filter { it != targetRarity }
        val otherOldSum = otherRarities.sumOf { currentPcts[it] ?: 0.0 }
        val newOtherTotal = 100.0 - newTargetPct

        if (otherOldSum > 0.0001) {
            for (r in otherRarities) {
                val oldVal = currentPcts[r] ?: 0.0
                currentPcts[r] = (oldVal / otherOldSum) * newOtherTotal
            }
        } else {
            val fallback = if (targetRarity == Rarity.COMMON) Rarity.UNCOMMON else Rarity.COMMON
            currentPcts[fallback] = newOtherTotal
        }

        // Round to 1 decimal place
        var sumRounded = 0.0
        for (r in Rarity.entries) {
            val rounded = kotlin.math.round(currentPcts[r]!! * 10.0) / 10.0
            currentPcts[r] = rounded
            sumRounded += rounded
        }

        // Adjust any rounding discrepancy on highest non-target rarity
        val diff = kotlin.math.round((100.0 - sumRounded) * 10.0) / 10.0
        if (kotlin.math.abs(diff) > 0.001) {
            val highestOther = otherRarities.maxByOrNull { currentPcts[it] ?: 0.0 } ?: otherRarities.first()
            currentPcts[highestOther] = (currentPcts[highestOther]!! + diff).coerceAtLeast(0.0)
        }

        // Apply back to category
        for ((r, pct) in currentPcts) {
            setRarityWeight(r, pct)
        }
    }

    /**
     * Resets rarity weights to the default presets for this category, summing to 100.0%.
     */
    fun resetRarityWeightsToDefault() {
        val defaults = defaultRarityWeights(id)
        rarityWeights.clear()
        rarityWeights.putAll(defaults)
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

