package com.luminaris.extraction.core.extraction

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * Custom event fired when a player finishes their extraction countdown in an extraction zone.
 * Folia and Paper multi-threaded regional execution safe.
 *
 * @param player The extracting player.
 * @param zone The extraction zone where extraction completed.
 * @param extractionState The designated extraction result state (defaults to [ExtractionState.SURVIVED]).
 * @param targetLobbyLocation The destination lobby location where the player will be evacuated.
 */
class PlayerExtractEvent(
    player: Player,
    val zone: ExtractionZone,
    val extractionState: ExtractionState = ExtractionState.SURVIVED,
    var targetLobbyLocation: Location? = null
) : PlayerEvent(player), Cancellable {

    private var cancelled: Boolean = false

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST

    companion object {
        private val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }
}
