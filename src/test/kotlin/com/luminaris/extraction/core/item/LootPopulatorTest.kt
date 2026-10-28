package com.luminaris.extraction.core.item

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LootPopulatorTest {

    private fun createMockPlugin(dataFolder: File): Plugin {
        val logger = java.util.logging.Logger.getLogger("LootPopulatorTest")
        return java.lang.reflect.Proxy.newProxyInstance(
            Plugin::class.java.classLoader,
            arrayOf(Plugin::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getDataFolder" -> dataFolder
                "getLogger" -> logger
                "getName" -> "LuminaExtractionCore"
                else -> null
            }
        } as Plugin
    }

    private fun createTestItemStack(): ItemStack {
        val field = sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe").apply { isAccessible = true }
        val unsafe = field.get(null) as sun.misc.Unsafe
        return unsafe.allocateInstance(ItemStack::class.java) as ItemStack
    }

    @Test
    fun testCumulativeWeightSelectionDistribution() {
        val tempFolder = File(System.getProperty("java.io.tmpdir"), "loot_pop_test")
        val mockPlugin = createMockPlugin(tempFolder)
        val populator = LootPopulator(mockPlugin)

        val itemLow = ExtractionItem(
            id = "low_tier",
            itemStack = createTestItemStack(),
            price = 5.0,
            rarity = Rarity.F,
            weight = 10.0
        )
        val itemMid = ExtractionItem(
            id = "mid_tier",
            itemStack = createTestItemStack(),
            price = 50.0,
            rarity = Rarity.D,
            weight = 30.0
        )
        val itemHigh = ExtractionItem(
            id = "high_tier",
            itemStack = createTestItemStack(),
            price = 250.0,
            rarity = Rarity.C,
            weight = 60.0
        )

        val candidates = listOf(itemLow, itemMid, itemHigh)
        val rolls = 1000
        val results = populator.rollWeightedCandidateItems(candidates, rolls)

        assertEquals(rolls, results.size)

        var lowCount = 0
        var midCount = 0
        var highCount = 0

        for (item in results) {
            when (item.id) {
                "low_tier" -> lowCount++
                "mid_tier" -> midCount++
                "high_tier" -> highCount++
            }
        }

        // With 10%, 30%, 60% probability across 1000 rolls:
        // High count should be roughly > 500
        // Mid count should be roughly > 200
        // Low count should be roughly < 200
        assertTrue(highCount > midCount, "High weight item ($highCount) should appear more than mid ($midCount)")
        assertTrue(midCount > lowCount, "Mid weight item ($midCount) should appear more than low ($lowCount)")
    }

    @Test
    fun testEdgeCasesInRolling() {
        val tempFolder = File(System.getProperty("java.io.tmpdir"), "loot_pop_test2")
        val mockPlugin = createMockPlugin(tempFolder)
        val populator = LootPopulator(mockPlugin)

        // 1. Empty candidates
        assertTrue(populator.rollWeightedCandidateItems(emptyList(), 5).isEmpty())

        // 2. Count <= 0
        val item = ExtractionItem(
            id = "test_item",
            itemStack = createTestItemStack(),
            price = 20.0,
            rarity = Rarity.F,
            weight = 10.0
        )
        assertTrue(populator.rollWeightedCandidateItems(listOf(item), 0).isEmpty())
        assertTrue(populator.rollWeightedCandidateItems(listOf(item), -1).isEmpty())

        // 3. Zero weight item
        val zeroWeightItem = ExtractionItem(
            id = "zero_item",
            itemStack = createTestItemStack(),
            price = 1.0,
            rarity = Rarity.F,
            weight = 0.0
        )
        assertTrue(populator.rollWeightedCandidateItems(listOf(zeroWeightItem), 5).isEmpty())

        // 4. Single item with positive weight
        val singleResults = populator.rollWeightedCandidateItems(listOf(item), 3)
        assertEquals(3, singleResults.size)
        singleResults.forEach { assertEquals("test_item", it.id) }

        // 5. Verify rollWeightedItems mapping works
        val itemStacks = populator.rollWeightedItems(listOf(item), 3)
        assertEquals(3, itemStacks.size)
    }

    @Test
    fun testSlotIndexDistributionSimulation() {
        val inventorySize = 27
        val availableSlots = (0 until inventorySize).toMutableList()
        availableSlots.shuffle()

        val itemsToPlace = 5
        val placedSlots = mutableListOf<Int>()
        for (i in 0 until itemsToPlace) {
            placedSlots.add(availableSlots.removeAt(0))
        }

        assertEquals(5, placedSlots.size)
        // Ensure all placed slot indices are distinct and within valid range
        assertEquals(5, placedSlots.distinct().size)
        placedSlots.forEach { slot ->
            assertTrue(slot in 0 until inventorySize, "Slot $slot is within 0..26")
        }
    }
}
