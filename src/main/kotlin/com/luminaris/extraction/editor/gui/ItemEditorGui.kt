package com.luminaris.extraction.editor.gui

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.item.ExtractionItem
import com.luminaris.extraction.core.item.Rarity
import com.luminaris.extraction.editor.dialog.ItemDialogBuilder
import com.luminaris.extraction.localization.TranslationManager
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * Intermediate GUI layer for an [ExtractionItem].
 * Displays a comprehensive preview of the item and its associated data,
 * allows configuring details (ID, name, lore, rarity, price) via Dialog,
 * selecting allowed drop box types via a nested paginated GUI,
 * and performing quick in-GUI adjustments to rarity and sell price.
 */
class ItemEditorGui(
    private val plugin: LuminarisCorePlugin,
    private val targetStack: ItemStack,
    private val existingItem: ExtractionItem? = null,
    initialRarity: Rarity? = null,
    initialPrice: Double? = null,
    initialAllowedContainers: MutableSet<String>? = null,
    initialItemId: String? = null,
    initialSearchTime: Int? = null,
    initialRevealSound: String? = null
) : EditorGuiHolder {

    private lateinit var inventory: Inventory

    private var currentRarity: Rarity = initialRarity ?: existingItem?.rarity ?: Rarity.fromPrice(existingItem?.price ?: 100.0)
    private var currentPrice: Double = initialPrice ?: existingItem?.price ?: run {
        val cfg = plugin.config
        when (currentRarity) {
            Rarity.COMMON -> cfg.getDouble("economy.default_prices.COMMON", 50.0)
            Rarity.UNCOMMON -> cfg.getDouble("economy.default_prices.UNCOMMON", 150.0)
            Rarity.RARE -> cfg.getDouble("economy.default_prices.RARE", 500.0)
            Rarity.EPIC -> cfg.getDouble("economy.default_prices.EPIC", 1500.0)
            Rarity.LEGENDARY -> cfg.getDouble("economy.default_prices.LEGENDARY", 4000.0)
        }
    }
    private val allowedContainers: MutableSet<String> = initialAllowedContainers ?: existingItem?.allowedContainers?.toMutableSet() ?: mutableSetOf()
    private val currentItemId: String = initialItemId ?: existingItem?.id ?: run {
        val baseName = targetStack.type.name.lowercase()
        val hash = UUID.randomUUID().toString().substring(0, 5)
        "${baseName}_$hash"
    }
    private var currentSearchTime: Int = initialSearchTime ?: existingItem?.searchTime ?: ExtractionItem.defaultSearchTime(currentRarity)
    private var currentRevealSound: String? = initialRevealSound ?: existingItem?.revealSound

    private val soundPresets = listOf(
        "ENTITY_ITEM_PICKUP",
        "BLOCK_NOTE_BLOCK_BELL",
        "BLOCK_AMETHYST_BLOCK_CHIME",
        "ENTITY_PLAYER_LEVELUP",
        "UI_TOAST_CHALLENGE_COMPLETE",
        "BLOCK_BEACON_ACTIVATE",
        "ITEM_ARMOR_EQUIP_DIAMOND",
        "ENTITY_EXPERIENCE_ORB_PICKUP"
    )

    private val raritySlots = mapOf(
        29 to Rarity.COMMON,
        30 to Rarity.UNCOMMON,
        31 to Rarity.RARE,
        32 to Rarity.EPIC,
        33 to Rarity.LEGENDARY
    )

    fun open(player: Player) {
        val title = TranslationManager.render("editor.gui.item_edit_title")
        inventory = Bukkit.createInventory(this, 54, title)

        GuiUtils.fill54Border(inventory, Material.GRAY_STAINED_GLASS_PANE)

        // Slot 10: Give Copy of Item
        val copyLore = listOf(
            TranslationManager.render("editor.gui.item_card_click_copy")
        )
        val copyItem = GuiUtils.createItem(
            Material.CHEST_MINECART,
            TranslationManager.render("editor.gui.item_card_click_copy"),
            copyLore
        )
        inventory.setItem(10, copyItem)

        // Slot 11: Edit Properties via Dialog Window
        val dialogLore = listOf(
            TranslationManager.render("editor.gui.item_dialog_lore_1"),
            Component.empty(),
            TranslationManager.render("editor.gui.item_dialog_lore_action")
        )
        val dialogItem = GuiUtils.createItem(
            Material.WRITABLE_BOOK,
            TranslationManager.render("editor.gui.btn_item_dialog"),
            dialogLore,
            glowing = true
        )
        inventory.setItem(11, dialogItem)

        // Slot 13: Item Preview & Associated Data
        val previewStack = targetStack.clone()
        previewStack.editMeta { meta ->
            val lore = meta.lore()?.toMutableList() ?: mutableListOf()
            lore.add(Component.empty())
            lore.add(TranslationManager.parseComponent("<color:#555555>----------------------------------------</color>"))
            lore.add(TranslationManager.render("editor.gui.item_card_id", "id" to currentItemId))
            val rarityLocalized = TranslationManager.get("rarity.${currentRarity.name.lowercase()}")
            lore.add(TranslationManager.render(
                "editor.gui.item_preview_selected_rarity",
                "tag" to currentRarity.miniMessageTag,
                "name" to rarityLocalized
            ))
            lore.add(TranslationManager.render("editor.gui.item_preview_price", "price" to String.format("%.2f", currentPrice)))

            val isAll = allowedContainers.isEmpty() && !allowedContainers.contains("__none__")
            val catText = if (isAll) {
                TranslationManager.get("general.all")
            } else if (allowedContainers.contains("__none__")) {
                "0"
            } else {
                allowedContainers.joinToString(", ")
            }
            lore.add(TranslationManager.render("editor.gui.item_preview_containers", "containers" to catText))
            lore.add(TranslationManager.render("editor.gui.item_card_search_time", "time" to currentSearchTime))
            lore.add(TranslationManager.render("editor.gui.item_card_reveal_sound", "sound" to (currentRevealSound ?: "Default")))
            lore.add(TranslationManager.parseComponent("<color:#555555>----------------------------------------</color>"))
            meta.lore(lore)
        }
        inventory.setItem(13, previewStack)

        // Slot 15: Select Drop Boxes via Nested GUI
        val isAll = allowedContainers.isEmpty() && !allowedContainers.contains("__none__")
        val statusText = if (isAll) {
            TranslationManager.get("editor.gui.all_containers_allowed_title")
        } else if (allowedContainers.contains("__none__")) {
            "0"
        } else {
            "${allowedContainers.size}"
        }
        val selectBoxesLore = listOf(
            TranslationManager.render("editor.gui.select_containers_lore_1"),
            Component.empty(),
            TranslationManager.render("editor.gui.select_containers_lore_status", "status" to statusText),
            Component.empty(),
            TranslationManager.render("editor.gui.select_containers_lore_action")
        )
        val selectBoxesItem = GuiUtils.createItem(
            Material.CHEST,
            TranslationManager.render("editor.gui.btn_select_containers"),
            selectBoxesLore,
            glowing = true
        )
        inventory.setItem(15, selectBoxesItem)

        // Slot 16: Delete Item Button
        if (existingItem != null) {
            val deleteLore = listOf(
                TranslationManager.render("editor.gui.item_card_click_delete")
            )
            val deleteItem = GuiUtils.createItem(
                Material.LAVA_BUCKET,
                TranslationManager.render("editor.gui.item_card_click_delete"),
                deleteLore
            )
            inventory.setItem(16, deleteItem)
        }

        // Slot 22: Section Header for Rarity & Price Quick Controls
        val headerItem = GuiUtils.createItem(
            Material.COMPARATOR,
            TranslationManager.render("editor.gui.rarity_rates_header"),
            listOf(
                TranslationManager.render("editor.gui.rarity_status_select")
            )
        )
        inventory.setItem(22, headerItem)

        // Slot 28: Search Duration Adjustment Button
        val searchTimeLore = listOf(
            TranslationManager.render("editor.gui.search_time_card_current", "time" to currentSearchTime),
            Component.empty(),
            TranslationManager.render("editor.gui.click_plus_1"),
            TranslationManager.render("editor.gui.click_minus_1"),
            TranslationManager.render("editor.gui.click_plus_5"),
            TranslationManager.render("editor.gui.click_minus_5")
        )
        val searchTimeItem = GuiUtils.createItem(
            Material.CLOCK,
            TranslationManager.render("editor.gui.btn_adjust_search_time"),
            searchTimeLore,
            amount = maxOf(1, currentSearchTime.coerceIn(1, 64))
        )
        inventory.setItem(28, searchTimeItem)

        // Slots 29..33: Rarity Selection Buttons
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

        // Slot 34: Price Adjustment Button
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
        inventory.setItem(34, priceItem)

        // Slot 35: Reveal Sound Selector Button
        val revealSoundLore = listOf(
            TranslationManager.render("editor.gui.reveal_sound_card_current", "sound" to (currentRevealSound ?: "Default")),
            Component.empty(),
            TranslationManager.render("editor.gui.reveal_sound_click_cycle"),
            TranslationManager.render("editor.gui.reveal_sound_click_dialog")
        )
        val revealSoundItem = GuiUtils.createItem(
            Material.JUKEBOX,
            TranslationManager.render("editor.gui.btn_adjust_reveal_sound"),
            revealSoundLore,
            glowing = (currentRevealSound != null)
        )
        inventory.setItem(35, revealSoundItem)

        // Slot 45: Back / Cancel
        inventory.setItem(45, GuiUtils.createItem(Material.ARROW, TranslationManager.render("editor.gui.btn_back")))

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
            10 -> {
                // Give copy of item
                GuiUtils.playSuccessSound(player)
                val leftover = player.inventory.addItem(targetStack.clone())
                for (drop in leftover.values) {
                    player.world.dropItemNaturally(player.location, drop)
                }
                TranslationManager.send(player, "editor.gui.item_given")
                return
            }
            11 -> {
                // Open Item Dialog to edit ID, name, lore, rarity, price, search duration, reveal sound
                GuiUtils.playClickSound(player)
                player.closeInventory()
                ItemDialogBuilder.open(
                    player = player,
                    itemRegistry = plugin.itemRegistry,
                    itemStack = targetStack,
                    existingItem = existingItem,
                    plugin = plugin,
                    currentAllowedContainers = if (allowedContainers.contains("__none__")) emptySet() else allowedContainers,
                    initialSearchTime = currentSearchTime,
                    initialRevealSound = currentRevealSound
                ) { savedItem ->
                    ItemEditorGui(plugin, savedItem.itemStack, savedItem).open(player)
                }
                return
            }
            15 -> {
                // Open Nested GUI to select drop box types
                GuiUtils.playClickSound(player)
                ItemContainerSelectGui(
                    plugin = plugin,
                    targetStack = targetStack,
                    existingItem = existingItem,
                    currentRarity = currentRarity,
                    currentPrice = currentPrice,
                    allowedContainers = allowedContainers,
                    currentItemId = currentItemId,
                    currentSearchTime = currentSearchTime,
                    currentRevealSound = currentRevealSound
                ).open(player, 0)
                return
            }
            16 -> {
                // Delete item
                if (existingItem != null && event.isShiftClick) {
                    GuiUtils.playDeleteSound(player)
                    plugin.itemRegistry.unregister(existingItem.id, saveToDisk = true)
                    TranslationManager.send(player, "editor.gui.item_deleted", "id" to existingItem.id)
                    ItemListGui(plugin).open(player)
                } else if (existingItem != null) {
                    GuiUtils.playErrorSound(player)
                }
                return
            }
            28 -> {
                // Adjust Search Duration (+/-1s, +/-5s)
                val delta = when (event.click) {
                    ClickType.LEFT -> 1
                    ClickType.RIGHT -> -1
                    ClickType.SHIFT_LEFT -> 5
                    ClickType.SHIFT_RIGHT -> -5
                    else -> 0
                }
                if (delta != 0) {
                    GuiUtils.playClickSound(player)
                    currentSearchTime = maxOf(0, minOf(60, currentSearchTime + delta))
                    open(player)
                }
                return
            }
            34 -> {
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
            35 -> {
                // Adjust Reveal Sound
                if (event.isShiftClick) {
                    GuiUtils.playClickSound(player)
                    player.closeInventory()
                    ItemDialogBuilder.open(
                        player = player,
                        itemRegistry = plugin.itemRegistry,
                        itemStack = targetStack,
                        existingItem = existingItem,
                        plugin = plugin,
                        currentAllowedContainers = if (allowedContainers.contains("__none__")) emptySet() else allowedContainers,
                        initialSearchTime = currentSearchTime,
                        initialRevealSound = currentRevealSound
                    ) { savedItem ->
                        ItemEditorGui(plugin, savedItem.itemStack, savedItem).open(player)
                    }
                } else {
                    val currentIndex = soundPresets.indexOf(currentRevealSound)
                    val nextIndex = (currentIndex + 1) % soundPresets.size
                    currentRevealSound = soundPresets[nextIndex]
                    val soundEnum = runCatching { Sound.valueOf(currentRevealSound!!) }.getOrNull()
                    if (soundEnum != null) {
                        player.playSound(player.location, soundEnum, 1.0f, 1.0f)
                    }
                    open(player)
                }
                return
            }
            45 -> {
                // Back to ItemListGui
                GuiUtils.playClickSound(player)
                ItemListGui(plugin).open(player)
                return
            }
            53 -> {
                // Save item to registry
                GuiUtils.playSuccessSound(player)

                val finalAllowed = if (allowedContainers.contains("__none__")) {
                    mutableSetOf()
                } else {
                    allowedContainers.toMutableSet()
                }

                val finalItem = ExtractionItem(
                    id = currentItemId,
                    itemStack = targetStack.clone(),
                    price = currentPrice,
                    rarity = currentRarity,
                    weight = existingItem?.weight ?: 1.0,
                    allowedContainers = finalAllowed,
                    searchTime = currentSearchTime,
                    revealSound = currentRevealSound
                )

                plugin.itemRegistry.register(finalItem, saveToDisk = true)
                TranslationManager.send(player, "editor.gui.item_saved", "id" to currentItemId)
                ItemListGui(plugin).open(player)
                return
            }
        }

        // Rarity Selection
        val selectedRarity = raritySlots[slot]
        if (selectedRarity != null) {
            GuiUtils.playClickSound(player)
            currentRarity = selectedRarity
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
        }
    }

    override fun getInventory(): Inventory = inventory
}
