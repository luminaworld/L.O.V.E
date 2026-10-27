package com.luminaris.extraction.core.container

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.editor.tool.ContainerMarkerTool
import com.luminaris.extraction.util.FoliaScheduler
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Shulker
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * High-performance Folia-safe Container Highlight & Glowing Service.
 *
 * Implements line-of-sight raycast occlusion so extraction drop boxes and container
 * markers receive an authentic white glowing outline without penetrating solid walls.
 * Fully supports double chests (highlighting both halves) and ambient lighting calculation
 * to prevent solid container blocks (Barrels, Furnaces, Bookshelves) from turning pitch black.
 */
class ContainerHighlightService(
    private val plugin: LuminarisCorePlugin,
    private val scheduler: FoliaScheduler = plugin.scheduler
) {

    companion object {
        const val TEAM_NAME = "lumina_glow"
        val KEY_HIGHLIGHT_HELPER = NamespacedKey("luminaris", "container_highlight_helper")
    }

    /**
     * Data holder representing a tracked container highlight.
     */
    data class HighlightEntry(
        val x: Int,
        val y: Int,
        val z: Int,
        val categoryId: String,
        val isArmorStand: Boolean,
        val helperEntityIds: MutableList<UUID> = CopyOnWriteArrayList(),
        var partnerX: Int? = null,
        var partnerZ: Int? = null
    ) {
        val locationKey: String get() = "${x}_${y}_${z}"
    }

    // World Name -> (Location Key -> HighlightEntry)
    private val worldEntries = ConcurrentHashMap<String, ConcurrentHashMap<String, HighlightEntry>>()

    // Entity UUID -> HighlightEntry
    private val helperToEntry = ConcurrentHashMap<UUID, HighlightEntry>()

    private var heartbeatTask: ScheduledTask? = null
    private var team: Team? = null

    var isEnabled: Boolean = true
        private set
    var maxDistance: Double = 16.0
        private set
    var checkIntervalTicks: Long = 6L
        private set
    var mode: String = "BLOCK_DISPLAY"
        private set
    var fullbright: Boolean = false
        private set
    var glowColor: NamedTextColor = NamedTextColor.WHITE
        private set
    var teamName: String = TEAM_NAME
        private set
    var matchWorldsEnabled: Boolean = true
        private set
    var templateWorldsEnabled: Boolean = true
        private set

    /**
     * Initializes configuration, scoreboard team, and commences heartbeat polling.
     */
    fun start() {
        reloadConfig()
        setupScoreboardTeam()

        if (!isEnabled) return

        // Folia repeating global heartbeat evaluating line-of-sight across online operatives
        heartbeatTask = scheduler.runGlobalRepeating(checkIntervalTicks, checkIntervalTicks) {
            tick()
        }
    }

    /**
     * Reads configuration parameters from plugin config.
     */
    fun reloadConfig() {
        val config = plugin.config
        isEnabled = config.getBoolean("container_highlight.enabled", true)
        maxDistance = config.getDouble("container_highlight.max_distance", 16.0).coerceIn(4.0, 48.0)
        checkIntervalTicks = config.getLong("container_highlight.check_interval_ticks", 6L).coerceIn(1L, 40L)
        mode = config.getString("container_highlight.mode", "BLOCK_DISPLAY")?.uppercase() ?: "BLOCK_DISPLAY"
        fullbright = config.getBoolean("container_highlight.fullbright", false)
        val colorStr = config.getString("container_highlight.glow_color", "WHITE")?.lowercase()?.trim() ?: "white"
        glowColor = NamedTextColor.NAMES.value(colorStr) ?: NamedTextColor.WHITE
        teamName = config.getString("container_highlight.team_name", TEAM_NAME) ?: TEAM_NAME
        matchWorldsEnabled = config.getBoolean("container_highlight.worlds.match", true)
        templateWorldsEnabled = config.getBoolean("container_highlight.worlds.template", true)
        team?.color(glowColor)
    }

    /**
     * Sets up the dedicated scoreboard team for glowing outline effects.
     */
    private fun setupScoreboardTeam() {
        runCatching {
            val scoreboard: Scoreboard = Bukkit.getScoreboardManager().mainScoreboard
            val existingTeam = scoreboard.getTeam(teamName)
            val glowTeam = existingTeam ?: scoreboard.registerNewTeam(teamName)
            glowTeam.color(glowColor)
            glowTeam.setCanSeeFriendlyInvisibles(false)
            glowTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
            glowTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER)
            team = glowTeam
        }
    }

    /**
     * Periodic global heartbeat dispatching line-of-sight checks per online player on their Folia entity threads.
     */
    fun tick() {
        if (!isEnabled) return

        for (player in Bukkit.getOnlinePlayers()) {
            if (!player.isOnline) continue
            val world = player.world
            if (!isWorldEligible(world)) continue

            val map = getContainersInWorld(world)
            if (map.isEmpty()) continue

            // Run on player's Folia EntityScheduler
            scheduler.runAtEntity(player) {
                if (player.isOnline && !player.isDead) {
                    evaluatePlayerHighlights(player, map.values)
                }
            }
        }
    }

    /**
     * Checks if a world should render container glowing.
     */
    fun isWorldEligible(world: World): Boolean {
        if (!isEnabled) return false
        val isMatch = plugin.matchManager.getMatchByWorld(world) != null
        if (isMatch) return matchWorldsEnabled

        val isTemplate = plugin.templateStorage.isTemplateWorld(world)
        if (isTemplate) return templateWorldsEnabled

        return false
    }

    /**
     * Retrieves active container highlight entries for a world, auto-registering template containers if needed.
     */
    fun getContainersInWorld(world: World): Map<String, HighlightEntry> {
        var map = worldEntries[world.name]
        if (map == null || map.isEmpty()) {
            val templateId = plugin.templateStorage.resolveTemplateId(world)
            if (templateId != null) {
                val templateData = plugin.templateStorage.getTemplate(templateId)
                if (templateData != null && templateData.containers.isNotEmpty()) {
                    registerContainers(world, templateData.containers)
                    map = worldEntries[world.name]
                }
            }
        }
        return map ?: emptyMap()
    }

    /**
     * Registers a container marker for line-of-sight highlight tracking.
     */
    fun registerContainer(world: World, container: PlacedContainer) {
        val map = worldEntries.computeIfAbsent(world.name) { ConcurrentHashMap() }
        val key = "${container.x}_${container.y}_${container.z}"

        val block = world.getBlockAt(container.x, container.y, container.z)
        val isArmorStand = block.type == Material.ARMOR_STAND || runCatching {
            world.getNearbyEntities(block.location.clone().add(0.5, 1.0, 0.5), 1.0, 1.2, 1.0)
                .any { it is ArmorStand }
        }.getOrDefault(false)

        var partnerX: Int? = null
        var partnerZ: Int? = null
        if (ContainerMarkerTool.isChest(block.type)) {
            val partner = ContainerMarkerTool.getAdjacentOrConnectedChest(block)
            if (partner != null) {
                partnerX = partner.x
                partnerZ = partner.z
            }
        }

        val existing = map[key]
        val entry = if (existing != null) {
            existing.partnerX = partnerX
            existing.partnerZ = partnerZ
            existing
        } else {
            HighlightEntry(
                x = container.x,
                y = container.y,
                z = container.z,
                categoryId = container.categoryId,
                isArmorStand = isArmorStand,
                partnerX = partnerX,
                partnerZ = partnerZ
            ).also { map[key] = it }
        }

        // Spawn or link helper entities across regional threads
        val location = Location(world, container.x.toDouble(), container.y.toDouble(), container.z.toDouble())
        scheduler.runAtLocation(location) {
            spawnOrAttachHelper(world, entry)
        }
    }

    /**
     * Registers a list of containers (e.g. on match commencement or template load).
     */
    fun registerContainers(world: World, containers: List<PlacedContainer>) {
        for (container in containers) {
            registerContainer(world, container)
        }
    }

    /**
     * Unregisters a container coordinate and removes all active helper entities (including double chest partners).
     */
    fun unregisterContainer(world: World, x: Int, y: Int, z: Int) {
        val map = worldEntries[world.name] ?: return
        val key = "${x}_${y}_${z}"
        val entry = map.remove(key) ?: return

        clearEntryHelpers(world, entry)
    }

    /**
     * Clears all spawned helper entities belonging to a highlight entry.
     */
    private fun clearEntryHelpers(world: World, entry: HighlightEntry) {
        val idsToRemove = ArrayList(entry.helperEntityIds)
        entry.helperEntityIds.clear()

        for (id in idsToRemove) {
            helperToEntry.remove(id)
            val entity = Bukkit.getEntity(id)
            if (entity != null && !entry.isArmorStand) {
                team?.removeEntity(entity)
                entity.remove()
            }
        }
    }

    /**
     * Spawns highlight entities (BlockDisplay or Shulker) for primary and partner blocks,
     * or enlists an existing ArmorStand.
     */
    private fun spawnOrAttachHelper(world: World, entry: HighlightEntry) {
        clearEntryHelpers(world, entry)

        val location = Location(world, entry.x.toDouble(), entry.y.toDouble(), entry.z.toDouble())

        if (entry.isArmorStand) {
            val armorStand = world.getNearbyEntities(location.clone().add(0.5, 1.0, 0.5), 1.0, 1.2, 1.0)
                .filterIsInstance<ArmorStand>()
                .firstOrNull()

            if (armorStand != null) {
                armorStand.isGlowing = true
                entry.helperEntityIds.add(armorStand.uniqueId)
                helperToEntry[armorStand.uniqueId] = entry
                team?.addEntity(armorStand)

                for (player in world.players) {
                    player.hideEntity(plugin, armorStand)
                }
                return
            }
        }

        // Spawn primary helper entity
        val primaryHelper = spawnBlockHelper(world, entry.x, entry.y, entry.z, entry)
        if (primaryHelper != null) {
            entry.helperEntityIds.add(primaryHelper.uniqueId)
        }

        // Spawn partner helper entity for double chest if connected
        val px = entry.partnerX
        val pz = entry.partnerZ
        if (px != null && pz != null) {
            val partnerLoc = Location(world, px.toDouble(), entry.y.toDouble(), pz.toDouble())
            scheduler.runAtLocation(partnerLoc) {
                val partnerHelper = spawnBlockHelper(world, px, entry.y, pz, entry)
                if (partnerHelper != null) {
                    entry.helperEntityIds.add(partnerHelper.uniqueId)
                }
            }
        }
    }

    /**
     * Spawns a single block helper (BlockDisplay or Shulker) with appropriate lighting and glowing.
     */
    private fun spawnBlockHelper(world: World, x: Int, y: Int, z: Int, entry: HighlightEntry): Entity? {
        val location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
        val block = world.getBlockAt(x, y, z)
        if (block.type == Material.AIR) return null

        if (mode == "SHULKER") {
            val centerLoc = location.clone().add(0.5, 0.0, 0.5)
            val shulker = world.spawn(centerLoc, Shulker::class.java) { s ->
                s.isInvisible = true
                s.isGlowing = true
                s.setAI(false)
                s.isInvulnerable = true
                s.isSilent = true
                s.isCollidable = false
                s.isPersistent = false
                s.persistentDataContainer.set(KEY_HIGHLIGHT_HELPER, PersistentDataType.BYTE, 1.toByte())
            }
            helperToEntry[shulker.uniqueId] = entry
            team?.addEntity(shulker)

            for (player in world.players) {
                player.hideEntity(plugin, shulker)
            }
            return shulker
        } else {
            // Modern BlockDisplay mode with custom ambient brightness
            val brightness = calculateBlockBrightness(world, x, y, z)
            val display = world.spawn(location, BlockDisplay::class.java) { d ->
                d.block = block.blockData
                d.brightness = brightness
                d.isGlowing = true
                d.isPersistent = false
                d.transformation = Transformation(
                    Vector3f(-0.001f, -0.001f, -0.001f),
                    AxisAngle4f(),
                    Vector3f(1.002f, 1.002f, 1.002f),
                    AxisAngle4f()
                )
                d.persistentDataContainer.set(KEY_HIGHLIGHT_HELPER, PersistentDataType.BYTE, 1.toByte())
            }
            helperToEntry[display.uniqueId] = entry
            team?.addEntity(display)

            for (player in world.players) {
                player.hideEntity(plugin, display)
            }
            return display
        }
    }

    /**
     * Calculates ambient room brightness for container blocks, preventing solid opaque blocks
     * (Barrels, Furnaces, Bookshelves) from rendering as pitch black cubes.
     */
    fun calculateBlockBrightness(world: World, x: Int, y: Int, z: Int): Display.Brightness {
        if (fullbright) {
            return Display.Brightness(15, 15)
        }

        val block = world.getBlockAt(x, y, z)
        var maxBlockLight = 0
        var maxSkyLight = 0

        // 1. If the block itself is non-occluding (e.g. Chest, Ender Chest), sample its direct light
        if (!block.type.isOccluding) {
            maxBlockLight = maxOf(maxBlockLight, block.lightFromBlocks.toInt())
            maxSkyLight = maxOf(maxSkyLight, block.lightFromSky.toInt())
        }

        // 2. Sample all 6 adjacent neighbor faces (UP, NORTH, SOUTH, EAST, WEST, DOWN)
        val faces = arrayOf(
            BlockFace.UP,
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.DOWN
        )

        for (face in faces) {
            val neighbor = block.getRelative(face)
            if (!neighbor.type.isOccluding) {
                maxBlockLight = maxOf(maxBlockLight, neighbor.lightFromBlocks.toInt())
                maxSkyLight = maxOf(maxSkyLight, neighbor.lightFromSky.toInt())
            }
        }

        // 3. Fallback and minimum floor to eliminate the pitch black display bug:
        // If ambient light is detected from torches/sky, clamp to at least 12, or 15 if totally dark
        val finalBlock = if (maxBlockLight > 0 || maxSkyLight > 0) maxOf(maxBlockLight, 12) else 15
        val finalSky = if (maxSkyLight > 0) maxSkyLight else 15

        return Display.Brightness(finalBlock.coerceIn(0, 15), finalSky.coerceIn(0, 15))
    }

    /**
     * Evaluates line-of-sight raycasts for a single player against nearby container entries.
     */
    private fun evaluatePlayerHighlights(player: Player, entries: Collection<HighlightEntry>) {
        val eye = player.eyeLocation
        val world = eye.world
        val maxDistSq = maxDistance * maxDistance

        for (entry in entries) {
            if (entry.helperEntityIds.isEmpty()) continue

            val centerLoc = if (entry.isArmorStand) {
                Location(world, entry.x + 0.5, entry.y + 1.0, entry.z + 0.5)
            } else {
                Location(world, entry.x + 0.5, entry.y + 0.5, entry.z + 0.5)
            }
            val distSq = eye.distanceSquared(centerLoc)

            val hasLos = if (distSq <= maxDistSq) {
                hasLineOfSight(player, world, entry)
            } else {
                false
            }

            for (entityId in entry.helperEntityIds) {
                val entity = Bukkit.getEntity(entityId) ?: continue
                if (hasLos) {
                    if (!player.canSee(entity)) {
                        player.showEntity(plugin, entity)
                    }
                } else {
                    if (player.canSee(entity)) {
                        player.hideEntity(plugin, entity)
                    }
                }
            }
        }
    }

    /**
     * Checks if player has unobstructed line of sight to the container block (or partner chest) or ArmorStand.
     */
    fun hasLineOfSight(player: Player, world: World, entry: HighlightEntry): Boolean {
        if (entry.isArmorStand) {
            return hasArmorStandLineOfSight(player, world, entry)
        }
        return hasBlockLineOfSight(player, world, entry)
    }

    /**
     * Comprehensive multi-point line-of-sight check for ArmorStand containers across full height (1.975m)
     * and bounding width. Ensures that even if the armor stand is partially obstructed (up to 80-90% occluded
     * behind slabs, counters, walls, or doorframes), the remaining visible portion (head, helmet, chest,
     * or shoulders) is recognized and the armor stand remains visible to the player.
     */
    fun hasArmorStandLineOfSight(player: Player, world: World, entry: HighlightEntry): Boolean {
        val eye = player.eyeLocation
        val armorStand = entry.helperEntityIds.firstNotNullOfOrNull { id ->
            Bukkit.getEntity(id) as? ArmorStand
        }

        val baseX = armorStand?.location?.x ?: (entry.x + 0.5)
        val baseY = armorStand?.location?.y ?: entry.y.toDouble()
        val baseZ = armorStand?.location?.z ?: (entry.z + 0.5)

        // 1. Head / Helmet points (top ~20% of the armor stand):
        // Solves the scenario where the lower 80% is hidden behind a 1.5-block wall or counter
        if (checkRayTrace(eye, Location(world, baseX, baseY + 1.85, baseZ), entry)) return true
        if (checkRayTrace(eye, Location(world, baseX, baseY + 1.60, baseZ), entry)) return true

        // 2. Chest / Upper Torso (upper 30-40% of the armor stand):
        // Solves the scenario where the lower 50-60% is hidden behind a 1-block wall, table, or chest
        if (checkRayTrace(eye, Location(world, baseX, baseY + 1.30, baseZ), entry)) return true

        // 3. Shoulders / Arms (horizontal extremities ±0.25m):
        // Solves the scenario where the armor stand is peeking around a pillar, doorframe, or vertical corner
        if (checkRayTrace(eye, Location(world, baseX + 0.25, baseY + 1.35, baseZ), entry)) return true
        if (checkRayTrace(eye, Location(world, baseX - 0.25, baseY + 1.35, baseZ), entry)) return true
        if (checkRayTrace(eye, Location(world, baseX, baseY + 1.35, baseZ + 0.25), entry)) return true
        if (checkRayTrace(eye, Location(world, baseX, baseY + 1.35, baseZ - 0.25), entry)) return true

        // 4. Waist / Mid Torso:
        if (checkRayTrace(eye, Location(world, baseX, baseY + 0.95, baseZ), entry)) return true

        // 5. Knees / Lower Body:
        if (checkRayTrace(eye, Location(world, baseX, baseY + 0.55, baseZ), entry)) return true

        // 6. Feet / Base Plate (lower 10% of the armor stand):
        // Solves the scenario where the upper body is behind a ceiling/overhang and only feet are visible
        if (checkRayTrace(eye, Location(world, baseX, baseY + 0.15, baseZ), entry)) return true

        return false
    }

    /**
     * Line-of-sight evaluation for standard container blocks (Chests, Barrels, etc.).
     */
    fun hasBlockLineOfSight(player: Player, world: World, entry: HighlightEntry): Boolean {
        val eye = player.eyeLocation
        val cx = entry.x + 0.5
        val cy = entry.y + 0.5
        val cz = entry.z + 0.5

        // 1. Raycast to center of primary container
        if (checkRayTrace(eye, Location(world, cx, cy, cz), entry)) return true

        // 2. If center is obstructed by a slab or edge, check upper top surface
        if (checkRayTrace(eye, Location(world, cx, entry.y + 0.85, cz), entry)) return true

        // 3. Check 4 horizontal perimeter points (corners/sides peeking around obstacles)
        if (checkRayTrace(eye, Location(world, cx + 0.35, cy, cz), entry)) return true
        if (checkRayTrace(eye, Location(world, cx - 0.35, cy, cz), entry)) return true
        if (checkRayTrace(eye, Location(world, cx, cy, cz + 0.35), entry)) return true
        if (checkRayTrace(eye, Location(world, cx, cy, cz - 0.35), entry)) return true

        // 4. If double chest, check partner chest center, top, and perimeter
        val px = entry.partnerX
        val pz = entry.partnerZ
        if (px != null && pz != null) {
            val pcx = px + 0.5
            val pcz = pz + 0.5
            if (checkRayTrace(eye, Location(world, pcx, cy, pcz), entry)) return true
            if (checkRayTrace(eye, Location(world, pcx, entry.y + 0.85, pcz), entry)) return true
            if (checkRayTrace(eye, Location(world, pcx + 0.35, cy, pcz), entry)) return true
            if (checkRayTrace(eye, Location(world, pcx - 0.35, cy, pcz), entry)) return true
            if (checkRayTrace(eye, Location(world, pcx, cy, pcz + 0.35), entry)) return true
            if (checkRayTrace(eye, Location(world, pcx, cy, pcz - 0.35), entry)) return true
        }

        return false
    }

    private fun checkRayTrace(eye: Location, target: Location, entry: HighlightEntry): Boolean {
        val dir = target.toVector().subtract(eye.toVector())
        val dist = dir.length()
        if (dist < 0.2) return true

        dir.normalize()
        val trace = eye.world.rayTraceBlocks(eye, dir, dist, FluidCollisionMode.NEVER, true)
            ?: return true // Path completely unobstructed

        val hit = trace.hitBlock ?: return true
        if (hit.x == entry.x && hit.z == entry.z) {
            if (entry.isArmorStand) {
                // ArmorStand occupies vertical space across y, y+1, and potentially y+2
                if (hit.y == entry.y || hit.y == entry.y + 1 || hit.y == entry.y + 2) {
                    return true
                }
            } else if (hit.y == entry.y) {
                return true
            }
        }

        // Double chest partner hit
        val px = entry.partnerX
        val pz = entry.partnerZ
        if (px != null && pz != null && hit.x == px && hit.y == entry.y && hit.z == pz) {
            return true
        }

        return false
    }

    /**
     * Clears and despawns all highlight entries belonging to a given world.
     */
    fun clearWorld(worldName: String) {
        val map = worldEntries.remove(worldName) ?: return
        for (entry in map.values) {
            clearEntryHelpers(worldName, entry)
        }
    }

    private fun clearEntryHelpers(worldName: String, entry: HighlightEntry) {
        for (id in entry.helperEntityIds) {
            helperToEntry.remove(id)
            val entity = Bukkit.getEntity(id)
            if (entity != null && !entry.isArmorStand) {
                team?.removeEntity(entity)
                entity.remove()
            }
        }
        entry.helperEntityIds.clear()
    }

    /**
     * Handles player joining/switching world: initially hide active helpers.
     */
    fun handlePlayerJoinOrChangeWorld(player: Player) {
        val world = player.world
        val map = getContainersInWorld(world)
        if (map.isEmpty()) return

        scheduler.runAtEntity(player) {
            for (entry in map.values) {
                for (entityId in entry.helperEntityIds) {
                    val entity = Bukkit.getEntity(entityId) ?: continue
                    player.hideEntity(plugin, entity)
                }
            }
        }
    }

    /**
     * Shuts down the service, canceling tasks and purging all helper entities.
     */
    fun shutdown() {
        heartbeatTask?.cancel()
        heartbeatTask = null

        for (map in worldEntries.values) {
            for (entry in map.values) {
                for (id in entry.helperEntityIds) {
                    val entity = Bukkit.getEntity(id)
                    if (entity != null && !entry.isArmorStand) {
                        team?.removeEntity(entity)
                        entity.remove()
                    }
                }
                entry.helperEntityIds.clear()
            }
        }
        worldEntries.clear()
        helperToEntry.clear()

        team?.let {
            runCatching {
                for (entry in it.entries) {
                    it.removeEntry(entry)
                }
                it.unregister()
            }
        }
        team = null
    }
}
