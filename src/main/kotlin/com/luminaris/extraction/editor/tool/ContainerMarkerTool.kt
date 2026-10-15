package com.luminaris.extraction.editor.tool

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Utility tool service for creating and inspecting container dispenser items.
 * Used by server administrators and map editors to place registered loot containers.
 */
object ContainerMarkerTool {

    val KEY_CONTAINER_CATEGORY = NamespacedKey("luminaris", "container_category")
    private val miniMessage = MiniMessage.miniMessage()

    /**
     * Checks if the given ItemStack is a valid container marker tool item.
     */
    fun isContainerMarker(item: ItemStack?): Boolean {
        if (item == null || item.isEmpty || !item.hasItemMeta()) return false
        val pdc = item.itemMeta.persistentDataContainer
        return pdc.has(KEY_CONTAINER_CATEGORY, PersistentDataType.STRING)
    }

    /**
     * Extracts the container category identifier from the item's PDC, or null if absent.
     */
    fun getCategoryId(item: ItemStack?): String? {
        if (item == null || item.isEmpty || !item.hasItemMeta()) return null
        return item.itemMeta.persistentDataContainer.get(KEY_CONTAINER_CATEGORY, PersistentDataType.STRING)
    }

    /**
     * Creates a Container Marker ItemStack (CHEST or BARREL) with:
     * - PDC key `luminaris:container_category = category_id`
     * - Lore with MiniMessage: `"<gray>Category: <yellow>{category_id}</yellow></gray>"`
     * - Enchantment glow meta flag enabled via [org.bukkit.inventory.meta.ItemMeta.setEnchantmentGlintOverride]
     *
     * @param categoryId Loot category identifier.
     * @param material Target container block material (must be CHEST or BARREL, defaults to CHEST).
     * @param categoryDisplayName Optional display name formatted for the title.
     * @return Configured marker ItemStack.
     */
    fun createContainerMarker(
        categoryId: String,
        material: Material = Material.CHEST,
        categoryDisplayName: String? = null
    ): ItemStack {
        val targetMaterial = if (material == Material.BARREL) Material.BARREL else Material.CHEST
        val stack = ItemStack(targetMaterial)
        val cleanCatId = categoryId.lowercase().trim()

        stack.editMeta { meta ->
            // Display name
            val label = categoryDisplayName ?: cleanCatId
            meta.displayName(
                miniMessage.deserialize("<gradient:#00FFA3:#00B8FF><bold>Container Marker</bold></gradient> <gray>(</gray><yellow>$label</yellow><gray>)</gray>")
            )

            // Lore with MiniMessage: "<gray>Category: <yellow>{category_id}</yellow></gray>"
            val loreLines = listOf(
                miniMessage.deserialize("<gray>Category: <yellow>$cleanCatId</yellow></gray>"),
                miniMessage.deserialize("<dark_gray>▪ Place in a template world to register.</dark_gray>"),
                miniMessage.deserialize("<dark_gray>▪ Break with editor permission to remove.</dark_gray>")
            )
            meta.lore(loreLines)

            // Enchantment glow meta flag
            meta.setEnchantmentGlintOverride(true)

            // PDC key luminaris:container_category = category_id
            val pdc = meta.persistentDataContainer
            pdc.set(KEY_CONTAINER_CATEGORY, PersistentDataType.STRING, cleanCatId)
        }

        return stack
    }
}
