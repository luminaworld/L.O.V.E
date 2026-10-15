package com.luminaris.extraction.core.item

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.container.ContainerRegistry
import com.luminaris.extraction.core.container.PlacedContainer
import com.luminaris.extraction.core.template.TemplateData
import com.luminaris.extraction.util.FoliaScheduler
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Container
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom

/**
 * High-performance Folia-safe Weighted Loot Populator Engine.
 *
 * Responsibilities:
 * - Traverses container markers within a [TemplateData] map definition.
 * - Schedules regional block state inspection and population via [FoliaScheduler.runAtLocation].
 * - Validates tile entity containers (Chest, Barrel, etc.) and flushes existing contents.
 * - Executes Cumulative Weight Selection to pick randomized extraction loot from [ItemRegistry].
 * - Distributes generated items non-contiguously into randomized slot indices across the inventory.
 */
class LootPopulator(
    plugin: Plugin? = null,
    itemRegistry: ItemRegistry? = null,
    containerRegistry: ContainerRegistry? = null,
    scheduler: FoliaScheduler? = null
) {

    private val activePlugin: Plugin by lazy {
        plugin ?: runCatching { LuminarisCorePlugin.instance }.getOrNull()
            ?: error("LuminarisCorePlugin instance has not been initialized.")
    }

    private val activeItemRegistry: ItemRegistry by lazy {
        itemRegistry ?: (plugin as? LuminarisCorePlugin)?.itemRegistry
            ?: runCatching { LuminarisCorePlugin.instance.itemRegistry }.getOrNull()
            ?: ItemRegistry(activePlugin)
    }

    private val activeContainerRegistry: ContainerRegistry by lazy {
        containerRegistry ?: (plugin as? LuminarisCorePlugin)?.containerRegistry
            ?: runCatching { LuminarisCorePlugin.instance.containerRegistry }.getOrNull()
            ?: ContainerRegistry(activePlugin)
    }

    private val activeScheduler: FoliaScheduler by lazy {
        scheduler ?: (plugin as? LuminarisCorePlugin)?.scheduler
            ?: runCatching { LuminarisCorePlugin.instance.scheduler }.getOrNull()
            ?: runCatching { FoliaScheduler.default }.getOrNull()
            ?: FoliaScheduler(activePlugin)
    }

    private val miniMessage = MiniMessage.miniMessage()

    /**
     * Asynchronously populates all registered containers for a match world across their respective Folia regions.
     *
     * @param world The target instance world.
     * @param templateData The template data containing container placement coordinates and categories.
     * @return [CompletableFuture] resolving to the count of successfully populated containers.
     */
    fun populateMatchWorld(world: World, templateData: TemplateData): CompletableFuture<Int> {
        val totalContainers = templateData.containers.size
        if (totalContainers == 0) {
            logInfo("<gray>No containers registered for template </gray><yellow>${templateData.templateId}</yellow> <gray>in world ${world.name}.</gray>")
            return CompletableFuture.completedFuture(0)
        }

        logInfo("<gray>Initiating regional loot population for </gray><yellow>$totalContainers</yellow> <gray>containers in world </gray><aqua>${world.name}</aqua><gray>...</gray>")

        val populationFutures = templateData.containers.map { placedContainer ->
            val future = CompletableFuture<Boolean>()
            val location = Location(
                world,
                placedContainer.x.toDouble(),
                placedContainer.y.toDouble(),
                placedContainer.z.toDouble()
            )

            val chunkX = placedContainer.x shr 4
            val chunkZ = placedContainer.z shr 4

            // Asynchronously ensure chunk is loaded before scheduling on region thread
            val chunkAsync = runCatching { world.getChunkAtAsync(chunkX, chunkZ) }.getOrNull()
            if (chunkAsync != null) {
                chunkAsync.thenAccept {
                    scheduleContainerPopulation(world, placedContainer, location, future)
                }.exceptionally { ex ->
                    logError("Failed to asynchronously load chunk ($chunkX, $chunkZ) for container ${placedContainer.id}: ${ex.message}", ex)
                    future.complete(false)
                    null
                }
            } else {
                scheduleContainerPopulation(world, placedContainer, location, future)
            }

            future
        }

        return CompletableFuture.allOf(*populationFutures.toTypedArray()).thenApply {
            val successfulCount = populationFutures.count { it.join() }
            logInfo("<green>✔ Successfully populated </green><yellow>$successfulCount/$totalContainers</yellow> <green>containers across regions in world </green><aqua>${world.name}</aqua> <gray>(template: ${templateData.templateId})</gray>")
            successfulCount
        }
    }

    /**
     * Schedules container loot population on the Folia Region thread owning the container [Location].
     */
    private fun scheduleContainerPopulation(
        world: World,
        placedContainer: PlacedContainer,
        location: Location,
        future: CompletableFuture<Boolean>
    ) {
        activeScheduler.runAtLocation(location) {
            try {
                val success = populateContainer(world, placedContainer)
                future.complete(success)
            } catch (t: Throwable) {
                logError("<red>Failed to populate container ${placedContainer.id} at (${placedContainer.x}, ${placedContainer.y}, ${placedContainer.z}):</red> ${t.message}", t)
                future.complete(false)
            }
        }
    }

    /**
     * Populates a single placed container on its owning Folia regional thread.
     *
     * @param world The target world.
     * @param container The placed container descriptor.
     * @return True if the container was found and populated, false otherwise.
     */
    fun populateContainer(world: World, container: PlacedContainer): Boolean {
        val block = world.getBlockAt(container.x, container.y, container.z)
        val state = block.state

        // Ensure the block is an actual tile-entity container (Chest, Barrel, etc.)
        if (state !is Container) {
            logWarning("<yellow>Block at (${container.x}, ${container.y}, ${container.z}) in ${world.name} is ${block.type}, expected Container (Chest, Barrel). Container '${container.id}' skipped.</yellow>")
            return false
        }

        val inventory = state.inventory
        // Clear any existing contents
        inventory.clear()

        val categoryId = container.categoryId
        val category = activeContainerRegistry.getCategory(categoryId)
        if (category == null) {
            logWarning("<yellow>Category '$categoryId' for container '${container.id}' not found in ContainerRegistry. Leaving container empty.</yellow>")
            return false
        }

        // Calculate randomized number of item rolls
        val minRolls = maxOf(0, category.minRolls)
        val maxRolls = maxOf(minRolls, category.maxRolls)
        val rolls = if (maxRolls > minRolls) {
            ThreadLocalRandom.current().nextInt(minRolls, maxRolls + 1)
        } else {
            minRolls
        }

        if (rolls <= 0) {
            state.update(true, false)
            return true
        }

        // Retrieve extraction items eligible for this category
        val candidateItems = activeItemRegistry.getItemsForCategory(categoryId).filter { it.weight > 0.0 }
        if (candidateItems.isEmpty()) {
            logWarning("<yellow>No loot items registered with weight > 0 for category '$categoryId' (container: '${container.id}'). Left empty.</yellow>")
            state.update(true, false)
            return true
        }

        // Execute Cumulative Weight Selection
        val rolledStacks = rollWeightedItems(candidateItems, rolls)

        // Distribute the rolled ItemStacks into randomized slot indices across the inventory
        val invSize = inventory.size
        if (invSize <= 0) {
            return false
        }

        val availableSlots = (0 until invSize).toMutableList()
        availableSlots.shuffle()

        for (stack in rolledStacks) {
            if (availableSlots.isNotEmpty()) {
                val slot = availableSlots.removeAt(0)
                inventory.setItem(slot, stack)
            } else {
                inventory.addItem(stack)
            }
        }

        state.update(true, false)
        return true
    }

    /**
     * Executes Cumulative Weight Selection to roll a requested number of items from candidate extraction items.
     * Pure algorithm, thread-safe and deterministic with regards to weights.
     */
    fun rollWeightedItems(candidates: List<ExtractionItem>, count: Int): List<ItemStack> {
        val selectedItems = rollWeightedCandidateItems(candidates, count)
        return selectedItems.map { runCatching { it.itemStack.clone() }.getOrDefault(it.itemStack) }
    }

    /**
     * Executes Cumulative Weight Selection to select a list of [ExtractionItem] models based on their relative weights.
     *
     * @param candidates Eligible extraction item entries.
     * @param count Number of items to roll.
     * @return List of selected [ExtractionItem] instances.
     */
    fun rollWeightedCandidateItems(candidates: List<ExtractionItem>, count: Int): List<ExtractionItem> {
        if (count <= 0 || candidates.isEmpty()) return emptyList()

        val totalWeight = candidates.sumOf { it.weight }
        if (totalWeight <= 0.0) return emptyList()

        val results = ArrayList<ExtractionItem>(count)
        val random = ThreadLocalRandom.current()

        for (i in 0 until count) {
            val roll = random.nextDouble() * totalWeight
            var cumulative = 0.0
            var selected: ExtractionItem = candidates.last()

            for (candidate in candidates) {
                cumulative += candidate.weight
                if (roll < cumulative) {
                    selected = candidate
                    break
                }
            }

            results.add(selected)
        }

        return results
    }

    private fun logInfo(message: String) {
        Bukkit.getConsoleSender().sendMessage(miniMessage.deserialize("<gradient:#00FFA3:#00B8FF><bold>[LootPopulator]</bold></gradient> $message"))
    }

    private fun logWarning(message: String) {
        Bukkit.getConsoleSender().sendMessage(miniMessage.deserialize("<gradient:#FFAA00:#FF5500><bold>[LootPopulator]</bold></gradient> $message"))
    }

    private fun logError(message: String, throwable: Throwable? = null) {
        Bukkit.getConsoleSender().sendMessage(miniMessage.deserialize("<gradient:#FF0055:#FF5555><bold>[LootPopulator]</bold></gradient> $message"))
        if (throwable != null) {
            runCatching { activePlugin.logger.log(java.util.logging.Level.SEVERE, "[LootPopulator] $message", throwable) }
        }
    }
}
