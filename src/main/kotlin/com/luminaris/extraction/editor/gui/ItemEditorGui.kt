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
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * Item Property Editor GUI.
 * Allows the administrator to configure an item's [Rarity] tier,
 * which container types the item can drop in, and sell price before saving.
 */
class ItemEditorGui(
    private val plugin: LuminarisCorePlugin,
    private val targetStack: ItemStack,
    private val existingItem: ExtractionItem? = null
) : EditorGuiHolder {

    private lateinit var inventory: Inventory
    private var currentRarity: Rarity = existingItem?.rarity ?: Rarity.fromPrice(existingItem?.price ?: 100.0)
    private var currentPrice: Double = existingItem?.price ?: when (currentRarity) {
        Rarity.COMMON -> 50.0
        Rarity.UNCOMMON -> 150.0
        Rarity.RARE -> 500.0
        Rarity.EPIC -> 1500.0
        Rarity.LEGENDARY -> 4000.0
    }
    private val allowedContainers: MutableSet<String> = existingItem?.allowedContainers?.toMutableSet() ?: mutableSetOf()

    private val raritySlots = mapOf(
        20 to Rarity.COMMON,
        21 to Rarity.UNCOMMON,
        22 to Rarity.RARE,
        23 to Rarity.EPIC,
        24 to Rarity.LEGENDARY
    )

    private val containerSlots = listOf(28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 41, 42, 43)
    private val slotToCategoryMap = mutableMapOf<Int, ContainerCategory>()

    fun open(player: Player) {
        slotToCategoryMap.clear()
        val title = TranslationManager.render("editor.gui.item_edit_title")
        inventory = Bukkit.createInventory(this, 54, title)

        GuiUtils.fill54Border(inventory, Material.GRAY_STAINED_GLASS_PANE)

        // Slot 13: Item Preview
        val previewStack = targetStack.clone()
        previewStack.editMeta { meta ->
            val lore = meta.lore()?.toMutableList() ?: mutableListOf()
            lore.add(Component.empty())
            lore.add(TranslationManager.parseComponent("<color:#555555>----------------------------------------</color>"))
            val rarityLocalized = TranslationManager.get("rarity.${currentRarity.name.lowercase()}")
            lore.add(TranslationManager.render(
                "editor.gui.item_preview_selected_rarity",
                "tag" to currentRarity.miniMessageTag,
                "name" to rarityLocalized
            ))
            val catText = if (allowedContainers.isEmpty()) {
                TranslationManager.get("general.all")
            } else {
                allowedContainers.joinToString(", ")
            }
            lore.add(TranslationManager.render("editor.gui.item_preview_containers", "containers" to catText))
            lore.add(TranslationManager.render("editor.gui.item_preview_price", "price" to String.format("%.2f", currentPrice)))
            lore.add(TranslationManager.parseComponent("<color:#555555>----------------------------------------</color>"))
            meta.lore(lore)
        }
        inventory.setItem(13, previewStack)

        // Slots 20..24: Rarity Selector
        for ((slot, rarity) in raritySlots) {
            val isSelected = (rarity == currentRarity)
            val iconMat = when (rarity) {
                Rarity.COMMON -> Material.GRAY_DYE
                Rarity.UNCOMMON -> Material.LIME_DYE
                Rarity.RARE -> Material.LIGHT_BLUE_DYE
                Rarity.EPIC -> Material.PURPLE_DYE
                Rarity.LEGENDARY -> Material.ORANGE_DYE
            }

            val statusText = if (isSelected) {
                TranslationManager.render("editor.gui.rarity_status_selected")
            } else {
                TranslationManager.render("editor.gui.rarity_status_select")
            }

            val rarityName = TranslationManager.get("rarity.${rarity.name.lowercase()}")
            val rarityItem = GuiUtils.createItem(
                iconMat,
                TranslationManager.parseComponent("${rarity.miniMessageTag}<bold>$rarityName</bold></color>"),
                listOf(statusText),
                glowing = isSelected
            )
            inventory.setItem(slot, rarityItem)
        }

        // Slot 28: Toggle ALL Containers
        val isAll = allowedContainers.isEmpty()
        val allContainersItem = GuiUtils.createItem(
            if (isAll) Material.LIME_STAINED_GLASS_PANE else Material.GRAY_STAINED_GLASS_PANE,
            TranslationManager.render("editor.gui.all_containers"),
            listOf(
                if (isAll) {
                    TranslationManager.render("editor.gui.all_containers_allowed")
                } else {
                    TranslationManager.render("editor.gui.all_containers_click")
                }
            ),
            glowing = isAll
        )
        inventory.setItem(28, allContainersItem)

        // Slots 29+: Individual Container Categories Selector
        val categories = plugin.containerRegistry.getAllCategories()
        var slotIdx = 0
        for (category in categories) {
            if (slotIdx >= containerSlots.size - 1) break
            val slot = containerSlots[slotIdx + 1]
            slotToCategoryMap[slot] = category

            val isAllowed = allowedContainers.contains(category.id.lowercase())
            val statusColor = if (isAllowed) TranslationManager.render("editor.gui.cat_status_allowed") else TranslationManager.render("editor.gui.cat_status_not_allowed")
            val glassMat = if (isAllowed) Material.LIME_CONCRETE else Material.RED_CONCRETE

            val catItem = GuiUtils.createItem(
                glassMat,
                category.displayName,
                listOf(
                    TranslationManager.render("editor.gui.cat_card_id", "id" to category.id),
                    statusColor,
                    Component.empty(),
                    TranslationManager.render("editor.gui.cat_card_toggle")
                ),
                glowing = isAllowed
            )
            inventory.setItem(slot, catItem)
            slotIdx++
        }

        // Slot 40: Price Adjustment
        val priceLore = listOf(
            TranslationManager.render("editor.gui.price_card_current", "price" to String.format("%.2f", currentPrice)),
            Component.empty(),
            TranslationManager.render("editor.gui.click_plus_50"),
            TranslationManager.render("editor.gui.click_minus_50"),
            TranslationManager.render("editor.gui.click_plus_250"),
            TranslationManager.render("editor.gui.click_minus_250")
        )
        val priceItem = GuiUtils.createItem(
            Material.GOLD_INGOT,
            TranslationManager.render("editor.gui.btn_adjust_price"),
            priceLore
        )
        inventory.setItem(40, priceItem)

        // Slot 45: Cancel
        inventory.setItem(45, GuiUtils.createItem(Material.BARRIER, TranslationManager.render("editor.gui.btn_close")))

        // Slot 49: Edit in Dialog Window
        val dialogLore = listOf(
            TranslationManager.render("editor.gui.open_dialog_lore"),
            Component.empty(),
            TranslationManager.render("editor.gui.btn_open_dialog")
        )
        inventory.setItem(49, GuiUtils.createItem(Material.WRITABLE_BOOK, TranslationManager.render("editor.gui.btn_open_dialog"), dialogLore, glowing = true))

        // Slot 53: Confirm & Save Item
        val saveLore = listOf(
            TranslationManager.render("editor.gui.save_item_lore_1"),
            Component.empty(),
            TranslationManager.render("editor.gui.save_item_lore_action")
        )
        val saveBtn = GuiUtils.createItem(
            Material.EMERALD_BLOCK,
            TranslationManager.render("editor.gui.btn_save"),
            saveLore,
            glowing = true
        )
        inventory.setItem(53, saveBtn)

        plugin.scheduler.runAtEntity(player) {
            player.openInventory(inventory)
        }
    }

    override fun handleClick(event: InventoryClickEvent, player: Player) {
        if (event.clickedInventory != inventory) return

        val slot = event.slot
        when (slot) {
            45 -> {
                // Cancel
                GuiUtils.playClickSound(player)
                ItemListGui(plugin).open(player)
                return
            }
            40 -> {
                // Adjust Price
                val delta = when (event.click) {
                    ClickType.LEFT -> 50.0
                    ClickType.RIGHT -> -50.0
                    ClickType.SHIFT_LEFT -> 250.0
                    ClickType.SHIFT_RIGHT -> -250.0
                    else -> 0.0
                }
                if (delta != 0.0) {
                    GuiUtils.playClickSound(player)
                    currentPrice = maxOf(0.0, currentPrice + delta)
                    open(player)
                }
                return
            }
            28 -> {
                // Toggle ALL Containers
                GuiUtils.playClickSound(player)
                if (allowedContainers.isEmpty()) {
                    // Turn on specific categories instead of all
                    val allCats = plugin.containerRegistry.getAllCategories().map { it.id.lowercase() }
                    allowedContainers.addAll(allCats)
                } else {
                    // Clear to mean ALL containers
                    allowedContainers.clear()
                }
                open(player)
                return
            }
            49 -> {
                // Open Item Dialog
                GuiUtils.playClickSound(player)
                player.closeInventory()
                com.luminaris.extraction.editor.dialog.ItemDialogBuilder.open(
                    player = player,
                    itemRegistry = plugin.itemRegistry,
                    itemStack = targetStack,
                    existingItem = existingItem,
                    plugin = plugin
                )
                return
            }
            53 -> {
                // Save item to registry
                GuiUtils.playSuccessSound(player)

                val itemId = existingItem?.id ?: run {
                    val baseName = targetStack.type.name.lowercase()
                    val hash = UUID.randomUUID().toString().substring(0, 5)
                    "${baseName}_$hash"
                }

                val finalItem = ExtractionItem(
                    id = itemId,
                    itemStack = targetStack.clone(),
                    price = currentPrice,
                    rarity = currentRarity,
                    weight = 20.0,
                    allowedContainers = allowedContainers.toMutableSet()
                )

                plugin.itemRegistry.register(finalItem, saveToDisk = true)
                TranslationManager.send(player, "editor.gui.item_saved", "id" to itemId)
                ItemListGui(plugin).open(player)
                return
            }
        }

        // Rarity Selection
        val selectedRarity = raritySlots[slot]
        if (selectedRarity != null) {
            GuiUtils.playClickSound(player)
            currentRarity = selectedRarity
            // If price wasn't modified from default, update price based on rarity
            if (existingItem == null) {
                currentPrice = when (currentRarity) {
                    Rarity.COMMON -> 50.0
                    Rarity.UNCOMMON -> 150.0
                    Rarity.RARE -> 500.0
                    Rarity.EPIC -> 1500.0
                    Rarity.LEGENDARY -> 4000.0
                }
            }
            open(player)
            return
        }

        // Container Category Toggle
        val clickedCategory = slotToCategoryMap[slot]
        if (clickedCategory != null) {
            GuiUtils.playClickSound(player)
            val catId = clickedCategory.id.lowercase()
            if (allowedContainers.contains(catId)) {
                allowedContainers.remove(catId)
            } else {
                allowedContainers.add(catId)
            }
            open(player)
            return
        }
    }

    override fun getInventory(): Inventory = inventory
}
