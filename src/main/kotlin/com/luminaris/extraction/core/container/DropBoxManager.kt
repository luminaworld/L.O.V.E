package com.luminaris.extraction.core.container

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.item.ExtractionItem
import com.luminaris.extraction.core.item.Rarity
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom

/**
 * Service managing extraction drop boxes, match loot state persistence,
 * search sessions, and drop test simulation dispatching.
 */
class DropBoxManager(private val plugin: LuminarisCorePlugin) {

    // Key format: "${worldName}:${x}_${y}_${z}" or "sim:${uuid}:${categoryId}"
    private val activeSessions = ConcurrentHashMap<String, ContainerSearchSession>()

    /**
     * Opens a live drop simulation for an administrator to test roll chances, animation, and audio.
     */
    fun openSimulation(player: Player, categoryId: String, containerMaterial: Material = Material.CHEST) {
        val category = plugin.containerRegistry.getCategory(categoryId) ?: ContainerCategory(
            id = categoryId,
            displayName = Component.text(categoryId),
            minRolls = 1,
            maxRolls = 3
        )
        openSimulation(player, category, containerMaterial)
    }

    /**
     * Opens a live drop simulation for an administrator using the specified category definition.
     * If an existing search is in-progress for this category, it resumes seamlessly.
     */
    fun openSimulation(player: Player, category: ContainerCategory, containerMaterial: Material = Material.CHEST) {
        val key = "sim:${player.uniqueId}:${category.id}"
        var session = activeSessions[key]

        // If previous simulation was completely finished, start a fresh one on new open
        if (session != null && session.isOverallFinished) {
            activeSessions.remove(key)
            session = null
        }

        if (session == null) {
            session = createNewSession(category, containerMaterial)
            activeSessions[key] = session
        }

        val gui = DropBoxGui(
            plugin = plugin,
            category = category,
            session = session,
            isSimulation = true,
            onSessionUpdate = {
                if (session.isOverallFinished) {
                    activeSessions.remove(key)
                }
            }
        )
        gui.open(player)
    }

    /**
     * Opens an in-game match drop container for an operative, maintaining item states across visits.
     * Unfinished searches continue countdown upon reopening; revealed items display immediately.
     */
    fun openMatchContainer(
        player: Player,
        world: World,
        container: PlacedContainer,
        containerMaterial: Material = Material.CHEST
    ) {
        val key = "${world.name}:${container.x}_${container.y}_${container.z}"
        val category = plugin.containerRegistry.getCategory(container.categoryId) ?: return

        val session = activeSessions.computeIfAbsent(key) {
            createNewSession(category, containerMaterial)
        }

        val gui = DropBoxGui(
            plugin = plugin,
            category = category,
            session = session,
            isSimulation = false,
            onSessionUpdate = {
                // If container completely empty and looted, we can either keep or remove
                if (session.isOverallFinished && session.getAllRemainingLoot().isEmpty()) {
                    // Container looted clean
                }
            }
        )
        gui.open(player)
    }

    /**
     * Creates a fresh ContainerSearchSession for a given category and material.
     */
    fun createNewSession(
        category: ContainerCategory,
        containerMaterial: Material = Material.CHEST
    ): ContainerSearchSession {
        val candidateItems = plugin.itemRegistry.getItemsForCategory(category.id).filter { it.weight > 0.0 }
        val minRolls = maxOf(0, category.minRolls)
        val maxRolls = maxOf(minRolls, category.maxRolls)
        val rolls = if (maxRolls > minRolls) {
            ThreadLocalRandom.current().nextInt(minRolls, maxRolls + 1)
        } else {
            minRolls
        }

        val minSearchSec = maxOf(0, category.searchTimeMin)
        val maxSearchSec = maxOf(minSearchSec, category.searchTimeMax)
        val containerSearchSec = if (maxSearchSec > minSearchSec) {
            ThreadLocalRandom.current().nextInt(minSearchSec, maxSearchSec + 1)
        } else {
            minSearchSec
        }

        val rolledCandidates = if (rolls > 0 && candidateItems.isNotEmpty()) {
            plugin.lootPopulator.rollCategoryCandidateItems(category, candidateItems, rolls)
        } else {
            emptyList()
        }

        val availableSlots = (0 until 27).toMutableList().apply { shuffle() }
        val slotStates = mutableListOf<SearchSlotState>()

        for (candidate in rolledCandidates) {
            if (availableSlots.isEmpty()) break
            val slot = availableSlots.removeAt(0)
            val targetRounds = maxOf(0, candidate.searchTime)
            slotStates.add(
                SearchSlotState(
                    slot = slot,
                    item = candidate,
                    targetRounds = targetRounds
                )
            )
        }

        // Sort searching sequence strictly by lowest slot index ascending ("เริ่มเปิดของจากช่องที่น้อยสุด")
        slotStates.sortBy { it.slot }

        val session = ContainerSearchSession(
            categoryId = category.id,
            containerMaterial = containerMaterial,
            containerSearchTotalSeconds = containerSearchSec,
            containerSearchElapsedSeconds = 0,
            isContainerSearchComplete = (containerSearchSec <= 0),
            slots = slotStates,
            isOverallFinished = false,
            currentSearchingIndex = 0
        )

        return session
    }

    /**
     * Pre-populates or updates loot items for a container coordinate inside an active match world.
     */
    fun setContainerLoot(worldName: String, containerId: String, x: Int, y: Int, z: Int, loot: List<ItemStack>) {
        val key = "${worldName}:${x}_${y}_${z}"
        val slots = (0 until 27).toMutableList().apply { shuffle() }
        val slotStates = mutableListOf<SearchSlotState>()

        for (stack in loot) {
            if (slots.isEmpty()) break
            val slot = slots.removeAt(0)
            val mockItem = ExtractionItem(
                id = stack.type.name.lowercase(),
                itemStack = stack.clone(),
                price = 100.0,
                rarity = Rarity.F,
                weight = 10.0,
                searchTime = 1
            )
            slotStates.add(
                SearchSlotState(
                    slot = slot,
                    item = mockItem,
                    targetRounds = 1,
                    currentRound = 0,
                    isRevealed = false
                )
            )
        }

        slotStates.sortBy { it.slot }

        val session = ContainerSearchSession(
            categoryId = "prepopulated",
            containerMaterial = Material.CHEST,
            containerSearchTotalSeconds = 2,
            containerSearchElapsedSeconds = 0,
            isContainerSearchComplete = false,
            slots = slotStates,
            isOverallFinished = false,
            currentSearchingIndex = 0
        )

        activeSessions[key] = session
    }

    /**
     * Retrieves the remaining loot items for a match container coordinate.
     */
    fun getContainerLoot(worldName: String, x: Int, y: Int, z: Int): Map<Int, ItemStack>? {
        val key = "${worldName}:${x}_${y}_${z}"
        return activeSessions[key]?.getAllRemainingLoot()
    }

    /**
     * Cleans up all cached container inventories for a decommissioned match world.
     */
    fun clearWorld(worldName: String) {
        val prefix = "${worldName}:"
        activeSessions.keys.removeIf { it.startsWith(prefix) }
    }

    /**
     * Clears all cached container inventories upon plugin disable.
     */
    fun shutdown() {
        activeSessions.clear()
    }
}
