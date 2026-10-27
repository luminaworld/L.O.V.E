package com.luminaris.extraction.core.container

import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import kotlin.test.Test
import kotlin.test.assertNotNull

class ContainerHighlightServiceTest {

    @Test
    fun testTransformationCreation() {
        val transformation = Transformation(
            Vector3f(-0.001f, -0.001f, -0.001f),
            AxisAngle4f(),
            Vector3f(1.002f, 1.002f, 1.002f),
            AxisAngle4f()
        )
        assertNotNull(transformation)
    }

    @Test
    fun testNamedTextColorWhite() {
        val color = net.kyori.adventure.text.format.NamedTextColor.WHITE
        assertNotNull(color)
    }

    @Test
    fun testDisplayBrightness() {
        val brightness = org.bukkit.entity.Display.Brightness(15, 15)
        kotlin.test.assertEquals(15, brightness.blockLight)
        kotlin.test.assertEquals(15, brightness.skyLight)
    }

    @Test
    fun testLineOfSightOcclusionLogic() {
        data class V3(val x: Double, val y: Double, val z: Double)
        data class BlockPos(val x: Int, val y: Int, val z: Int)

        fun checkLineOfSight(
            eye: V3,
            target: BlockPos,
            maxDistance: Double,
            hitBlock: BlockPos?
        ): Boolean {
            val targetCenter = V3(target.x + 0.5, target.y + 0.5, target.z + 0.5)
            val dx = targetCenter.x - eye.x
            val dy = targetCenter.y - eye.y
            val dz = targetCenter.z - eye.z
            val distSq = dx * dx + dy * dy + dz * dz
            if (distSq > maxDistance * maxDistance) return false

            // If raycast hit nothing, path is unobstructed
            if (hitBlock == null) return true

            // If raycast hit the container block itself
            if (hitBlock == target) return true

            // A wall / other block was hit
            return false
        }

        val playerEye = V3(10.0, 65.6, 10.0)
        val containerPos = BlockPos(10, 64, 15) // 5 blocks away
        val wallPos = BlockPos(10, 64, 12)     // 2 blocks away, in between

        // Case 1: Wall is in between -> obstructed (cannot see through wall)
        val withWall = checkLineOfSight(playerEye, containerPos, maxDistance = 16.0, hitBlock = wallPos)
        kotlin.test.assertFalse(withWall, "Glow must be hidden when obstructed by wall")

        // Case 2: No wall in between (hit container directly) -> clear line of sight
        val directSight = checkLineOfSight(playerEye, containerPos, maxDistance = 16.0, hitBlock = containerPos)
        kotlin.test.assertTrue(directSight, "Glow must be visible when line of sight is clear")

        // Case 3: Player is out of range (> 16 blocks away)
        val farPlayer = V3(10.0, 65.6, 35.0) // ~19.5 blocks away
        val outOfRange = checkLineOfSight(farPlayer, containerPos, maxDistance = 16.0, hitBlock = null)
        kotlin.test.assertFalse(outOfRange, "Glow must be hidden when out of max distance range")
    }

    @Test
    fun testDoubleChestPartnerOcclusionDetection() {
        data class BlockPos(val x: Int, val y: Int, val z: Int)

        fun isChestVisible(
            hitBlock: BlockPos?,
            mainChest: BlockPos,
            partnerChest: BlockPos?
        ): Boolean {
            if (hitBlock == null) return true
            if (hitBlock == mainChest) return true
            if (partnerChest != null && hitBlock == partnerChest) return true
            return false
        }

        val chest1 = BlockPos(5, 64, 5)
        val chest2 = BlockPos(6, 64, 5)
        val wall = BlockPos(5, 64, 3)

        // Hit main chest directly -> visible
        kotlin.test.assertTrue(isChestVisible(chest1, chest1, chest2))
        // Hit partner chest half -> visible
        kotlin.test.assertTrue(isChestVisible(chest2, chest1, chest2))
        // Hit wall block -> hidden
        kotlin.test.assertFalse(isChestVisible(wall, chest1, chest2))
    }

    @Test
    fun testTopSurfaceRaycastFallback() {
        data class BlockPos(val x: Int, val y: Int, val z: Int)

        // When a low obstacle (e.g. half slab or low barrier) occludes center, top surface fallback rescues line of sight
        fun resolveOcclusion(centerHit: BlockPos?, topHit: BlockPos?, container: BlockPos): Boolean {
            if (centerHit == null || centerHit == container) return true
            if (topHit == null || topHit == container) return true
            return false
        }

        val container = BlockPos(10, 64, 10)
        val lowSlab = BlockPos(10, 64, 9)

        // Center raycast hit low slab, but top raycast hit container -> visible!
        val rescued = resolveOcclusion(centerHit = lowSlab, topHit = container, container = container)
        kotlin.test.assertTrue(rescued, "Top surface fallback should permit line-of-sight if upper container is clear")

        // Both center and top hit wall -> hidden!
        val fullWall = BlockPos(10, 64, 8)
        val fullyHidden = resolveOcclusion(centerHit = fullWall, topHit = fullWall, container = container)
        kotlin.test.assertFalse(fullyHidden, "Solid wall blocking all rays must hide glowing outline")
    }

    @Test
    fun testDoubleChestHelperTracking() {
        val entry = ContainerHighlightService.HighlightEntry(
            x = 10,
            y = 64,
            z = 10,
            categoryId = "loot",
            isArmorStand = false,
            partnerX = 11,
            partnerZ = 10
        )

        val id1 = java.util.UUID.randomUUID()
        val id2 = java.util.UUID.randomUUID()
        entry.helperEntityIds.add(id1)
        entry.helperEntityIds.add(id2)

        // Both halves of double chest have tracked helpers
        kotlin.test.assertEquals(2, entry.helperEntityIds.size)
        kotlin.test.assertTrue(entry.helperEntityIds.contains(id1))
        kotlin.test.assertTrue(entry.helperEntityIds.contains(id2))

        // When cleared, all helper entities are removed
        entry.helperEntityIds.clear()
        kotlin.test.assertTrue(entry.helperEntityIds.isEmpty())
    }

    @Test
    fun testAmbientBrightnessCalculation() {
        // Test function simulating calculateBlockBrightness logic
        fun computeBrightness(
            isOccluding: Boolean,
            directBlock: Int,
            directSky: Int,
            neighborMaxBlock: Int,
            neighborMaxSky: Int,
            fullbright: Boolean
        ): org.bukkit.entity.Display.Brightness {
            if (fullbright) return org.bukkit.entity.Display.Brightness(15, 15)

            var maxBlock = if (!isOccluding) directBlock else 0
            var maxSky = if (!isOccluding) directSky else 0

            maxBlock = maxOf(maxBlock, neighborMaxBlock)
            maxSky = maxOf(maxSky, neighborMaxSky)

            val finalBlock = if (maxBlock > 0 || maxSky > 0) maxOf(maxBlock, 12) else 15
            val finalSky = if (maxSky > 0) maxSky else 15

            return org.bukkit.entity.Display.Brightness(finalBlock.coerceIn(0, 15), finalSky.coerceIn(0, 15))
        }

        // Case 1: Solid block (Barrel / Furnace) in room with torch (light 14) and window (sky 15)
        // Internal light was 0, but neighbor sampling recovers (14, 15)
        val barrelInLitRoom = computeBrightness(
            isOccluding = true,
            directBlock = 0,
            directSky = 0,
            neighborMaxBlock = 14,
            neighborMaxSky = 15,
            fullbright = false
        )
        kotlin.test.assertEquals(14, barrelInLitRoom.blockLight)
        kotlin.test.assertEquals(15, barrelInLitRoom.skyLight)

        // Case 2: Solid block in pitch black cellar (0, 0)
        // Must fallback to minimum 15 so it never renders as pitch black cube
        val barrelInDark = computeBrightness(
            isOccluding = true,
            directBlock = 0,
            directSky = 0,
            neighborMaxBlock = 0,
            neighborMaxSky = 0,
            fullbright = false
        )
        kotlin.test.assertEquals(15, barrelInDark.blockLight)
        kotlin.test.assertEquals(15, barrelInDark.skyLight)

        // Case 3: Fullbright option enabled
        val fullbrightMode = computeBrightness(
            isOccluding = true,
            directBlock = 0,
            directSky = 0,
            neighborMaxBlock = 5,
            neighborMaxSky = 5,
            fullbright = true
        )
        kotlin.test.assertEquals(15, fullbrightMode.blockLight)
        kotlin.test.assertEquals(15, fullbrightMode.skyLight)
    }

    @Test
    fun testArmorStand80PercentOcclusionDetection() {
        data class V3(val x: Double, val y: Double, val z: Double)
        data class BlockPos(val x: Int, val y: Int, val z: Int)

        // Simulates ArmorStand multi-point line of sight check
        fun checkArmorStandVisibility(
            playerEye: V3,
            asPos: BlockPos,
            // Function returning whether a line to target point is blocked by an obstacle
            isRayBlocked: (target: V3) -> Boolean
        ): Boolean {
            val baseX = asPos.x + 0.5
            val baseY = asPos.y.toDouble()
            val baseZ = asPos.z + 0.5

            val samplePoints = listOf(
                V3(baseX, baseY + 1.85, baseZ),        // Head top
                V3(baseX, baseY + 1.60, baseZ),        // Head / Face
                V3(baseX, baseY + 1.30, baseZ),        // Chest center
                V3(baseX + 0.25, baseY + 1.35, baseZ), // Shoulder left
                V3(baseX - 0.25, baseY + 1.35, baseZ), // Shoulder right
                V3(baseX, baseY + 0.95, baseZ),        // Waist
                V3(baseX, baseY + 0.55, baseZ),        // Knees
                V3(baseX, baseY + 0.15, baseZ)         // Feet
            )

            // If ANY sample point has clear line of sight, the armor stand is visible
            return samplePoints.any { pt -> !isRayBlocked(pt) }
        }

        val playerEye = V3(10.0, 65.6, 10.0)
        val armorStandPos = BlockPos(10, 64, 15) // standing on y=64 (occupies 64.0..65.975)

        // Scenario 1: A 1.5-block obstacle (e.g. wall/fence/table) blocks the lower 80% (y <= 65.5)
        // All points below 65.5 are blocked; only Head (1.60 -> 65.6) and Head top (1.85 -> 65.85) are visible!
        val lower80PercentBlocked = checkArmorStandVisibility(playerEye, armorStandPos) { target ->
            target.y <= 65.5 // lower 80% blocked
        }
        kotlin.test.assertTrue(
            lower80PercentBlocked,
            "ArmorStand must remain visible when lower 80% is occluded and only head/helmet is clear"
        )

        // Scenario 2: Vertical obstacle (pillar/corner) blocks center column, but right shoulder (x+0.25) is clear
        val horizontalCornerOccluded = checkArmorStandVisibility(playerEye, armorStandPos) { target ->
            target.x <= 10.6 // blocks center (10.5) and left, but right (10.75) is clear
        }
        kotlin.test.assertTrue(
            horizontalCornerOccluded,
            "ArmorStand must remain visible when peeking out past a vertical pillar or corner"
        )

        // Scenario 3: 100% solid wall between player and armor stand (in another room)
        val completelyHiddenBehindWall = checkArmorStandVisibility(playerEye, armorStandPos) { _ ->
            true // all rays blocked by solid wall
        }
        kotlin.test.assertFalse(
            completelyHiddenBehindWall,
            "ArmorStand must be hidden when 100% occluded behind a solid wall"
        )
    }
}





