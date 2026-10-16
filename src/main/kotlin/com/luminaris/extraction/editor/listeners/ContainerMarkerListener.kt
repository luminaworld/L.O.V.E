package com.luminaris.extraction.editor.listeners

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.container.PlacedContainer
import com.luminaris.extraction.editor.tool.ContainerMarkerTool
import com.luminaris.extraction.localization.TranslationManager
import com.luminaris.extraction.storage.TemplateStorage
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntityPlaceEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType

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

    /**
     * Handles ArmorStand container marker placement.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityPlace(event: EntityPlaceEvent) {
        val entity = event.entity
        if (entity !is ArmorStand) return

        val player = event.player ?: return
        val item = if (ContainerMarkerTool.isContainerMarker(player.inventory.itemInMainHand)) {
            player.inventory.itemInMainHand
        } else if (ContainerMarkerTool.isContainerMarker(player.inventory.itemInOffHand)) {
            player.inventory.itemInOffHand
        } else {
            return
        }

        val categoryId = ContainerMarkerTool.getCategoryId(item) ?: return
        val world = entity.world

        if (!templateStorage.isTemplateWorld(world)) {
            event.isCancelled = true
            TranslationManager.send(player, "listener.marker.only_template_world")
            TranslationManager.send(player, "listener.marker.template_hint")
            return
        }

        val templateId = templateStorage.resolveTemplateId(world) ?: world.name.lowercase()
        val x = entity.location.blockX
        val y = entity.location.blockY
        val z = entity.location.blockZ

        val containerId = PlacedContainer.generateId(x, y, z)
        val placedContainer = PlacedContainer(
            id = containerId,
            categoryId = categoryId,
            x = x,
            y = y,
            z = z
        )
        templateStorage.addContainer(templateId, placedContainer, saveToDisk = true)

        // Tag entity with container marker PDC
        entity.persistentDataContainer.set(ContainerMarkerTool.KEY_CONTAINER_CATEGORY, PersistentDataType.STRING, categoryId)
        entity.persistentDataContainer.set(ContainerMarkerTool.KEY_CONTAINER_ID, PersistentDataType.STRING, containerId)
        entity.customName(TranslationManager.render("tool.marker.display_name", "label" to categoryId))
        entity.isCustomNameVisible = true

        val centerLoc = entity.location.clone().add(0.0, 1.0, 0.0)
        world.spawnParticle(villagerHappyParticle, centerLoc, 25, 0.35, 0.35, 0.35, 0.05)
        world.playSound(centerLoc, Sound.BLOCK_CHEST_LOCKED, 1.0f, 1.2f)

        TranslationManager.sendActionBar(
            player,
            "listener.marker.actionbar_placed",
            "category" to categoryId,
            "x" to x,
            "y" to y,
            "z" to z
        )
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
     * Handles ArmorStand container marker destruction.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        if (entity !is ArmorStand) return

        val categoryId = entity.persistentDataContainer.get(ContainerMarkerTool.KEY_CONTAINER_CATEGORY, PersistentDataType.STRING)
            ?: run {
                val tmplId = templateStorage.resolveTemplateId(entity.world)
                if (tmplId != null) {
                    templateStorage.getContainerAt(tmplId, entity.location.blockX, entity.location.blockY, entity.location.blockZ)?.categoryId
                } else null
            } ?: return

        event.isCancelled = true

        val damager = event.damager as? Player
        if (damager == null || (!damager.hasPermission("luminaris.editor") && !damager.hasPermission("luminaris.admin"))) {
            if (damager != null) {
                TranslationManager.send(damager, "listener.marker.no_permission_break")
                TranslationManager.sendActionBar(damager, "listener.marker.actionbar_denied")
            }
            return
        }

        val world = entity.world
        val templateId = templateStorage.resolveTemplateId(world)
        if (templateId != null) {
            templateStorage.removeContainer(templateId, entity.location.blockX, entity.location.blockY, entity.location.blockZ, saveToDisk = true)
        }

        entity.remove()

        val markerTool = ContainerMarkerTool.createContainerMarker(categoryId, Material.ARMOR_STAND)
        val leftover = damager.inventory.addItem(markerTool)
        if (leftover.isNotEmpty()) {
            world.dropItemNaturally(entity.location, markerTool)
        }

        TranslationManager.send(
            damager,
            "listener.marker.chat_removed",
            "category" to categoryId,
            "x" to entity.location.blockX,
            "y" to entity.location.blockY,
            "z" to entity.location.blockZ,
            "template" to (templateId ?: world.name)
        )
        TranslationManager.sendActionBar(
            damager,
            "listener.marker.actionbar_removed",
            "category" to categoryId,
            "x" to entity.location.blockX,
            "y" to entity.location.blockY,
            "z" to entity.location.blockZ
        )
        world.playSound(entity.location, Sound.BLOCK_CHEST_CLOSE, 1.0f, 0.8f)
    }

    /**
     * Prevents players from manipulating or stealing items from ArmorStand container markers.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onArmorStandManipulate(event: PlayerArmorStandManipulateEvent) {
        val entity = event.rightClicked
        if (entity.persistentDataContainer.has(ContainerMarkerTool.KEY_CONTAINER_CATEGORY, PersistentDataType.STRING)) {
            event.isCancelled = true
        }
    }

    /**
     * Protects ArmorStand container markers from environmental and explosion damage.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageEvent) {
        val entity = event.entity
        if (entity is ArmorStand && entity.persistentDataContainer.has(ContainerMarkerTool.KEY_CONTAINER_CATEGORY, PersistentDataType.STRING)) {
            if (event.cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                event.isCancelled = true
            }
        }
    }

    /**
     * Intercepts player interaction on registered drop box container blocks.
     * Prevents default vanilla block interfaces and routes to DropBoxGui (or drop test simulation).
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.hand != EquipmentSlot.HAND) return

        val block = event.clickedBlock ?: return
        val world = block.world
        val player = event.player

        val itemInHand = player.inventory.itemInMainHand
        if (ContainerMarkerTool.isContainerMarker(itemInHand)) {
            // Placing marker tool - allow place event to process
            return
        }

        val templateId = templateStorage.resolveTemplateId(world)
        val match = plugin.matchManager.getMatchByWorld(world)

        val container = (if (templateId != null) {
            templateStorage.getContainerAt(templateId, block.x, block.y, block.z)
        } else if (match != null) {
            match.templateData.containers.find { it.x == block.x && it.y == block.y && it.z == block.z }
        } else {
            null
        }) ?: return

        // Intercept vanilla block interaction
        event.isCancelled = true

        val category = plugin.containerRegistry.getCategory(container.categoryId)
        if (category == null) {
            TranslationManager.send(player, "editor.gui.container_not_found", "id" to container.categoryId)
            return
        }

        val isAdmin = player.hasPermission("luminaris.admin") || player.hasPermission("luminaris.editor")

        // Admin sneak-click: simulate real game drop opening
        if (player.isSneaking && isAdmin) {
            plugin.dropBoxManager.openSimulation(player, category, block.type)
            return
        }

        // Live match container opening
        if (match != null) {
            plugin.dropBoxManager.openMatchContainer(player, world, container, block.type)
            return
        }

        // In template world without sneaking: show tip to admin
        if (isAdmin) {
            TranslationManager.sendActionBar(player, "listener.marker.hint_sneak_click")
        }
    }

    /**
     * Intercepts player interaction on registered ArmorStand container markers.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerInteractEntity(event: PlayerInteractAtEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        val entity = event.rightClicked
        if (entity !is ArmorStand) return

        val player = event.player
        val world = entity.world

        val categoryId = entity.persistentDataContainer.get(ContainerMarkerTool.KEY_CONTAINER_CATEGORY, PersistentDataType.STRING)
            ?: run {
                val tmplId = templateStorage.resolveTemplateId(world)
                if (tmplId != null) {
                    templateStorage.getContainerAt(tmplId, entity.location.blockX, entity.location.blockY, entity.location.blockZ)?.categoryId
                } else null
            } ?: return

        event.isCancelled = true

        val category = plugin.containerRegistry.getCategory(categoryId)
        if (category == null) {
            TranslationManager.send(player, "editor.gui.container_not_found", "id" to categoryId)
            return
        }

        val isAdmin = player.hasPermission("luminaris.admin") || player.hasPermission("luminaris.editor")
        val match = plugin.matchManager.getMatchByWorld(world)

        if (player.isSneaking && isAdmin) {
            plugin.dropBoxManager.openSimulation(player, category, Material.ARMOR_STAND)
            return
        }

        if (match != null) {
            val placedContainer = match.templateData.containers.find {
                it.x == entity.location.blockX && it.y == entity.location.blockY && it.z == entity.location.blockZ
            } ?: PlacedContainer("c_as", categoryId, entity.location.blockX, entity.location.blockY, entity.location.blockZ)
            plugin.dropBoxManager.openMatchContainer(player, world, placedContainer, Material.ARMOR_STAND)
            return
        }

        if (isAdmin) {
            TranslationManager.sendActionBar(player, "listener.marker.hint_sneak_click")
        }
    }
}
