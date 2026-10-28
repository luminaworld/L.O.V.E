package com.luminaris.extraction.editor

import com.luminaris.extraction.core.container.ContainerCategory
import com.luminaris.extraction.core.item.ExtractionItem
import com.luminaris.extraction.core.item.LootPopulator
import com.luminaris.extraction.core.item.Rarity
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.io.File
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExtractionEditorTest {

    private fun createBareItem(): ItemStack {
        val field = sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe").apply { isAccessible = true }
        val unsafe = field.get(null) as sun.misc.Unsafe
        return unsafe.allocateInstance(ItemStack::class.java) as ItemStack
    }

    private fun createMockPlugin(): Plugin {
        val tempFolder = File(System.getProperty("java.io.tmpdir"), "editor_test")
        val logger = java.util.logging.Logger.getLogger("ExtractionEditorTest")
        return Proxy.newProxyInstance(
            Plugin::class.java.classLoader,
            arrayOf(Plugin::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getDataFolder" -> tempFolder
                "getLogger" -> logger
                "getName" -> "LuminaExtractionCore"
                else -> null
            }
        } as Plugin
    }

    @Test
    fun testContainerCategoryRarityWeightsAndPercentages() {
        val category = ContainerCategory(
            id = "test_crate",
            displayName = Component.text("Test Crate"),
            minRolls = 2,
            maxRolls = 5
        )

        // Set custom weights
        category.setRarityWeight(Rarity.F, 70.0)
        category.setRarityWeight(Rarity.D, 20.0)
        category.setRarityWeight(Rarity.C, 10.0)
        category.setRarityWeight(Rarity.B, 0.0)
        category.setRarityWeight(Rarity.A, 0.0)
        category.setRarityWeight(Rarity.S, 0.0)

        assertEquals(70.0, category.getRarityPercentage(Rarity.F), 0.01)
        assertEquals(20.0, category.getRarityPercentage(Rarity.D), 0.01)
        assertEquals(10.0, category.getRarityPercentage(Rarity.C), 0.01)
        assertEquals(0.0, category.getRarityPercentage(Rarity.B), 0.01)
        assertEquals(0.0, category.getRarityPercentage(Rarity.A), 0.01)
        assertEquals(0.0, category.getRarityPercentage(Rarity.S), 0.01)

        // Test default presets
        val normalDefaults = ContainerCategory.defaultRarityWeights("normal_crate")
        assertTrue(normalDefaults[Rarity.F]!! > normalDefaults[Rarity.C]!!)

        val airdropDefaults = ContainerCategory.defaultRarityWeights("airdrop")
        assertTrue(airdropDefaults[Rarity.C]!! > airdropDefaults[Rarity.F]!!)

        val safeDefaults = ContainerCategory.defaultRarityWeights("safe_box")
        assertTrue(safeDefaults[Rarity.B]!! > safeDefaults[Rarity.F]!!)
    }

    @Test
    fun testContainerDrivenRarityLootRolls() {
        val mockPlugin = createMockPlugin()
        val populator = LootPopulator(mockPlugin)

        val category = ContainerCategory(
            id = "airdrop_crate",
            displayName = Component.text("Airdrop Crate"),
            minRolls = 3,
            maxRolls = 6
        )

        // Only C (80%) and A (20%) drop in this crate
        category.setRarityWeight(Rarity.F, 0.0)
        category.setRarityWeight(Rarity.D, 0.0)
        category.setRarityWeight(Rarity.C, 80.0)
        category.setRarityWeight(Rarity.B, 0.0)
        category.setRarityWeight(Rarity.A, 20.0)
        category.setRarityWeight(Rarity.S, 0.0)

        val commonItem = ExtractionItem("junk", createBareItem(), 10.0, Rarity.F, 100.0)
        val rareItem = ExtractionItem("rifle", createBareItem(), 500.0, Rarity.C, 50.0)
        val legItem = ExtractionItem("keycard", createBareItem(), 5000.0, Rarity.A, 10.0)

        val candidates = listOf(commonItem, rareItem, legItem)

        val rolls = 1000
        val results = populator.rollCategoryCandidateItems(category, candidates, rolls)

        assertEquals(rolls, results.size)

        val commonCount = results.count { it.rarity == Rarity.F }
        val rareCount = results.count { it.rarity == Rarity.C }
        val legCount = results.count { it.rarity == Rarity.A }

        // Common weight is 0.0, so no common items should be selected
        assertEquals(0, commonCount, "Items with 0% rarity chance in container must never drop")
        assertTrue(rareCount > legCount, "Rare items (80%) must significantly exceed Legendary items (20%)")
        assertTrue(rareCount in 700..900, "Rare count ($rareCount) expected around 800")
        assertTrue(legCount in 100..300, "Legendary count ($legCount) expected around 200")
    }

    @Test
    fun testGracefulFallbackWhenRolledRarityHasNoRegisteredItems() {
        val mockPlugin = createMockPlugin()
        val populator = LootPopulator(mockPlugin)

        val category = ContainerCategory(
            id = "safe_crate",
            displayName = Component.text("Safe Crate"),
            minRolls = 1,
            maxRolls = 3
        )
        // Crate wants A 100%
        category.setRarityWeight(Rarity.F, 0.0)
        category.setRarityWeight(Rarity.D, 0.0)
        category.setRarityWeight(Rarity.C, 0.0)
        category.setRarityWeight(Rarity.B, 0.0)
        category.setRarityWeight(Rarity.A, 100.0)
        category.setRarityWeight(Rarity.S, 0.0)

        // But admin hasn't registered any A item yet, only C
        val rareItem = ExtractionItem("rare_gold", createBareItem(), 600.0, Rarity.C, 20.0)
        val candidates = listOf(rareItem)

        // Must gracefully fall back to existing candidate rather than producing empty container
        val results = populator.rollCategoryCandidateItems(category, candidates, 5)
        assertEquals(5, results.size)
        results.forEach { assertEquals("rare_gold", it.id) }
    }

    @Test
    fun testWeightOver1000HandledSafely() {
        val category = ContainerCategory(
            id = "custom_crate",
            displayName = Component.text("Custom Crate"),
            minRolls = 1,
            maxRolls = 3
        )

        for (r in Rarity.entries) {
            category.setRarityWeight(r, 0.0)
        }
        // Set weight over 1000.0 (like in the reported bug with 1020.0)
        category.setRarityWeight(Rarity.A, 1020.0)

        // Percentage must be safely computed without any bound exception
        val percentage = category.getRarityPercentage(Rarity.A)
        assertEquals(100.0, percentage, 0.001)

        val commonPct = category.getRarityPercentage(Rarity.F)
        assertEquals(0.0, commonPct, 0.001)
    }

    @Test
    fun testAdjustRarityPercentageAutoBalancesToExact100() {
        val category = ContainerCategory(
            id = "test_balance",
            displayName = Component.text("Test Balance"),
            minRolls = 1,
            maxRolls = 3
        )
        // Default normal_crate: F 70, D 25, C 5, B 0, A 0, S 0
        assertEquals(100.0, Rarity.entries.sumOf { category.getRarityPercentage(it) }, 0.001)

        // Adjust B by +5%
        category.adjustRarityPercentage(Rarity.B, 5.0)
        assertEquals(5.0, category.getRarityPercentage(Rarity.B), 0.1)
        assertEquals(100.0, Rarity.entries.sumOf { category.getRarityPercentage(it) }, 0.001)

        // Adjust B by +1%
        category.adjustRarityPercentage(Rarity.B, 1.0)
        assertEquals(6.0, category.getRarityPercentage(Rarity.B), 0.1)
        assertEquals(100.0, Rarity.entries.sumOf { category.getRarityPercentage(it) }, 0.001)

        // Adjust F by -5%
        category.adjustRarityPercentage(Rarity.F, -5.0)
        assertEquals(100.0, Rarity.entries.sumOf { category.getRarityPercentage(it) }, 0.001)

        // Adjust C by -1%
        category.adjustRarityPercentage(Rarity.C, -1.0)
        assertEquals(100.0, Rarity.entries.sumOf { category.getRarityPercentage(it) }, 0.001)

        // Repeated adjustments of +1
        for (i in 1..20) {
            category.adjustRarityPercentage(Rarity.A, 1.0)
            assertEquals(100.0, Rarity.entries.sumOf { category.getRarityPercentage(it) }, 0.001)
        }
    }
}


