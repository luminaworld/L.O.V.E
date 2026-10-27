package com.luminaris.extraction.core.container

import com.luminaris.extraction.core.item.ExtractionItem
import com.luminaris.extraction.core.item.Rarity
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DropBoxSimulationTest {

    private fun createBareItem(): ItemStack {
        val field = sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe").apply { isAccessible = true }
        val unsafe = field.get(null) as sun.misc.Unsafe
        return unsafe.allocateInstance(ItemStack::class.java) as ItemStack
    }

    @Test
    fun testDefaultSearchTimes() {
        assertEquals(1, ExtractionItem.defaultSearchTime(Rarity.COMMON))
        assertEquals(2, ExtractionItem.defaultSearchTime(Rarity.UNCOMMON))
        assertEquals(3, ExtractionItem.defaultSearchTime(Rarity.RARE))
        assertEquals(4, ExtractionItem.defaultSearchTime(Rarity.EPIC))
        assertEquals(5, ExtractionItem.defaultSearchTime(Rarity.LEGENDARY))
    }

    @Test
    fun testItemSearchTimeOverridesRarity() {
        val dummyStack = createBareItem()
        // Legendary item configured to search in only 2 seconds instead of default 5
        val fastLegendary = ExtractionItem(
            id = "fast_gem",
            itemStack = dummyStack,
            rarity = Rarity.LEGENDARY,
            searchTime = 2,
            revealSound = "BLOCK_AMETHYST_BLOCK_CHIME"
        )
        assertEquals(2, fastLegendary.searchTime)

        // Common item configured to search in 4 seconds
        val slowCommon = ExtractionItem(
            id = "heavy_junk",
            itemStack = dummyStack,
            rarity = Rarity.COMMON,
            searchTime = 4
        )
        assertEquals(4, slowCommon.searchTime)
    }

    @Test
    fun testTwoPhaseContainerSearchWithItemTimes() {
        val dummyStack = createBareItem()
        val item1 = ExtractionItem(id = "rare_item", itemStack = dummyStack, rarity = Rarity.RARE, searchTime = 3)
        val item2 = ExtractionItem(id = "common_item", itemStack = dummyStack, rarity = Rarity.COMMON, searchTime = 1)
        val item3 = ExtractionItem(id = "epic_item", itemStack = dummyStack, rarity = Rarity.EPIC, searchTime = 4)

        val slot1 = SearchSlotState(slot = 2, item = item1, targetRounds = item1.searchTime)
        val slot2 = SearchSlotState(slot = 10, item = item2, targetRounds = item2.searchTime)
        val slot3 = SearchSlotState(slot = 18, item = item3, targetRounds = item3.searchTime)

        // Container search phase: randomized 3 seconds total
        val session = ContainerSearchSession(
            categoryId = "normal_crate",
            containerMaterial = Material.CHEST,
            containerSearchTotalSeconds = 3,
            slots = mutableListOf(slot1, slot2, slot3)
        )

        assertEquals(3, session.containerSearchTotalSeconds)
        assertFalse(session.isContainerSearchComplete)

        // Phase 1: Container covered countdown (3 seconds)
        for (sec in 1..3) {
            session.containerSearchElapsedSeconds++
        }
        if (session.containerSearchElapsedSeconds >= session.containerSearchTotalSeconds) {
            session.isContainerSearchComplete = true
        }
        assertTrue(session.isContainerSearchComplete)
        assertEquals(0, session.getRevealedUntakenLoot().size, "No items should be revealed during phase 1")

        // Phase 2: Sequential item countdown
        var elapsedItemSeconds = 0
        val reveals = mutableMapOf<Int, Int>()

        while (session.currentSearchingIndex < session.slots.size) {
            elapsedItemSeconds++
            val currentSlot = session.slots[session.currentSearchingIndex]
            currentSlot.currentRound++

            if (currentSlot.currentRound >= currentSlot.targetRounds) {
                currentSlot.isRevealed = true
                reveals[currentSlot.slot] = elapsedItemSeconds
                session.currentSearchingIndex++
            }
        }
        session.isOverallFinished = true

        // item1 (slot 2) takes 3s
        assertEquals(3, reveals[2], "Slot 2 should reveal after 3 seconds of item search")
        // item2 (slot 10) takes 1s -> 3 + 1 = 4s
        assertEquals(4, reveals[10], "Slot 10 should reveal at second 4 (3 + 1)")
        // item3 (slot 18) takes 4s -> 4 + 4 = 8s
        assertEquals(8, reveals[18], "Slot 18 should reveal at second 8 (3 + 1 + 4)")

        assertEquals(8, elapsedItemSeconds)
        assertTrue(session.areAllItemsRevealed())
        assertEquals(3, session.getRevealedUntakenLoot().size)
    }

    @Test
    fun testCloseAndReopenMidSearchResumesAccurately() {
        val dummyStack = createBareItem()
        val item1 = ExtractionItem(id = "item_1", itemStack = dummyStack, searchTime = 2)
        val item2 = ExtractionItem(id = "item_2", itemStack = dummyStack, searchTime = 3)

        val slot1 = SearchSlotState(slot = 4, item = item1, targetRounds = 2)
        val slot2 = SearchSlotState(slot = 12, item = item2, targetRounds = 3)

        val session = ContainerSearchSession(
            categoryId = "safe_box",
            containerMaterial = Material.BARREL,
            containerSearchTotalSeconds = 2,
            slots = mutableListOf(slot1, slot2)
        )

        // Step 1: Complete container phase (2s)
        session.containerSearchElapsedSeconds = 2
        session.isContainerSearchComplete = true

        // Step 2: Item 1 searches for 2 seconds and finishes
        slot1.currentRound = 2
        slot1.isRevealed = true
        session.currentSearchingIndex = 1

        // Step 3: Item 2 searches for 1 second out of 3 seconds... then player closes window!
        slot2.currentRound = 1
        assertFalse(slot2.isRevealed)
        assertFalse(session.isOverallFinished)

        // === SIMULATE PLAYER REOPENING GUI ===
        // 1. Revealed items must be visible immediately
        val immediatelyRevealed = session.slots.filter { it.isRevealed }
        assertEquals(1, immediatelyRevealed.size)
        assertEquals(4, immediatelyRevealed[0].slot)

        // 2. Unfinished items resume counting from currentRound (1) to targetRounds (3)
        val currentSearching = session.slots[session.currentSearchingIndex]
        assertEquals(12, currentSearching.slot)
        assertEquals(1, currentSearching.currentRound)
        assertEquals(3, currentSearching.targetRounds)

        // Tick 1 after reopen: round becomes 2
        currentSearching.currentRound++
        assertFalse(currentSearching.isRevealed)

        // Tick 2 after reopen: round becomes 3 -> revealed!
        currentSearching.currentRound++
        if (currentSearching.currentRound >= currentSearching.targetRounds) {
            currentSearching.isRevealed = true
            session.currentSearchingIndex++
        }

        assertTrue(currentSearching.isRevealed)
        assertEquals(2, session.currentSearchingIndex)
        assertTrue(session.areAllItemsRevealed())
    }

    @Test
    fun testSoundResolutionHierarchy() {
        val dummyStack = createBareItem()
        val itemWithCustomSound = ExtractionItem(
            id = "special_tech",
            itemStack = dummyStack,
            revealSound = "BLOCK_BELL_USE"
        )
        val itemWithoutCustomSound = ExtractionItem(
            id = "common_junk",
            itemStack = dummyStack,
            revealSound = null
        )

        val categoryDefaultRevealSound = "ENTITY_EXPERIENCE_ORB_PICKUP"

        // 1. When item has a custom sound, it takes precedence
        val resolvedSound1 = itemWithCustomSound.revealSound ?: categoryDefaultRevealSound
        assertEquals("BLOCK_BELL_USE", resolvedSound1)

        // 2. When item has no custom sound, category sound is used
        val resolvedSound2 = itemWithoutCustomSound.revealSound ?: categoryDefaultRevealSound
        assertEquals("ENTITY_EXPERIENCE_ORB_PICKUP", resolvedSound2)

        // 3. When neither has sound, fallback to global default
        val categoryWithoutSound: String? = null
        val resolvedSound3 = itemWithoutCustomSound.revealSound ?: categoryWithoutSound ?: "ENTITY_ITEM_PICKUP"
        assertEquals("ENTITY_ITEM_PICKUP", resolvedSound3)
    }
}

