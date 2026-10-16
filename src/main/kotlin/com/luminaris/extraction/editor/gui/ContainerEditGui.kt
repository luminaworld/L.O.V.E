package com.luminaris.extraction.editor.gui

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.container.ContainerCategory
import com.luminaris.extraction.core.item.Rarity
import com.luminaris.extraction.editor.dialog.CategoryDialogBuilder
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
 * Intermediate GUI layer for a specific [ContainerCategory].
 * Displays a comprehensive preview of the drop box and its associated data,
 * allows configuring Name and ID via Dialog, adjusting roll quantities with +/-1 and +/-5,
 * and managing drop percentages directly in the GUI with automatic 100% calculation.
 */
class ContainerEditGui(
    private val plugin: LuminarisCorePlugin,
    private val category: ContainerCategory
) : EditorGuiHolder {

    private lateinit var inventory: Inventory

    // Slot mappings for each Rarity tier
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
            TranslationManager.render("editor.gui.marker_tool_lore_left"),
            TranslationManager.render("editor.gui.marker_tool_lore_right")
        )
        val markerItem = GuiUtils.createItem(
            Material.CHEST,
            TranslationManager.render("editor.gui.btn_marker_tool"),
            markerLore
        )
        inventory.setItem(10, markerItem)

        // Slot 11: Edit Name & ID via Dialog
        val nameIdLore = listOf(
            TranslationManager.render("editor.gui.edit_name_id_lore_1"),
            Component.empty(),
            TranslationManager.render("editor.gui.edit_name_id_lore_action")
        )
        val nameIdItem = GuiUtils.createItem(
            Material.NAME_TAG,
            TranslationManager.render("editor.gui.btn_edit_name_id"),
            nameIdLore,
            glowing = true
        )
        inventory.setItem(11, nameIdItem)

        // Slot 12: Min Rolls Adjustment
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

        // Slot 13: Drop Box Preview & Associated Data
        val previewLore = mutableListOf<Component>()
        previewLore.add(TranslationManager.render("editor.gui.container_card_id", "id" to category.id))
        previewLore.add(TranslationManager.render("editor.gui.container_card_rolls", "min" to category.minRolls, "max" to category.maxRolls))
        val linkedItems = plugin.itemRegistry.getItemsForCategory(category.id).size
        previewLore.add(TranslationManager.render("editor.gui.container_card_linked", "count" to linkedItems))
        previewLore.add(Component.empty())
        previewLore.add(TranslationManager.render("editor.gui.container_card_rarity_header"))

        for (rarity in Rarity.entries) {
            val pct = category.getRarityPercentage(rarity)
            val rarityName = TranslationManager.get("rarity.${rarity.name.lowercase()}")
            previewLore.add(TranslationManager.render(
                "editor.gui.container_card_rarity_entry",
                "tag" to rarity.miniMessageTag,
                "name" to rarityName,
                "pct" to String.format("%.1f", pct),
                "weight" to String.format("%.1f", category.getRarityWeight(rarity))
            ))
        }

        previewLore.add(Component.empty())
        previewLore.add(TranslationManager.render("editor.gui.rarity_rates_total"))

        val previewItem = GuiUtils.createItem(
            resolveIconForCategory(category.id),
            category.displayName,
            previewLore,
            glowing = true
        )
        inventory.setItem(13, previewItem)

        // Slot 14: Max Rolls Adjustment
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

        // Slot 15: Auto-Balance / Reset Rates
        val balanceLore = listOf(
            TranslationManager.render("editor.gui.balance_rates_lore_1"),
            Component.empty(),
            TranslationManager.render("editor.gui.balance_rates_lore_reset")
        )
        val balanceItem = GuiUtils.createItem(
            Material.NETHER_STAR,
            TranslationManager.render("editor.gui.btn_balance_rates"),
            balanceLore
        )
        inventory.setItem(15, balanceItem)

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

        // Slot 17: Test Drop Simulation Button
        val testLore = listOf(
            TranslationManager.render("editor.gui.test_drop_lore_1"),
            Component.empty(),
            TranslationManager.render("editor.gui.test_drop_lore_action")
        )
        val testItem = GuiUtils.createItem(
            Material.ENDER_EYE,
            TranslationManager.render("editor.gui.btn_test_drop"),
            testLore,
            glowing = true
        )
        inventory.setItem(17, testItem)

        // Slot 22: Header for Rarity Section
        val rarityHeader = GuiUtils.createItem(
            Material.COMPARATOR,
            TranslationManager.render("editor.gui.rarity_rates_header"),
            listOf(
                TranslationManager.render("editor.gui.rarity_rates_desc"),
                TranslationManager.render("editor.gui.rarity_auto_calc_hint"),
                Component.empty(),
                TranslationManager.render("editor.gui.rarity_rates_total")
            )
        )
        inventory.setItem(22, rarityHeader)

        // Slots 29..33: Rarity Tiers Drop Percentages (Managed via GUI with +/-1 and +/-5)
        for ((slot, rarity) in raritySlots) {
            val pct = category.getRarityPercentage(rarity)
            val eligibleItems = plugin.itemRegistry.getItemsForCategory(category.id).count { it.rarity == rarity }

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
                TranslationManager.render("editor.gui.rarity_card_items", "count" to eligibleItems),
                Component.empty(),
                TranslationManager.render("editor.gui.click_plus_1_pct"),
                TranslationManager.render("editor.gui.click_minus_1_pct"),
                TranslationManager.render("editor.gui.click_plus_5_pct"),
                TranslationManager.render("editor.gui.click_minus_5_pct"),
                Component.empty(),
                TranslationManager.render("editor.gui.rarity_auto_calc_hint")
            )

            val amount = maxOf(1, Math.round(pct).toInt().coerceIn(1, 64))
            val rarityItem = GuiUtils.createItem(
                iconMat,
                TranslationManager.parseComponent("${rarity.miniMessageTag}<bold>$rarityName</bold></color>"),
                lore,
                glowing = pct > 0.0,
                amount = amount
            )
            inventory.setItem(slot, rarityItem)
        }

        // Bottom Navigation Controls
        // Slot 45: Back to Containers List
        inventory.setItem(45, GuiUtils.createItem(Material.ARROW, TranslationManager.render("editor.gui.btn_back")))

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
                if (event.click == ClickType.RIGHT) {
                    // Open Material Picker
                    GuiUtils.playClickSound(player)
                    ContainerMaterialSelectGui(plugin, category).open(player)
                } else {
                    // Get Default Chest Marker Tool
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
                }
                return
            }
            11 -> {
                // Open Category Dialog to edit Name and ID
                GuiUtils.playClickSound(player)
                player.closeInventory()
                CategoryDialogBuilder.open(
                    player = player,
                    containerRegistry = plugin.containerRegistry,
                    existingCategory = category,
                    plugin = plugin
                )
                return
            }
            12 -> {
                // Adjust Min Rolls (+/-1, +/-5)
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
                // Adjust Max Rolls (+/-1, +/-5)
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
            15 -> {
                // Reset / Balance rates to defaults
                GuiUtils.playSuccessSound(player)
                category.resetRarityWeightsToDefault()
                plugin.containerRegistry.save()
                open(player)
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
            17 -> {
                // Test Drop Simulation
                GuiUtils.playClickSound(player)
                plugin.dropBoxManager.openSimulation(player, category)
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

        // Adjust Rarity Drop Percentages with auto-balance to 100%
        val rarity = raritySlots[slot]
        if (rarity != null) {
            val delta = when (event.click) {
                ClickType.LEFT -> 1.0
                ClickType.RIGHT -> -1.0
                ClickType.SHIFT_LEFT -> 5.0
                ClickType.SHIFT_RIGHT -> -5.0
                else -> 0.0
            }
            if (delta != 0.0) {
                GuiUtils.playClickSound(player)
                category.adjustRarityPercentage(rarity, delta)
                plugin.containerRegistry.save()
                open(player)
            }
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
