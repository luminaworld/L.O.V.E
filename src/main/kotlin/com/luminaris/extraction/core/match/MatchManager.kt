package com.luminaris.extraction.core.match

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.core.extraction.ExtractionManager
import com.luminaris.extraction.core.item.LootPopulator
import com.luminaris.extraction.core.spawn.SpawnAllocator
import com.luminaris.extraction.core.world.InstanceWorldService
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
        logInfo("<gray>Initiating match deployment </gray><yellow>$matchId</yellow> <gray>for template </gray><aqua>$cleanTemplateId</aqua> <gray>with </gray><white>${players.size}</white> <gray>player(s)...</gray>")

        // Stage 1: Asynchronously clone and mount ephemeral instance world
        instanceWorldService.createInstance(cleanTemplateId, matchId).thenAccept { world ->
            // Stage 2: Populate all marked loot containers across Folia regions
            lootPopulator.populateMatchWorld(world, templateData).thenAccept { containerCount ->
                logInfo("<green>✔ Populated </green><yellow>$containerCount</yellow><green> container(s) for match </green><yellow>$matchId</yellow><green>.</green>")

                // Stage 3: Distribute squads via SpawnAllocator
                val squadNames = players.map { it.name }
                val spawnCandidates = templateData.spawns
                val allocatedSpawns = if (spawnCandidates.isNotEmpty()) {
                    spawnAllocator.allocateSpawns(squadNames, spawnCandidates)
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
                    raidDurationSeconds = raidDurationSeconds,
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
                                logWarning("Teleport error for ${player.name} to match $matchId: ${tpEx.message}")
                            }

                            // Show BossBar to operative
                            player.showBossBar(match.bossBar)

                            // Start extraction countdown listener on player's EntityScheduler
                            extractionManager.startExtractionCountdown(
                                player = player,
                                matchElapsedProvider = { match.elapsedSeconds },
                                teamIdProvider = { player.name }
                            )

                            player.sendMessage(
                                miniMessage.deserialize(
                                    "<gradient:#00FFA3:#00B8FF><bold>[INFILTRATION CONFIRMED]</bold></gradient> <gray>Deployed to </gray><aqua>${templateData.displayName}</aqua><gray>. Raid timer active!</gray>"
                                )
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
                logError("Failed to populate loot containers for match $matchId: ${popEx.message}", popEx)
                instanceWorldService.deleteInstance(world)
                future.completeExceptionally(popEx)
                null
            }
        }.exceptionally { createEx ->
            logError("Failed to create world instance for match $matchId: ${createEx.message}", createEx)
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
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: </red><yellow>/$label match start <template_id> [players...]</yellow>"))
                    val available = templateStorage.getAllTemplates().map { it.templateId }
                    if (available.isNotEmpty()) {
                        sender.sendMessage(miniMessage.deserialize("<gray>Available templates: </gray><aqua>${available.joinToString(", ")}</aqua>"))
                    }
                    return true
                }

                val templateId = args[1].lowercase().trim()
                val targetPlayers = if (args.size > 2) {
                    args.drop(2).mapNotNull { Bukkit.getPlayerExact(it) }
                } else if (sender is Player) {
                    listOf(sender)
                } else {
                    sender.sendMessage(miniMessage.deserialize("<red>You must specify at least one target player when running from console.</red>"))
                    return true
                }

                if (targetPlayers.isEmpty()) {
                    sender.sendMessage(miniMessage.deserialize("<red>No valid online players found to deploy into match.</red>"))
                    return true
                }

                sender.sendMessage(
                    miniMessage.deserialize(
                        "<yellow>⏳ Initializing match instance for template </yellow><aqua>$templateId</aqua><yellow> with </yellow><white>${targetPlayers.size}</white><yellow> operative(s)...</yellow>"
                    )
                )

                startMatch(templateId, targetPlayers).thenAccept { match ->
                    sender.sendMessage(
                        miniMessage.deserialize(
                            "<green>✔ Match </green><yellow>${match.matchId}</yellow><green> launched successfully in world </green><aqua>${match.world.name}</aqua><green>!</green>"
                        )
                    )
                }.exceptionally { ex ->
                    sender.sendMessage(
                        miniMessage.deserialize(
                            "<red>✕ Failed to launch match: ${ex.cause?.message ?: ex.message}</red>"
                        )
                    )
                    null
                }
            }

            "stop", "end" -> {
                if (args.size < 2) {
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: </red><yellow>/$label match stop <match_id></yellow>"))
                    return true
                }
                val matchId = args[1].lowercase().trim()
                val stopped = stopMatch(matchId)
                if (stopped) {
                    sender.sendMessage(miniMessage.deserialize("<green>✔ Match </green><yellow>$matchId</yellow><green> has been decommissioned.</green>"))
                } else {
                    sender.sendMessage(miniMessage.deserialize("<red>No active match found with ID: $matchId</red>"))
                }
            }

            "list" -> {
                val matches = getActiveMatches()
                if (matches.isEmpty()) {
                    sender.sendMessage(miniMessage.deserialize("<gray>No extraction matches currently active.</gray>"))
                    return true
                }

                val sb = StringBuilder()
                sb.append("<dark_gray>----------------------------------------</dark_gray>\n")
                sb.append("<gradient:#00FFA3:#00B8FF><bold>ACTIVE EXTRACTION MATCHES (${matches.size})</bold></gradient>\n")
                for (m in matches) {
                    val remaining = maxOf(0L, m.raidDurationSeconds - m.elapsedSeconds)
                    val timeFormatted = String.format("%02d:%02d", remaining / 60, remaining % 60)
                    sb.append("<dark_gray>▪ </dark_gray><yellow>${m.matchId}</yellow> <gray>|</gray> <aqua>${m.templateData.templateId}</aqua> <gray>|</gray> <white>${m.world.name}</white> <gray>|</gray> <green>${m.players.size} players</green> <gray>| Time Left: </gray><red>$timeFormatted</red>\n")
                }
                sb.append("<dark_gray>----------------------------------------</dark_gray>")
                sender.sendMessage(miniMessage.deserialize(sb.toString()))
            }

            "status" -> {
                if (args.size > 1) {
                    val match = getMatch(args[1])
                    if (match == null) {
                        sender.sendMessage(miniMessage.deserialize("<red>Match not found: ${args[1]}</red>"))
                        return true
                    }
                    val remaining = maxOf(0L, match.raidDurationSeconds - match.elapsedSeconds)
                    val timeFormatted = String.format("%02d:%02d", remaining / 60, remaining % 60)
                    sender.sendMessage(
                        miniMessage.deserialize(
                            """
                            <dark_gray>----------------------------------------</dark_gray>
                            <gradient:#00FFA3:#00B8FF><bold>MATCH STATUS: ${match.matchId}</bold></gradient>
                            <gray>Template:</gray> <aqua>${match.templateData.displayName}</aqua>
                            <gray>World:</gray> <white>${match.world.name}</white>
                            <gray>Elapsed / Total:</gray> <yellow>${match.elapsedSeconds}s / ${match.raidDurationSeconds}s</yellow> (<red>$timeFormatted remaining</red>)
                            <gray>Operatives (${match.players.size}):</gray>
                            """.trimIndent()
                        )
                    )
                    for ((uuid, state) in match.players) {
                        val pName = Bukkit.getPlayer(uuid)?.name ?: uuid.toString().take(8)
                        sender.sendMessage(miniMessage.deserialize("  <dark_gray>• </dark_gray><white>$pName</white> <gray>-</gray> <yellow>$state</yellow>"))
                    }
                    sender.sendMessage(miniMessage.deserialize("<dark_gray>----------------------------------------</dark_gray>"))
                } else {
                    sender.sendMessage(miniMessage.deserialize("<gray>Active match instances: </gray><yellow>${activeMatches.size}</yellow>"))
                }
            }

            else -> sendMatchHelp(sender, label)
        }

        return true
    }

    private fun sendMatchHelp(sender: CommandSender, label: String) {
        val help = """
            <dark_gray>----------------------------------------</dark_gray>
            <gradient:#00FFA3:#00B8FF><bold>EXTRACTION MATCH COMMANDS</bold></gradient>
            <yellow>/$label match start <template_id> [players...]</yellow> <gray>- Deploy an extraction match</gray>
            <yellow>/$label match stop <match_id></yellow>               <gray>- Force terminate an active match</gray>
            <yellow>/$label match list</yellow>                           <gray>- List active extraction matches</gray>
            <yellow>/$label match status [match_id]</yellow>              <gray>- View match status and roster</gray>
            <dark_gray>----------------------------------------</dark_gray>
        """.trimIndent()
        sender.sendMessage(miniMessage.deserialize(help))
    }

    private fun logInfo(message: String) {
        Bukkit.getConsoleSender().sendMessage(miniMessage.deserialize("<gradient:#00FFA3:#00B8FF><bold>[LuminarisMatch]</bold></gradient> $message"))
    }

    private fun logWarning(message: String) {
        Bukkit.getConsoleSender().sendMessage(miniMessage.deserialize("<gradient:#FFAA00:#FF5500><bold>[LuminarisMatch]</bold></gradient> $message"))
    }

    private fun logError(message: String, throwable: Throwable? = null) {
        Bukkit.getConsoleSender().sendMessage(miniMessage.deserialize("<gradient:#FF0055:#FF5555><bold>[LuminarisMatch]</bold></gradient> $message"))
        if (throwable != null) {
            runCatching { plugin.logger.log(java.util.logging.Level.SEVERE, "[LuminarisMatch] $message", throwable) }
        }
    }
}
