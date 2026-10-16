package com.luminaris.extraction.core.container

import com.luminaris.extraction.core.item.Rarity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropBoxSimulationTest {

    private fun getTargetRounds(rarity: Rarity): Int {
        return when (rarity) {
            Rarity.COMMON -> 1
            Rarity.UNCOMMON -> 2
            Rarity.RARE -> 3
            Rarity.EPIC -> 4
            Rarity.LEGENDARY -> 5
        }
    }

    @Test
    fun testRarityDurations() {
        assertEquals(1, getTargetRounds(Rarity.COMMON))
        assertEquals(2, getTargetRounds(Rarity.UNCOMMON))
        assertEquals(3, getTargetRounds(Rarity.RARE))
        assertEquals(4, getTargetRounds(Rarity.EPIC))
        assertEquals(5, getTargetRounds(Rarity.LEGENDARY))
    }

    @Test
    fun testSequentialSearchTimelineUserScenario() {
        // User scenario:
        // 3 items: RARE, COMMON, LEGENDARY placed at slots 2, 10, 18
        data class MockSearchingSlot(
            val slot: Int,
            val rarity: Rarity,
            val targetRounds: Int,
            var currentRound: Int = 0,
            var isRevealed: Boolean = false
        )

        val slots = mutableListOf(
            MockSearchingSlot(slot = 18, rarity = Rarity.LEGENDARY, targetRounds = getTargetRounds(Rarity.LEGENDARY)),
            MockSearchingSlot(slot = 2, rarity = Rarity.RARE, targetRounds = getTargetRounds(Rarity.RARE)),
            MockSearchingSlot(slot = 10, rarity = Rarity.COMMON, targetRounds = getTargetRounds(Rarity.COMMON))
        )

        // Sort ascending by slot index ("เริ่มเปิดของจากช่องที่น้อยสุด")
        slots.sortBy { it.slot }

        assertEquals(2, slots[0].slot)
        assertEquals(Rarity.RARE, slots[0].rarity)

        assertEquals(10, slots[1].slot)
        assertEquals(Rarity.COMMON, slots[1].rarity)

        assertEquals(18, slots[2].slot)
        assertEquals(Rarity.LEGENDARY, slots[2].rarity)

        // Simulate 1-second interval execution
        var currentIndex = 0
        var totalElapsedSeconds = 0
        val revealTimestamps = mutableMapOf<Int, Int>()

        while (currentIndex < slots.size) {
            totalElapsedSeconds++
            val current = slots[currentIndex]
            current.currentRound++

            if (current.currentRound >= current.targetRounds) {
                current.isRevealed = true
                revealTimestamps[current.slot] = totalElapsedSeconds
                currentIndex++
            }
        }

        // Verify reveal timestamps:
        // 1. RARE (slot 2) takes 3s -> reveals at second 3
        assertEquals(3, revealTimestamps[2], "RARE should reveal after 3 seconds")

        // 2. COMMON (slot 10) takes 1s -> reveals at second 4 (3 + 1)
        assertEquals(4, revealTimestamps[10], "COMMON should reveal at second 4 (total 3 + 1)")

        // 3. LEGENDARY (slot 18) takes 5s -> reveals at second 9 (4 + 5)
        assertEquals(9, revealTimestamps[18], "LEGENDARY should reveal at second 9 (total 4 + 5)")

        assertEquals(9, totalElapsedSeconds)
        assertTrue(slots.all { it.isRevealed })
    }

    @Test
    fun testSameRaritySequentialOpening() {
        // User scenario: 3 RARE items
        data class MockSearchingSlot(
            val slot: Int,
            val targetRounds: Int,
            var currentRound: Int = 0,
            var isRevealed: Boolean = false
        )

        val slots = mutableListOf(
            MockSearchingSlot(slot = 14, targetRounds = 3),
            MockSearchingSlot(slot = 3, targetRounds = 3),
            MockSearchingSlot(slot = 20, targetRounds = 3)
        )

        slots.sortBy { it.slot }

        var currentIndex = 0
        var totalSeconds = 0
        val reveals = mutableMapOf<Int, Int>()

        while (currentIndex < slots.size) {
            totalSeconds++
            val current = slots[currentIndex]
            current.currentRound++

            if (current.currentRound >= current.targetRounds) {
                current.isRevealed = true
                reveals[current.slot] = totalSeconds
                currentIndex++
            }
        }

        // Each RARE item takes 3 seconds sequentially
        assertEquals(3, reveals[3], "1st RARE reveals at 3s")
        assertEquals(6, reveals[14], "2nd RARE reveals at 6s")
        assertEquals(9, reveals[20], "3rd RARE reveals at 9s")
        assertEquals(9, totalSeconds)
    }
}
