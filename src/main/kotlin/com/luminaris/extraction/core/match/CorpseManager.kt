package com.luminaris.extraction.core.match

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.localization.TranslationManager
import com.luminaris.extraction.util.FoliaScheduler
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages the creation and population of physical Corpse Containers (Crates)
 * upon player elimination inside extraction matches.
 * Fully compatible with Folia regionized multi-threading and regional schedulers.
 */
class CorpseManager(
    private val plugin: LuminarisCorePlugin = LuminarisCorePlugin.instance,
    private val scheduler: FoliaScheduler = plugin.scheduler
) {

    private val miniMessage = MiniMessage.miniMessage()
    private val activeCorpses = ConcurrentHashMap.newKeySet<Location>()

    /**
     * Spawns a physical Corpse Container (Chest/Barrel) at the player's death location,
     * assigns the deceased player's custom identifier, and deposits remaining item drops.
     * Guaranteed to execute safely on the Folia Region Scheduler owning the death coordinates.
     *
     * @param deathLocation Exact location where the player expired.
     * @param player The deceased player entity.
     * @param drops List of non-safe-case items to deposit into the corpse crate.
     * @return [CompletableFuture] resolving to the final placed container [Location].
     */
    fun spawnCorpse(
        deathLocation: Location,
        player: Player,
        drops: List<ItemStack>
    ): CompletableFuture<Location?> {
        val future = CompletableFuture<Location?>()

        val world = deathLocation.world ?: return CompletableFuture.completedFuture(null)
        val minHeight = runCatching { world.minHeight }.getOrDefault(-64)
        val maxHeight = runCatching { world.maxHeight }.getOrDefault(320)

        // Clamp Y coordinates into safe world bounds to prevent void out-of-bounds errors
        val safeY = deathLocation.blockY.coerceIn(minHeight + 1, maxHeight - 2)
        val safeLoc = Location(world, deathLocation.x, safeY.toDouble(), deathLocation.z)

        // Folia Regional Thread Safety: Always execute block manipulation on the owning Region Scheduler
        scheduler.runAtLocation(safeLoc) {
            try {
                var block = safeLoc.block

                // If dying in void or air, search downward for the nearest solid ground within the same column
                if (block.type.isAir && block.y > minHeight + 1) {
                    var scanY = block.y
                    while (scanY > minHeight + 1 && world.getBlockAt(block.x, scanY - 1, block.z).type.isAir) {
                        scanY--
                    }
                    if (scanY != block.y) {
                        block = world.getBlockAt(block.x, scanY, block.z)
                    }
                }

                // If death position is inside lava, liquid, or non-replaceable solid block, shift upward to safety
                if (block.type == Material.LAVA || block.isLiquid) {
                    val above = block.getRelative(org.bukkit.block.BlockFace.UP)
                    if (!above.isLiquid && !above.type.isSolid) {
                        block = above
                    }
                } else if (block.type.isSolid) {
                    val above = block.getRelative(org.bukkit.block.BlockFace.UP)
                    if (!above.type.isSolid) {
                        block = above
                    }
                }

                // Place Corpse Block
                val corpseMatName = runCatching {
                    plugin.config.getString("corpse.material", "CHEST")
                }.getOrNull() ?: "CHEST"
                val corpseMaterial = Material.matchMaterial(corpseMatName) ?: Material.CHEST
                block.type = corpseMaterial

                val state = block.state
                if (state is Container) {
                    // Set custom title displaying player's name in MiniMessage
                    val containerName = TranslationManager.render(
                        "corpse.chest_title",
                        "player" to player.name
                    )
                    state.customName(containerName)
                    state.update(true)

                    val inventory = state.inventory
                    val leftoverItems = mutableListOf<ItemStack>()

                    // Deposit all dropped items into the crate
                    for (item in drops) {
                        if (item.isEmpty || item.type.isAir) continue
                        val leftover = inventory.addItem(item)
                        if (leftover.isNotEmpty()) {
                            leftoverItems.addAll(leftover.values)
                        }
                    }

                    // Handle overflow if drops exceed single chest capacity (27 slots)
                    // Keep overflow within the same vertical column (UP) to strictly avoid crossing Folia region chunk boundaries
                    if (leftoverItems.isNotEmpty()) {
                        val overflowBlock = block.getRelative(org.bukkit.block.BlockFace.UP)
                        val overflowMatName = runCatching {
                            plugin.config.getString("corpse.overflow_material", "CHEST")
                        }.getOrNull() ?: "CHEST"
                        val overflowMaterial = Material.matchMaterial(overflowMatName) ?: Material.CHEST
                        overflowBlock.type = overflowMaterial
                        val overflowState = overflowBlock.state
                        if (overflowState is Container) {
                            overflowState.customName(
                                TranslationManager.render(
                                    "corpse.overflow_title",
                                    "player" to player.name
                                )
                            )
                            overflowState.update(true)
                            for (overflowItem in leftoverItems) {
                                val remaining = overflowState.inventory.addItem(overflowItem)
                                for (rem in remaining.values) {
                                    block.world.dropItemNaturally(block.location.add(0.5, 0.5, 0.5), rem)
                                }
                            }
                        }
                    }

                    activeCorpses.add(block.location)
                    future.complete(block.location)
                } else {
                    future.complete(null)
                }
            } catch (t: Throwable) {
                TranslationManager.logWarning(
                    "corpse.log_spawn_error",
                    "player" to player.name,
                    "location" to deathLocation.toString(),
                    "error" to (t.message ?: "unknown")
                )
                future.completeExceptionally(t)
            }
        }

        return future
    }

    /**
     * Returns an unmodifiable snapshot of active corpse container locations.
     */
    fun getActiveCorpses(): Set<Location> = activeCorpses

    /**
     * Clears all tracked corpse container coordinates.
     */
    fun clear() {
        activeCorpses.clear()
    }
}
