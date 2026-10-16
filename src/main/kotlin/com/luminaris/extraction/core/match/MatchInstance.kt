package com.luminaris.extraction.core.match

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.extraction.ExtractionManager
import com.luminaris.extraction.core.template.TemplateMapData
import com.luminaris.extraction.core.world.InstanceWorldService
import com.luminaris.extraction.localization.TranslationManager
import com.luminaris.extraction.util.FoliaScheduler
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Encapsulates the runtime lifecycle of an active extraction match instance.
 * Governs the raid countdown timer, adventure bossbar telemetry, timeout MIA enforcement,
 * and ephemeral instance decommissioning.
 *
 * Compliant with Folia regionized multi-threading and modern Paper architectures.
 *
 * @param matchId Unique alphanumeric match identifier (e.g. "a1b2c3d4").
 * @param world Loaded ephemeral Bukkit [World] hosting this match.
 * @param templateData Metadata defining map name, spawn locations, and extraction zones.
 * @param raidDurationSeconds Total match operational time window before timeout (default 1200s / 20m).
 * @param players Concurrent map tracking participating players and their tactical states.
 * @param elapsedSeconds Running count of elapsed seconds since match commencement.
 */
class MatchInstance(
    val matchId: String,
    val world: World,
    val templateData: TemplateMapData,
    val raidDurationSeconds: Long = 1200L,
    val players: MutableMap<UUID, PlayerRaidState> = ConcurrentHashMap(),
    var elapsedSeconds: Long = 0L,
    plugin: LuminarisCorePlugin? = null,
    scheduler: FoliaScheduler? = null,
    instanceWorldService: InstanceWorldService? = null,
    extractionManager: ExtractionManager? = null,
    private val onComplete: (MatchInstance) -> Unit = {}
) {

    private val activePlugin: LuminarisCorePlugin? by lazy {
        plugin ?: runCatching { LuminarisCorePlugin.instance }.getOrNull()
    }
    private val activeScheduler: FoliaScheduler? by lazy {
        scheduler ?: activePlugin?.scheduler ?: runCatching { FoliaScheduler.default }.getOrNull()
    }
    private val activeInstanceWorldService: InstanceWorldService? by lazy {
        instanceWorldService ?: activePlugin?.instanceWorldService
    }
    private val activeExtractionManager: ExtractionManager? by lazy {
        extractionManager ?: activePlugin?.extractionManager
    }

    private val miniMessage = MiniMessage.miniMessage()
    private val isTeardownInitiated = AtomicBoolean(false)
    private var timerTask: ScheduledTask? = null

    /**
     * Adventure BossBar broadcast displaying raid time to participating operatives:
     * `<red>RAID TIME: <yellow>{mm:ss}</yellow></red>`
     */
    val bossBar: BossBar = BossBar.bossBar(
        formatBossBarTitle(maxOf(0L, raidDurationSeconds - elapsedSeconds)),
        1.0f,
        BossBar.Color.RED,
        BossBar.Overlay.PROGRESS
    )

    /**
     * Whether this match has concluded and completed teardown.
     */
    val isFinished: Boolean
        get() = isTeardownInitiated.get()

    /**
     * Commences the match lifecycle loop.
     * Schedules the repeating 20-tick (1 second) heartbeat on the Folia Global Region Scheduler.
     */
    fun start() {
        updateBossBar()

        // Folia Global Region repeating timer ticking every 20 ticks (1 second)
        timerTask = activeScheduler?.runGlobalRepeating(20L, 20L) {
            tick()
        }

        logInfo(
            "match.log_started",
            "match_id" to matchId,
            "world" to world.name,
            "duration" to raidDurationSeconds,
            "count" to players.size
        )
    }

    /**
     * Evaluates one second of match progression on the Folia Global Region Scheduler.
     */
    fun tick() {
        if (isTeardownInitiated.get()) return

        elapsedSeconds++
        updateBossBar()

        // Check if all players have reached a terminal state (extracted, killed, or MIA)
        if (players.isNotEmpty() && players.values.all { it.isTerminal }) {
            logInfo("match.log_early_teardown", "match_id" to matchId)
            teardown()
            return
        }

        // On timeout: Remaining non-terminal players are flagged MIA, killed, and teardown is executed
        if (elapsedSeconds >= raidDurationSeconds) {
            handleTimeout()
        }
    }

    /**
     * Handles raid expiration when elapsed time reaches or exceeds [raidDurationSeconds].
     * Flags all remaining in-world players as MIA, eliminates them, and triggers instance teardown.
     */
    fun handleTimeout() {
        if (!isTeardownInitiated.compareAndSet(false, true)) return

        logWarning("match.log_timeout", "match_id" to matchId)

        // Stop timer task immediately
        timerTask?.cancel()
        timerTask = null

        val miaFutures = mutableListOf<java.util.concurrent.CompletableFuture<Void>>()

        // Identify all players who failed to extract
        for ((uuid, state) in players) {
            if (!state.isTerminal) {
                players[uuid] = PlayerRaidState.MISSING_IN_ACTION

                val player = runCatching { Bukkit.getPlayer(uuid) }.getOrNull()
                if (player != null && player.isOnline && player.world == world) {
                    val pFuture = java.util.concurrent.CompletableFuture<Void>()
                    miaFutures.add(pFuture)

                    // Eliminate MIA player on their Folia EntityScheduler
                    val task = activeScheduler?.runAtEntity(
                        entity = player,
                        runnable = {
                            try {
                                if (player.isOnline && !player.isDead) {
                                    player.damage(100000.0)
                                    TranslationManager.send(player, "match.player_mia")
                                }
                            } finally {
                                pFuture.complete(null)
                            }
                        },
                        retired = { pFuture.complete(null) }
                    )

                    if (task == null) {
                        pFuture.complete(null)
                    }
                }
            }
        }

        // Wait until all MIA players are processed on their entity threads before executing teardown
        if (miaFutures.isNotEmpty()) {
            java.util.concurrent.CompletableFuture.allOf(*miaFutures.toTypedArray()).whenComplete { _, _ ->
                activeScheduler?.runGlobal {
                    executeTeardown()
                } ?: executeTeardown()
            }
        } else {
            executeTeardown()
        }
    }

    /**
     * Concludes the match instance safely, stopping all timers, removing bossbars,
     * unregistering extraction zones, printing the debrief log, and deleting the world instance.
     */
    fun teardown() {
        if (!isTeardownInitiated.compareAndSet(false, true)) return

        timerTask?.cancel()
        timerTask = null

        executeTeardown()
    }

    /**
     * Internal implementation of the teardown lifecycle.
     */
    private fun executeTeardown() {
        // 1. Hide BossBar and stop extraction countdowns for all registered players
        for (uuid in players.keys) {
            val player = runCatching { Bukkit.getPlayer(uuid) }.getOrNull()
            if (player != null && player.isOnline) {
                activeScheduler?.runAtEntity(player) {
                    player.hideBossBar(bossBar)
                }
            }
            activeExtractionManager?.stopExtractionCountdown(uuid)
        }

        // 2. Unregister template extraction zones from ExtractionManager
        for (zoneData in templateData.extractions) {
            activeExtractionManager?.unregisterZone(zoneData.id)
        }

        // 3. Print match debrief log to server console
        printDebriefLog()

        // 4. Notify match manager callback
        runCatching { onComplete(this) }

        // 5. Clean up cached match drop containers and decommission world asynchronously
        activePlugin?.dropBoxManager?.clearWorld(world.name)
        activeInstanceWorldService?.deleteInstance(world)?.whenComplete { _, ex ->
            if (ex != null) {
                logWarning("match.log_world_deletion_error", "world" to world.name, "error" to (ex.message ?: "unknown"))
            } else {
                logInfo("match.log_world_purged", "world" to world.name)
            }
        }
    }

    /**
     * Registers a player to the match instance and presents the BossBar.
     */
    fun addPlayer(player: Player, initialState: PlayerRaidState = PlayerRaidState.IN_RAID) {
        players[player.uniqueId] = initialState
        activeScheduler?.runAtEntity(player) {
            if (player.isOnline) {
                player.showBossBar(bossBar)
            }
        }
    }

    /**
     * Updates tactical state for the specified player UUID.
     */
    fun setPlayerState(uuid: UUID, state: PlayerRaidState) {
        players[uuid] = state
    }

    /**
     * Retrieves the tactical state of a player, or null if not participating.
     */
    fun getPlayerState(uuid: UUID): PlayerRaidState? = players[uuid]

    /**
     * Checks if all players have completed the raid (extracted or eliminated).
     * If so, initiates match teardown.
     */
    fun checkCompletion() {
        if (!isTeardownInitiated.get() && players.isNotEmpty() && players.values.all { it.isTerminal }) {
            teardown()
        }
    }

    /**
     * Refreshes the BossBar title and countdown progress based on remaining seconds.
     */
    fun updateBossBar() {
        val remaining = maxOf(0L, raidDurationSeconds - elapsedSeconds)
        bossBar.name(formatBossBarTitle(remaining))

        val progress = if (raidDurationSeconds > 0) {
            (remaining.toFloat() / raidDurationSeconds.toFloat()).coerceIn(0.0f, 1.0f)
        } else {
            0.0f
        }
        bossBar.progress(progress)
    }

    /**
     * Formats the remaining seconds into `<red>RAID TIME: <yellow>{mm:ss}</yellow></red>`.
     */
    fun formatBossBarTitle(remainingSeconds: Long): Component {
        val mm = remainingSeconds / 60
        val ss = remainingSeconds % 60
        val timeFormatted = String.format("%02d:%02d", mm, ss)
        return TranslationManager.render("match.bossbar_title", "time" to timeFormatted)
    }

    /**
     * Prints a comprehensive MiniMessage debrief log of the completed match to the server console.
     */
    fun printDebriefLog() {
        val durationFormatted = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60)
        val maxDurationFormatted = String.format("%02d:%02d", raidDurationSeconds / 60, raidDurationSeconds % 60)

        val totalSurv = players.values.count { it == PlayerRaidState.EXTRACTED }
        val totalKia = players.values.count { it == PlayerRaidState.KILLED_IN_ACTION }
        val totalMia = players.values.count { it == PlayerRaidState.MISSING_IN_ACTION }

        val console = runCatching { Bukkit.getConsoleSender() }.getOrNull() ?: return
        console.sendMessage(TranslationManager.render("general.divider_long"))
        console.sendMessage(TranslationManager.render("match.debrief.title"))
        console.sendMessage(TranslationManager.render("general.divider_long_sub"))
        console.sendMessage(TranslationManager.render("match.debrief.match_id", "match_id" to matchId))
        console.sendMessage(TranslationManager.render("match.debrief.template", "template" to templateData.displayName, "template_id" to templateData.templateId))
        console.sendMessage(TranslationManager.render("match.debrief.world", "world" to world.name))
        console.sendMessage(TranslationManager.render("match.debrief.elapsed", "elapsed" to durationFormatted, "duration" to maxDurationFormatted, "seconds" to elapsedSeconds))
        console.sendMessage(TranslationManager.render("match.debrief.operatives", "count" to players.size, "survived" to totalSurv, "kia" to totalKia, "mia" to totalMia))
        console.sendMessage(TranslationManager.render("match.debrief.outcomes_header"))

        if (players.isEmpty()) {
            console.sendMessage(TranslationManager.render("match.debrief.no_operatives"))
        } else {
            for ((uuid, state) in players) {
                val pName = runCatching { Bukkit.getPlayer(uuid)?.name }.getOrNull() ?: uuid.toString().take(8)
                val stateTag = when (state) {
                    PlayerRaidState.EXTRACTED -> TranslationManager.render("match.debrief.state_extracted")
                    PlayerRaidState.KILLED_IN_ACTION -> TranslationManager.render("match.debrief.state_kia")
                    PlayerRaidState.MISSING_IN_ACTION -> TranslationManager.render("match.debrief.state_mia")
                    PlayerRaidState.EXTRACTING -> TranslationManager.render("match.debrief.state_extracting")
                    PlayerRaidState.IN_RAID -> TranslationManager.render("match.debrief.state_in_raid")
                }
                console.sendMessage(TranslationManager.render("match.debrief.outcome_entry", "player" to pName, "state" to stateTag))
            }
        }

        console.sendMessage(TranslationManager.render("general.divider_long_sub"))
        console.sendMessage(TranslationManager.render("match.debrief.purged"))
        console.sendMessage(TranslationManager.render("general.divider_long"))
    }

    private fun logInfo(key: String, vararg placeholders: Pair<String, Any?>) {
        TranslationManager.logInfo(key, *placeholders)
    }

    private fun logWarning(key: String, vararg placeholders: Pair<String, Any?>) {
        TranslationManager.logWarning(key, *placeholders)
    }
}
