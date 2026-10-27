package com.luminaris.extraction.core.item

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.inventory.ItemStack

/**
 * Core model representing an extraction loot item in the Luminaris engine.
 *
 * @property id Unique alphanumeric identifier.
 * @property itemStack Bukkit item representation preserving all components, NBT, lore, and custom model data.
 * @property price Economy baseline or sell value.
 * @property rarity Kyori-colored rarity classification.
 * @property weight Drop / spawn probability weight in containers.
 * @property allowedContainers Set of container category IDs where this item is permitted to appear.
 */
data class ExtractionItem(
    val id: String,
    val itemStack: ItemStack,
    var price: Double = 100.0,
    var rarity: Rarity = Rarity.COMMON,
    var weight: Double = 10.0,
    val allowedContainers: MutableSet<String> = mutableSetOf(),
    var searchTime: Int = defaultSearchTime(rarity),
    var revealSound: String? = null
) {
    companion object {
        fun defaultSearchTime(rarity: Rarity): Int {
            val cfgTime = runCatching {
                com.luminaris.extraction.LuminarisCorePlugin.instanceOrNull?.config?.getInt("drop_box.rarity_search_times.${rarity.name}")
            }.getOrNull()
            if (cfgTime != null && cfgTime > 0) return cfgTime

            return when (rarity) {
                Rarity.COMMON -> 1
                Rarity.UNCOMMON -> 2
                Rarity.RARE -> 3
                Rarity.EPIC -> 4
                Rarity.LEGENDARY -> 5
            }
        }
    }

    /**
     * Resolves the effective display name of this item.
     * Preserves custom item display name if present; otherwise falls back to the Bukkit type name.
     */
    fun resolveDisplayName(): Component {
        if (itemStack.hasItemMeta()) {
            val meta = itemStack.itemMeta
            if (meta.hasDisplayName()) {
                return meta.displayName() ?: Component.text(itemStack.type.name)
            }
            if (meta.hasItemName()) {
                return meta.itemName()
            }
        }
        return Component.text(itemStack.type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })
    }

    /**
     * Creates a deep clone of this extraction item.
     */
    fun clone(): ExtractionItem = copy(
        itemStack = itemStack.clone(),
        allowedContainers = allowedContainers.toMutableSet()
    )
}
