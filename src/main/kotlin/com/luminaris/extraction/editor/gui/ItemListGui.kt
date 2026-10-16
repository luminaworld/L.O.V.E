package com.luminaris.extraction.editor.gui

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.item.ExtractionItem
import com.luminaris.extraction.localization.TranslationManager
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

/**
 * GUI for browsing, adding, editing, and deleting registered Extraction Loot Items.
 */
class ItemListGui(
    private val plugin: LuminarisCorePlugin
) : EditorGuiHolder {

    private lateinit var inventory: Inventory
    private var currentPage: Int = 0
    private val slotIndices = listOf(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    )
    private val itemSlotMap = mutableMapOf<Int, ExtractionItem>()

    fun open(player: Player, page: Int = 0) {
        this.currentPage = page
        itemSlotMap.clear()

        val allItems = plugin.itemRegistry.getAllItems()
        val title = TranslationManager.render("editor.gui.items_title", "count" to allItems.size)
        inventory = Bukkit.createInventory(this, 54, title)

        GuiUtils.fill54Border(inventory, Material.GRAY_STAINED_GLASS_PANE)

        val pageSize = slotIndices.size
        val totalPages = maxOf(1, (allItems.size + pageSize - 1) / pageSize)
        val validPage = page.coerceIn(0, totalPages - 1)
        this.currentPage = validPage

        val startIndex = validPage * pageSize
        val endIndex = minOf(allItems.size, startIndex + pageSize)
        val pageItems = allItems.subList(startIndex, endIndex)

        for (i in pageItems.indices) {
            val item = pageItems[i]
            val slot = slotIndices[i]
            itemSlotMap[slot] = item

            val displayStack = item.itemStack.clone()
            displayStack.editMeta { meta ->
                val currentLore = meta.lore()?.toMutableList() ?: mutableListOf()
                currentLore.add(Component.empty())
                currentLore.add(TranslationManager.parseComponent("<color:#555555>----------------------------------------</color>"))
                currentLore.add(TranslationManager.render("editor.gui.item_card_id", "id" to item.id))
                val rarityLocalized = TranslationManager.get("rarity.${item.rarity.name.lowercase()}")
                currentLore.add(TranslationManager.render(
                    "editor.gui.item_card_rarity",
                    "tag" to item.rarity.miniMessageTag,
                    "name" to rarityLocalized
                ))
                currentLore.add(TranslationManager.render(
                    "editor.gui.item_card_price",
                    "price" to String.format("%.2f", item.price)
                ))
                val containerText = if (item.allowedContainers.isEmpty()) {
                    TranslationManager.get("general.all")
                } else {
                    item.allowedContainers.joinToString(", ")
                }
                currentLore.add(TranslationManager.render("editor.gui.item_card_containers", "containers" to containerText))
                currentLore.add(TranslationManager.parseComponent("<color:#555555>----------------------------------------</color>"))
                currentLore.add(TranslationManager.render("editor.gui.item_card_click_edit"))
                currentLore.add(TranslationManager.render("editor.gui.item_card_click_copy"))
                currentLore.add(TranslationManager.render("editor.gui.item_card_click_delete"))
                meta.lore(currentLore)
            }
            inventory.setItem(slot, displayStack)
        }

        // Navigation Controls
        // Back to Main Menu
        inventory.setItem(45, GuiUtils.createItem(Material.ARROW, TranslationManager.render("editor.gui.btn_back")))

        // Previous Page
        if (validPage > 0) {
            inventory.setItem(48, GuiUtils.createItem(Material.SPECTRAL_ARROW, TranslationManager.render("editor.gui.btn_prev_page")))
        }

        // Add Item Button ("ช่องเพิ่มในหน้า GUI")
        val addLore = listOf(
            TranslationManager.render("editor.gui.item_add_lore_1"),
            TranslationManager.render("editor.gui.item_add_lore_2"),
            Component.empty(),
            TranslationManager.render("editor.gui.item_add_lore_action")
        )
        val addItemBtn = GuiUtils.createItem(
            Material.HOPPER,
            TranslationManager.render("editor.gui.btn_add_item"),
            addLore,
            glowing = true
        )
        inventory.setItem(49, addItemBtn)

        // Next Page
        if (validPage < totalPages - 1) {
            inventory.setItem(50, GuiUtils.createItem(Material.SPECTRAL_ARROW, TranslationManager.render("editor.gui.btn_next_page")))
        }

        // Page info
        val pageInfo = TranslationManager.render("editor.gui.page_info", "current" to (validPage + 1), "total" to totalPages)
        inventory.setItem(53, GuiUtils.createItem(Material.PAPER, pageInfo))

        plugin.scheduler.runAtEntity(player) {
            player.openInventory(inventory)
        }
    }

    override fun handleClick(event: InventoryClickEvent, player: Player) {
        if (event.clickedInventory != inventory) return

        val slot = event.slot
        when (slot) {
            45 -> {
                GuiUtils.playClickSound(player)
                EditorMainMenuGui(plugin).open(player)
                return
            }
            48 -> {
                if (currentPage > 0) {
                    GuiUtils.playClickSound(player)
                    open(player, currentPage - 1)
                }
                return
            }
            49 -> {
                // Open Item Selector GUI
                GuiUtils.playClickSound(player)
                ItemAddSelectorGui(plugin).open(player)
                return
            }
            50 -> {
                val allItems = plugin.itemRegistry.getAllItems()
                val totalPages = (allItems.size + slotIndices.size - 1) / slotIndices.size
                if (currentPage < totalPages - 1) {
                    GuiUtils.playClickSound(player)
                    open(player, currentPage + 1)
                }
                return
            }
        }

        val clickedItem = itemSlotMap[slot] ?: return

        if (event.click == ClickType.SHIFT_RIGHT) {
            // Delete Item
            GuiUtils.playDeleteSound(player)
            plugin.itemRegistry.unregister(clickedItem.id, saveToDisk = true)
            TranslationManager.send(player, "editor.gui.item_deleted", "id" to clickedItem.id)
            open(player, currentPage)
        } else if (event.click == ClickType.RIGHT || event.click == ClickType.MIDDLE) {
            // Give item copy to player
            GuiUtils.playSuccessSound(player)
            val leftover = player.inventory.addItem(clickedItem.itemStack.clone())
            for (drop in leftover.values) {
                player.world.dropItemNaturally(player.location, drop)
            }
            TranslationManager.send(player, "editor.gui.item_given")
        } else {
            // Left click: Open native Paper Dialog to edit item properties (ID, name, lore, rarity, price, containers)
            GuiUtils.playClickSound(player)
            player.closeInventory()
            com.luminaris.extraction.editor.dialog.ItemDialogBuilder.open(
                player = player,
                itemRegistry = plugin.itemRegistry,
                itemStack = clickedItem.itemStack.clone(),
                existingItem = clickedItem,
                plugin = plugin
            )
        }
    }

    override fun getInventory(): Inventory = inventory
}
