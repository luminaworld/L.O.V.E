package com.luminaris.extraction.editor.gui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.InventoryHolder

/**
 * Base interface for all Extraction In-Game Editor GUI screens.
 */
interface EditorGuiHolder : InventoryHolder {
    /**
     * Handles an inventory click event within this editor GUI.
     */
    fun handleClick(event: InventoryClickEvent, player: Player)

    /**
     * Optional callback when this editor GUI is closed.
     */
    fun handleClose(event: InventoryCloseEvent, player: Player) {}
}
