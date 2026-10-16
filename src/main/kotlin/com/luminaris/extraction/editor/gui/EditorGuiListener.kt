package com.luminaris.extraction.editor.gui

import com.luminaris.extraction.LuminarisCorePlugin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent

/**
 * Event listener for handling inventory interactions with custom Extraction In-Game Editor GUIs.
 */
class EditorGuiListener(private val plugin: LuminarisCorePlugin) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryClick(event: InventoryClickEvent) {
        val topInventory = event.view.topInventory
        val holder = topInventory.holder as? EditorGuiHolder ?: return
        val player = event.whoClicked as? Player ?: return

        // Always cancel default Bukkit item moving / stealing behaviors in editor menus
        event.isCancelled = true

        holder.handleClick(event, player)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryDrag(event: InventoryDragEvent) {
        val topInventory = event.view.topInventory
        if (topInventory.holder is EditorGuiHolder) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onInventoryClose(event: InventoryCloseEvent) {
        val holder = event.inventory.holder as? EditorGuiHolder ?: return
        val player = event.player as? Player ?: return

        holder.handleClose(event, player)
    }
}
