package com.luminaris.extraction.editor.tool

import com.luminaris.extraction.localization.TranslationManager
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Utility tool service for creating and inspecting container dispenser items.
 * Used by server administrators and map editors to place registered loot containers.
 */
object ContainerMarkerTool {

    val KEY_CONTAINER_CATEGORY = NamespacedKey("luminaris", "container_category")
    val KEY_CONTAINER_ID = NamespacedKey("luminaris", "container_id")

    // 1. Wooden Chests
    val WOODEN_CHESTS = listOf(
        Material.CHEST,
        Material.TRAPPED_CHEST
    )

    // 2. Copper Chests (all variants)
    val COPPER_CHESTS = listOf(
        Material.COPPER_CHEST,
        Material.EXPOSED_COPPER_CHEST,
        Material.WEATHERED_COPPER_CHEST,
        Material.OXIDIZED_COPPER_CHEST,
        Material.WAXED_COPPER_CHEST,
        Material.WAXED_EXPOSED_COPPER_CHEST,
        Material.WAXED_WEATHERED_COPPER_CHEST,
        Material.WAXED_OXIDIZED_COPPER_CHEST
    )

    // 3. Barrel
    val BARRELS = listOf(
        Material.BARREL
    )

    // 4. Shulker Boxes (all colors)
    val SHULKER_BOXES = listOf(
        Material.SHULKER_BOX,
        Material.WHITE_SHULKER_BOX,
        Material.ORANGE_SHULKER_BOX,
        Material.MAGENTA_SHULKER_BOX,
        Material.LIGHT_BLUE_SHULKER_BOX,
        Material.YELLOW_SHULKER_BOX,
        Material.LIME_SHULKER_BOX,
        Material.PINK_SHULKER_BOX,
        Material.GRAY_SHULKER_BOX,
        Material.LIGHT_GRAY_SHULKER_BOX,
        Material.CYAN_SHULKER_BOX,
        Material.PURPLE_SHULKER_BOX,
        Material.BLUE_SHULKER_BOX,
        Material.BROWN_SHULKER_BOX,
        Material.GREEN_SHULKER_BOX,
        Material.RED_SHULKER_BOX,
        Material.BLACK_SHULKER_BOX
    )

    // 5. Crafting Table
    val WORKSTATIONS = listOf(
        Material.CRAFTING_TABLE
    )

    // 6. Furnaces (all variants)
    val FURNACES = listOf(
        Material.FURNACE,
        Material.BLAST_FURNACE,
        Material.SMOKER
    )

    // 7. Enchanting Table
    val ENCHANTING_TABLES = listOf(
        Material.ENCHANTING_TABLE
    )

    // 8. Decorated Pot
    val DECORATED_POTS = listOf(
        Material.DECORATED_POT
    )

    // 9. Bookshelves
    val BOOKSHELVES = listOf(
        Material.BOOKSHELF,
        Material.CHISELED_BOOKSHELF
    )

    // 10. Ender Chest
    val ENDER_CHESTS = listOf(
        Material.ENDER_CHEST
    )

    // 11. Armor Stand
    val ARMOR_STANDS = listOf(
        Material.ARMOR_STAND
    )

    /**
     * Complete list of all supported materials eligible to act as extraction drop boxes.
     */
    val ALL_SUPPORTED_MATERIALS: List<Material> = (
        WOODEN_CHESTS + COPPER_CHESTS + BARRELS + SHULKER_BOXES +
        WORKSTATIONS + FURNACES + ENCHANTING_TABLES + DECORATED_POTS +
        BOOKSHELVES + ENDER_CHESTS + ARMOR_STANDS
    ).distinct()

    /**
     * Checks if a material is supported as a drop box container.
     */
    fun isSupportedMaterial(material: Material): Boolean = material in ALL_SUPPORTED_MATERIALS

    /**
     * Resolves the appropriate audio feedback when opening a container of the specified material.
     */
    fun getOpenSound(material: Material): Sound {
        return when {
            material == Material.BARREL -> Sound.BLOCK_BARREL_OPEN
            material.name.contains("SHULKER_BOX") -> Sound.BLOCK_SHULKER_BOX_OPEN
            material == Material.ENDER_CHEST -> Sound.BLOCK_ENDER_CHEST_OPEN
            material in FURNACES -> Sound.BLOCK_FURNACE_FIRE_CRACKLE
            material in ENCHANTING_TABLES -> Sound.BLOCK_ENCHANTMENT_TABLE_USE
            material in DECORATED_POTS -> Sound.BLOCK_DECORATED_POT_INSERT
            material in BOOKSHELVES -> Sound.ITEM_BOOK_PAGE_TURN
            material in ARMOR_STANDS -> Sound.ITEM_ARMOR_EQUIP_GENERIC
            else -> Sound.BLOCK_CHEST_OPEN
        }
    }

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
     * Creates a Container Marker ItemStack for any supported material with:
     * - PDC key `luminaris:container_category = category_id`
     * - Lore with MiniMessage
     * - Enchantment glow meta flag
     *
     * @param categoryId Loot category identifier.
     * @param material Target container block material (defaults to CHEST).
     * @param categoryDisplayName Optional display name formatted for the title.
     * @return Configured marker ItemStack.
     */
    fun createContainerMarker(
        categoryId: String,
        material: Material = Material.CHEST,
        categoryDisplayName: String? = null
    ): ItemStack {
        val targetMaterial = if (isSupportedMaterial(material)) material else Material.CHEST
        val stack = ItemStack(targetMaterial)
        val cleanCatId = categoryId.lowercase().trim()

        stack.editMeta { meta ->
            // Display name
            val label = categoryDisplayName ?: cleanCatId
            meta.displayName(
                TranslationManager.render("tool.marker.display_name", "label" to label)
            )

            // Lore with MiniMessage
            val loreLines = listOf(
                TranslationManager.render("tool.marker.lore_category", "category" to cleanCatId),
                TranslationManager.render("tool.marker.lore_place"),
                TranslationManager.render("tool.marker.lore_break"),
                TranslationManager.render("tool.marker.lore_test")
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
