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
    var price: Double,
    var rarity: Rarity,
    var weight: Double,
    val allowedContainers: MutableSet<String> = mutableSetOf()
) {
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
