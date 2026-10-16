package com.luminaris.extraction.core.safe

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.localization.TranslationManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

/**
 * Event listener enforcing the safe container user interactions, anti-nesting restrictions,
 * hotbar swapping guards, and automatic persistence upon inventory close.
 */
class SafeCaseListener(
    private val plugin: LuminarisCorePlugin,
    private val safeCaseManager: SafeCaseManager
) : Listener {

    /**
     * Detects right-click interactions with a Safe Case item in main hand or off hand.
     * Cancels block placement / interaction and launches the virtual safe inventory GUI.
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }

        // Avoid triggering twice if the player is holding safe items in both hands
        if (event.hand == EquipmentSlot.OFF_HAND && safeCaseManager.isSafeCase(event.player.inventory.itemInMainHand)) {
            return
        }

        val item = event.item ?: return
        if (!safeCaseManager.isSafeCase(item)) {
            return
        }

        event.isCancelled = true
        safeCaseManager.openSafeInventory(event.player, item)
    }

    /**
     * Enforces strict Anti-Nesting guards and prevents item duplication exploits:
     * - Prevents placing or shift-clicking another Safe Case into an open Safe Case.
     * - Blocks hotbar swapping another Safe Case into the open Safe Case.
     * - Blocks moving or dropping the active Safe Case item while its GUI is open.
     * - Prevents placing items into or taking items from locked filler slots.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        val topInventory = event.view.topInventory
        val holder = topInventory.holder as? SafeCaseHolder ?: return
        val player = event.whoClicked as? Player ?: return
        val clickedInventory = event.clickedInventory

        // 1. Guard Locked slots inside the Safe Case GUI
        if (clickedInventory == topInventory) {
            if (event.slot >= holder.capacity || safeCaseManager.isLockedSlot(event.currentItem)) {
                event.isCancelled = true
                return
            }
        }

        // 2. Anti-Nesting: Block placing any Safe Case into the open top inventory via cursor
        if (clickedInventory == topInventory) {
            if (safeCaseManager.isSafeCase(event.cursor)) {
                event.isCancelled = true
                TranslationManager.send(player, "safe_case.error_nesting_cursor")
                return
            }
        }

        // 3. Anti-Nesting: Block shift-clicking any Safe Case from player inventory into the open Safe Case
        if (event.isShiftClick) {
            val clickedItem = event.currentItem
            if (safeCaseManager.isSafeCase(clickedItem)) {
                event.isCancelled = true
                TranslationManager.send(player, "safe_case.error_nesting_shift")
                return
            }
        }

        // 4. Hotbar Swap Guard (Number keys 1-9): Block swapping Safe Cases into top inventory
        if (event.click == ClickType.NUMBER_KEY) {
            val hotbarSlot = event.hotbarButton
            if (hotbarSlot in 0..8) {
                val hotbarItem = player.inventory.getItem(hotbarSlot)

                // If hotbar swapping into top inventory
                if (clickedInventory == topInventory) {
                    if (safeCaseManager.isSafeCase(hotbarItem)) {
                        event.isCancelled = true
                        TranslationManager.send(player, "safe_case.error_hotbar_swap")
                        return
                    }
                }

                // If hotbar swapping on the active container in player inventory
                if (clickedInventory != topInventory) {
                    val clickedItem = event.currentItem
                    if (hotbarItem != null && safeCaseManager.getSafeUuid(hotbarItem) == holder.safeUuid) {
                        event.isCancelled = true
                        return
                    }
                    if (clickedItem != null && safeCaseManager.isSafeCase(clickedItem) && safeCaseManager.getSafeUuid(clickedItem) == holder.safeUuid) {
                        event.isCancelled = true
                        return
                    }
                }
            }
        }

        // 5. Offhand Swap Guard (Key F): Block swapping Safe Cases into top inventory or swapping active pouch
        if (event.click == ClickType.SWAP_OFFHAND) {
            if (clickedInventory == topInventory) {
                event.isCancelled = true
                return
            }
            val offhandItem = player.inventory.itemInOffHand
            if (safeCaseManager.isSafeCase(offhandItem) && safeCaseManager.getSafeUuid(offhandItem) == holder.safeUuid) {
                event.isCancelled = true
                return
            }
            val clickedItem = event.currentItem
            if (clickedItem != null && safeCaseManager.isSafeCase(clickedItem) && safeCaseManager.getSafeUuid(clickedItem) == holder.safeUuid) {
                event.isCancelled = true
                return
            }
        }

        // 6. Active Pouch Guard: Prevent moving, swapping, or dropping the currently open Safe Case
        if (clickedInventory != topInventory) {
            val clickedItem = event.currentItem
            if (safeCaseManager.isSafeCase(clickedItem)) {
                val itemUuid = safeCaseManager.getSafeUuid(clickedItem)
                if (itemUuid == holder.safeUuid) {
                    event.isCancelled = true
                    return
                }
            }
        }

        // 7. Drop key Guard: Prevent dropping the active container item
        if (event.click == ClickType.DROP || event.click == ClickType.CONTROL_DROP) {
            val clickedItem = event.currentItem
            if (safeCaseManager.isSafeCase(clickedItem)) {
                val itemUuid = safeCaseManager.getSafeUuid(clickedItem)
                if (itemUuid == holder.safeUuid) {
                    event.isCancelled = true
                    return
                }
            }
        }

        // 8. Double-click collection Guard
        if (event.click == ClickType.DOUBLE_CLICK && (safeCaseManager.isSafeCase(event.cursor) || safeCaseManager.isSafeCase(event.currentItem))) {
            event.isCancelled = true
            return
        }
    }

    /**
     * Prevents dragging Safe Cases into the top inventory,
     * and prevents dragging any items into locked container slots.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onInventoryDrag(event: InventoryDragEvent) {
        val topInventory = event.view.topInventory
        val holder = topInventory.holder as? SafeCaseHolder ?: return
        val player = event.whoClicked as? Player ?: return

        // Anti-Nesting: Block dragging a Safe Case across the top inventory
        val isDraggingSafeCase = safeCaseManager.isSafeCase(event.oldCursor) ||
            safeCaseManager.isSafeCase(event.cursor) ||
            event.newItems.values.any { safeCaseManager.isSafeCase(it) }

        if (isDraggingSafeCase) {
            val touchesTop = event.rawSlots.any { it < topInventory.size }
            if (touchesTop) {
                event.isCancelled = true
                TranslationManager.send(player, "safe_case.error_nesting_drag")
                return
            }
        }

        // Guard Locked slots: Block dragging any items into slots exceeding capacity
        for (rawSlot in event.rawSlots) {
            if (rawSlot < topInventory.size && rawSlot >= holder.capacity) {
                event.isCancelled = true
                return
            }
        }
    }

    /**
     * Automatically serializes and saves the Safe Case contents to the physical item's PDC
     * upon closing the virtual inventory GUI.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onInventoryClose(event: InventoryCloseEvent) {
        val holder = event.inventory.holder as? SafeCaseHolder ?: return
        val player = event.player as? Player ?: return

        safeCaseManager.saveSafeInventory(player, holder, event.inventory)
    }

    /**
     * Preserves Safe Containers and their internal loot when a player dies,
     * honoring the `keeps_on_death` property from safe_cases.yml.
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        // If player is participating in an active extraction match, DeathListener handles match corpse & safe case logic
        if (runCatching { plugin.matchManager.isPlayerInMatch(event.player.uniqueId) }.getOrDefault(false)) {
            return
        }

        val dropsIterator = event.drops.iterator()
        while (dropsIterator.hasNext()) {
            val drop = dropsIterator.next()
            if (safeCaseManager.isSafeCase(drop)) {
                val safeId = safeCaseManager.getSafeId(drop)
                val def = if (safeId != null) safeCaseManager.getDefinition(safeId) else null
                if (def == null || def.keepsOnDeath) {
                    dropsIterator.remove()
                    event.itemsToKeep.add(drop)
                }
            }
        }
    }
}
