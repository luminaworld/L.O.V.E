package com.luminaris.extraction.editor.gui

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.localization.TranslationManager
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

/**
 * Item Selection GUI allowing an administrator to select any item directly from their inventory.
 */
class ItemAddSelectorGui(
    private val plugin: LuminarisCorePlugin
) : EditorGuiHolder {

    private lateinit var inventory: Inventory

    fun open(player: Player) {
        val title = TranslationManager.render("editor.gui.item_selector_title")
        inventory = Bukkit.createInventory(this, 36, title)

        // Fill border
        for (i in 0..8) {
            inventory.setItem(i, GuiUtils.createBorderPane(Material.CYAN_STAINED_GLASS_PANE))
            inventory.setItem(27 + i, GuiUtils.createBorderPane(Material.CYAN_STAINED_GLASS_PANE))
        }
        inventory.setItem(9, GuiUtils.createBorderPane(Material.CYAN_STAINED_GLASS_PANE))
        inventory.setItem(17, GuiUtils.createBorderPane(Material.CYAN_STAINED_GLASS_PANE))
        inventory.setItem(18, GuiUtils.createBorderPane(Material.CYAN_STAINED_GLASS_PANE))
        inventory.setItem(26, GuiUtils.createBorderPane(Material.CYAN_STAINED_GLASS_PANE))

        // Center Indicator Slot
        val hintLore = listOf(
            TranslationManager.render("editor.gui.item_selector_hint_1"),
            TranslationManager.render("editor.gui.item_selector_hint_2"),
            Component.empty(),
            TranslationManager.render("editor.gui.item_selector_hint_3")
        )
        val hintItem = GuiUtils.createItem(
            Material.HOPPER,
            TranslationManager.render("editor.gui.item_selector_hint"),
            hintLore,
            glowing = true
        )
        inventory.setItem(13, hintItem)

        // Cancel / Back Button (Slot 27)
        inventory.setItem(27, GuiUtils.createItem(Material.ARROW, TranslationManager.render("editor.gui.btn_back")))

        plugin.scheduler.runAtEntity(player) {
            player.openInventory(inventory)
        }
    }

    override fun handleClick(event: InventoryClickEvent, player: Player) {
        // If clicking the back button in top inventory
        if (event.clickedInventory == inventory && event.slot == 27) {
            GuiUtils.playClickSound(player)
            ItemListGui(plugin).open(player)
            return
        }

        // If player clicked with an item on cursor onto slot 13
        if (event.clickedInventory == inventory && event.slot == 13) {
            val cursorItem = event.cursor
            if (!cursorItem.isEmpty && !cursorItem.type.isAir) {
                GuiUtils.playSuccessSound(player)
                ItemEditorGui(
                    plugin = plugin,
                    targetStack = cursorItem.clone(),
                    existingItem = null
                ).open(player)
                return
            }
        }

        // If player clicked ANY item in their own player inventory (bottom inventory)
        val clickedItem = event.currentItem
        if (clickedItem != null && !clickedItem.isEmpty && !clickedItem.type.isAir) {
            GuiUtils.playSuccessSound(player)
            ItemEditorGui(
                plugin = plugin,
                targetStack = clickedItem.clone(),
                existingItem = null
            ).open(player)
        }
    }

    override fun getInventory(): Inventory = inventory
}
