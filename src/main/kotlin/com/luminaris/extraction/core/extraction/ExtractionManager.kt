package com.luminaris.extraction.core.extraction

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.item.ItemRegistry
import com.luminaris.extraction.core.safe.SafeCaseManager
import com.luminaris.extraction.localization.TranslationManager
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Core manager service orchestrating extraction zones, eligibility matrices,
 * and Folia-safe regional countdown tasks.
 */
class ExtractionManager(
    private val plugin: Plugin,
    private val itemRegistry: ItemRegistry? = null,
    private val safeCaseManager: SafeCaseManager? = null
) {
    companion object {
        val KEY_ITEM_ID = NamespacedKey("luminaris", "item_id")
        val KEY_ALT_ITEM_ID = NamespacedKey("luminaris", "id")
    }

    private val miniMessage = MiniMessage.miniMessage()

    /** Registered extraction zones indexed by zone ID (case-insensitive). */
    private val zones = ConcurrentHashMap<String, ExtractionZone>()

    /** Active player extraction states. */
    private val playerStates = ConcurrentHashMap<UUID, ExtractionState>()

    /** Player squad/team assignments. */
    private val playerTeams = ConcurrentHashMap<UUID, String>()

    /** Running countdown tasks per player. */
    private val activeTasks = ConcurrentHashMap<UUID, ExtractionCountdownTask>()

    /** Scheduled task handles on the Folia/Paper EntityScheduler. */
    private val scheduledHandles = ConcurrentHashMap<UUID, ScheduledTask>()

    /** Explicit configured lobby location fallback. */
    var lobbyLocation: Location? = null

    /**
     * Resolves the effective item registry.
     */
    private val activeItemRegistry: ItemRegistry?
        get() = itemRegistry ?: (plugin as? LuminarisCorePlugin)?.itemRegistry

    /**
     * Resolves the effective safe case manager.
     */
    private val activeSafeCaseManager: SafeCaseManager?
        get() = safeCaseManager ?: (plugin as? LuminarisCorePlugin)?.safeCaseManager

    // ==========================================
    // Zone Registration & Lookup
    // ==========================================

    /**
     * Registers an extraction zone in memory.
     */
    fun registerZone(zone: ExtractionZone) {
        zones[zone.id.lowercase()] = zone
    }

    /**
     * Unregisters an extraction zone by its ID.
     */
    fun unregisterZone(id: String): ExtractionZone? {
        return zones.remove(id.lowercase())
    }

    /**
     * Retrieves an extraction zone by ID.
     */
    fun getZone(id: String): ExtractionZone? = zones[id.lowercase()]

    /**
     * Returns an unmodifiable snapshot list of all registered extraction zones.
     */
    fun getAllZones(): List<ExtractionZone> = zones.values.toList()

    /**
     * Finds an extraction zone containing the given location coordinate.
     */
    fun getZoneAt(location: Location): ExtractionZone? {
        val vector = location.toVector()
        return zones.values.firstOrNull { it.boundingBox.contains(vector) }
    }

    /**
     * Clears all registered extraction zones from memory.
     */
    fun clearZones() {
        zones.clear()
    }

    // ==========================================
    // Dynamic Eligibility Evaluation Matrix
    // ==========================================

    /**
     * Evaluates if a player meets all extraction criteria for a given zone.
     *
     * Validates:
     * 1. Elapsed match time window [zone.timeStartSeconds, zone.timeEndSeconds]
     * 2. Team-specific squad permissions [zone.allowedTeamIds]
     * 3. Required extraction key items in player inventory or safe pouches
     *
     * @param player The candidate player.
     * @param zone The target extraction zone.
     * @param matchElapsedSeconds Total seconds elapsed since the instance match began.
     * @param teamId The team or squad identifier of the player.
     * @return Pair of (eligible: Boolean, rejectionReason: Component?)
     */
    fun isPlayerEligible(
        player: Player,
        zone: ExtractionZone,
        matchElapsedSeconds: Long,
        teamId: String
    ): Pair<Boolean, Component?> {
        // 1. Time Window Evaluation
        if (zone.type == ExtractionType.TIMED_WINDOW || zone.timeStartSeconds > 0L || zone.timeEndSeconds < Long.MAX_VALUE) {
            if (matchElapsedSeconds < zone.timeStartSeconds) {
                val secondsUntilOpen = zone.timeStartSeconds - matchElapsedSeconds
                val reason = TranslationManager.render(
                    "extraction.reason_inactive",
                    "remaining" to secondsUntilOpen
                )
                return Pair(false, reason)
            }
            if (matchElapsedSeconds > zone.timeEndSeconds) {
                val reason = TranslationManager.render("extraction.reason_expired")
                return Pair(false, reason)
            }
        }

        // 2. Team Specific Squad Evaluation
        if (zone.type == ExtractionType.TEAM_SPECIFIC || zone.allowedTeamIds.isNotEmpty()) {
            val normalizedTeam = teamId.trim().lowercase()
            val matchesTeam = zone.allowedTeamIds.any { it.trim().lowercase() == normalizedTeam }
            if (!matchesTeam) {
                val allowedNames = zone.allowedTeamIds.joinToString(", ")
                val reason = TranslationManager.render(
                    "extraction.reason_restricted",
                    "squads" to allowedNames
                )
                return Pair(false, reason)
            }
        }

        // 3. Conditional Required Item Evaluation
        if (zone.type == ExtractionType.CONDITIONAL_ITEM || !zone.requiredItemId.isNullOrBlank()) {
            val reqItemId = zone.requiredItemId
            if (!reqItemId.isNullOrBlank() && !hasRequiredItem(player, reqItemId)) {
                val displayLabel = resolveItemDisplayLabel(reqItemId)
                val reason = TranslationManager.render(
                    "extraction.reason_requires_item",
                    "item" to displayLabel
                )
                return Pair(false, reason)
            }
        }

        // All checks passed
        return Pair(true, null)
    }

    /**
     * Helper to safely check if a material is air without triggering Paper RegistryAccess exceptions in unit tests.
     */
    private fun isAir(material: Material?): Boolean {
        if (material == null) return true
        if (material.name.equals("AIR", ignoreCase = true) || material.name.endsWith("_AIR")) return true
        return runCatching { material.isAir }.getOrDefault(false)
    }

    /**
     * Checks whether the player carries the required extraction key item in their inventory
     * or within an equipped Safe Pouch.
     */
    fun hasRequiredItem(player: Player, requiredItemId: String): Boolean {
        val target = requiredItemId.trim().lowercase()
        val inv = player.inventory

        // 1. Search player's standard inventory contents (including armor and off-hand)
        for (item in inv.contents) {
            if (item == null) continue
            val itemType = runCatching { item.type }.getOrNull()
            if (isAir(itemType)) continue
            if (matchesItemCriteria(item, target)) {
                return true
            }

            // 2. Inspect safe case contents if the item is a safe pouch
            val safeMgr = activeSafeCaseManager
            if (safeMgr != null && safeMgr.isSafeCase(item)) {
                val meta = item.itemMeta
                if (meta != null) {
                    val pdc = meta.persistentDataContainer
                    val serialized = pdc.get(SafeCaseManager.KEY_SERIALIZED_CONTENTS, PersistentDataType.STRING)
                    if (!serialized.isNullOrBlank()) {
                        val safeContents = SafeCaseManager.itemStackArrayFromBase64(serialized)
                        for (safeItem in safeContents) {
                            if (safeItem != null && !safeItem.isEmpty) {
                                val safeType = runCatching { safeItem.type }.getOrNull()
                                if (!isAir(safeType) && matchesItemCriteria(safeItem, target)) {
                                    return true
                                }
                            }
                        }
                    }
                }
            }
        }

        return false
    }

    /**
     * Consumes one count of the required item from the player's inventory or safe pouch.
     */
    fun consumeRequiredItem(player: Player, requiredItemId: String): Boolean {
        val target = requiredItemId.trim().lowercase()
        val inv = player.inventory

        // Try consuming from player inventory first
        for (i in 0 until inv.size) {
            val item = inv.getItem(i) ?: continue
            if (matchesItemCriteria(item, target)) {
                if (item.amount > 1) {
                    item.amount -= 1
                } else {
                    inv.setItem(i, null)
                }
                return true
            }
        }

        // Fallback: search and consume from inside an equipped safe case
        val safeMgr = activeSafeCaseManager
        if (safeMgr != null) {
            for (i in 0 until inv.size) {
                val item = inv.getItem(i) ?: continue
                if (safeMgr.isSafeCase(item)) {
                    val meta = item.itemMeta ?: continue
                    val pdc = meta.persistentDataContainer
                    val serialized = pdc.get(SafeCaseManager.KEY_SERIALIZED_CONTENTS, PersistentDataType.STRING)
                    if (!serialized.isNullOrBlank()) {
                        val contents = SafeCaseManager.itemStackArrayFromBase64(serialized)
                        var modified = false
                        for (cIdx in contents.indices) {
                            val subItem = contents[cIdx] ?: continue
                            if (matchesItemCriteria(subItem, target)) {
                                if (subItem.amount > 1) {
                                    subItem.amount -= 1
                                } else {
                                    contents[cIdx] = null
                                }
                                modified = true
                                break
                            }
                        }
                        if (modified) {
                            val newSerialized = SafeCaseManager.itemStackArrayToBase64(contents)
                            pdc.set(SafeCaseManager.KEY_SERIALIZED_CONTENTS, PersistentDataType.STRING, newSerialized)
                            item.itemMeta = meta
                            return true
                        }
                    }
                }
            }
        }

        return false
    }

    /**
     * Checks whether a specific ItemStack matches the given required item identifier.
     */
    private fun matchesItemCriteria(item: ItemStack, targetId: String): Boolean {
        // 1. PersistentDataContainer custom ID
        if (runCatching { item.hasItemMeta() }.getOrDefault(false)) {
            val meta = item.itemMeta
            if (meta != null) {
                val pdc = meta.persistentDataContainer
                val pdcId = pdc.get(KEY_ITEM_ID, PersistentDataType.STRING)
                    ?: pdc.get(KEY_ALT_ITEM_ID, PersistentDataType.STRING)
                if (pdcId != null && pdcId.trim().lowercase() == targetId) {
                    return true
                }
            }
        }

        // 2. ItemRegistry registered item match
        val registry = activeItemRegistry
        val regItem = registry?.getItem(targetId)
        if (regItem != null) {
            if (runCatching { item.isSimilar(regItem.itemStack) }.getOrDefault(false)) {
                return true
            }
        }

        // 3. Bukkit Material match
        val itemType = runCatching { item.type }.getOrNull()
        if (itemType != null) {
            if (itemType.name.equals(targetId, ignoreCase = true)) {
                return true
            }
            val mat = runCatching { Material.matchMaterial(targetId) }.getOrNull()
            if (mat != null && itemType == mat) {
                return true
            }
        }

        // 4. Custom Display Name match
        if (runCatching { item.hasItemMeta() }.getOrDefault(false)) {
            val meta = item.itemMeta
            if (meta != null && meta.hasDisplayName()) {
                val plainText = miniMessage.serialize(meta.displayName()!!).lowercase()
                if (plainText.contains(targetId)) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Resolves human-readable label for a required item ID.
     */
    private fun resolveItemDisplayLabel(itemId: String): String {
        val reg = activeItemRegistry?.getItem(itemId)
        if (reg != null) {
            return miniMessage.serialize(reg.resolveDisplayName())
        }
        return itemId.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }

    // ==========================================
    // Player State & Task Management (Folia-Safe)
    // ==========================================

    /**
     * Retrieves the current extraction state of a player.
     */
    fun getPlayerState(player: Player): ExtractionState = getPlayerState(player.uniqueId)

    fun getPlayerState(uuid: UUID): ExtractionState = playerStates.getOrDefault(uuid, ExtractionState.IN_MATCH)

    /**
     * Sets the extraction state of a player.
     */
    fun setPlayerState(player: Player, state: ExtractionState) {
        setPlayerState(player.uniqueId, state)
    }

    fun setPlayerState(uuid: UUID, state: ExtractionState) {
        playerStates[uuid] = state
    }

    /**
     * Retrieves the assigned team/squad ID of a player.
     */
    fun getPlayerTeam(player: Player): String = getPlayerTeam(player.uniqueId)

    fun getPlayerTeam(uuid: UUID): String = playerTeams.getOrDefault(uuid, "default")

    /**
     * Assigns a team/squad ID to a player.
     */
    fun setPlayerTeam(player: Player, teamId: String) {
        setPlayerTeam(player.uniqueId, teamId)
    }

    fun setPlayerTeam(uuid: UUID, teamId: String) {
        playerTeams[uuid] = teamId
    }

    /**
     * Starts the Folia EntityScheduler extraction countdown task for a player.
     */
    fun startExtractionCountdown(
        player: Player,
        matchElapsedProvider: () -> Long,
        teamIdProvider: () -> String = { getPlayerTeam(player) }
    ): ExtractionCountdownTask {
        val uuid = player.uniqueId
        stopExtractionCountdown(player)

        val task = ExtractionCountdownTask(
            plugin = plugin,
            player = player,
            manager = this,
            matchElapsedProvider = matchElapsedProvider,
            teamIdProvider = teamIdProvider,
            zonesProvider = { getAllZones() }
        )
        activeTasks[uuid] = task

        // Schedule at fixed rate on the player's EntityScheduler (1 second = 20 ticks)
        val entityScheduler = player.scheduler
        val scheduled = entityScheduler.runAtFixedRate(
            plugin,
            { scheduledTask -> task.accept(scheduledTask) },
            { cleanupPlayer(uuid) },
            1L,
            20L
        )

        if (scheduled != null) {
            scheduledHandles[uuid] = scheduled
        }

        return task
    }

    /**
     * Stops and cancels any active extraction countdown task for a player.
     */
    fun stopExtractionCountdown(player: Player) {
        stopExtractionCountdown(player.uniqueId)
    }

    fun stopExtractionCountdown(uuid: UUID) {
        scheduledHandles.remove(uuid)?.cancel()
        val task = activeTasks.remove(uuid)
        task?.resetCountdown()
    }

    /**
     * Handles player taking damage: interrupts active extraction countdown if enabled in config.
     */
    fun handlePlayerDamage(player: Player, damage: Double = 0.0) {
        val interruptEnabled = runCatching {
            (plugin as? LuminarisCorePlugin)?.config?.getBoolean("extraction.interrupt_on_damage", true) ?: true
        }.getOrDefault(true)
        if (!interruptEnabled) return

        val threshold = runCatching {
            (plugin as? LuminarisCorePlugin)?.config?.getDouble("extraction.damage_threshold", 0.0) ?: 0.0
        }.getOrDefault(0.0)

        if (damage < threshold) return

        val task = activeTasks[player.uniqueId]
        task?.onDamage()
    }

    /**
     * Purges player data and cancels tasks to prevent memory leaks when player quits or retires.
     */
    fun cleanupPlayer(uuid: UUID) {
        scheduledHandles.remove(uuid)?.cancel()
        activeTasks.remove(uuid)
        playerStates.remove(uuid)
        playerTeams.remove(uuid)
    }

    /**
     * Resolves the safe lobby evacuation destination.
     */
    fun resolveLobbyLocation(): Location {
        lobbyLocation?.let { return it }
        val service = (plugin as? LuminarisCorePlugin)?.instanceWorldService
        if (service != null) {
            runCatching { return service.getEffectiveLobbyLocation() }
        }
        val cfg = (plugin as? LuminarisCorePlugin)?.config
        val cfgWorldName = cfg?.getString("lobby.world")
        if (!cfgWorldName.isNullOrBlank()) {
            val targetWorld = Bukkit.getWorld(cfgWorldName)
            if (targetWorld != null) {
                val x = cfg.getDouble("lobby.x", 0.5)
                val y = cfg.getDouble("lobby.y", 64.0)
                val z = cfg.getDouble("lobby.z", 0.5)
                val yaw = cfg.getDouble("lobby.yaw", 0.0).toFloat()
                val pitch = cfg.getDouble("lobby.pitch", 0.0).toFloat()
                return Location(targetWorld, x, y, z, yaw, pitch)
            }
        }
        val defaultWorld = runCatching { Bukkit.getWorlds().firstOrNull() }.getOrNull()
        return defaultWorld?.spawnLocation ?: Location(defaultWorld, 0.0, 64.0, 0.0)
    }

    /**
     * Shuts down all running extraction countdown tasks and clears player tracking maps.
     */
    fun shutdown() {
        for ((_, handle) in scheduledHandles) {
            runCatching { handle.cancel() }
        }
        scheduledHandles.clear()
        activeTasks.clear()
        playerStates.clear()
        playerTeams.clear()
        zones.clear()
    }
}
