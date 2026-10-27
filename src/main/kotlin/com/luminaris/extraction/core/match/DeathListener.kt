package com.luminaris.extraction.core.match

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.extraction.PlayerExtractEvent
import com.luminaris.extraction.core.safe.SafeCaseManager
import com.luminaris.extraction.util.FoliaScheduler
import com.luminaris.extraction.localization.TranslationManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Event listener handling in-match player deaths, corpse crate generation,
 * bulletproof safe case death immunity, and respawn restoration.
 *
 * Compliant with Folia regionized multi-threading and regional execution.
 */
class DeathListener(
    private val plugin: LuminarisCorePlugin = LuminarisCorePlugin.instance,
    private val matchManager: MatchManager,
    private val corpseManager: CorpseManager,
    private val safeCaseManager: SafeCaseManager = plugin.safeCaseManager,
    private val scheduler: FoliaScheduler = plugin.scheduler
) : Listener {

    /**
     * Intercepts player deaths inside active extraction matches at HIGHEST priority.
     * Enforces absolute death immunity for safe cases, generates corpse containers,
     * and clears item drops so zero items scatter on the floor.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player
        val match = matchManager.getMatchByPlayer(player.uniqueId) ?: return

        // 1) Scan event.drops
        val dropsToStore = mutableListOf<ItemStack>()
        val preservedSafeCases = mutableListOf<ItemStack>()

        val dropsIterator = event.drops.iterator()
        while (dropsIterator.hasNext()) {
            val drop = dropsIterator.next()
            if (drop == null || drop.isEmpty || drop.type.isAir) {
                dropsIterator.remove()
                continue
            }

            // 2) Filter out all items where SafeCaseManager.isSafeCase(item) == true
            if (safeCaseManager.isSafeCase(drop)) {
                preservedSafeCases.add(drop.clone())
                // 4) Remove safe case items from event.drops
                dropsIterator.remove()
            } else {
                dropsToStore.add(drop.clone())
            }
        }

        // Defensive check: also scan event.itemsToKeep for any safe cases to ensure bulletproof preservation
        val keepIterator = event.itemsToKeep.iterator()
        while (keepIterator.hasNext()) {
            val keep = keepIterator.next()
            if (safeCaseManager.isSafeCase(keep)) {
                preservedSafeCases.add(keep.clone())
                keepIterator.remove()
            }
        }

        // 3) Cache and save filtered safe cases into SafeCaseManager.pendingReturns[player.uniqueId]
        if (preservedSafeCases.isNotEmpty()) {
            val pendingList = SafeCaseManager.pendingReturns.computeIfAbsent(player.uniqueId) {
                CopyOnWriteArrayList()
            }
            pendingList.addAll(preservedSafeCases)
        }

        val corpseEnabled = runCatching { plugin.config.getBoolean("corpse.enabled", true) }.getOrDefault(true)
        if (corpseEnabled) {
            // 5) At death location, spawn a Corpse Block (Chest/Barrel) using the Region Scheduler
            // 6) Deposit the remaining drops into the Corpse Container
            corpseManager.spawnCorpse(player.location, player, dropsToStore)

            // 7) Clear event.drops completely so zero items scatter on the floor
            event.drops.clear()
        }

        // 8) Mark player state as KILLED_IN_ACTION
        match.setPlayerState(player.uniqueId, PlayerRaidState.KILLED_IN_ACTION)

        // Clean up bossbar and active extraction tasks for this player
        scheduler.runAtEntity(player) {
            player.hideBossBar(match.bossBar)
        }
        plugin.extractionManager.stopExtractionCountdown(player)

        TranslationManager.send(player, "death.player_kia")

        // Check if all players have extracted or died to trigger teardown
        match.checkCompletion()
    }

    /**
     * Listens to PlayerRespawnEvent and restores all preserved safe cases directly back
     * into their inventory on respawn.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player
        restorePendingSafeCases(player)
    }

    /**
     * Fallback listener restoring preserved safe cases if a player disconnected while dead.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        restorePendingSafeCases(player)
    }

    /**
     * Delivers cached safe cases directly back into the player's inventory
     * within the Folia EntityScheduler context.
     */
    private fun restorePendingSafeCases(player: Player) {
        val pending = SafeCaseManager.pendingReturns.remove(player.uniqueId) ?: return
        if (pending.isEmpty()) return

        scheduler.runAtEntity(player) {
            if (!player.isOnline) return@runAtEntity

            for (safeItem in pending) {
                val leftover = player.inventory.addItem(safeItem)
                if (leftover.isNotEmpty()) {
                    for (drop in leftover.values) {
                        player.world.dropItemNaturally(player.location, drop)
                    }
                }
            }

            TranslationManager.send(player, "death.safe_cases_restored")
        }
    }

    /**
     * Handles player successful evacuation through [PlayerExtractEvent].
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerExtract(event: PlayerExtractEvent) {
        val player = event.player
        val match = matchManager.getMatchByPlayer(player.uniqueId) ?: return

        match.setPlayerState(player.uniqueId, PlayerRaidState.EXTRACTED)

        scheduler.runAtEntity(player) {
            player.hideBossBar(match.bossBar)
        }

        TranslationManager.send(player, "death.extracted_success")

        match.checkCompletion()
    }

    /**
     * Handles player disconnect during an active match.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val match = matchManager.getMatchByPlayer(player.uniqueId) ?: return

        val currentState = match.getPlayerState(player.uniqueId)
        if (currentState == PlayerRaidState.IN_RAID || currentState == PlayerRaidState.EXTRACTING) {
            match.setPlayerState(player.uniqueId, PlayerRaidState.MISSING_IN_ACTION)
        }

        match.checkCompletion()
    }
}
