package com.luminaris.extraction.editor.gui

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.container.ContainerCategory
import com.luminaris.extraction.core.item.Rarity
import com.luminaris.extraction.editor.tool.ContainerMarkerTool
import com.luminaris.extraction.localization.TranslationManager
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

/**
 * GUI for browsing, creating, and deleting Container / Box Types (Normal Crate, Airdrop, Safe, etc.).
 */
class ContainerListGui(
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
    private val categorySlotMap = mutableMapOf<Int, ContainerCategory>()

    fun open(player: Player, page: Int = 0) {
        this.currentPage = page
        categorySlotMap.clear()

        val title = TranslationManager.render("editor.gui.containers_title")
        inventory = Bukkit.createInventory(this, 54, title)

        // Fill borders
        GuiUtils.fill54Border(inventory, Material.GRAY_STAINED_GLASS_PANE)

        val categories = plugin.containerRegistry.getAllCategories()
        val pageSize = slotIndices.size
        val totalPages = maxOf(1, (categories.size + pageSize - 1) / pageSize)
        val validPage = page.coerceIn(0, totalPages - 1)
        this.currentPage = validPage

        val startIndex = validPage * pageSize
        val endIndex = minOf(categories.size, startIndex + pageSize)
        val pageCategories = categories.subList(startIndex, endIndex)

        for (i in pageCategories.indices) {
            val category = pageCategories[i]
            val slot = slotIndices[i]
            categorySlotMap[slot] = category

            val icon = resolveIconForCategory(category.id)
            val linkedItems = plugin.itemRegistry.getItemsForCategory(category.id).size

            val lore = mutableListOf<Component>()
            lore.add(TranslationManager.render("editor.gui.container_card_id", "id" to category.id))
            lore.add(TranslationManager.render("editor.gui.container_card_rolls", "min" to category.minRolls, "max" to category.maxRolls))
            lore.add(TranslationManager.render("editor.gui.container_card_linked", "count" to linkedItems))
            lore.add(Component.empty())
            lore.add(TranslationManager.render("editor.gui.container_card_rarity_header"))

            for (rarity in Rarity.entries) {
                val pct = category.getRarityPercentage(rarity)
                val weight = category.getRarityWeight(rarity)
                val rarityName = TranslationManager.get("rarity.${rarity.name.lowercase()}")
                lore.add(TranslationManager.render(
                    "editor.gui.container_card_rarity_entry",
                    "tag" to rarity.miniMessageTag,
                    "name" to rarityName,
                    "pct" to String.format("%.1f", pct),
                    "weight" to weight
                ))
            }

            lore.add(Component.empty())
            lore.add(TranslationManager.render("editor.gui.container_card_click_edit"))
            lore.add(TranslationManager.render("editor.gui.container_card_click_marker"))
            lore.add(TranslationManager.render("editor.gui.container_card_click_delete"))

            val itemStack = GuiUtils.createItem(
                icon,
                category.displayName,
                lore
            )
            inventory.setItem(slot, itemStack)
        }

        // Navigation Controls
        // Back to Main Menu
        inventory.setItem(45, GuiUtils.createItem(Material.ARROW, TranslationManager.render("editor.gui.btn_back")))

        // Previous Page
        if (validPage > 0) {
            inventory.setItem(48, GuiUtils.createItem(Material.SPECTRAL_ARROW, TranslationManager.render("editor.gui.btn_prev_page")))
        }

        // Add Container Type
        val addLore = listOf(
            TranslationManager.render("editor.gui.container_add_lore_1"),
            Component.empty(),
            TranslationManager.render("editor.gui.container_add_lore_action")
        )
        inventory.setItem(49, GuiUtils.createItem(Material.EMERALD, TranslationManager.render("editor.gui.btn_add_container"), addLore, glowing = true))

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
                // Open Category Dialog to create new container type
                GuiUtils.playClickSound(player)
                player.closeInventory()
                com.luminaris.extraction.editor.dialog.CategoryDialogBuilder.open(
                    player = player,
                    containerRegistry = plugin.containerRegistry,
                    existingCategory = null,
                    plugin = plugin
                )
                return
            }
            50 -> {
                val categories = plugin.containerRegistry.getAllCategories()
                val totalPages = (categories.size + slotIndices.size - 1) / slotIndices.size
                if (currentPage < totalPages - 1) {
                    GuiUtils.playClickSound(player)
                    open(player, currentPage + 1)
                }
                return
            }
        }

        val clickedCategory = categorySlotMap[slot] ?: return

        if (event.click == ClickType.SHIFT_RIGHT) {
            // Delete container type
            GuiUtils.playDeleteSound(player)
            plugin.containerRegistry.unregister(clickedCategory.id, saveToDisk = true)
            TranslationManager.send(player, "editor.gui.container_deleted", "id" to clickedCategory.id)
            open(player, currentPage)
        } else if (event.click == ClickType.RIGHT) {
            // Give marker tool
            GuiUtils.playSuccessSound(player)
            val markerItem = ContainerMarkerTool.createContainerMarker(
                categoryId = clickedCategory.id,
                material = Material.CHEST,
                categoryDisplayName = null
            )
            val leftover = player.inventory.addItem(markerItem)
            for (item in leftover.values) {
                player.world.dropItemNaturally(player.location, item)
            }
            TranslationManager.send(player, "editor.gui.marker_tool_given", "id" to clickedCategory.id)
        } else {
            // Left click: Open intermediate GUI layer first providing options and preview
            GuiUtils.playClickSound(player)
            ContainerEditGui(plugin, clickedCategory).open(player)
        }
    }

    override fun getInventory(): Inventory = inventory

    private fun resolveIconForCategory(id: String): Material {
        val lower = id.lowercase()
        return when {
            lower.contains("airdrop") -> Material.BEACON
            lower.contains("safe") -> Material.ENDER_CHEST
            lower.contains("barrel") -> Material.BARREL
            lower.contains("military") -> Material.TRAPPED_CHEST
            lower.contains("legendary") -> Material.GOLD_BLOCK
            lower.contains("epic") -> Material.PURPLE_SHULKER_BOX
            lower.contains("rare") -> Material.CYAN_SHULKER_BOX
            lower.contains("uncommon") -> Material.LIME_SHULKER_BOX
            else -> Material.CHEST
        }
    }
}
