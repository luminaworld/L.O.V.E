package com.luminaris.extraction.core.match

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.extraction.ExtractionManager
import com.luminaris.extraction.core.item.LootPopulator
import com.luminaris.extraction.core.spawn.SpawnAllocator
import com.luminaris.extraction.core.world.InstanceWorldService
import com.luminaris.extraction.localization.TranslationManager
import com.luminaris.extraction.storage.TemplateStorage
import com.luminaris.extraction.util.FoliaScheduler
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * Core manager orchestrating extraction match instances, squad spawn distribution,
 * regional world creation, and command execution for `/ext match`.
 *
 * Compliant with Folia regionized multi-threading and asynchronous pipelines.
 */
class MatchManager(
    private val plugin: LuminarisCorePlugin = LuminarisCorePlugin.instance,
    private val instanceWorldService: InstanceWorldService = plugin.instanceWorldService,
    private val lootPopulator: LootPopulator = plugin.lootPopulator,
    private val spawnAllocator: SpawnAllocator = plugin.spawnAllocator,
    private val templateStorage: TemplateStorage = plugin.templateStorage,
    private val extractionManager: ExtractionManager = plugin.extractionManager,
    private val scheduler: FoliaScheduler = plugin.scheduler
) {

    private val miniMessage = MiniMessage.miniMessage()
    private val activeMatches = ConcurrentHashMap<String, MatchInstance>()

    /**
     * Starts an extraction match instance for the specified template and player cohort.
     *
     * 5-Stage Orchestration Pipeline:
     * 1) Calls [InstanceWorldService.createInstance] to asynchronously clone and mount the ephemeral world.
     * 2) Invokes [LootPopulator.populateMatchWorld] across Folia regional chunks.
     * 3) Distributes squads via [SpawnAllocator] using Anti-Clumping optimization.
     * 4) Teleports players using Folia EntitySchedulers to assigned spawn points and presents the BossBar.
     * 5) Starts [MatchInstance] lifecycle loop on the Global Region Scheduler.
     *
     * @param templateId The identifier of the map template (e.g. "factory").
     * @param players Cohort of participating players.
     * @param raidDurationSeconds Match operational window in seconds (defaults to 1200L / 20 minutes).
     * @return [CompletableFuture] resolving to the running [MatchInstance].
     */
    fun startMatch(
        templateId: String,
        players: List<Player>,
        raidDurationSeconds: Long = 1200L
    ): CompletableFuture<MatchInstance> {
        val future = CompletableFuture<MatchInstance>()
        val cleanTemplateId = templateId.lowercase().trim()

        val effectiveDuration = if (raidDurationSeconds != 1200L) {
            raidDurationSeconds
        } else {
            runCatching { plugin.config.getLong("match.default_duration_seconds", 1200L) }.getOrDefault(1200L)
        }

        val templateData = templateStorage.getTemplate(cleanTemplateId)
        if (templateData == null) {
            val ex = IllegalArgumentException("Template '$cleanTemplateId' not found in TemplateStorage.")
            future.completeExceptionally(ex)
            return future
        }

        if (players.isEmpty()) {
            val ex = IllegalArgumentException("Cannot start match with zero players.")
            future.completeExceptionally(ex)
            return future
        }

        // Check if any player is already in an active match
        for (player in players) {
            val existing = getMatchByPlayer(player.uniqueId)
            if (existing != null) {
                val ex = IllegalStateException("Player ${player.name} is already participating in match ${existing.matchId}.")
                future.completeExceptionally(ex)
                return future
            }
        }

        val matchId = UUID.randomUUID().toString().take(8)
        logInfo(
            "match.log_deployment_initiating",
            "match_id" to matchId,
            "template" to cleanTemplateId,
            "count" to players.size
        )

        // Stage 1: Asynchronously clone and mount ephemeral instance world
        instanceWorldService.createInstance(cleanTemplateId, matchId).thenAccept { world ->
            // Stage 2: Populate all marked loot containers across Folia regions
            lootPopulator.populateMatchWorld(world, templateData).thenAccept { containerCount ->
                logInfo(
                    "match.log_populated_containers",
                    "count" to containerCount,
                    "match_id" to matchId
                )

                // Stage 3: Distribute squads via SpawnAllocator
                val squadNames = players.map { it.name }
                val spawnCandidates = templateData.spawns
                val allowReuse = runCatching { plugin.config.getBoolean("spawn.allow_spawn_reuse", true) }.getOrDefault(true)
                val allocatedSpawns = if (spawnCandidates.isNotEmpty()) {
                    spawnAllocator.allocateSpawns(squadNames, spawnCandidates, allowReuse = allowReuse)
                } else {
                    emptyMap()
                }

                // Register template extraction zones into ExtractionManager
                for (zoneData in templateData.extractions) {
                    extractionManager.registerZone(zoneData.toExtractionZone())
                }

                // Instantiate MatchInstance
                val match = MatchInstance(
                    matchId = matchId,
                    world = world,
                    templateData = templateData,
                    raidDurationSeconds = effectiveDuration,
                    plugin = plugin,
                    scheduler = scheduler,
                    instanceWorldService = instanceWorldService,
                    extractionManager = extractionManager,
                    onComplete = { completedMatch ->
                        activeMatches.remove(completedMatch.matchId)
                    }
                )

                activeMatches[matchId] = match

                // Stage 4: Teleport players using entity schedulers to assigned spawn points
                val teleportFutures = mutableListOf<CompletableFuture<Void>>()

                for (player in players) {
                    val pFuture = CompletableFuture<Void>()
                    teleportFutures.add(pFuture)

                    match.players[player.uniqueId] = PlayerRaidState.IN_RAID

                    val assignedSpawn = allocatedSpawns[player.name] ?: spawnCandidates.firstOrNull()
                    val targetLocation = assignedSpawn?.toLocation(world) ?: world.spawnLocation

                    // Folia EntityScheduler safe teleportation
                    scheduler.runAtEntity(player) {
                        player.teleportAsync(targetLocation).whenComplete { _, tpEx ->
                            if (tpEx != null) {
                                logWarning(
                                    "match.log_teleport_error",
                                    "player" to player.name,
                                    "match_id" to matchId,
                                    "error" to (tpEx.message ?: "unknown")
                                )
                            }

                            // Show BossBar to operative
                            if (match.isBossBarEnabled) {
                                player.showBossBar(match.bossBar)
                            }

                            // Start extraction countdown listener on player's EntityScheduler
                            extractionManager.startExtractionCountdown(
                                player = player,
                                matchElapsedProvider = { match.elapsedSeconds },
                                teamIdProvider = { player.name }
                            )

                            TranslationManager.send(
                                player,
                                "match.player_deployed",
                                "template" to templateData.displayName
                            )

                            pFuture.complete(null)
                        }
                    }
                }

                // Stage 5: Starts MatchInstance lifecycle loop once players are positioned
                CompletableFuture.allOf(*teleportFutures.toTypedArray()).whenComplete { _, _ ->
                    match.start()
                    future.complete(match)
                }

            }.exceptionally { popEx ->
                logError(
                    "match.log_populate_error",
                    popEx,
                    "match_id" to matchId,
                    "error" to (popEx.message ?: "unknown")
                )
                instanceWorldService.deleteInstance(world)
                future.completeExceptionally(popEx)
                null
            }
        }.exceptionally { createEx ->
            logError(
                "match.log_create_world_error",
                createEx,
                "match_id" to matchId,
                "error" to (createEx.message ?: "unknown")
            )
            future.completeExceptionally(createEx)
            null
        }

        return future
    }

    /**
     * Retrieves an active match instance by its unique match ID.
     */
    fun getMatch(matchId: String): MatchInstance? = activeMatches[matchId.lowercase()]

    /**
     * Retrieves an active match instance corresponding to a given Bukkit [World].
     */
    fun getMatchByWorld(world: World): MatchInstance? {
        return activeMatches.values.firstOrNull { it.world == world || it.world.name == world.name }
    }

    /**
     * Retrieves an active match instance containing the specified player UUID.
     */
    fun getMatchByPlayer(uuid: UUID): MatchInstance? {
        return activeMatches.values.firstOrNull { it.players.containsKey(uuid) }
    }

    /**
     * Checks if a player is currently enrolled in an active extraction match.
     */
    fun isPlayerInMatch(uuid: UUID): Boolean = getMatchByPlayer(uuid) != null

    /**
     * Returns an unmodifiable snapshot of all currently running matches.
     */
    fun getActiveMatches(): Collection<MatchInstance> = activeMatches.values.toList()

    /**
     * Force-terminates a running match instance by ID.
     */
    fun stopMatch(matchId: String): Boolean {
        val match = activeMatches.remove(matchId.lowercase()) ?: return false
        match.teardown()
        return true
    }

    /**
     * Cleans up and dismantles all running match instances on server shutdown.
     */
    fun shutdown() {
        for ((_, match) in activeMatches) {
            runCatching { match.teardown() }
        }
        activeMatches.clear()
    }

    /**
     * Handles command dispatch for `/ext match <subcommand>`.
     */
    fun handleCommand(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sendMatchHelp(sender, label)
            return true
        }

        when (args[0].lowercase()) {
            "start" -> {
                if (args.size < 2) {
                    TranslationManager.send(sender, "match.command_start_usage", "label" to label)
                    val available = templateStorage.getAllTemplates().map { it.templateId }
                    if (available.isNotEmpty()) {
                        TranslationManager.send(sender, "match.command_available_templates", "templates" to available.joinToString(", "))
                    }
                    return true
                }

                val templateId = args[1].lowercase().trim()
                val targetPlayers = if (args.size > 2) {
                    args.drop(2).mapNotNull { Bukkit.getPlayerExact(it) }
                } else if (sender is Player) {
                    listOf(sender)
                } else {
                    TranslationManager.send(sender, "match.command_console_requires_players")
                    return true
                }

                if (targetPlayers.isEmpty()) {
                    TranslationManager.send(sender, "match.command_no_valid_players")
                    return true
                }

                TranslationManager.send(
                    sender,
                    "match.command_initializing",
                    "template" to templateId,
                    "count" to targetPlayers.size
                )

                startMatch(templateId, targetPlayers).thenAccept { match ->
                    TranslationManager.send(
                        sender,
                        "match.command_launched",
                        "match_id" to match.matchId,
                        "world" to match.world.name
                    )
                }.exceptionally { ex ->
                    TranslationManager.send(
                        sender,
                        "match.command_launch_failed",
                        "error" to (ex.cause?.message ?: (ex.message ?: "unknown"))
                    )
                    null
                }
            }

            "stop", "end" -> {
                if (args.size < 2) {
                    TranslationManager.send(sender, "match.command_stop_usage", "label" to label)
                    return true
                }
                val matchId = args[1].lowercase().trim()
                val stopped = stopMatch(matchId)
                if (stopped) {
                    TranslationManager.send(sender, "match.command_stopped", "match_id" to matchId)
                } else {
                    TranslationManager.send(sender, "match.command_not_found", "match_id" to matchId)
                }
            }

            "list" -> {
                val matches = getActiveMatches()
                if (matches.isEmpty()) {
                    TranslationManager.send(sender, "match.command_list_empty")
                    return true
                }

                sender.sendMessage(TranslationManager.render("general.divider"))
                sender.sendMessage(TranslationManager.render("match.command_list_header", "count" to matches.size))
                for (m in matches) {
                    val remaining = maxOf(0L, m.raidDurationSeconds - m.elapsedSeconds)
                    val timeFormatted = String.format("%02d:%02d", remaining / 60, remaining % 60)
                    sender.sendMessage(
                        TranslationManager.render(
                            "match.command_list_entry",
                            "match_id" to m.matchId,
                            "template" to m.templateData.templateId,
                            "world" to m.world.name,
                            "players" to m.players.size,
                            "time_left" to timeFormatted
                        )
                    )
                }
                sender.sendMessage(TranslationManager.render("general.divider"))
            }

            "status" -> {
                if (args.size > 1) {
                    val match = getMatch(args[1])
                    if (match == null) {
                        TranslationManager.send(sender, "match.command_not_found", "match_id" to args[1])
                        return true
                    }
                    val remaining = maxOf(0L, match.raidDurationSeconds - match.elapsedSeconds)
                    val timeFormatted = String.format("%02d:%02d", remaining / 60, remaining % 60)
                    sender.sendMessage(TranslationManager.render("general.divider"))
                    sender.sendMessage(TranslationManager.render("match.command_status_header", "match_id" to match.matchId))
                    sender.sendMessage(TranslationManager.render("match.command_status_template", "template" to match.templateData.displayName))
                    sender.sendMessage(TranslationManager.render("match.command_status_world", "world" to match.world.name))
                    sender.sendMessage(
                        TranslationManager.render(
                            "match.command_status_time",
                            "elapsed" to match.elapsedSeconds,
                            "duration" to match.raidDurationSeconds,
                            "time_left" to timeFormatted
                        )
                    )
                    sender.sendMessage(TranslationManager.render("match.command_status_operatives", "count" to match.players.size))
                    for ((uuid, state) in match.players) {
                        val pName = Bukkit.getPlayer(uuid)?.name ?: uuid.toString().take(8)
                        sender.sendMessage(TranslationManager.render("match.command_status_player", "player" to pName, "state" to state))
                    }
                    sender.sendMessage(TranslationManager.render("general.divider"))
                } else {
                    TranslationManager.send(sender, "match.command_status_count", "count" to activeMatches.size)
                }
            }

            else -> sendMatchHelp(sender, label)
        }

        return true
    }

    private fun sendMatchHelp(sender: CommandSender, label: String) {
        sender.sendMessage(TranslationManager.render("general.divider"))
        sender.sendMessage(TranslationManager.render("match.command_help_title"))
        sender.sendMessage(TranslationManager.render("match.command_help_start", "label" to label))
        sender.sendMessage(TranslationManager.render("match.command_help_stop", "label" to label))
        sender.sendMessage(TranslationManager.render("match.command_help_list", "label" to label))
        sender.sendMessage(TranslationManager.render("match.command_help_status", "label" to label))
        sender.sendMessage(TranslationManager.render("general.divider"))
    }

    private fun logInfo(key: String, vararg placeholders: Pair<String, Any?>) {
        TranslationManager.logInfo(key, *placeholders)
    }

    private fun logWarning(key: String, vararg placeholders: Pair<String, Any?>) {
        TranslationManager.logWarning(key, *placeholders)
    }

    private fun logError(key: String, throwable: Throwable? = null, vararg placeholders: Pair<String, Any?>) {
        TranslationManager.logError(key, throwable, *placeholders)
    }
}
