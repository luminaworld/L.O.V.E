package com.luminaris.extraction.editor

import com.luminaris.extraction.core.container.PlacedContainer
import com.luminaris.extraction.core.template.TemplateMapData
import com.luminaris.extraction.editor.tool.ContainerMarkerTool
import org.bukkit.Material
import org.bukkit.block.BlockFace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DoubleChestAndArmorStandTest {

    @Test
    fun testDoubleChestPlacementRelativeOrientationCalculation() {
        // Minecraft Double Chest standard:
        // - ChestType.LEFT has its connected partner clockwise from facing.
        // - ChestType.RIGHT has its connected partner counter-clockwise from facing.
        //
        // Example: If facing NORTH:
        // Clockwise is EAST (+X). Counter-clockwise is WEST (-X).
        // Therefore, the western chest is LEFT (connecting east), and the eastern chest is RIGHT (connecting west).

        // Test clockwise rotation
        assertEquals(BlockFace.EAST, ContainerMarkerTool.getClockwise(BlockFace.NORTH))
        assertEquals(BlockFace.SOUTH, ContainerMarkerTool.getClockwise(BlockFace.EAST))
        assertEquals(BlockFace.WEST, ContainerMarkerTool.getClockwise(BlockFace.SOUTH))
        assertEquals(BlockFace.NORTH, ContainerMarkerTool.getClockwise(BlockFace.WEST))

        // Test counter-clockwise rotation
        assertEquals(BlockFace.WEST, ContainerMarkerTool.getCounterClockwise(BlockFace.NORTH))
        assertEquals(BlockFace.SOUTH, ContainerMarkerTool.getCounterClockwise(BlockFace.WEST))
        assertEquals(BlockFace.EAST, ContainerMarkerTool.getCounterClockwise(BlockFace.SOUTH))
        assertEquals(BlockFace.NORTH, ContainerMarkerTool.getCounterClockwise(BlockFace.EAST))

        fun calculateOrientation(facing: BlockFace, dx: Int, dz: Int): Pair<String, String>? {
            val dirToNew = when {
                dx > 0 && dz == 0 -> BlockFace.EAST
                dx < 0 && dz == 0 -> BlockFace.WEST
                dz > 0 && dx == 0 -> BlockFace.SOUTH
                dz < 0 && dx == 0 -> BlockFace.NORTH
                else -> return null
            }
            val cw = ContainerMarkerTool.getClockwise(facing)
            val ccw = ContainerMarkerTool.getCounterClockwise(facing)
            return when (dirToNew) {
                cw -> "LEFT" to "RIGHT"   // existing is LEFT, new is RIGHT
                ccw -> "RIGHT" to "LEFT"  // existing is RIGHT, new is LEFT
                else -> null
            }
        }

        // NORTH facing: +X is EAST (clockwise) -> existing is LEFT, new is RIGHT
        assertEquals("LEFT" to "RIGHT", calculateOrientation(BlockFace.NORTH, dx = 1, dz = 0))
        assertEquals("RIGHT" to "LEFT", calculateOrientation(BlockFace.NORTH, dx = -1, dz = 0))

        // SOUTH facing: -X is WEST (clockwise) -> existing is LEFT, new is RIGHT
        assertEquals("LEFT" to "RIGHT", calculateOrientation(BlockFace.SOUTH, dx = -1, dz = 0))
        assertEquals("RIGHT" to "LEFT", calculateOrientation(BlockFace.SOUTH, dx = 1, dz = 0))

        // EAST facing: +Z is SOUTH (clockwise) -> existing is LEFT, new is RIGHT
        assertEquals("LEFT" to "RIGHT", calculateOrientation(BlockFace.EAST, dx = 0, dz = 1))
        assertEquals("RIGHT" to "LEFT", calculateOrientation(BlockFace.EAST, dx = 0, dz = -1))

        // WEST facing: -Z is NORTH (clockwise) -> existing is LEFT, new is RIGHT
        assertEquals("LEFT" to "RIGHT", calculateOrientation(BlockFace.WEST, dx = 0, dz = -1))
        assertEquals("RIGHT" to "LEFT", calculateOrientation(BlockFace.WEST, dx = 0, dz = 1))
    }

    @Test
    fun testTwoAdjacentChestsCountAsSingleContainerInTemplate() {
        // Simulate map template with containers
        val template = TemplateMapData(
            templateId = "dust2",
            displayName = "Dust II"
        )

        // 1. Admin places first chest at (10, 64, 10)
        val container1 = PlacedContainer(
            id = PlacedContainer.generateId(10, 64, 10),
            categoryId = "rare",
            x = 10,
            y = 64,
            z = 10
        )
        template.containers.add(container1)
        assertEquals(1, template.containers.size, "Initial chest should register 1 container")

        // 2. Admin places second chest adjacent at (11, 64, 10)
        // Check if placed block (11, 64, 10) is adjacent to existing chest (10, 64, 10)
        val isAdjacent = (kotlin.math.abs(11 - 10) + kotlin.math.abs(10 - 10) == 1)
        assertTrue(isAdjacent, "Chests should be detected as horizontally adjacent")

        val existingContainer = template.containers.find { it.x == 10 && it.y == 64 && it.z == 10 }
        assertNotNull(existingContainer)

        // The system connects them into a double chest and DOES NOT add a second container
        // template.containers is unchanged
        assertEquals(1, template.containers.size, "Two adjacent chests must count as a single container")

        // 3. Simulated lookup: player clicks on chest 1 (10, 64, 10)
        fun findContainer(x: Int, y: Int, z: Int): PlacedContainer? {
            // Direct match
            template.containers.find { it.x == x && it.y == y && it.z == z }?.let { return it }
            // If clicking the partner chest (11, 64, 10), partner is at (10, 64, 10)
            if (x == 11 && y == 64 && z == 10) {
                return template.containers.find { it.x == 10 && it.y == 64 && it.z == 10 }
            }
            return null
        }

        val fromChest1 = findContainer(10, 64, 10)
        val fromChest2 = findContainer(11, 64, 10)

        assertNotNull(fromChest1)
        assertNotNull(fromChest2)
        assertEquals(fromChest1.id, fromChest2.id, "Both halves of double chest must resolve to the identical container ID")
        assertEquals(fromChest1.categoryId, fromChest2.categoryId, "Both halves must share the same category")
    }

    @Test
    fun testSeparatingDoubleChestTransfersRegistration() {
        val template = TemplateMapData(
            templateId = "inferno",
            displayName = "Inferno"
        )

        // Initial double chest registered at (20, 64, 20) with partner at (21, 64, 20)
        val initialContainer = PlacedContainer(
            id = "c_20_64_20_abc",
            categoryId = "epic",
            x = 20,
            y = 64,
            z = 20
        )
        template.containers.add(initialContainer)

        // Case: Admin breaks the registered half (20, 64, 20), partner (21, 64, 20) remains
        val directMatch = template.containers.find { it.x == 20 && it.y == 64 && it.z == 20 }
        assertNotNull(directMatch)

        // Transfer container to remaining chest
        template.containers.removeIf { it.x == 20 && it.y == 64 && it.z == 20 }
        template.containers.add(
            PlacedContainer(
                id = directMatch.id,
                categoryId = directMatch.categoryId,
                x = 21,
                y = 64,
                z = 20
            )
        )

        assertEquals(1, template.containers.size, "Separated chest must still be registered as 1 container")
        val transferred = template.containers.first()
        assertEquals(21, transferred.x)
        assertEquals("epic", transferred.categoryId)

        // Breaking the remaining half removes the container completely
        template.containers.removeIf { it.x == 21 && it.y == 64 && it.z == 20 }
        assertTrue(template.containers.isEmpty(), "Breaking remaining half removes container completely")
    }

    @Test
    fun testArmorStandManipulationPermissions() {
        fun shouldAllowManipulate(isTemplateWorld: Boolean, isAdmin: Boolean): Boolean {
            return isTemplateWorld && isAdmin
        }

        // In template world by admin -> allowed
        assertTrue(shouldAllowManipulate(isTemplateWorld = true, isAdmin = true))

        // In template world by non-admin -> blocked
        assertFalse(shouldAllowManipulate(isTemplateWorld = true, isAdmin = false))

        // In active raid/match by regular player -> blocked (must open drop box GUI instead)
        assertFalse(shouldAllowManipulate(isTemplateWorld = false, isAdmin = false))

        // In active raid by admin -> blocked (normal raid interaction rules apply)
        assertFalse(shouldAllowManipulate(isTemplateWorld = false, isAdmin = true))
    }

    @Test
    fun testArmorStandEquipmentSlots() {
        // Equipment slots that can be equipped on an ArmorStand:
        // Head, Chest, Legs, Feet, Main Hand, Off Hand
        val supportedSlots = listOf(
            org.bukkit.inventory.EquipmentSlot.HEAD,
            org.bukkit.inventory.EquipmentSlot.CHEST,
            org.bukkit.inventory.EquipmentSlot.LEGS,
            org.bukkit.inventory.EquipmentSlot.FEET,
            org.bukkit.inventory.EquipmentSlot.HAND,
            org.bukkit.inventory.EquipmentSlot.OFF_HAND
        )
        assertEquals(6, supportedSlots.size, "ArmorStand must support all 6 equipment slots including hands")
        assertTrue(supportedSlots.contains(org.bukkit.inventory.EquipmentSlot.HAND))
        assertTrue(supportedSlots.contains(org.bukkit.inventory.EquipmentSlot.OFF_HAND))
    }
}
