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
import java.util.concurrent.ThreadLocalRandom

/**
 * Interactive Drop Box Inventory GUI handling loot discovery, searching animation,
 * timed stone block reveal based on rarity tiers, and live test simulations.
 */
class DropBoxGui(
    private val plugin: LuminarisCorePlugin,
    private val category: ContainerCategory,
    private val isSimulation: Boolean = false,
    private val containerMaterial: Material = Material.CHEST,
    private val existingLoot: MutableMap<Int, ItemStack>? = null,
    private val onSaveLoot: ((Map<Int, ItemStack>) -> Unit)? = null
) : EditorGuiHolder {

    private lateinit var inventory: Inventory
    private var searchTask: ScheduledTask? = null
    private var currentElapsedSeconds = 0
    private var currentSearchingIndex = 0
    private var isSearchComplete = false

    data class SearchingSlot(
        val slot: Int,
        val item: ExtractionItem,
        val targetRounds: Int,
        var currentRound: Int = 0,
        var isRevealed: Boolean = false
    )

    private val searchingSlots = mutableListOf<SearchingSlot>()
    private val revealedItems = mutableMapOf<Int, ItemStack>()

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
        val openSound = ContainerMarkerTool.getOpenSound(containerMaterial)
        player.playSound(player.location, openSound, 1.0f, 1.0f)

        // Check if reopening an already searched container in match
        if (existingLoot != null && existingLoot.isNotEmpty()) {
            for ((slot, stack) in existingLoot) {
                if (slot in 0..26) {
                    inventory.setItem(slot, stack.clone())
                    revealedItems[slot] = stack.clone()
                }
            }
            isSearchComplete = true
            plugin.scheduler.runAtEntity(player) {
                player.openInventory(inventory)
            }
            return
        }

        // Fresh roll of candidate items
        val candidateItems = plugin.itemRegistry.getItemsForCategory(category.id).filter { it.weight > 0.0 }
        val minRolls = maxOf(0, category.minRolls)
        val maxRolls = maxOf(minRolls, category.maxRolls)
        val rolls = if (maxRolls > minRolls) {
            ThreadLocalRandom.current().nextInt(minRolls, maxRolls + 1)
        } else {
            minRolls
        }

        if (rolls <= 0 || candidateItems.isEmpty()) {
            isSearchComplete = true
            TranslationManager.sendActionBar(player, "drop.empty_container")
            if (isSimulation) {
                TranslationManager.send(player, "drop.empty_container")
            }
            plugin.scheduler.runAtEntity(player) {
                player.openInventory(inventory)
            }
            return
        }

        // Roll candidate extraction items respecting category rarity drop rates
        val rolledCandidates = plugin.lootPopulator.rollCategoryCandidateItems(category, candidateItems, rolls)
        if (rolledCandidates.isEmpty()) {
            isSearchComplete = true
            TranslationManager.sendActionBar(player, "drop.empty_container")
            plugin.scheduler.runAtEntity(player) {
                player.openInventory(inventory)
            }
            return
        }

        // Randomize slot placements across 27 container slots
        val availableSlots = (0 until 27).toMutableList()
        availableSlots.shuffle()

        for (candidate in rolledCandidates) {
            if (availableSlots.isEmpty()) break
            val slot = availableSlots.removeAt(0)

            // Duration:
            // common: 1 round (1s)
            // uncommon: 2 rounds (2s)
            // rare: 3 rounds (3s)
            // epic: 4 rounds (4s)
            // legendary: 5 rounds (5s)
            val targetRounds = when (candidate.rarity) {
                Rarity.COMMON -> 1
                Rarity.UNCOMMON -> 2
                Rarity.RARE -> 3
                Rarity.EPIC -> 4
                Rarity.LEGENDARY -> 5
            }

            val searchingSlot = SearchingSlot(
                slot = slot,
                item = candidate,
                targetRounds = targetRounds
            )
            searchingSlots.add(searchingSlot)
        }

        // Sort searching sequence strictly by lowest slot index ascending ("เริ่มเปิดของจากช่องที่น้อยสุด")
        searchingSlots.sortBy { it.slot }

        // Place initial stone placeholders (active on first item, waiting on subsequent items)
        for (i in searchingSlots.indices) {
            val slotInfo = searchingSlots[i]
            val stone = createStonePlaceholder(slotInfo, isActive = (i == 0))
            inventory.setItem(slotInfo.slot, stone)
        }

        if (isSimulation) {
            TranslationManager.send(
                player,
                "drop.sim_started",
                "category" to category.id,
                "count" to searchingSlots.size
            )
        }

        plugin.scheduler.runAtEntity(player) {
            player.openInventory(inventory)
        }

        // Start 1-second interval sequential searching animation loop
        startSearchAnimation(player)
    }

    private fun createStonePlaceholder(slot: SearchingSlot, isActive: Boolean): ItemStack {
        val stone = ItemStack(Material.STONE)
        stone.editMeta { meta ->
            if (isActive) {
                meta.displayName(TranslationManager.render("drop.searching_title"))
                meta.lore(
                    listOf(
                        TranslationManager.render("drop.searching_lore_desc"),
                        TranslationManager.render(
                            "drop.searching_lore_time",
                            "current" to slot.currentRound,
                            "time" to slot.targetRounds
                        )
                    )
                )
                meta.setEnchantmentGlintOverride(true)
            } else {
                meta.displayName(TranslationManager.render("drop.waiting_title"))
                meta.lore(
                    listOf(
                        TranslationManager.render("drop.waiting_lore"),
                        TranslationManager.render(
                            "drop.searching_lore_time",
                            "current" to 0,
                            "time" to slot.targetRounds
                        )
                    )
                )
                meta.setEnchantmentGlintOverride(false)
            }
        }
        return stone
    }

    private fun startSearchAnimation(player: Player) {
        if (searchingSlots.isEmpty()) return

        searchTask = plugin.scheduler.runAtEntityRepeating(player, 20L, 20L, task = {
            if (!player.isOnline || player.openInventory.topInventory != inventory) {
                searchTask?.cancel()
                searchTask = null
                return@runAtEntityRepeating
            }

            if (currentSearchingIndex >= searchingSlots.size) {
                isSearchComplete = true
                searchTask?.cancel()
                searchTask = null
                TranslationManager.sendActionBar(player, "drop.search_complete")
                player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f)
                return@runAtEntityRepeating
            }

            val currentSlot = searchingSlots[currentSearchingIndex]
            currentElapsedSeconds++
            currentSlot.currentRound++

            // Audio searching feedback: exactly 1 sound cycle per second
            player.playSound(
                player.location,
                Sound.BLOCK_STONE_HIT,
                0.85f,
                1.0f + (currentSlot.currentRound * 0.08f).coerceAtMost(0.4f)
            )
            player.playSound(player.location, Sound.BLOCK_GRAVEL_HIT, 0.6f, 1.2f)

            // Send action bar update
            TranslationManager.sendActionBar(
                player,
                "drop.searching_actionbar",
                "round" to currentElapsedSeconds,
                "item_num" to (currentSearchingIndex + 1),
                "total_items" to searchingSlots.size,
                "current" to currentSlot.currentRound,
                "duration" to currentSlot.targetRounds
            )

            // Update active stone block visual state
            val updatedStone = createStonePlaceholder(currentSlot, isActive = true)
            inventory.setItem(currentSlot.slot, updatedStone)

            // Check if current slot has completed its required search duration
            if (currentSlot.currentRound >= currentSlot.targetRounds) {
                currentSlot.isRevealed = true

                val stack = runCatching { currentSlot.item.itemStack.clone() }.getOrDefault(currentSlot.item.itemStack)
                inventory.setItem(currentSlot.slot, stack)
                revealedItems[currentSlot.slot] = stack

                // Play reveal sound according to item rarity tier
                playRevealSound(player, currentSlot.item.rarity)

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

                // Advance sequentially to next slot
                currentSearchingIndex++

                if (currentSearchingIndex < searchingSlots.size) {
                    val nextSlot = searchingSlots[currentSearchingIndex]
                    val nextStone = createStonePlaceholder(nextSlot, isActive = true)
                    inventory.setItem(nextSlot.slot, nextStone)
                } else {
                    // All slots finished!
                    isSearchComplete = true
                    searchTask?.cancel()
                    searchTask = null
                    TranslationManager.sendActionBar(player, "drop.search_complete")
                    player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f)
                }
            }
        })
    }

    private fun playRevealSound(player: Player, rarity: Rarity) {
        when (rarity) {
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

        // Prevent taking stone block while item is still searching
        val searching = searchingSlots.find { it.slot == slot && !it.isRevealed }
        if (searching != null) {
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.6f, 0.5f)
            TranslationManager.sendActionBar(player, "drop.still_searching")
            return
        }

        // Allow taking revealed item
        val stack = inventory.getItem(slot)
        if (stack != null && !stack.isEmpty && stack.type != Material.STONE) {
            val leftover = player.inventory.addItem(stack.clone())
            if (leftover.isEmpty()) {
                inventory.setItem(slot, null)
                revealedItems.remove(slot)
                player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.2f)
            } else {
                val addedAmount = stack.amount - leftover.values.sumOf { it.amount }
                if (addedAmount > 0) {
                    stack.amount -= addedAmount
                    inventory.setItem(slot, stack)
                    revealedItems[slot] = stack.clone()
                    player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.2f)
                }
                TranslationManager.sendActionBar(player, "drop.inventory_full")
            }
        }
    }

    override fun handleClose(event: InventoryCloseEvent, player: Player) {
        searchTask?.cancel()
        searchTask = null

        if (isSimulation) {
            TranslationManager.send(player, "drop.sim_closed")
            return
        }

        // In match mode: force reveal any remaining unrevealed items so no loot is lost
        for (slot in searchingSlots) {
            if (!slot.isRevealed) {
                val stack = runCatching { slot.item.itemStack.clone() }.getOrDefault(slot.item.itemStack)
                revealedItems[slot.slot] = stack
            }
        }

        // Persist remaining items for this container in match memory
        val remaining = mutableMapOf<Int, ItemStack>()
        for (i in 0 until 27) {
            val item = inventory.getItem(i)
            if (item != null && !item.isEmpty && item.type != Material.STONE) {
                remaining[i] = item.clone()
            }
        }
        for ((s, stack) in revealedItems) {
            if (!remaining.containsKey(s) && stack.type != Material.STONE) {
                remaining[s] = stack.clone()
            }
        }

        onSaveLoot?.invoke(remaining)
    }

    override fun getInventory(): Inventory = inventory
}
