package com.luminaris.extraction.editor

import com.luminaris.extraction.editor.tool.ContainerMarkerTool
import org.bukkit.NamespacedKey
import kotlin.test.Test
import kotlin.test.assertEquals

class ContainerMarkerToolTest {

    @Test
    fun testNamespacedKey() {
        assertEquals("luminaris:container_category", ContainerMarkerTool.KEY_CONTAINER_CATEGORY.toString())
        assertEquals("luminaris", ContainerMarkerTool.KEY_CONTAINER_CATEGORY.namespace)
        assertEquals("container_category", ContainerMarkerTool.KEY_CONTAINER_CATEGORY.key)
        assertEquals("luminaris:container_id", ContainerMarkerTool.KEY_CONTAINER_ID.toString())
    }

    @Test
    fun testAllSupportedMaterialsPresent() {
        val materials = ContainerMarkerTool.ALL_SUPPORTED_MATERIALS
        assertEquals(38, materials.size, "Should support exactly 38 drop container materials")

        // 1. Wooden Chests
        org.bukkit.Material.CHEST.let { kotlin.test.assertTrue(ContainerMarkerTool.isSupportedMaterial(it)) }
        org.bukkit.Material.TRAPPED_CHEST.let { kotlin.test.assertTrue(ContainerMarkerTool.isSupportedMaterial(it)) }

        // 2. Copper Chests (all 8 variants)
        assertEquals(8, ContainerMarkerTool.COPPER_CHESTS.size)
        ContainerMarkerTool.COPPER_CHESTS.forEach {
            kotlin.test.assertTrue(ContainerMarkerTool.isSupportedMaterial(it), "$it should be supported")
        }

        // 3. Barrel
        org.bukkit.Material.BARREL.let { kotlin.test.assertTrue(ContainerMarkerTool.isSupportedMaterial(it)) }

        // 4. Shulker Boxes (17 colors)
        assertEquals(17, ContainerMarkerTool.SHULKER_BOXES.size)
        ContainerMarkerTool.SHULKER_BOXES.forEach {
            kotlin.test.assertTrue(ContainerMarkerTool.isSupportedMaterial(it), "$it should be supported")
        }

        // 5. Workstation (Crafting Table)
        org.bukkit.Material.CRAFTING_TABLE.let { kotlin.test.assertTrue(ContainerMarkerTool.isSupportedMaterial(it)) }

        // 6. Furnaces (3 variants)
        assertEquals(3, ContainerMarkerTool.FURNACES.size)
        ContainerMarkerTool.FURNACES.forEach {
            kotlin.test.assertTrue(ContainerMarkerTool.isSupportedMaterial(it), "$it should be supported")
        }

        // 7. Enchanting Table
        org.bukkit.Material.ENCHANTING_TABLE.let { kotlin.test.assertTrue(ContainerMarkerTool.isSupportedMaterial(it)) }

        // 8. Decorated Pot
        org.bukkit.Material.DECORATED_POT.let { kotlin.test.assertTrue(ContainerMarkerTool.isSupportedMaterial(it)) }

        // 9. Bookshelves (2 variants)
        assertEquals(2, ContainerMarkerTool.BOOKSHELVES.size)
        ContainerMarkerTool.BOOKSHELVES.forEach {
            kotlin.test.assertTrue(ContainerMarkerTool.isSupportedMaterial(it), "$it should be supported")
        }

        // 10. Ender Chest
        org.bukkit.Material.ENDER_CHEST.let { kotlin.test.assertTrue(ContainerMarkerTool.isSupportedMaterial(it)) }

        // 11. Armor Stand
        org.bukkit.Material.ARMOR_STAND.let { kotlin.test.assertTrue(ContainerMarkerTool.isSupportedMaterial(it)) }

        // Negative assertions
        kotlin.test.assertFalse(ContainerMarkerTool.isSupportedMaterial(org.bukkit.Material.STONE))
        kotlin.test.assertFalse(ContainerMarkerTool.isSupportedMaterial(org.bukkit.Material.DIRT))
        kotlin.test.assertFalse(ContainerMarkerTool.isSupportedMaterial(org.bukkit.Material.DIAMOND))
    }
}
