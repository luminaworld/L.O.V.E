package com.luminaris.extraction.core.container

import com.luminaris.extraction.LuminarisCorePlugin
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

/**
 * Service managing extraction drop boxes, match loot state persistence,
 * audio feedback, and drop test simulation dispatching.
 */
class DropBoxManager(private val plugin: LuminarisCorePlugin) {

    // Key format: "${worldName}:${x}_${y}_${z}"
    private val matchContainerLoot = ConcurrentHashMap<String, MutableMap<Int, ItemStack>>()

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
     */
    fun openSimulation(player: Player, category: ContainerCategory, containerMaterial: Material = Material.CHEST) {
        val gui = DropBoxGui(
            plugin = plugin,
            category = category,
            isSimulation = true,
            containerMaterial = containerMaterial
        )
        gui.open(player)
    }

    /**
     * Opens an in-game match drop container for an operative, maintaining item states across visits.
     */
    fun openMatchContainer(
        player: Player,
        world: World,
        container: PlacedContainer,
        containerMaterial: Material = Material.CHEST
    ) {
        val key = "${world.name}:${container.x}_${container.y}_${container.z}"
        val category = plugin.containerRegistry.getCategory(container.categoryId) ?: return
        val existing = matchContainerLoot[key]

        val gui = DropBoxGui(
            plugin = plugin,
            category = category,
            isSimulation = false,
            containerMaterial = containerMaterial,
            existingLoot = existing,
            onSaveLoot = { remaining ->
                if (remaining.isEmpty()) {
                    matchContainerLoot.remove(key)
                } else {
                    matchContainerLoot[key] = ConcurrentHashMap(remaining)
                }
            }
        )
        gui.open(player)
    }

    /**
     * Pre-populates or updates loot items for a container coordinate inside an active match world.
     */
    fun setContainerLoot(worldName: String, containerId: String, x: Int, y: Int, z: Int, loot: List<ItemStack>) {
        val key = "${worldName}:${x}_${y}_${z}"
        val map = ConcurrentHashMap<Int, ItemStack>()
        val slots = (0 until 27).toMutableList().apply { shuffle() }
        for (item in loot) {
            if (slots.isNotEmpty()) {
                map[slots.removeAt(0)] = item.clone()
            }
        }
        matchContainerLoot[key] = map
    }

    /**
     * Retrieves the remaining loot items for a match container coordinate.
     */
    fun getContainerLoot(worldName: String, x: Int, y: Int, z: Int): Map<Int, ItemStack>? {
        val key = "${worldName}:${x}_${y}_${z}"
        return matchContainerLoot[key]?.toMap()
    }

    /**
     * Cleans up all cached container inventories for a decommissioned match world.
     */
    fun clearWorld(worldName: String) {
        val prefix = "${worldName}:"
        matchContainerLoot.keys.removeIf { it.startsWith(prefix) }
    }
}
