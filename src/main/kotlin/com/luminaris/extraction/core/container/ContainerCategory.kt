package com.luminaris.extraction.core.container

import com.luminaris.extraction.core.item.Rarity
import net.kyori.adventure.text.Component

import org.bukkit.Material

/**
 * Core model representing a container category classification for extraction loot containers.
 *
 * @property id Unique category identifier.
 * @property displayName Adventure component title for display across UI and dialogs.
 * @property minRolls Minimum number of item roll attempts when generating container loot.
 * @property maxRolls Maximum number of item roll attempts when generating container loot.
 * @property rarityWeights Relative weight / chance for each [Rarity] tier to drop in this container.
 * @property searchTimeMin Minimum seconds for overall container search before revealing slots.
 * @property searchTimeMax Maximum seconds for overall container search before revealing slots.
 * @property coveredMaterial Block material shown covering all slots upon opening.
 * @property searchingMaterial Block material shown while counting down item drops.
 * @property searchSound Custom sound played during searching intervals.
 * @property revealSound Custom fallback sound played when an item is revealed.
 */
data class ContainerCategory(
    val id: String,
    val displayName: Component,
    var minRolls: Int,
    var maxRolls: Int,
    val rarityWeights: MutableMap<Rarity, Double> = defaultRarityWeights(id),
    var searchTimeMin: Int = 2,
    var searchTimeMax: Int = 4,
    var coveredMaterial: Material = Material.BLACK_STAINED_GLASS_PANE,
    var searchingMaterial: Material = Material.STONE,
    var searchSound: String? = null,
    var revealSound: String? = null,
    var timedUnlockEnabled: Boolean = false,
    var unlockCountdownSeconds: Int = 0,
    var unlockTickSound: String? = "BLOCK_NOTE_BLOCK_HAT",
    var unlockFinishSound: String? = "BLOCK_IRON_DOOR_OPEN"
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
            currentPcts[Rarity.F] = 100.0
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
            val fallback = if (targetRarity == Rarity.F) Rarity.D else Rarity.F
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
                lower.contains("mythic") || lower.contains("rank_s") || lower.contains("s_grade") || lower.contains("boss") -> mutableMapOf(
                    Rarity.F to 0.0,
                    Rarity.D to 10.0,
                    Rarity.C to 20.0,
                    Rarity.B to 35.0,
                    Rarity.A to 25.0,
                    Rarity.S to 10.0
                )
                lower.contains("legendary") || lower.contains("safe") || lower.contains("rank_a") -> mutableMapOf(
                    Rarity.F to 10.0,
                    Rarity.D to 20.0,
                    Rarity.C to 35.0,
                    Rarity.B to 20.0,
                    Rarity.A to 10.0,
                    Rarity.S to 5.0
                )
                lower.contains("epic") || lower.contains("airdrop") || lower.contains("rank_b") -> mutableMapOf(
                    Rarity.F to 5.0,
                    Rarity.D to 15.0,
                    Rarity.C to 25.0,
                    Rarity.B to 30.0,
                    Rarity.A to 17.0,
                    Rarity.S to 8.0
                )
                lower.contains("rare") || lower.contains("rank_c") -> mutableMapOf(
                    Rarity.F to 15.0,
                    Rarity.D to 35.0,
                    Rarity.C to 35.0,
                    Rarity.B to 14.0,
                    Rarity.A to 1.0,
                    Rarity.S to 0.0
                )
                lower.contains("uncommon") || lower.contains("rank_d") -> mutableMapOf(
                    Rarity.F to 40.0,
                    Rarity.D to 40.0,
                    Rarity.C to 18.0,
                    Rarity.B to 2.0,
                    Rarity.A to 0.0,
                    Rarity.S to 0.0
                )
                else -> mutableMapOf(
                    Rarity.F to 70.0,
                    Rarity.D to 25.0,
                    Rarity.C to 5.0,
                    Rarity.B to 0.0,
                    Rarity.A to 0.0,
                    Rarity.S to 0.0
                )
            }
        }
    }
}

