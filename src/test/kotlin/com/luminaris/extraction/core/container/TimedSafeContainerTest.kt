package com.luminaris.extraction.core.container

import com.luminaris.extraction.editor.tool.SafeSetupTool
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimedSafeContainerTest {

    @Test
    fun testSafeSetupToolSupportedFaces() {
        // Horizontal faces must be supported
        assertTrue(SafeSetupTool.isSupportedFace(BlockFace.NORTH))
        assertTrue(SafeSetupTool.isSupportedFace(BlockFace.SOUTH))
        assertTrue(SafeSetupTool.isSupportedFace(BlockFace.WEST))
        assertTrue(SafeSetupTool.isSupportedFace(BlockFace.EAST))

        // Non-horizontal faces must be rejected
        assertFalse(SafeSetupTool.isSupportedFace(BlockFace.UP))
        assertFalse(SafeSetupTool.isSupportedFace(BlockFace.DOWN))
        assertFalse(SafeSetupTool.isSupportedFace(BlockFace.NORTH_EAST))
        assertFalse(SafeSetupTool.isSupportedFace(BlockFace.SELF))
    }

    @Test
    fun testPlacedContainerDisplayFace() {
        val containerWithFace = PlacedContainer(
            id = "safe_1",
            categoryId = "safe_box",
            x = 100,
            y = 64,
            z = 200,
            displayFace = "NORTH"
        )
        assertEquals("NORTH", containerWithFace.displayFace)

        val containerWithoutFace = PlacedContainer(
            id = "normal_1",
            categoryId = "normal_crate",
            x = 101,
            y = 64,
            z = 201
        )
        assertEquals(null, containerWithoutFace.displayFace)
    }

    @Test
    fun testContainerCategorySafeConfiguration() {
        val safeCat = ContainerCategory(
            id = "safe_box",
            displayName = Component.text("Armored Safe"),
            minRolls = 1,
            maxRolls = 3,
            timedUnlockEnabled = true,
            unlockCountdownSeconds = 30,
            unlockTickSound = "BLOCK_NOTE_BLOCK_HAT",
            unlockFinishSound = "BLOCK_IRON_DOOR_OPEN"
        )

        assertTrue(safeCat.timedUnlockEnabled)
        assertEquals(30, safeCat.unlockCountdownSeconds)
        assertEquals("BLOCK_NOTE_BLOCK_HAT", safeCat.unlockTickSound)
        assertEquals("BLOCK_IRON_DOOR_OPEN", safeCat.unlockFinishSound)

        val normalCat = ContainerCategory(
            id = "normal_crate",
            displayName = Component.text("Normal Crate"),
            minRolls = 2,
            maxRolls = 4
        )

        assertFalse(normalCat.timedUnlockEnabled)
        assertEquals(0, normalCat.unlockCountdownSeconds)
    }

    @Test
    fun testSideMountedDisplayOffsetAndYawCalculation() {
        // Simulated math matching TimedSafeManager.calculateDisplayLocation
        fun calculateOffsetAndYaw(x: Int, y: Int, z: Int, face: BlockFace): Triple<Double, Double, Float> {
            val centerX = x + 0.5
            val centerZ = z + 0.5
            val offset = 0.53

            return when (face) {
                BlockFace.NORTH -> Triple(centerX, centerZ - offset, 180.0f)
                BlockFace.SOUTH -> Triple(centerX, centerZ + offset, 0.0f)
                BlockFace.WEST -> Triple(centerX - offset, centerZ, 90.0f)
                BlockFace.EAST -> Triple(centerX + offset, centerZ, 270.0f)
                else -> Triple(centerX, centerZ - offset, 180.0f)
            }
        }

        val x = 10
        val y = 64
        val z = 20

        // 1. NORTH: Mounted on -Z surface, faces NORTH (180 deg)
        val north = calculateOffsetAndYaw(x, y, z, BlockFace.NORTH)
        assertEquals(10.5, north.first)
        assertTrue(abs((20.5 - 0.53) - north.second) < 0.0001)
        assertEquals(180.0f, north.third)

        // 2. SOUTH: Mounted on +Z surface, faces SOUTH (0 deg)
        val south = calculateOffsetAndYaw(x, y, z, BlockFace.SOUTH)
        assertEquals(10.5, south.first)
        assertTrue(abs((20.5 + 0.53) - south.second) < 0.0001)
        assertEquals(0.0f, south.third)

        // 3. WEST: Mounted on -X surface, faces WEST (90 deg)
        val west = calculateOffsetAndYaw(x, y, z, BlockFace.WEST)
        assertTrue(abs((10.5 - 0.53) - west.first) < 0.0001)
        assertEquals(20.5, west.second)
        assertEquals(90.0f, west.third)

        // 4. EAST: Mounted on +X surface, faces EAST (270 deg)
        val east = calculateOffsetAndYaw(x, y, z, BlockFace.EAST)
        assertTrue(abs((10.5 + 0.53) - east.first) < 0.0001)
        assertEquals(20.5, east.second)
        assertEquals(270.0f, east.third)
    }

    @Test
    fun testProgressBarCalculation() {
        fun formatProgressBar(remaining: Int, total: Int, length: Int = 10): Pair<Int, Int> {
            if (total <= 0) return Pair(length, 0)
            val elapsed = (total - remaining).coerceAtLeast(0)
            val progressPct = (elapsed.toDouble() / total.toDouble()).coerceIn(0.0, 1.0)
            val filled = (progressPct * length).toInt().coerceIn(0, length)
            val empty = length - filled
            return Pair(filled, empty)
        }

        val total = 30

        // At start (0 seconds elapsed) -> 0 filled, 10 empty
        val start = formatProgressBar(remaining = 30, total = total)
        assertEquals(0, start.first)
        assertEquals(10, start.second)

        // At midpoint (15 seconds remaining) -> 5 filled, 5 empty
        val mid = formatProgressBar(remaining = 15, total = total)
        assertEquals(5, mid.first)
        assertEquals(5, mid.second)

        // At finish (0 seconds remaining) -> 10 filled, 0 empty
        val finish = formatProgressBar(remaining = 0, total = total)
        assertEquals(10, finish.first)
        assertEquals(0, finish.second)
    }

    @Test
    fun testSimulationInteractionStateResolution() {
        val safeCategory = ContainerCategory(
            id = "safe_box",
            displayName = Component.text("Safe"),
            minRolls = 1,
            maxRolls = 2,
            timedUnlockEnabled = true,
            unlockCountdownSeconds = 30
        )
        val normalCategory = ContainerCategory(
            id = "normal",
            displayName = Component.text("Normal"),
            minRolls = 1,
            maxRolls = 2,
            timedUnlockEnabled = false,
            unlockCountdownSeconds = 0
        )

        fun isSafe(cat: ContainerCategory): Boolean = cat.timedUnlockEnabled && cat.unlockCountdownSeconds > 0

        assertTrue(isSafe(safeCategory))
        assertFalse(isSafe(normalCategory))

        // Simulated state machine in template simulation
        var isUnlocked = false
        var isCountingDown = false

        // 1. First interaction: start countdown
        val action1 = if (isSafe(safeCategory)) {
            if (!isUnlocked) {
                if (isCountingDown) "SHOW_TIME" else { isCountingDown = true; "START_COUNTDOWN" }
            } else "OPEN_SIMULATION"
        } else "OPEN_SIMULATION"

        assertEquals("START_COUNTDOWN", action1)
        assertTrue(isCountingDown)
        assertFalse(isUnlocked)

        // 2. Second interaction while counting down: show remaining time
        val action2 = if (isSafe(safeCategory)) {
            if (!isUnlocked) {
                if (isCountingDown) "SHOW_TIME" else { isCountingDown = true; "START_COUNTDOWN" }
            } else "OPEN_SIMULATION"
        } else "OPEN_SIMULATION"

        assertEquals("SHOW_TIME", action2)

        // 3. Countdown finishes
        isCountingDown = false
        isUnlocked = true

        // 4. Third interaction when unlocked: open simulation GUI!
        val action3 = if (isSafe(safeCategory)) {
            if (!isUnlocked) {
                if (isCountingDown) "SHOW_TIME" else { isCountingDown = true; "START_COUNTDOWN" }
            } else "OPEN_SIMULATION"
        } else "OPEN_SIMULATION"
        assertEquals("OPEN_SIMULATION", action3)
    }

    @Test
    fun testIsSafeContainerResolution() {
        fun isSafe(container: PlacedContainer?, cat: ContainerCategory): Boolean {
            if (!container?.displayFace.isNullOrBlank()) return true
            if (cat.timedUnlockEnabled) return true
            if (cat.id.contains("safe", ignoreCase = true)) return true
            return false
        }

        fun getSeconds(cat: ContainerCategory): Int {
            if (cat.unlockCountdownSeconds > 0) return cat.unlockCountdownSeconds
            return 30
        }

        val normalCat = ContainerCategory(id = "crate", displayName = Component.text("Crate"), minRolls = 1, maxRolls = 2)
        val safeCatOld = ContainerCategory(id = "safe_box", displayName = Component.text("Old Safe"), minRolls = 1, maxRolls = 2, timedUnlockEnabled = false, unlockCountdownSeconds = 0)
        val containerWithFace = PlacedContainer(id = "c1", categoryId = "crate", x = 1, y = 2, z = 3, displayFace = "NORTH")
        val containerWithoutFace = PlacedContainer(id = "c2", categoryId = "crate", x = 1, y = 2, z = 3, displayFace = null)

        // 1. Box with displayFace set via SafeSetupTool is ALWAYS safe
        assertTrue(isSafe(containerWithFace, normalCat))

        // 2. Category named safe_box is ALWAYS safe even if YAML had 0 seconds/false
        assertTrue(isSafe(containerWithoutFace, safeCatOld))
        assertEquals(30, getSeconds(safeCatOld))

        // 3. Normal crate without face is NOT safe
        assertFalse(isSafe(containerWithoutFace, normalCat))
    }

    @Test
    fun testYamlAutoUpdateLogic() {
        val yaml = org.bukkit.configuration.file.YamlConfiguration()
        val catSec = yaml.createSection("categories").createSection("safe_box")
        catSec.set("display_name", "Safe")

        assertFalse(catSec.contains("timed_unlock_enabled"))
        assertFalse(catSec.contains("unlock_countdown_seconds"))

        // Simulates ContainerRegistry auto-update logic
        var needsSave = false
        val key = "safe_box"
        val isDefaultSafe = key.equals("safe_box", ignoreCase = true) || key.contains("safe", ignoreCase = true)

        if (!catSec.contains("timed_unlock_enabled")) {
            catSec.set("timed_unlock_enabled", isDefaultSafe)
            needsSave = true
        }
        if (!catSec.contains("unlock_countdown_seconds")) {
            catSec.set("unlock_countdown_seconds", if (isDefaultSafe) 30 else 0)
            needsSave = true
        }
        if (!catSec.contains("unlock_tick_sound")) {
            catSec.set("unlock_tick_sound", "BLOCK_NOTE_BLOCK_HAT")
            needsSave = true
        }
        if (!catSec.contains("unlock_finish_sound")) {
            catSec.set("unlock_finish_sound", "BLOCK_IRON_DOOR_OPEN")
            needsSave = true
        }

        assertTrue(needsSave)
        assertTrue(catSec.getBoolean("timed_unlock_enabled"))
        assertEquals(30, catSec.getInt("unlock_countdown_seconds"))
        assertEquals("BLOCK_NOTE_BLOCK_HAT", catSec.getString("unlock_tick_sound"))
        assertEquals("BLOCK_IRON_DOOR_OPEN", catSec.getString("unlock_finish_sound"))
    }

    @Test
    fun testSimulationResetAndBreakCleanup() {
        val activeUnlocks = mutableMapOf<String, String>()
        val unlockedBoxes = mutableSetOf<String>()
        val displays = mutableMapOf<String, String>()

        val key = "world:10_64_20"
        activeUnlocks[key] = "session"
        unlockedBoxes.add(key)
        displays[key] = "entity-uuid"

        fun resetContainer(k: String): Boolean {
            var changed = false
            if (activeUnlocks.remove(k) != null) changed = true
            if (unlockedBoxes.remove(k)) changed = true
            if (displays.remove(k) != null) changed = true
            return changed
        }

        assertTrue(unlockedBoxes.contains(key))
        assertTrue(displays.containsKey(key))

        // When block broken or simulation opened, reset must wipe all state
        val reset = resetContainer(key)
        assertTrue(reset)
        assertFalse(unlockedBoxes.contains(key))
        assertFalse(displays.containsKey(key))
        assertFalse(activeUnlocks.containsKey(key))
    }
}

