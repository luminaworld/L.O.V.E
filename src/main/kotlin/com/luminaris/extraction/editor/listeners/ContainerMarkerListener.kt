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
import com.luminaris.extraction.core.container.ContainerHighlightService
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent

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

    private fun getMarkerParticle(): Particle {
        val cfgName = runCatching { plugin.config.getString("editor.marker_place_particle") }.getOrNull()
        if (!cfgName.isNullOrBlank()) {
            val p = runCatching { Particle.valueOf(cfgName.uppercase(java.util.Locale.ROOT)) }.getOrNull()
            if (p != null) return p
        }
        return villagerHappyParticle
    }

    private fun getMarkerParticleCount(): Int =
        runCatching { plugin.config.getInt("editor.marker_place_particle_count", 25) }.getOrDefault(25)

    private fun playEditorSound(world: org.bukkit.World, loc: org.bukkit.Location, configPath: String, defaultSound: Sound, volume: Float, pitch: Float) {
        val soundName = runCatching { plugin.config.getString(configPath) }.getOrNull()
        val sound = if (!soundName.isNullOrBlank()) {
            runCatching { Sound.valueOf(soundName.uppercase(java.util.Locale.ROOT).replace(".", "_")) }.getOrNull() ?: defaultSound
        } else {
            defaultSound
        }
        world.playSound(loc, sound, volume, pitch)
    }

    /**
     * Handles container marker placement.
     * When wooden or copper chests are placed adjacent to an existing registered chest,
     * they are linked as a double chest and counted as a single container.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val block = event.blockPlaced
        val world = block.world
        val player = event.player
        val item = event.itemInHand
        val categoryId = ContainerMarkerTool.getCategoryId(item)

        // Check if placed inside a template world
        if (!templateStorage.isTemplateWorld(world)) {
            if (categoryId != null) {
                event.isCancelled = true
                TranslationManager.send(player, "listener.marker.only_template_world")
                TranslationManager.send(player, "listener.marker.template_hint")
            }
            return
        }

        val templateId = templateStorage.resolveTemplateId(world) ?: world.name.lowercase()

        // Check if placed block is a wooden chest or copper chest adjacent to an existing container chest
        if (ContainerMarkerTool.isChest(block.type)) {
            val adjacentBlock = ContainerMarkerTool.getAdjacentOrConnectedChest(block)
            if (adjacentBlock != null) {
                val existingContainer = templateStorage.getContainerAt(templateId, adjacentBlock.x, adjacentBlock.y, adjacentBlock.z)
                if (existingContainer != null) {
                    // Connect the two chests into a double chest
                    ContainerMarkerTool.connectChests(adjacentBlock, block)
                    plugin.containerHighlightService.registerContainer(world, existingContainer)

                    val centerLoc = block.location.clone().add(0.5, 0.6, 0.5)
                    val pCount = getMarkerParticleCount()
                    world.spawnParticle(getMarkerParticle(), centerLoc, pCount, 0.35, 0.35, 0.35, 0.05)
                    playEditorSound(world, centerLoc, "editor.marker_place_sound", Sound.BLOCK_CHEST_LOCKED, 1.0f, 1.2f)

                    TranslationManager.sendActionBar(
                        player,
                        "listener.marker.actionbar_connected",
                        "category" to existingContainer.categoryId
                    )
                    TranslationManager.send(
                        player,
                        "listener.marker.chat_connected",
                        "category" to existingContainer.categoryId,
                        "template" to templateId
                    )
                    return
                }
            }
        }

        if (categoryId == null) return

        // Calculate coordinate vector (X, Y, Z) relative to world origin / absolute vector
        val x = block.x
        val y = block.y
        val z = block.z

        // Clean up any old safe sessions at this coordinate
        plugin.timedSafeManager.resetContainer(world, x, y, z)

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
        plugin.containerHighlightService.registerContainer(world, placedContainer)

        // Spawn particle effect
        val centerLoc = block.location.clone().add(0.5, 0.6, 0.5)
        val pCount = getMarkerParticleCount()
        world.spawnParticle(getMarkerParticle(), centerLoc, pCount, 0.35, 0.35, 0.35, 0.05)

        // Play feedback sound
        playEditorSound(world, centerLoc, "editor.marker_place_sound", Sound.BLOCK_CHEST_LOCKED, 1.0f, 1.2f)

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
     * Breaking one half of a double chest separates it back into a single chest while preserving
     * the container registration on the remaining half.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        val world = block.world

        // Check if inside template world
        val templateId = templateStorage.resolveTemplateId(world) ?: return

        // Check if block at (x, y, z) or its connected partner is a registered container block
        val directContainer = templateStorage.getContainerAt(templateId, block.x, block.y, block.z)
        val partnerBlock = ContainerMarkerTool.getAdjacentOrConnectedChest(block)
        val partnerContainer = if (directContainer == null && partnerBlock != null) {
            templateStorage.getContainerAt(templateId, partnerBlock.x, partnerBlock.y, partnerBlock.z)
        } else null

        val container = directContainer ?: partnerContainer ?: return
        val player = event.player

        // If an admin with editor permission breaks a registered container block, proceed
        val hasPermission = player.hasPermission("luminaris.editor") || player.hasPermission("luminaris.admin")
        if (!hasPermission) {
            event.isCancelled = true
            TranslationManager.send(player, "listener.marker.no_permission_break")
            TranslationManager.sendActionBar(player, "listener.marker.actionbar_denied")
            return
        }

        // If part of a double chest and partner block is still in the world:
        if (partnerBlock != null && ContainerMarkerTool.areChestMaterialsCompatible(block.type, partnerBlock.type)) {
            val partnerData = partnerBlock.blockData as? org.bukkit.block.data.type.Chest
            if (partnerData != null) {
                partnerData.type = org.bukkit.block.data.type.Chest.Type.SINGLE
                partnerBlock.setBlockData(partnerData, true)
            }

            if (directContainer != null) {
                // Transfer registered container coordinates to remaining chest
                templateStorage.removeContainer(templateId, block.x, block.y, block.z, saveToDisk = false)
                val transferred = PlacedContainer(
                    id = directContainer.id,
                    categoryId = directContainer.categoryId,
                    x = partnerBlock.x,
                    y = partnerBlock.y,
                    z = partnerBlock.z
                )
                templateStorage.addContainer(templateId, transferred, saveToDisk = true)
                plugin.containerHighlightService.unregisterContainer(world, block.x, block.y, block.z)
                plugin.containerHighlightService.registerContainer(world, transferred)
            } else {
                plugin.containerHighlightService.registerContainer(world, container)
            }


            TranslationManager.sendActionBar(
                player,
                "listener.marker.actionbar_separated",
                "category" to container.categoryId
            )
            playEditorSound(world, block.location.clone().add(0.5, 0.5, 0.5), "editor.marker_break_sound", Sound.BLOCK_CHEST_CLOSE, 1.0f, 0.8f)
            return
        }

        // Remove from TemplateStorage
        templateStorage.removeContainer(templateId, block.x, block.y, block.z, saveToDisk = true)
        plugin.containerHighlightService.unregisterContainer(world, block.x, block.y, block.z)
        plugin.timedSafeManager.resetContainer(world, block.x, block.y, block.z)

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
        playEditorSound(world, block.location.clone().add(0.5, 0.5, 0.5), "editor.marker_break_sound", Sound.BLOCK_CHEST_CLOSE, 1.0f, 0.8f)

        // Prevent vanilla drops and return custom marker tool item to the editor
        event.isDropItems = false
        val markerTool = ContainerMarkerTool.createContainerMarker(container.categoryId, block.type)
        val leftover = player.inventory.addItem(markerTool)
        if (leftover.isNotEmpty()) {
            world.dropItemNaturally(block.location, markerTool)
        }
    }

    private fun isContainerBlock(templateId: String, b: Block): Boolean {
        if (templateStorage.getContainerAt(templateId, b.x, b.y, b.z) != null) return true
        val partner = ContainerMarkerTool.getAdjacentOrConnectedChest(b)
        return partner != null && templateStorage.getContainerAt(templateId, partner.x, partner.y, partner.z) != null
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

        blocks.removeIf { b -> isContainerBlock(templateId, b) }
    }

    /**
     * Prevents pistons from moving registered template container blocks.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPistonExtend(event: BlockPistonExtendEvent) {
        val world = event.block.world
        val templateId = templateStorage.resolveTemplateId(world) ?: return
        for (b in event.blocks) {
            if (isContainerBlock(templateId, b)) {
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
            if (isContainerBlock(templateId, b)) {
                event.isCancelled = true
                return
            }
        }
    }

    /**
     * Handles ArmorStand container marker placement.
     * Enables arms and base plate so that armor and hand items can be equipped.
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
        plugin.containerHighlightService.registerContainer(world, placedContainer)

        // Tag entity with container marker PDC
        entity.persistentDataContainer.set(ContainerMarkerTool.KEY_CONTAINER_CATEGORY, PersistentDataType.STRING, categoryId)
        entity.persistentDataContainer.set(ContainerMarkerTool.KEY_CONTAINER_ID, PersistentDataType.STRING, containerId)
        entity.customName(TranslationManager.render("tool.marker.display_name", "label" to categoryId))
        entity.isCustomNameVisible = true
        entity.setArms(true)
        entity.setBasePlate(true)

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
     * Safely returns any equipped armor and hand items to the editor upon removal.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        if (entity.persistentDataContainer.has(ContainerHighlightService.KEY_HIGHLIGHT_HELPER, PersistentDataType.BYTE)) {
            event.isCancelled = true
            return
        }
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
        plugin.containerHighlightService.unregisterContainer(world, entity.location.blockX, entity.location.blockY, entity.location.blockZ)

        // Return equipped armor and held items to the damager
        val eq = entity.equipment
        val equippedItems = listOf(
            eq.helmet, eq.chestplate, eq.leggings, eq.boots,
            eq.itemInMainHand, eq.itemInOffHand
        ).filterNotNull().filter { !it.isEmpty }

        for (item in equippedItems) {
            val left = damager.inventory.addItem(item)
            for (dropped in left.values) {
                world.dropItemNaturally(entity.location, dropped)
            }
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
     * Allows editors in template worlds to equip armor and held items onto ArmorStand container markers,
     * while protecting the armor stands from being manipulated in matches.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onArmorStandManipulate(event: PlayerArmorStandManipulateEvent) {
        val entity = event.rightClicked
        if (entity.persistentDataContainer.has(ContainerMarkerTool.KEY_CONTAINER_CATEGORY, PersistentDataType.STRING)) {
            val player = event.player
            val world = entity.world
            val isAdmin = player.hasPermission("luminaris.admin") || player.hasPermission("luminaris.editor")
            if (templateStorage.isTemplateWorld(world) && isAdmin) {
                if (!entity.hasArms()) {
                    entity.setArms(true)
                }
                // Allow manipulating armor and hand items in editor/template worlds
                return
            }
            // In matches or non-editor worlds, protect the armor stand from being stripped
            event.isCancelled = true
        }
    }

    /**
     * Protects ArmorStand container markers from environmental and explosion damage.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageEvent) {
        val entity = event.entity
        if (entity.persistentDataContainer.has(ContainerHighlightService.KEY_HIGHLIGHT_HELPER, PersistentDataType.BYTE)) {
            event.isCancelled = true
            return
        }
        if (entity is ArmorStand && entity.persistentDataContainer.has(ContainerMarkerTool.KEY_CONTAINER_CATEGORY, PersistentDataType.STRING)) {
            if (event.cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                event.isCancelled = true
            }
        }
    }

    /**
     * Resolves a PlacedContainer for a block by checking direct coordinates or connected double chest partner.
     */
    fun findContainerForBlock(
        world: org.bukkit.World,
        templateId: String?,
        match: com.luminaris.extraction.core.match.MatchInstance?,
        block: Block
    ): PlacedContainer? {
        if (templateId != null) {
            templateStorage.getContainerAt(templateId, block.x, block.y, block.z)?.let { return it }
            val partner = ContainerMarkerTool.getAdjacentOrConnectedChest(block)
            if (partner != null) {
                templateStorage.getContainerAt(templateId, partner.x, partner.y, partner.z)?.let { return it }
            }
        } else if (match != null) {
            match.templateData.containers.find { it.x == block.x && it.y == block.y && it.z == block.z }?.let { return it }
            val partner = ContainerMarkerTool.getAdjacentOrConnectedChest(block)
            if (partner != null) {
                match.templateData.containers.find { it.x == partner.x && it.y == partner.y && it.z == partner.z }?.let { return it }
            }
        }
        return null
    }

    /**
     * Intercepts player interaction on registered drop box container blocks.
     * Prevents default vanilla block interfaces and routes to DropBoxGui (or drop test simulation).
     * Double chest partners resolve to the same container instance.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        // Admin Sneak + Left Click to reset safe countdown in template worlds
        if (event.action == Action.LEFT_CLICK_BLOCK) {
            val block = event.clickedBlock ?: return
            val player = event.player
            val world = block.world
            val isAdmin = player.hasPermission("luminaris.admin") || player.hasPermission("luminaris.editor")
            if (player.isSneaking && isAdmin && templateStorage.isTemplateWorld(world)) {
                val templateId = templateStorage.resolveTemplateId(world)
                val container = findContainerForBlock(world, templateId, null, block)
                if (container != null) {
                    val category = plugin.containerRegistry.getCategory(container.categoryId)
                    if (category != null && plugin.timedSafeManager.isSafeContainer(container, category)) {
                        event.isCancelled = true
                        val reset = plugin.timedSafeManager.resetContainer(world, block.x, block.y, block.z)
                        if (reset) {
                            player.playSound(block.location, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f)
                            world.spawnParticle(org.bukkit.Particle.SMOKE, block.location.clone().add(0.5, 0.5, 0.5), 8, 0.2, 0.2, 0.2, 0.02)
                            TranslationManager.sendActionBar(player, "safe.simulation_reset")
                        }
                        return
                    }
                }
            }
            return
        }

        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.hand != EquipmentSlot.HAND) return

        val block = event.clickedBlock ?: return
        val world = block.world
        val player = event.player

        val itemInHand = player.inventory.itemInMainHand

        // Check if player is holding the SafeSetupTool:
        if (com.luminaris.extraction.editor.tool.SafeSetupTool.isSafeSetupTool(itemInHand)) {
            val isAdmin = player.hasPermission("luminaris.admin") || player.hasPermission("luminaris.editor")
            if (!isAdmin) {
                event.isCancelled = true
                TranslationManager.send(player, "general.no_permission")
                return
            }

            event.isCancelled = true

            // Validate that target block is a supported single-block container
            if (!ContainerMarkerTool.isSupportedMaterial(block.type) || block.type == Material.ARMOR_STAND) {
                TranslationManager.send(player, "safe.tool.unsupported_block")
                return
            }

            // Ensure single block only (no Double Chest)
            if (ContainerMarkerTool.getConnectedDoubleChestBlock(block) != null) {
                TranslationManager.send(player, "safe.tool.no_double_chest")
                return
            }

            val face = event.blockFace
            if (!com.luminaris.extraction.editor.tool.SafeSetupTool.isSupportedFace(face)) {
                TranslationManager.send(player, "safe.tool.must_be_horizontal_face")
                return
            }

            val templateId = templateStorage.resolveTemplateId(world)
            if (templateId == null) {
                TranslationManager.send(player, "safe.tool.must_be_template_world")
                return
            }

            val targetCategory = com.luminaris.extraction.editor.tool.SafeSetupTool.getTargetCategory(itemInHand)
            plugin.timedSafeManager.resetContainer(world, block.x, block.y, block.z)
            val existing = templateStorage.getContainerAt(templateId, block.x, block.y, block.z)

            if (existing != null) {
                val updated = existing.copy(categoryId = targetCategory, displayFace = face.name)
                templateStorage.removeContainer(templateId, block.x, block.y, block.z, false)
                templateStorage.addContainer(templateId, updated, true)
            } else {
                val newContainer = PlacedContainer(
                    id = PlacedContainer.generateId(block.x, block.y, block.z),
                    categoryId = targetCategory,
                    x = block.x,
                    y = block.y,
                    z = block.z,
                    displayFace = face.name
                )
                templateStorage.addContainer(templateId, newContainer, true)
            }

            // Audio & Particle feedback
            player.playSound(block.location, Sound.BLOCK_ANVIL_USE, 0.8f, 1.4f)
            world.spawnParticle(org.bukkit.Particle.WAX_ON, block.location.clone().add(0.5, 0.5, 0.5), 15, 0.3, 0.3, 0.3, 0.05)
            TranslationManager.send(
                player,
                "safe.tool.configured_success",
                "face" to face.name,
                "category" to targetCategory
            )
            return
        }

        if (ContainerMarkerTool.isContainerMarker(itemInHand)) {
            // Placing marker tool - allow place event to process
            return
        }

        val templateId = templateStorage.resolveTemplateId(world)
        val match = plugin.matchManager.getMatchByWorld(world)

        val container = findContainerForBlock(world, templateId, match, block) ?: return

        // Intercept vanilla block interaction
        event.isCancelled = true

        val category = plugin.containerRegistry.getCategory(container.categoryId)
        if (category == null) {
            TranslationManager.send(player, "editor.gui.container_not_found", "id" to container.categoryId)
            return
        }

        val isAdmin = player.hasPermission("luminaris.admin") || player.hasPermission("luminaris.editor")

        // 1. Timed safe box interaction (Handles both live matches and template simulations!)
        if (plugin.timedSafeManager.isSafeContainer(container, category)) {
            // Ensure safe is single block only
            if (ContainerMarkerTool.getConnectedDoubleChestBlock(block) != null) {
                TranslationManager.sendActionBar(player, "safe.single_block_only")
                return
            }

            // Check if not yet unlocked
            if (!plugin.timedSafeManager.isUnlocked(world, container.x, container.y, container.z)) {
                if (plugin.timedSafeManager.isCountingDown(world, container.x, container.y, container.z)) {
                    val remaining = plugin.timedSafeManager.getRemainingSeconds(world, container.x, container.y, container.z)
                    TranslationManager.sendActionBar(player, "safe.counting_down", "seconds" to remaining)
                    player.playSound(player.location, Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.6f)
                } else {
                    // First click! Start countdown (both in live match and in template simulation)
                    plugin.timedSafeManager.startUnlock(player, world, block, container, category)
                }
                return
            }

            // Unlocked! In match, open match container; in template world, open simulation
            if (match != null) {
                plugin.dropBoxManager.openMatchContainer(player, world, container, block.type)
            } else if (isAdmin) {
                // In template simulation: upon opening, clean up the Hologram and reset safe state
                plugin.timedSafeManager.resetContainer(world, container.x, container.y, container.z)
                plugin.dropBoxManager.openSimulation(player, category, block.type)
            }
            return
        }

        // 2. Regular non-safe container interaction
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
     * In template worlds, non-sneaking right clicks allow equipping armor and hand items.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerInteractEntity(event: PlayerInteractAtEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        val entity = event.rightClicked

        if (entity.persistentDataContainer.has(ContainerHighlightService.KEY_HIGHLIGHT_HELPER, PersistentDataType.BYTE)) {
            event.isCancelled = true
            val player = event.player
            val helperBlock = entity.location.block
            val interactEvent = PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, player.inventory.itemInMainHand, helperBlock, org.bukkit.block.BlockFace.UP, EquipmentSlot.HAND)
            onPlayerInteract(interactEvent)
            return
        }

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

        val category = plugin.containerRegistry.getCategory(categoryId)
        if (category == null) {
            event.isCancelled = true
            TranslationManager.send(player, "editor.gui.container_not_found", "id" to categoryId)
            return
        }

        val isAdmin = player.hasPermission("luminaris.admin") || player.hasPermission("luminaris.editor")
        val match = plugin.matchManager.getMatchByWorld(world)

        // Sneak + click: simulate real game drop opening
        if (player.isSneaking && isAdmin) {
            event.isCancelled = true
            plugin.dropBoxManager.openSimulation(player, category, Material.ARMOR_STAND)
            return
        }

        // Live match container opening
        if (match != null) {
            event.isCancelled = true
            if (plugin.timedSafeManager.isSafeCategory(category)) {
                TranslationManager.sendActionBar(player, "safe.armorstand_not_supported")
                return
            }
            val placedContainer = match.templateData.containers.find {
                it.x == entity.location.blockX && it.y == entity.location.blockY && it.z == entity.location.blockZ
            } ?: PlacedContainer("c_as", categoryId, entity.location.blockX, entity.location.blockY, entity.location.blockZ)
            plugin.dropBoxManager.openMatchContainer(player, world, placedContainer, Material.ARMOR_STAND)
            return
        }

        // In template world without sneaking: Allow manipulating armor stand equipment!
        if (templateStorage.isTemplateWorld(world) && isAdmin) {
            if (!entity.hasArms()) {
                entity.setArms(true)
            }
            // Do NOT cancel the event, allowing PlayerArmorStandManipulateEvent to execute
            return
        }

        event.isCancelled = true
        if (isAdmin) {
            TranslationManager.sendActionBar(player, "listener.marker.hint_sneak_click")
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        plugin.containerHighlightService.handlePlayerJoinOrChangeWorld(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        plugin.containerHighlightService.handlePlayerJoinOrChangeWorld(event.player)
    }
}

