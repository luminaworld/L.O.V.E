package com.luminaris.extraction.editor.gui

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.container.ContainerCategory
import com.luminaris.extraction.core.item.ExtractionItem
import com.luminaris.extraction.core.item.Rarity
import com.luminaris.extraction.localization.TranslationManager
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Nested selection menu GUI for choosing which drop box types an item can appear in.
 * Supports full pagination across any number of drop boxes, "Allow in ALL" toggle,
 * select all, deselect all, and persistent state return to [ItemEditorGui].
 */
class ItemContainerSelectGui(
    private val plugin: LuminarisCorePlugin,
    private val targetStack: ItemStack,
    private val existingItem: ExtractionItem? = null,
    private val currentRarity: Rarity,
    private val currentPrice: Double,
    private val allowedContainers: MutableSet<String>,
    private val currentItemId: String? = null
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

        val allCategories = plugin.containerRegistry.getAllCategories()
        val pageSize = slotIndices.size
        val totalPages = maxOf(1, (allCategories.size + pageSize - 1) / pageSize)
        val validPage = page.coerceIn(0, totalPages - 1)
        this.currentPage = validPage

        val isAllAllowed = allowedContainers.isEmpty() && !allowedContainers.contains("__none__")
        val allowedCount = if (isAllAllowed) {
            allCategories.size
        } else {
            allowedContainers.count { cat -> allCategories.any { it.id.equals(cat, ignoreCase = true) } }
        }

        val title = TranslationManager.render(
            "editor.gui.item_containers_title",
            "current" to allowedCount,
            "total" to allCategories.size
        )
        inventory = Bukkit.createInventory(this, 54, title)

        GuiUtils.fill54Border(inventory, Material.GRAY_STAINED_GLASS_PANE)

        // Slot 4: Info Header
        val headerLore = listOf(
            TranslationManager.render("editor.gui.containers_select_header_desc"),
            Component.empty(),
            TranslationManager.render(
                "editor.gui.containers_select_header_count",
                "count" to allowedCount,
                "total" to allCategories.size
            )
        )
        val headerItem = GuiUtils.createItem(
            Material.BOOK,
            TranslationManager.render("editor.gui.containers_select_header"),
            headerLore
        )
        inventory.setItem(4, headerItem)

        // Populate Categories for this page
        val startIndex = validPage * pageSize
        val endIndex = minOf(allCategories.size, startIndex + pageSize)
        val pageCategories = allCategories.subList(startIndex, endIndex)

        for (i in pageCategories.indices) {
            val category = pageCategories[i]
            val slot = slotIndices[i]
            categorySlotMap[slot] = category

            val catId = category.id.lowercase()
            val isAllowed = isAllAllowed || allowedContainers.contains(catId)

            val statusComponent = if (isAllowed) {
                TranslationManager.render("editor.gui.container_select_entry_status_allowed")
            } else {
                TranslationManager.render("editor.gui.container_select_entry_status_denied")
            }

            val iconMat = resolveIconForCategory(category.id)
            val lore = listOf(
                TranslationManager.render("editor.gui.cat_card_id", "id" to category.id),
                statusComponent,
                Component.empty(),
                TranslationManager.render("editor.gui.container_select_entry_toggle")
            )

            val itemStack = GuiUtils.createItem(
                iconMat,
                category.displayName,
                lore,
                glowing = isAllowed
            )
            inventory.setItem(slot, itemStack)
        }

        // Navigation Controls
        // Slot 45: Back to Item Editor
        inventory.setItem(45, GuiUtils.createItem(Material.ARROW, TranslationManager.render("editor.gui.btn_back")))

        // Slot 47: Select All
        val selectAllLore = listOf(
            TranslationManager.render("editor.gui.btn_select_all_lore")
        )
        inventory.setItem(47, GuiUtils.createItem(Material.LIME_DYE, TranslationManager.render("editor.gui.btn_select_all"), selectAllLore))

        // Slot 48: Previous Page
        if (validPage > 0) {
            inventory.setItem(48, GuiUtils.createItem(Material.SPECTRAL_ARROW, TranslationManager.render("editor.gui.btn_prev_page")))
        }

        // Slot 49: Allow in ALL Drop Boxes Toggle
        val allItemLore = listOf(
            if (isAllAllowed) TranslationManager.render("editor.gui.all_containers_allowed_desc") else TranslationManager.render("editor.gui.all_containers_restricted_desc"),
            Component.empty(),
            TranslationManager.render("editor.gui.all_containers_toggle_action")
        )
        val allItem = GuiUtils.createItem(
            if (isAllAllowed) Material.NETHER_STAR else Material.GRAY_DYE,
            if (isAllAllowed) TranslationManager.render("editor.gui.all_containers_allowed_title") else TranslationManager.render("editor.gui.all_containers_restricted_title"),
            allItemLore,
            glowing = isAllAllowed
        )
        inventory.setItem(49, allItem)

        // Slot 50: Next Page
        if (validPage < totalPages - 1) {
            inventory.setItem(50, GuiUtils.createItem(Material.SPECTRAL_ARROW, TranslationManager.render("editor.gui.btn_next_page")))
        }

        // Slot 51: Deselect All
        val deselectAllLore = listOf(
            TranslationManager.render("editor.gui.btn_deselect_all_lore")
        )
        inventory.setItem(51, GuiUtils.createItem(Material.RED_DYE, TranslationManager.render("editor.gui.btn_deselect_all"), deselectAllLore))

        // Slot 53: Page info
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
                // Return to ItemEditorGui preserving state
                GuiUtils.playClickSound(player)
                val sanitizedAllowed = if (allowedContainers.contains("__none__")) {
                    mutableSetOf("__none__")
                } else {
                    allowedContainers.toMutableSet()
                }
                ItemEditorGui(
                    plugin = plugin,
                    targetStack = targetStack,
                    existingItem = existingItem,
                    initialRarity = currentRarity,
                    initialPrice = currentPrice,
                    initialAllowedContainers = sanitizedAllowed,
                    initialItemId = currentItemId
                ).open(player)
                return
            }
            47 -> {
                // Select All
                GuiUtils.playSuccessSound(player)
                allowedContainers.clear()
                allowedContainers.addAll(plugin.containerRegistry.getAllCategories().map { it.id.lowercase() })
                open(player, currentPage)
                return
            }
            48 -> {
                // Previous page
                if (currentPage > 0) {
                    GuiUtils.playClickSound(player)
                    open(player, currentPage - 1)
                }
                return
            }
            49 -> {
                // Toggle ALL vs Specific
                GuiUtils.playClickSound(player)
                val isCurrentlyAll = allowedContainers.isEmpty() && !allowedContainers.contains("__none__")
                if (isCurrentlyAll) {
                    allowedContainers.clear()
                    allowedContainers.addAll(plugin.containerRegistry.getAllCategories().map { it.id.lowercase() })
                } else {
                    allowedContainers.clear()
                }
                open(player, currentPage)
                return
            }
            50 -> {
                // Next page
                val allCategories = plugin.containerRegistry.getAllCategories()
                val totalPages = (allCategories.size + slotIndices.size - 1) / slotIndices.size
                if (currentPage < totalPages - 1) {
                    GuiUtils.playClickSound(player)
                    open(player, currentPage + 1)
                }
                return
            }
            51 -> {
                // Deselect All
                GuiUtils.playDeleteSound(player)
                allowedContainers.clear()
                allowedContainers.add("__none__")
                open(player, currentPage)
                return
            }
        }

        // Toggle individual category
        val category = categorySlotMap[slot] ?: return
        GuiUtils.playClickSound(player)
        val catId = category.id.lowercase()

        val isCurrentlyAll = allowedContainers.isEmpty() && !allowedContainers.contains("__none__")
        if (isCurrentlyAll) {
            val allCats = plugin.containerRegistry.getAllCategories().map { it.id.lowercase() }.toMutableSet()
            allCats.remove(catId)
            allowedContainers.clear()
            allowedContainers.addAll(allCats)
        } else if (allowedContainers.contains(catId)) {
            allowedContainers.remove(catId)
            if (allowedContainers.isEmpty()) {
                allowedContainers.add("__none__")
            }
        } else {
            allowedContainers.remove("__none__")
            allowedContainers.add(catId)
        }

        open(player, currentPage)
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
