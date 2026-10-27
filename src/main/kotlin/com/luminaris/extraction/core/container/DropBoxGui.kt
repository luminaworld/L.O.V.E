package com.luminaris.extraction.core.container

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.item.ExtractionItem
import com.luminaris.extraction.core.item.Rarity
import com.luminaris.extraction.editor.gui.EditorGuiHolder
import com.luminaris.extraction.editor.tool.ContainerMarkerTool
import com.luminaris.extraction.localization.TranslationManager
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Interactive Drop Box Inventory GUI handling loot discovery, initial covered black glass phase,
 * sequential timed stone block reveal based on item configuration, and session resumption.
 */
class DropBoxGui(
    private val plugin: LuminarisCorePlugin,
    private val category: ContainerCategory,
    private val session: ContainerSearchSession,
    private val isSimulation: Boolean = false,
    private val onSessionUpdate: (() -> Unit)? = null
) : EditorGuiHolder {

    private lateinit var inventory: Inventory
    private var searchTask: ScheduledTask? = null

    fun open(player: Player) {
        val title = if (isSimulation) {
            TranslationManager.render(
                "drop.gui_title_sim",
                "category" to TranslationManager.serializeComponent(category.displayName)
            )
        } else {
            TranslationManager.render(
                "drop.gui_title_match",
                "category" to TranslationManager.serializeComponent(category.displayName)
            )
        }

        inventory = Bukkit.createInventory(this, 27, title)

        // Opening audio feedback based on container material
        val openSound = ContainerMarkerTool.getOpenSound(session.containerMaterial)
        player.playSound(player.location, openSound, 1.0f, 1.0f)

        // Case 1: Initial Covered Black Glass Phase (Container search not yet complete)
        if (!session.isContainerSearchComplete) {
            val coveredItem = createCoveredPlaceholder()
            for (i in 0 until 27) {
                inventory.setItem(i, coveredItem.clone())
            }

            if (isSimulation && session.containerSearchElapsedSeconds == 0) {
                TranslationManager.send(
                    player,
                    "drop.sim_started",
                    "category" to category.id,
                    "count" to session.slots.size
                )
            }

            plugin.scheduler.runAtEntity(player) {
                player.openInventory(inventory)
            }

            startSearchLoop(player)
            return
        }

        // Case 2: Container Search already completed previously (reopening or direct phase 2)
        // Immediately display already-revealed items, and show searching blocks for unrevealed ones
        populatePostContainerSearchInventory()

        // If the entire container search was already finished and all items were revealed
        if (session.isOverallFinished) {
            plugin.scheduler.runAtEntity(player) {
                player.openInventory(inventory)
            }
            return
        }

        // Reopening mid-search: Unrevealed items continue countdown
        plugin.scheduler.runAtEntity(player) {
            player.openInventory(inventory)
        }

        startSearchLoop(player)
    }

    /**
     * Populates the inventory with revealed items and unrevealed stone placeholders.
     */
    private fun populatePostContainerSearchInventory() {
        // Clear all slots first
        for (i in 0 until 27) {
            inventory.setItem(i, null)
        }

        for (i in session.slots.indices) {
            val slotState = session.slots[i]
            if (slotState.isRevealed) {
                if (!slotState.isTaken) {
                    val stack = slotState.currentStack ?: slotState.item.itemStack.clone()
                    inventory.setItem(slotState.slot, stack)
                }
            } else {
                val isActive = (i == session.currentSearchingIndex)
                val placeholder = createStonePlaceholder(slotState, isActive = isActive)
                inventory.setItem(slotState.slot, placeholder)
            }
        }
    }

    private fun createCoveredPlaceholder(): ItemStack {
        val mat = category.coveredMaterial
        val stack = ItemStack(mat)
        stack.editMeta { meta ->
            meta.displayName(TranslationManager.render("drop.searching_title"))
            meta.lore(listOf(TranslationManager.render("drop.searching_lore_desc")))
            meta.setEnchantmentGlintOverride(true)
        }
        return stack
    }

    private fun createStonePlaceholder(slot: SearchSlotState, isActive: Boolean): ItemStack {
        val mat = category.searchingMaterial
        val stone = ItemStack(mat)
        stone.editMeta { meta ->
            if (isActive) {
                meta.displayName(TranslationManager.render("drop.searching_title"))
                meta.lore(listOf(TranslationManager.render("drop.searching_lore_desc")))
                meta.setEnchantmentGlintOverride(true)
            } else {
                meta.displayName(TranslationManager.render("drop.waiting_title"))
                meta.lore(listOf(TranslationManager.render("drop.waiting_lore")))
                meta.setEnchantmentGlintOverride(false)
            }
        }
        return stone
    }

    private fun startSearchLoop(player: Player) {
        searchTask?.cancel()

        searchTask = plugin.scheduler.runAtEntityRepeating(player, 20L, 20L, task = {
            if (!player.isOnline || player.openInventory.topInventory != inventory) {
                searchTask?.cancel()
                searchTask = null
                return@runAtEntityRepeating
            }

            // Phase 1: Container Search (All black glass)
            if (!session.isContainerSearchComplete) {
                session.containerSearchElapsedSeconds++

                playSearchSound(player)
                TranslationManager.sendActionBar(player, "drop.searching_actionbar")

                if (session.containerSearchElapsedSeconds >= session.containerSearchTotalSeconds) {
                    session.isContainerSearchComplete = true

                    // Transition: clear black glass
                    for (i in 0 until 27) {
                        inventory.setItem(i, null)
                    }

                    if (session.slots.isEmpty()) {
                        // Empty container
                        session.isOverallFinished = true
                        searchTask?.cancel()
                        searchTask = null
                        TranslationManager.sendActionBar(player, "drop.empty_container")
                        if (isSimulation) {
                            TranslationManager.send(player, "drop.empty_container")
                        }
                        return@runAtEntityRepeating
                    }

                    // Populate stone placeholders on loot slots
                    populatePostContainerSearchInventory()

                    // If all rolled items happen to have 0 search time, reveal immediately
                    if (session.currentSearchingIndex < session.slots.size &&
                        session.slots[session.currentSearchingIndex].targetRounds <= 0
                    ) {
                        revealCurrentSlot(player)
                    }
                }
                return@runAtEntityRepeating
            }

            // Phase 2: Item by Item Sequential Countdown
            if (session.currentSearchingIndex >= session.slots.size) {
                session.isOverallFinished = true
                searchTask?.cancel()
                searchTask = null
                TranslationManager.sendActionBar(player, "drop.search_complete")
                player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f)
                return@runAtEntityRepeating
            }

            val currentSlot = session.slots[session.currentSearchingIndex]
            currentSlot.currentRound++

            playSearchSound(player, currentSlot.currentRound)
            TranslationManager.sendActionBar(player, "drop.searching_actionbar")

            // Update active stone block visual state
            val updatedStone = createStonePlaceholder(currentSlot, isActive = true)
            inventory.setItem(currentSlot.slot, updatedStone)

            if (currentSlot.currentRound >= currentSlot.targetRounds) {
                revealCurrentSlot(player)
            }
        })
    }

    private fun revealCurrentSlot(player: Player) {
        if (session.currentSearchingIndex >= session.slots.size) return
        val currentSlot = session.slots[session.currentSearchingIndex]
        currentSlot.isRevealed = true

        val stack = runCatching { currentSlot.item.itemStack.clone() }.getOrDefault(currentSlot.item.itemStack)
        currentSlot.currentStack = stack
        inventory.setItem(currentSlot.slot, stack)

        // Play reveal sound based on item configuration
        playRevealSound(player, currentSlot.item)

        val rarityName = TranslationManager.get("rarity.${currentSlot.item.rarity.name.lowercase()}")
        val itemName = if (stack.hasItemMeta() && stack.itemMeta.hasDisplayName()) {
            TranslationManager.serializeComponent(stack.itemMeta.displayName()!!)
        } else {
            stack.type.name
        }
        TranslationManager.sendActionBar(
            player,
            "drop.revealed_item",
            "tag" to currentSlot.item.rarity.miniMessageTag,
            "name" to itemName,
            "rarity" to rarityName
        )

        // Advance to next unrevealed slot
        session.currentSearchingIndex++

        if (session.currentSearchingIndex < session.slots.size) {
            val nextSlot = session.slots[session.currentSearchingIndex]
            val nextStone = createStonePlaceholder(nextSlot, isActive = true)
            inventory.setItem(nextSlot.slot, nextStone)
        } else {
            // All slots revealed!
            session.isOverallFinished = true
            searchTask?.cancel()
            searchTask = null
            TranslationManager.sendActionBar(player, "drop.search_complete")
            playSoundFromConfig(player, "drop_box.sounds.complete", Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f)
        }
    }

    private fun playSoundFromConfig(player: Player, configPath: String, defaultSound: Sound, volume: Float, pitch: Float) {
        val soundName = runCatching {
            plugin.config.getString(configPath)
        }.getOrNull()
        val sound = if (!soundName.isNullOrBlank()) {
            runCatching { Sound.valueOf(soundName.uppercase(java.util.Locale.ROOT).replace(".", "_")) }.getOrNull() ?: defaultSound
        } else {
            defaultSound
        }
        player.playSound(player.location, sound, volume, pitch)
    }

    private fun playSearchSound(player: Player, round: Int = 0) {
        val customSound = category.searchSound
        if (!customSound.isNullOrBlank()) {
            val soundEnum = runCatching { Sound.valueOf(customSound.uppercase(java.util.Locale.ROOT).replace(".", "_")) }.getOrNull()
            if (soundEnum != null) {
                player.playSound(player.location, soundEnum, 0.85f, 1.0f)
                return
            }
        }
        player.playSound(
            player.location,
            Sound.BLOCK_STONE_HIT,
            0.85f,
            1.0f + (round * 0.08f).coerceAtMost(0.4f)
        )
        player.playSound(player.location, Sound.BLOCK_GRAVEL_HIT, 0.6f, 1.2f)
    }

    private fun playRevealSound(player: Player, item: ExtractionItem) {
        // 1. Check item-specific reveal sound
        val itemSound = item.revealSound
        if (!itemSound.isNullOrBlank()) {
            val soundEnum = runCatching { Sound.valueOf(itemSound.uppercase(java.util.Locale.ROOT).replace(".", "_")) }.getOrNull()
            if (soundEnum != null) {
                player.playSound(player.location, soundEnum, 1.0f, 1.0f)
                return
            } else {
                runCatching {
                    player.playSound(player.location, itemSound.lowercase(java.util.Locale.ROOT), 1.0f, 1.0f)
                    return
                }
            }
        }

        // 2. Check category-level fallback reveal sound
        val catSound = category.revealSound
        if (!catSound.isNullOrBlank()) {
            val soundEnum = runCatching { Sound.valueOf(catSound.uppercase(java.util.Locale.ROOT).replace(".", "_")) }.getOrNull()
            if (soundEnum != null) {
                player.playSound(player.location, soundEnum, 1.0f, 1.0f)
                return
            }
        }

        // 3. Check config rarity reveal sound
        val cfgRaritySound = runCatching {
            plugin.config.getString("drop_box.rarity_reveal_sounds.${item.rarity.name}")
        }.getOrNull()
        if (!cfgRaritySound.isNullOrBlank()) {
            val soundEnum = runCatching { Sound.valueOf(cfgRaritySound.uppercase(java.util.Locale.ROOT).replace(".", "_")) }.getOrNull()
            if (soundEnum != null) {
                player.playSound(player.location, soundEnum, 1.0f, 1.0f)
                return
            }
        }

        // 4. Default Rarity fallback
        when (item.rarity) {
            Rarity.COMMON -> player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f)
            Rarity.UNCOMMON -> player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.2f)
            Rarity.RARE -> player.playSound(player.location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.4f)
            Rarity.EPIC -> player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.6f)
            Rarity.LEGENDARY -> {
                player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f)
                player.playSound(player.location, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f)
            }
        }
    }

    override fun handleClick(event: InventoryClickEvent, player: Player) {
        val slot = event.slot
        if (event.clickedInventory != inventory) {
            return
        }

        // Prevent taking placeholder while container search is active
        if (!session.isContainerSearchComplete) {
            playSoundFromConfig(player, "drop_box.sounds.click_searching_error", Sound.UI_BUTTON_CLICK, 0.6f, 0.5f)
            TranslationManager.sendActionBar(player, "drop.still_searching")
            return
        }

        // Prevent taking stone block while item is still searching
        val searching = session.slots.find { it.slot == slot && !it.isRevealed }
        if (searching != null) {
            playSoundFromConfig(player, "drop_box.sounds.click_searching_error", Sound.UI_BUTTON_CLICK, 0.6f, 0.5f)
            TranslationManager.sendActionBar(player, "drop.still_searching")
            return
        }

        // Allow taking revealed item
        val revealedSlot = session.slots.find { it.slot == slot && it.isRevealed && !it.isTaken }
        val stack = inventory.getItem(slot)
        if (revealedSlot != null && stack != null && !stack.isEmpty &&
            stack.type != category.searchingMaterial && stack.type != category.coveredMaterial
        ) {
            val leftover = player.inventory.addItem(stack.clone())
            if (leftover.isEmpty()) {
                inventory.setItem(slot, null)
                revealedSlot.isTaken = true
                revealedSlot.currentStack = null
                playSoundFromConfig(player, "drop_box.sounds.take_item", Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.2f)
            } else {
                val addedAmount = stack.amount - leftover.values.sumOf { it.amount }
                if (addedAmount > 0) {
                    stack.amount -= addedAmount
                    inventory.setItem(slot, stack)
                    revealedSlot.currentStack = stack.clone()
                    playSoundFromConfig(player, "drop_box.sounds.take_item", Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.2f)
                }
                TranslationManager.sendActionBar(player, "drop.inventory_full")
            }
        }
    }

    override fun handleClose(event: InventoryCloseEvent, player: Player) {
        searchTask?.cancel()
        searchTask = null

        if (isSimulation && session.isOverallFinished) {
            TranslationManager.send(player, "drop.sim_closed")
        }

        // Save session state (revealed items stay revealed, unfinished continue counting on reopen)
        onSessionUpdate?.invoke()
    }

    override fun getInventory(): Inventory = inventory
}
