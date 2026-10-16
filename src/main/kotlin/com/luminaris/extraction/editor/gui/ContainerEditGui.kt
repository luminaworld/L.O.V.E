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
 * Detailed configuration GUI for a specific [ContainerCategory].
 * Controls item roll quantities (min/max rolls) and drop chances/weights for each rarity tier.
 */
class ContainerEditGui(
    private val plugin: LuminarisCorePlugin,
    private val category: ContainerCategory
) : EditorGuiHolder {

    private lateinit var inventory: Inventory

    // Slot mappings for each Rarity
    private val raritySlots = mapOf(
        29 to Rarity.COMMON,
        30 to Rarity.UNCOMMON,
        31 to Rarity.RARE,
        32 to Rarity.EPIC,
        33 to Rarity.LEGENDARY
    )

    fun open(player: Player) {
        val title = TranslationManager.render("editor.gui.container_edit_title", "id" to category.id)
        inventory = Bukkit.createInventory(this, 54, title)

        GuiUtils.fill54Border(inventory, Material.GRAY_STAINED_GLASS_PANE)

        // Slot 10: Marker Tool Button
        val markerLore = listOf(
            TranslationManager.render("editor.gui.marker_tool_lore_1"),
            Component.empty(),
            TranslationManager.render("editor.gui.marker_tool_lore_action")
        )
        val markerItem = GuiUtils.createItem(
            Material.CHEST,
            TranslationManager.render("editor.gui.btn_marker_tool"),
            markerLore
        )
        inventory.setItem(10, markerItem)

        // Slot 12: Min Rolls
        val minLore = listOf(
            TranslationManager.render("editor.gui.min_rolls_lore_1"),
            Component.empty(),
            TranslationManager.render("editor.gui.click_plus_1"),
            TranslationManager.render("editor.gui.click_minus_1"),
            TranslationManager.render("editor.gui.click_plus_5"),
            TranslationManager.render("editor.gui.click_minus_5")
        )
        val minItem = GuiUtils.createItem(
            Material.DISPENSER,
            TranslationManager.render("editor.gui.min_rolls_title", "count" to category.minRolls),
            minLore,
            amount = category.minRolls.coerceIn(1, 64)
        )
        inventory.setItem(12, minItem)

        // Slot 14: Max Rolls
        val maxLore = listOf(
            TranslationManager.render("editor.gui.max_rolls_lore_1"),
            Component.empty(),
            TranslationManager.render("editor.gui.click_plus_1"),
            TranslationManager.render("editor.gui.click_minus_1"),
            TranslationManager.render("editor.gui.click_plus_5"),
            TranslationManager.render("editor.gui.click_minus_5")
        )
        val maxItem = GuiUtils.createItem(
            Material.DROPPER,
            TranslationManager.render("editor.gui.max_rolls_title", "count" to category.maxRolls),
            maxLore,
            amount = category.maxRolls.coerceIn(1, 64)
        )
        inventory.setItem(14, maxItem)

        // Slot 16: Delete Container Button
        val deleteLore = listOf(
            TranslationManager.render("editor.gui.delete_container_lore_1"),
            Component.empty(),
            TranslationManager.render("editor.gui.delete_container_lore_action")
        )
        val deleteItem = GuiUtils.createItem(
            Material.LAVA_BUCKET,
            TranslationManager.render("editor.gui.btn_delete_container"),
            deleteLore
        )
        inventory.setItem(16, deleteItem)

        // Header for Rarity Section
        val rarityHeader = GuiUtils.createItem(
            Material.NETHER_STAR,
            TranslationManager.render("editor.gui.rarity_rates_header"),
            listOf(
                TranslationManager.render("editor.gui.rarity_rates_desc")
            )
        )
        inventory.setItem(22, rarityHeader)

        // Slots 29..33: Rarity Tiers Drop Rates
        for ((slot, rarity) in raritySlots) {
            val weight = category.getRarityWeight(rarity)
            val pct = category.getRarityPercentage(rarity)
            val eligibleItems = plugin.itemRegistry.getItemsForCategory(category.id).filter { it.rarity == rarity }.size

            val iconMat = when (rarity) {
                Rarity.COMMON -> Material.GRAY_CONCRETE
                Rarity.UNCOMMON -> Material.LIME_CONCRETE
                Rarity.RARE -> Material.LIGHT_BLUE_CONCRETE
                Rarity.EPIC -> Material.PURPLE_CONCRETE
                Rarity.LEGENDARY -> Material.YELLOW_CONCRETE
            }

            val rarityName = TranslationManager.get("rarity.${rarity.name.lowercase()}")
            val lore = listOf(
                TranslationManager.render("editor.gui.rarity_card_chance", "pct" to String.format("%.1f", pct)),
                TranslationManager.render("editor.gui.rarity_card_weight", "weight" to weight),
                TranslationManager.render("editor.gui.rarity_card_items", "count" to eligibleItems),
                Component.empty(),
                TranslationManager.render("editor.gui.rarity_card_plus_5"),
                TranslationManager.render("editor.gui.rarity_card_minus_5"),
                TranslationManager.render("editor.gui.rarity_card_plus_20"),
                TranslationManager.render("editor.gui.rarity_card_disable")
            )

            val rarityItem = GuiUtils.createItem(
                iconMat,
                TranslationManager.parseComponent("${rarity.miniMessageTag}<bold>$rarityName</bold></color>"),
                lore,
                glowing = weight > 0.0
            )
            inventory.setItem(slot, rarityItem)
        }

        // Bottom Controls
        // Slot 45: Back
        inventory.setItem(45, GuiUtils.createItem(Material.ARROW, TranslationManager.render("editor.gui.btn_back")))

        // Slot 49: Edit in Dialog Window
        val dialogLore = listOf(
            TranslationManager.render("editor.gui.open_dialog_lore"),
            Component.empty(),
            TranslationManager.render("editor.gui.btn_open_dialog")
        )
        inventory.setItem(49, GuiUtils.createItem(Material.WRITABLE_BOOK, TranslationManager.render("editor.gui.btn_open_dialog"), dialogLore, glowing = true))

        // Slot 53: Save & Return
        inventory.setItem(53, GuiUtils.createItem(Material.EMERALD_BLOCK, TranslationManager.render("editor.gui.btn_save"), glowing = true))

        plugin.scheduler.runAtEntity(player) {
            player.openInventory(inventory)
        }
    }

    override fun handleClick(event: InventoryClickEvent, player: Player) {
        if (event.clickedInventory != inventory) return

        val slot = event.slot
        when (slot) {
            10 -> {
                // Get Marker Tool
                GuiUtils.playSuccessSound(player)
                val markerItem = ContainerMarkerTool.createContainerMarker(
                    categoryId = category.id,
                    material = Material.CHEST,
                    categoryDisplayName = null
                )
                val leftover = player.inventory.addItem(markerItem)
                for (item in leftover.values) {
                    player.world.dropItemNaturally(player.location, item)
                }
                TranslationManager.send(player, "editor.gui.marker_tool_given", "id" to category.id)
                return
            }
            12 -> {
                // Adjust Min Rolls
                val delta = when (event.click) {
                    ClickType.LEFT -> 1
                    ClickType.RIGHT -> -1
                    ClickType.SHIFT_LEFT -> 5
                    ClickType.SHIFT_RIGHT -> -5
                    else -> 0
                }
                if (delta != 0) {
                    GuiUtils.playClickSound(player)
                    category.minRolls = (category.minRolls + delta).coerceIn(1, category.maxRolls)
                    plugin.containerRegistry.save()
                    open(player)
                }
                return
            }
            14 -> {
                // Adjust Max Rolls
                val delta = when (event.click) {
                    ClickType.LEFT -> 1
                    ClickType.RIGHT -> -1
                    ClickType.SHIFT_LEFT -> 5
                    ClickType.SHIFT_RIGHT -> -5
                    else -> 0
                }
                if (delta != 0) {
                    GuiUtils.playClickSound(player)
                    category.maxRolls = (category.maxRolls + delta).coerceAtLeast(category.minRolls)
                    plugin.containerRegistry.save()
                    open(player)
                }
                return
            }
            16 -> {
                // Delete container
                if (event.isShiftClick) {
                    GuiUtils.playDeleteSound(player)
                    plugin.containerRegistry.unregister(category.id, saveToDisk = true)
                    TranslationManager.send(player, "editor.gui.container_deleted", "id" to category.id)
                    ContainerListGui(plugin).open(player)
                } else {
                    GuiUtils.playErrorSound(player)
                }
                return
            }
            49 -> {
                // Open Category Dialog
                GuiUtils.playClickSound(player)
                player.closeInventory()
                com.luminaris.extraction.editor.dialog.CategoryDialogBuilder.open(
                    player = player,
                    containerRegistry = plugin.containerRegistry,
                    existingCategory = category,
                    plugin = plugin
                )
                return
            }
            45, 53 -> {
                // Back or Save
                GuiUtils.playSuccessSound(player)
                plugin.containerRegistry.save()
                ContainerListGui(plugin).open(player)
                return
            }
        }

        // Adjust Rarity Weights
        val rarity = raritySlots[slot]
        if (rarity != null) {
            val currentWeight = category.getRarityWeight(rarity)
            val newWeight = when (event.click) {
                ClickType.LEFT -> currentWeight + 5.0
                ClickType.RIGHT -> (currentWeight - 5.0).coerceAtLeast(0.0)
                ClickType.SHIFT_LEFT -> currentWeight + 20.0
                ClickType.SHIFT_RIGHT -> 0.0
                else -> currentWeight
            }
            if (newWeight != currentWeight) {
                GuiUtils.playClickSound(player)
                category.setRarityWeight(rarity, newWeight)
                plugin.containerRegistry.save()
                open(player)
            }
        }
    }

    override fun getInventory(): Inventory = inventory
}
