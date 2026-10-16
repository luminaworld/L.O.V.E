package com.luminaris.extraction.editor.listeners

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.container.PlacedContainer
import com.luminaris.extraction.editor.tool.ContainerMarkerTool
import com.luminaris.extraction.localization.TranslationManager
import com.luminaris.extraction.storage.TemplateStorage
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent

/**
 * Listener handling the placement, registration, and removal of extraction container markers.
 * Listens for BlockPlaceEvent and BlockBreakEvent for blocks tagged with `luminaris:container_category`.
 */
class ContainerMarkerListener(
    private val plugin: LuminarisCorePlugin,
    private val templateStorage: TemplateStorage
) : Listener {

    private val villagerHappyParticle: Particle by lazy {
        runCatching { Particle.valueOf("HAPPY_VILLAGER") }
            .getOrElse {
                runCatching { Particle.valueOf("VILLAGER_HAPPY") }
                    .getOrDefault(Particle.FLAME)
            }
    }

    /**
     * Handles container marker placement.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val item = event.itemInHand
        val categoryId = ContainerMarkerTool.getCategoryId(item) ?: return

        val player = event.player
        val block = event.blockPlaced
        val world = block.world

        // Check if placed inside a template world
        if (!templateStorage.isTemplateWorld(world)) {
            event.isCancelled = true
            TranslationManager.send(player, "listener.marker.only_template_world")
            TranslationManager.send(player, "listener.marker.template_hint")
            return
        }

        val templateId = templateStorage.resolveTemplateId(world) ?: world.name.lowercase()

        // Calculate coordinate vector (X, Y, Z) relative to world origin / absolute vector
        val x = block.x
        val y = block.y
        val z = block.z

        // Append entry into TemplateStorage for that world
        val containerId = PlacedContainer.generateId(x, y, z)
        val placedContainer = PlacedContainer(
            id = containerId,
            categoryId = categoryId,
            x = x,
            y = y,
            z = z
        )
        templateStorage.addContainer(templateId, placedContainer, saveToDisk = true)

        // Spawn green particle effect VILLAGER_HAPPY
        val centerLoc = block.location.clone().add(0.5, 0.6, 0.5)
        world.spawnParticle(villagerHappyParticle, centerLoc, 25, 0.35, 0.35, 0.35, 0.05)

        // Play sound BLOCK_CHEST_LOCKED
        world.playSound(centerLoc, Sound.BLOCK_CHEST_LOCKED, 1.0f, 1.2f)

        // Send actionbar message
        TranslationManager.sendActionBar(
            player,
            "listener.marker.actionbar_placed",
            "category" to categoryId,
            "x" to x,
            "y" to y,
            "z" to z
        )

        // Confirm in chat
        TranslationManager.send(
            player,
            "listener.marker.chat_placed",
            "category" to categoryId,
            "x" to x,
            "y" to y,
            "z" to z,
            "template" to templateId
        )
    }

    /**
     * Handles container marker destruction.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        val world = block.world

        // Check if inside template world
        val templateId = templateStorage.resolveTemplateId(world) ?: return

        // Check if block at (x, y, z) is a registered container block
        val container = templateStorage.getContainerAt(templateId, block.x, block.y, block.z) ?: return

        val player = event.player

        // If an admin with editor permission breaks a registered container block, remove it from TemplateStorage
        val hasPermission = player.hasPermission("luminaris.editor") || player.hasPermission("luminaris.admin")
        if (!hasPermission) {
            event.isCancelled = true
            TranslationManager.send(player, "listener.marker.no_permission_break")
            TranslationManager.sendActionBar(player, "listener.marker.actionbar_denied")
            return
        }

        // Remove from TemplateStorage
        val removed = templateStorage.removeContainer(templateId, block.x, block.y, block.z, saveToDisk = true)

        // Send message notifying deletion
        TranslationManager.send(
            player,
            "listener.marker.chat_removed",
            "category" to container.categoryId,
            "x" to block.x,
            "y" to block.y,
            "z" to block.z,
            "template" to templateId
        )
        TranslationManager.sendActionBar(
            player,
            "listener.marker.actionbar_removed",
            "category" to container.categoryId,
            "x" to block.x,
            "y" to block.y,
            "z" to block.z
        )

        // Play feedback sound
        world.playSound(block.location.clone().add(0.5, 0.5, 0.5), Sound.BLOCK_CHEST_CLOSE, 1.0f, 0.8f)

        // Prevent vanilla drops and return custom marker tool item to the editor
        event.isDropItems = false
        val markerTool = ContainerMarkerTool.createContainerMarker(container.categoryId, block.type)
        val leftover = player.inventory.addItem(markerTool)
        if (leftover.isNotEmpty()) {
            world.dropItemNaturally(block.location, markerTool)
        }
    }

    /**
     * Prevents explosion damage from destroying registered template containers.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityExplode(event: EntityExplodeEvent) {
        protectBlocksFromExplosion(event.blockList())
    }

    /**
     * Prevents block explosions (beds, anchors, etc.) from destroying registered template containers.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockExplode(event: BlockExplodeEvent) {
        protectBlocksFromExplosion(event.blockList())
    }

    private fun protectBlocksFromExplosion(blocks: MutableList<Block>) {
        if (blocks.isEmpty()) return
        val world = blocks[0].world
        val templateId = templateStorage.resolveTemplateId(world) ?: return

        blocks.removeIf { b ->
            templateStorage.getContainerAt(templateId, b.x, b.y, b.z) != null
        }
    }

    /**
     * Prevents pistons from moving registered template container blocks.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPistonExtend(event: BlockPistonExtendEvent) {
        val world = event.block.world
        val templateId = templateStorage.resolveTemplateId(world) ?: return
        for (b in event.blocks) {
            if (templateStorage.getContainerAt(templateId, b.x, b.y, b.z) != null) {
                event.isCancelled = true
                return
            }
        }
    }

    /**
     * Prevents pistons from retracting registered template container blocks.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPistonRetract(event: BlockPistonRetractEvent) {
        val world = event.block.world
        val templateId = templateStorage.resolveTemplateId(world) ?: return
        for (b in event.blocks) {
            if (templateStorage.getContainerAt(templateId, b.x, b.y, b.z) != null) {
                event.isCancelled = true
                return
            }
        }
    }
}
