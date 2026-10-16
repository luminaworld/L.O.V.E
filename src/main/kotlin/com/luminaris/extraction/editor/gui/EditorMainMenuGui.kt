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
 * Main hub GUI for the Extraction Editor (/ext editor).
 * Provides navigation to Container Types manager and Drop Items Registry.
 */
class EditorMainMenuGui(
    private val plugin: LuminarisCorePlugin
) : EditorGuiHolder {

    private lateinit var inventory: Inventory

    fun open(player: Player) {
        val title = TranslationManager.render("editor.gui.main_title")
        inventory = Bukkit.createInventory(this, 27, title)

        // Fill background border
        GuiUtils.fill27Border(inventory, Material.GRAY_STAINED_GLASS_PANE)

        // 1. Container & Box Types
        val containerCount = plugin.containerRegistry.getAllCategories().size
        val containerLore = listOf(
            TranslationManager.render("editor.gui.containers_lore_1"),
            TranslationManager.render("editor.gui.containers_lore_2"),
            Component.empty(),
            TranslationManager.render("editor.gui.containers_lore_total", "count" to containerCount),
            Component.empty(),
            TranslationManager.render("editor.gui.containers_lore_action")
        )
        val containerItem = GuiUtils.createItem(
            Material.CHEST,
            TranslationManager.render("editor.gui.btn_containers"),
            containerLore,
            glowing = true
        )
        inventory.setItem(11, containerItem)

        // 2. Drop Items Registry
        val itemCount = plugin.itemRegistry.getAllItems().size
        val itemLore = listOf(
            TranslationManager.render("editor.gui.items_lore_1"),
            TranslationManager.render("editor.gui.items_lore_2"),
            Component.empty(),
            TranslationManager.render("editor.gui.items_lore_total", "count" to itemCount),
            Component.empty(),
            TranslationManager.render("editor.gui.items_lore_action")
        )
        val registryItem = GuiUtils.createItem(
            Material.NETHER_STAR,
            TranslationManager.render("editor.gui.btn_items"),
            itemLore,
            glowing = true
        )
        inventory.setItem(13, registryItem)

        // 3. Reload System
        val reloadLore = listOf(
            TranslationManager.render("editor.gui.reload_lore_1"),
            Component.empty(),
            TranslationManager.render("editor.gui.reload_lore_action")
        )
        val reloadItem = GuiUtils.createItem(
            Material.COMPASS,
            TranslationManager.render("editor.gui.btn_reload"),
            reloadLore
        )
        inventory.setItem(15, reloadItem)

        // 4. Close Menu
        val closeItem = GuiUtils.createItem(
            Material.BARRIER,
            TranslationManager.render("editor.gui.btn_close")
        )
        inventory.setItem(22, closeItem)

        plugin.scheduler.runAtEntity(player) {
            player.openInventory(inventory)
        }
    }

    override fun handleClick(event: InventoryClickEvent, player: Player) {
        if (event.clickedInventory != inventory) return

        when (event.slot) {
            11 -> {
                GuiUtils.playClickSound(player)
                ContainerListGui(plugin).open(player, 0)
            }
            13 -> {
                GuiUtils.playClickSound(player)
                ItemListGui(plugin).open(player, 0)
            }
            15 -> {
                GuiUtils.playSuccessSound(player)
                plugin.reloadAllConfigurations()
                TranslationManager.send(player, "editor.gui.reload_success")
                open(player)
            }
            22 -> {
                GuiUtils.playClickSound(player)
                player.closeInventory()
            }
        }
    }

    override fun getInventory(): Inventory = inventory

    companion object {
        fun open(player: Player, plugin: LuminarisCorePlugin) {
            EditorMainMenuGui(plugin).open(player)
        }
    }
}
