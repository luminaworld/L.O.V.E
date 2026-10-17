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
            material == Material.OXIDIZED_COPPER_CHEST || material == Material.WAXED_OXIDIZED_COPPER_CHEST -> Sound.BLOCK_COPPER_CHEST_OXIDIZED_OPEN
            material == Material.WEATHERED_COPPER_CHEST || material == Material.WAXED_WEATHERED_COPPER_CHEST -> Sound.BLOCK_COPPER_CHEST_WEATHERED_OPEN
            material in COPPER_CHESTS -> Sound.BLOCK_COPPER_CHEST_OPEN
            else -> Sound.BLOCK_CHEST_OPEN
        }
    }

    /**
     * Checks if the material is a wooden chest variant.
     */
    fun isWoodenChest(material: Material): Boolean = material in WOODEN_CHESTS

    /**
     * Checks if the material is a copper chest variant.
     */
    fun isCopperChest(material: Material): Boolean = material in COPPER_CHESTS

    /**
     * Checks if the material is any chest type eligible to connect as a double chest.
     */
    fun isChest(material: Material): Boolean = isWoodenChest(material) || isCopperChest(material)

    /**
     * Checks if two chest materials are compatible to connect together into a double chest.
     * Wooden chests connect with matching wooden chests, and copper chests with matching copper chests.
     */
    fun areChestMaterialsCompatible(m1: Material, m2: Material): Boolean {
        if (!isChest(m1) || !isChest(m2)) return false
        return m1 == m2
    }

    /**
     * Resolves the 90-degree clockwise horizontal direction.
     */
    fun getClockwise(face: org.bukkit.block.BlockFace): org.bukkit.block.BlockFace = when (face) {
        org.bukkit.block.BlockFace.NORTH -> org.bukkit.block.BlockFace.EAST
        org.bukkit.block.BlockFace.EAST -> org.bukkit.block.BlockFace.SOUTH
        org.bukkit.block.BlockFace.SOUTH -> org.bukkit.block.BlockFace.WEST
        org.bukkit.block.BlockFace.WEST -> org.bukkit.block.BlockFace.NORTH
        else -> face
    }

    /**
     * Resolves the 90-degree counter-clockwise horizontal direction.
     */
    fun getCounterClockwise(face: org.bukkit.block.BlockFace): org.bukkit.block.BlockFace = when (face) {
        org.bukkit.block.BlockFace.NORTH -> org.bukkit.block.BlockFace.WEST
        org.bukkit.block.BlockFace.WEST -> org.bukkit.block.BlockFace.SOUTH
        org.bukkit.block.BlockFace.SOUTH -> org.bukkit.block.BlockFace.EAST
        org.bukkit.block.BlockFace.EAST -> org.bukkit.block.BlockFace.NORTH
        else -> face
    }

    /**
     * Locates the paired connected double chest block, if any.
     * Complies with Minecraft's standard:
     * - ChestType.LEFT connects in the clockwise direction of facing.
     * - ChestType.RIGHT connects in the counter-clockwise direction of facing.
     */
    fun getConnectedDoubleChestBlock(block: org.bukkit.block.Block): org.bukkit.block.Block? {
        val blockData = block.blockData
        if (blockData is org.bukkit.block.data.type.Chest) {
            if (blockData.type != org.bukkit.block.data.type.Chest.Type.SINGLE) {
                // 1. DoubleChest inventory holder check
                val state = runCatching { block.state }.getOrNull()
                if (state is org.bukkit.block.Chest) {
                    val holder = runCatching { state.inventory.holder }.getOrNull()
                    if (holder is org.bukkit.block.DoubleChest) {
                        val left = (holder.leftSide as? org.bukkit.block.Chest)?.block
                        val right = (holder.rightSide as? org.bukkit.block.Chest)?.block
                        if (left != null && right != null) {
                            return if (block == left) right else left
                        }
                    }
                }
                // 2. Direct Minecraft directional relation check
                val partnerFace = if (blockData.type == org.bukkit.block.data.type.Chest.Type.LEFT) {
                    getClockwise(blockData.facing)
                } else {
                    getCounterClockwise(blockData.facing)
                }
                val partner = block.getRelative(partnerFace)
                if (areChestMaterialsCompatible(block.type, partner.type) && partner.blockData is org.bukkit.block.data.type.Chest) {
                    val partnerData = partner.blockData as org.bukkit.block.data.type.Chest
                    if (partnerData.facing == blockData.facing &&
                        partnerData.type != org.bukkit.block.data.type.Chest.Type.SINGLE &&
                        partnerData.type != blockData.type
                    ) {
                        return partner
                    }
                }
            }
        }
        return null
    }

    /**
     * Finds an adjacent horizontally placed compatible chest or connected partner block.
     */
    fun getAdjacentOrConnectedChest(block: org.bukkit.block.Block): org.bukkit.block.Block? {
        getConnectedDoubleChestBlock(block)?.let { return it }
        if (!isChest(block.type)) return null

        val horizontalFaces = listOf(
            org.bukkit.block.BlockFace.NORTH,
            org.bukkit.block.BlockFace.SOUTH,
            org.bukkit.block.BlockFace.EAST,
            org.bukkit.block.BlockFace.WEST
        )
        for (face in horizontalFaces) {
            val rel = block.getRelative(face)
            if (areChestMaterialsCompatible(block.type, rel.type)) {
                return rel
            }
        }
        return null
    }

    /**
     * Connects two adjacent compatible chests so that they form a double chest.
     * Complies with Minecraft's standard:
     * - ChestType.LEFT has its connected partner clockwise from facing.
     * - ChestType.RIGHT has its connected partner counter-clockwise from facing.
     */
    fun connectChests(existingBlock: org.bukkit.block.Block, newBlock: org.bukkit.block.Block): Boolean {
        if (!areChestMaterialsCompatible(existingBlock.type, newBlock.type)) return false
        val existData = existingBlock.blockData as? org.bukkit.block.data.type.Chest ?: return false
        val newData = newBlock.blockData as? org.bukkit.block.data.type.Chest ?: return false

        val facing = existData.facing
        newData.facing = facing

        val dx = newBlock.x - existingBlock.x
        val dz = newBlock.z - existingBlock.z

        val dirToNew = when {
            dx > 0 && dz == 0 -> org.bukkit.block.BlockFace.EAST
            dx < 0 && dz == 0 -> org.bukkit.block.BlockFace.WEST
            dz > 0 && dx == 0 -> org.bukkit.block.BlockFace.SOUTH
            dz < 0 && dx == 0 -> org.bukkit.block.BlockFace.NORTH
            else -> return false
        }

        val cw = getClockwise(facing)
        val ccw = getCounterClockwise(facing)

        when (dirToNew) {
            cw -> {
                // newBlock is clockwise from existingBlock:
                // existingBlock connects clockwise -> existingBlock is LEFT
                // newBlock connects counter-clockwise -> newBlock is RIGHT
                existData.type = org.bukkit.block.data.type.Chest.Type.LEFT
                newData.type = org.bukkit.block.data.type.Chest.Type.RIGHT
            }
            ccw -> {
                // newBlock is counter-clockwise from existingBlock:
                // existingBlock connects counter-clockwise -> existingBlock is RIGHT
                // newBlock connects clockwise -> newBlock is LEFT
                existData.type = org.bukkit.block.data.type.Chest.Type.RIGHT
                newData.type = org.bukkit.block.data.type.Chest.Type.LEFT
            }
            else -> return false
        }

        existingBlock.setBlockData(existData, true)
        newBlock.setBlockData(newData, true)
        return true
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
