package com.luminaris.extraction.security.clientguard

import com.luminaris.extraction.util.FoliaScheduler
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRegisterChannelEvent

/**
 * Event listener intercepting player connections, channel registrations,
 * and virtual sign probe responses for comprehensive cheat client detection.
 */
class ClientGuardListener(
    private val manager: ClientGuardManager,
    private val scheduler: FoliaScheduler
) : Listener {

    /**
     * Intercepts player upon joining to inspect client brand and initial channels.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        manager.debug("Player joined: ${player.name} (UUID: ${player.uniqueId}, OP: ${player.isOp})")

        // 1. Immediate Join Inspection
        val result = manager.inspectPlayer(player, "JOIN")
        if (result.isViolated) {
            manager.debug("Player ${player.name} detected on initial JOIN inspection")
            return
        }

        // 2. Multi-stage Ticked Polling to account for asynchronous brand packet delivery in Paper
        val pollingTicks = listOf(2L, 5L, 10L, 20L)
        for (tick in pollingTicks) {
            scheduler.runAtEntityDelayed(player, tick) {
                if (player.isOnline) {
                    val pollResult = manager.inspectPlayer(player, "TICK_POLL_$tick")
                    if (pollResult.isViolated) {
                        manager.debug("Player ${player.name} detected at TICK_POLL_$tick")
                    }
                    if (tick == 20L) {
                        // Scan listening plugin channels after initial handshake settles
                        val channels = player.listeningPluginChannels
                        if (channels.isNotEmpty()) {
                            manager.handleIncomingChannels(player, channels.joinToString(","))
                        }
                    }
                }
            }
        }

        // 3. Active Sign Translation Probe (MC-265322) targeting Meteor, Wurst, Freecam, and Baritone
        manager.signTranslationProbe.schedule(player, false)
    }

    /**
     * Intercepts channel registration packets (minecraft:register) from the client.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun onChannelRegister(event: PlayerRegisterChannelEvent) {
        val player = event.player
        val channel = event.channel
        manager.debug("PlayerRegisterChannelEvent from ${player.name}: channel='$channel'")

        manager.handleIncomingChannels(player, channel)
        manager.inspectPlayer(player, "CHANNEL_REGISTER", channel)
    }

    /**
     * Cleans up player session cache upon disconnection.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        manager.signTranslationProbe.cleanup(event.player.uniqueId)
        manager.removeProfile(event.player.uniqueId)
    }
}

