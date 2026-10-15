package com.luminaris.extraction.core.safe

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * Custom [InventoryHolder] representing an active virtual Safe Case container instance.
 *
 * @property safeUuid Unique instance UUID of the safe case.
 * @property sourceItem The item snapshot from which the safe container was opened.
 * @property capacity Storage capacity limit in usable slots.
 */
class SafeCaseHolder(
    val safeUuid: UUID,
    val sourceItem: ItemStack,
    val capacity: Int = 9
) : InventoryHolder {

    private var virtualInventory: Inventory? = null

    /**
     * Binds the live Bukkit [Inventory] to this holder upon creation.
     */
    fun bindInventory(inventory: Inventory) {
        this.virtualInventory = inventory
    }

    /**
     * Retrieves the live Bukkit [Inventory] associated with this Safe Case session.
     */
    override fun getInventory(): Inventory {
        return virtualInventory ?: throw IllegalStateException("SafeCase inventory has not been bound yet")
    }
}
