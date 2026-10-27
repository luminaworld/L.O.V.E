package com.luminaris.extraction.core.extraction

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Event listener monitoring damage interruptions and lifecycle events to prevent memory leaks.
 */
class ExtractionListener(private val manager: ExtractionManager) : Listener {

    /**
     * Resets extraction countdown when player takes damage inside an evacuation zone.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        manager.handlePlayerDamage(player, event.finalDamage)
    }

    /**
     * Cleans up player tracking data when player quits to prevent memory leaks.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        manager.cleanupPlayer(event.player.uniqueId)
    }

    /**
     * Cleans up player tracking data when player is kicked to prevent memory leaks.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerKick(event: PlayerKickEvent) {
        manager.cleanupPlayer(event.player.uniqueId)
    }

    /**
     * Resets extraction tracking and marks state as KIA upon death.
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player
        manager.stopExtractionCountdown(player)
        manager.setPlayerState(player, ExtractionState.KIA)
    }
}
