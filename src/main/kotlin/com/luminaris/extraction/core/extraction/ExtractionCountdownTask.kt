package com.luminaris.extraction.core.extraction

import com.luminaris.extraction.localization.TranslationManager
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.function.Consumer

/**
 * Folia-safe regional countdown extraction task scheduled on the player's EntityScheduler.
 *
 * Responsibilities:
 * - Detects if player's location is inside `zone.boundingBox.contains(location.toVector())`.
 * - Validates player eligibility with [ExtractionManager.isPlayerEligible].
 * - Increments active countdown timer and updates Adventure MiniMessage actionbars.
 * - Resets countdown and alerts player upon taking damage or moving outside the boundary.
 * - Dispatches [PlayerExtractEvent], updates state to [ExtractionState.SURVIVED], and teleports player to lobby.
 */
class ExtractionCountdownTask(
    private val plugin: Plugin,
    val player: Player,
    private val manager: ExtractionManager,
    private val matchElapsedProvider: () -> Long,
    private val teamIdProvider: () -> String,
    private val zonesProvider: () -> List<ExtractionZone> = { manager.getAllZones() }
) : Runnable, Consumer<ScheduledTask> {

    private val miniMessage = MiniMessage.miniMessage()

    /** Currently active extraction zone if player is inside an eligible zone. */
    var activeZone: ExtractionZone? = null
        private set

    /** Active timer tracking seconds elapsed within the zone. */
    var countdownTimer: Int = 0
        private set

    override fun accept(task: ScheduledTask) {
        run()
    }

    override fun run() {
        tick()
    }

    /**
     * Executes one tick of the extraction evaluation (invoked once per second / 20 ticks).
     */
    fun tick() {
        if (!player.isOnline || !player.isValid) {
            manager.cleanupPlayer(player.uniqueId)
            return
        }

        val location = player.location
        val vector = location.toVector()
        val currentZone = activeZone

        if (currentZone != null) {
            // Player was already in a zone
            if (!currentZone.boundingBox.contains(vector)) {
                // Player moved outside the bounding box: Reset countdown and alert player
                resetCountdown(alertComponent = TranslationManager.render("extraction.cancelled_outside"))
                return
            }

            // Player is still inside the bounding box: re-verify eligibility
            val elapsed = matchElapsedProvider()
            val teamId = teamIdProvider()
            val (eligible, reason) = manager.isPlayerEligible(player, currentZone, elapsed, teamId)

            if (!eligible) {
                resetCountdown(alertComponent = reason)
                return
            }

            // Increments active countdown timer
            countdownTimer++
            val remaining = maxOf(0, currentZone.countdownSeconds - countdownTimer)
            TranslationManager.sendActionBar(player, "extraction.countdown_actionbar", "remaining" to remaining)
            manager.setPlayerState(player, ExtractionState.EXTRACTING)

            // When timer hits 0: execute extraction
            if (remaining <= 0) {
                executeExtraction(currentZone)
            }
        } else {
            // Player is not currently in an active countdown: detect if player is inside any zone
            val candidateZone = zonesProvider().firstOrNull { it.boundingBox.contains(vector) } ?: return

            val elapsed = matchElapsedProvider()
            val teamId = teamIdProvider()
            val (eligible, reason) = manager.isPlayerEligible(player, candidateZone, elapsed, teamId)

            if (eligible) {
                activeZone = candidateZone
                // Increments active countdown timer starting from 1
                countdownTimer = 1
                val remaining = maxOf(0, candidateZone.countdownSeconds - countdownTimer)
                TranslationManager.sendActionBar(player, "extraction.countdown_actionbar", "remaining" to remaining)
                manager.setPlayerState(player, ExtractionState.EXTRACTING)

                if (remaining <= 0) {
                    executeExtraction(candidateZone)
                }
            } else if (reason != null) {
                player.sendActionBar(reason)
            }
        }
    }

    /**
     * Interruption hook triggered when player receives damage during extraction.
     */
    fun onDamage() {
        if (activeZone != null || countdownTimer > 0) {
            resetCountdown(alertComponent = TranslationManager.render("extraction.interrupted_damage"))
        }
    }

    /**
     * Resets active countdown state and sends notification alert to player.
     */
    fun resetCountdown(alertMessage: String? = null, alertComponent: Component? = null) {
        val wasActive = activeZone != null || countdownTimer > 0
        activeZone = null
        countdownTimer = 0
        manager.setPlayerState(player, ExtractionState.IN_MATCH)

        if (wasActive && player.isOnline) {
            if (alertComponent != null) {
                player.sendActionBar(alertComponent)
            } else if (!alertMessage.isNullOrBlank()) {
                player.sendActionBar(TranslationManager.parseComponent(alertMessage))
            } else {
                player.sendActionBar(Component.empty())
            }
        }
    }

    /**
     * Finalizes extraction: fires [PlayerExtractEvent], updates state, and teleports player to lobby.
     */
    private fun executeExtraction(zone: ExtractionZone) {
        // 1. Fire custom PlayerExtractEvent
        val event = PlayerExtractEvent(player, zone, ExtractionState.SURVIVED)
        runCatching {
            Bukkit.getPluginManager().callEvent(event)
        }

        if (event.isCancelled) {
            resetCountdown()
            return
        }

        // 2. Mark state as SURVIVED
        manager.setPlayerState(player, ExtractionState.SURVIVED)

        // 3. Consume item if applicable
        if (zone.type == ExtractionType.CONDITIONAL_ITEM && zone.requiredItemId != null) {
            manager.consumeRequiredItem(player, zone.requiredItemId)
        }

        // 4. Reset countdown state
        activeZone = null
        countdownTimer = 0

        // 5. Teleport player to safe lobby location via Folia async teleportation
        val targetLobby = event.targetLobbyLocation ?: manager.resolveLobbyLocation()
        player.teleportAsync(targetLobby).whenComplete { _, ex ->
            if (ex != null) {
                TranslationManager.logWarning("extraction.log_teleport_error", "player" to player.name, "error" to (ex.message ?: "unknown"))
            } else {
                TranslationManager.sendActionBar(player, "extraction.success_actionbar")
            }
        }
    }
}
