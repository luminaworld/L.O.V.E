package com.luminaris.extraction.core.container

import com.luminaris.extraction.core.item.ExtractionItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * Represents the search state of a specific container slot.
 *
 * @property slot Target inventory slot index (0..26).
 * @property item Extraction item definition rolled for this slot.
 * @property targetRounds Total search rounds (seconds) required before reveal.
 * @property currentRound Current search rounds elapsed.
 * @property isRevealed Whether this item has finished its countdown and been revealed.
 * @property isTaken Whether the player has extracted/taken this item into their inventory.
 * @property currentStack Current ItemStack reference (null if completely taken).
 */
data class SearchSlotState(
    val slot: Int,
    val item: ExtractionItem,
    val targetRounds: Int,
    var currentRound: Int = 0,
    var isRevealed: Boolean = false,
    var isTaken: Boolean = false,
    var currentStack: ItemStack? = null
)

/**
 * State session managing extraction container searching lifecycle,
 * covering initial covered material phase, sequential item countdown,
 * and state preservation when closing and reopening mid-search.
 */
class ContainerSearchSession(
    val categoryId: String,
    val containerMaterial: Material,
    val containerSearchTotalSeconds: Int,
    var containerSearchElapsedSeconds: Int = 0,
    var isContainerSearchComplete: Boolean = false,
    val slots: MutableList<SearchSlotState> = mutableListOf(),
    var isOverallFinished: Boolean = false,
    var currentSearchingIndex: Int = 0
) {
    /**
     * Checks if all rolled item slots have completed their individual countdowns.
     */
    fun areAllItemsRevealed(): Boolean {
        return slots.isEmpty() || slots.all { it.isRevealed }
    }

    /**
     * Retrieves all items that have not yet been taken by a player.
     */
    fun getAllRemainingLoot(): Map<Int, ItemStack> {
        val map = mutableMapOf<Int, ItemStack>()
        for (slotState in slots) {
            if (!slotState.isTaken) {
                val stack = slotState.currentStack ?: slotState.item.itemStack
                val cloned = runCatching { stack.clone() }.getOrDefault(stack)
                map[slotState.slot] = cloned
            }
        }
        return map
    }

    /**
     * Retrieves only items that have completed search countdown and remain untaken.
     */
    fun getRevealedUntakenLoot(): Map<Int, ItemStack> {
        val map = mutableMapOf<Int, ItemStack>()
        for (slotState in slots) {
            if (slotState.isRevealed && !slotState.isTaken) {
                val stack = slotState.currentStack ?: slotState.item.itemStack
                val cloned = runCatching { stack.clone() }.getOrDefault(stack)
                map[slotState.slot] = cloned
            }
        }
        return map
    }
}
