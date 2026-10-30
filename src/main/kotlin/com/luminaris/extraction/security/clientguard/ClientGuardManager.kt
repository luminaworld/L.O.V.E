package com.luminaris.extraction.security.clientguard

import com.luminaris.extraction.LuminarisCorePlugin
import com.luminaris.extraction.localization.TranslationManager
import com.luminaris.extraction.security.clientguard.model.ClientProfile
import com.luminaris.extraction.security.clientguard.model.DetectionRule
import com.luminaris.extraction.security.clientguard.model.GuardMode
import com.luminaris.extraction.security.clientguard.model.InspectionResult
import com.luminaris.extraction.security.clientguard.model.ViolationAction
import com.luminaris.extraction.util.FoliaScheduler
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Core security enforcement manager for detecting cheat clients, disallowed mods,
 * and malicious payloads upon join or channel handshake.
 */
class ClientGuardManager(
    val plugin: LuminarisCorePlugin,
    private val scheduler: FoliaScheduler
) {
    val configAdapter: ClientGuardConfigAdapter = ClientGuardConfigAdapter(plugin)
    val signTranslationProbe = com.luminaris.extraction.security.clientguard.probe.SignTranslationProbe(plugin, this, scheduler)
    val messageInterceptor = com.luminaris.extraction.security.clientguard.interceptor.ClientGuardMessageInterceptor(plugin, this)
    val modRegisterParser = com.luminaris.extraction.security.clientguard.parser.ModRegisterParser()

    private val profiles = ConcurrentHashMap<UUID, ClientProfile>()
    private val totalViolations = AtomicInteger(0)
    private val violationsByClient = ConcurrentHashMap<String, AtomicInteger>()

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val logLock = Any()

    /**
     * Initializes the client guard configuration and registers security logs folder.
     */
    fun initialize() {
        configAdapter.initialize()
        val logsFolder = File(plugin.dataFolder, "logs")
        if (!logsFolder.exists()) {
            logsFolder.mkdirs()
        }
        messageInterceptor.register()
        plugin.logger.info("[ClientGuard] Initialized Client Guard Engine in ${configAdapter.guardMode} mode.")
    }

    /**
     * Reloads client guard configuration from disk.
     */
    fun reload() {
        configAdapter.reload()
        plugin.logger.info("[ClientGuard] Reloaded client guard configuration (${configAdapter.getAllRules().size} detection rules loaded).")
    }

    /**
     * Cleans up tracking caches upon server or plugin shutdown.
     */
    fun shutdown() {
        messageInterceptor.unregister()
        signTranslationProbe.clearAll()
        profiles.clear()
    }

    /**
     * Retrieves or creates a client profile for the given player session.
     */
    fun getOrCreateProfile(player: Player): ClientProfile {
        return profiles.computeIfAbsent(player.uniqueId) {
            ClientProfile(
                playerUuid = player.uniqueId,
                playerName = player.name,
                brand = player.clientBrandName
            )
        }
    }

    /**
     * Removes player profile upon disconnection.
     */
    fun removeProfile(playerUuid: UUID) {
        profiles.remove(playerUuid)
    }

    /**
     * Determines if a player should bypass client guard inspections.
     * 1. Whitelisted bypass list (always honored, even for OPs)
     * 2. Permission check (honored unless check_operators forces inspection on OPs)
     */
    fun shouldBypass(player: Player): Boolean {
        if (!configAdapter.isGuardEnabled) return true
        if (configAdapter.isBypassed(player)) {
            debug("Player ${player.name} is in bypassed_players whitelist -> BYPASS GRANTED")
            return true
        }
        if (player.isOp && configAdapter.isCheckOperatorsEnabled) {
            return false
        }
        return player.hasPermission(configAdapter.bypassPermission)
    }

    /**
     * Outputs debug information to console if debug mode is enabled.
     */
    fun debug(message: String) {
        if (configAdapter.isDebugEnabled) {
            plugin.logger.info("[ClientGuard-Debug] $message")
        }
    }

    /**
     * Parses and evaluates incoming plugin messaging channel registrations (e.g. from minecraft:register).
     */
    fun handleIncomingChannels(player: Player, rawChannels: String) {
        if (!configAdapter.isGuardEnabled) return
        if (shouldBypass(player)) {
            debug("Player ${player.name} bypassed incoming channels inspection")
            return
        }

        val profile = getOrCreateProfile(player)
        if (profile.isPunished) return

        debug("Processing incoming channels from ${player.name}: $rawChannels")
        val parseResult = modRegisterParser.parseChannels(rawChannels, configAdapter.blacklistedModNamespaces)
        profile.discoveredMods.addAll(parseResult.allDiscoveredMods)
        if (parseResult.hasFabricChannels) {
            profile.hasFabricChannels = true
        }

        // 1. Check Brand Spoofing (Claiming Vanilla while sending Fabric Channels)
        if (configAdapter.isBrandSpoofDetectionEnabled && profile.hasFabricChannels) {
            val brand = (player.clientBrandName ?: profile.brand ?: "").trim().lowercase()
            if (brand == "vanilla" || brand == "minecraft") {
                val spoofRule = DetectionRule(
                    id = "brand_spoof",
                    name = "Brand Spoofing (Fabric disguised as Vanilla)",
                    enabled = true,
                    matchBrands = emptyList(),
                    matchChannels = emptyList(),
                    actions = configAdapter.brandSpoofActions
                )
                val result = InspectionResult(
                    isViolated = true,
                    matchedRule = spoofRule,
                    detectionSource = "BRAND_SPOOF_CORRELATION",
                    matchedBrand = player.clientBrandName ?: profile.brand,
                    matchedChannel = "fabric_handshake"
                )
                handleViolation(player, profile, result)
                return
            }
        }

        // 2. Check Mod Blacklist Namespaces
        for (mod in parseResult.detectedBlacklistMods) {
            val rule = configAdapter.getRule(mod) ?: DetectionRule(
                id = "mod_$mod",
                name = "Disallowed Mod: $mod",
                enabled = true,
                matchBrands = emptyList(),
                matchChannels = emptyList(),
                actions = listOf(
                    ViolationAction.Kick("<red><bold>[Luminaris Security]</bold></red><newline><gray>ตรวจพบ Mod ที่ไม่อนุญาตในเซิร์ฟเวอร์: <yellow>$mod</yellow></gray>"),
                    ViolationAction.Alert(),
                    ViolationAction.Log
                )
            )

            val result = InspectionResult(
                isViolated = true,
                matchedRule = rule,
                detectionSource = "MOD_NAMESPACE_BLACKLIST",
                matchedBrand = player.clientBrandName ?: profile.brand,
                matchedChannel = "mod:$mod"
            )
            handleViolation(player, profile, result)
            return
        }
    }

    /**
     * Inspects a player's client brand and listening plugin messaging channels.
     *
     * @param player The player to inspect.
     * @param triggerSource Source of inspection: "JOIN", "DELAYED_CHECK", or "CHANNEL_REGISTER".
     * @param specificChannel Channel name if triggered by [org.bukkit.event.player.PlayerRegisterChannelEvent].
     * @return [InspectionResult] containing violation details.
     */
    fun inspectPlayer(
        player: Player,
        triggerSource: String,
        specificChannel: String? = null,
        specificBrand: String? = null
    ): InspectionResult {
        if (!configAdapter.isGuardEnabled) {
            return InspectionResult.CLEAN
        }

        // Check bypass permission
        if (shouldBypass(player)) {
            debug("Player ${player.name} bypassed inspection at $triggerSource (isOp: ${player.isOp})")
            return InspectionResult.CLEAN
        }

        val profile = getOrCreateProfile(player)

        // Prevent repeated executions for already punished session
        if (profile.isPunished) {
            return InspectionResult.CLEAN
        }

        // Update brand and listening channels
        if (specificBrand != null) {
            profile.brand = specificBrand
        }
        val brand = player.clientBrandName ?: profile.brand ?: "unknown"
        profile.brand = brand

        if (specificChannel != null) {
            profile.registeredChannels.add(specificChannel)
        }
        profile.registeredChannels.addAll(player.listeningPluginChannels)

        debug("Inspecting ${player.name} [$triggerSource] - Brand: '$brand', Listening Channels: ${profile.registeredChannels.size}")

        // 1. Brand Spoofing Correlation Detection (Disguised Hacked Clients)
        // If a client claims brand is "vanilla" but sends modded channels/payloads, it is guaranteed spoofing
        if (configAdapter.isBrandSpoofDetectionEnabled) {
            val isClaimedVanilla = brand.equals("vanilla", ignoreCase = true)
            val detectedModdedChannel = profile.registeredChannels.firstOrNull { ch ->
                ch.startsWith("fabric:", ignoreCase = true) ||
                ch.startsWith("fabric-", ignoreCase = true) ||
                ch.startsWith("fml:", ignoreCase = true) ||
                ch.startsWith("forge:", ignoreCase = true) ||
                ch.startsWith("neoforge:", ignoreCase = true)
            }

            if (isClaimedVanilla && detectedModdedChannel != null) {
                val spoofRule = DetectionRule(
                    id = "brand_spoof",
                    name = "Brand Spoofing (Client claimed Vanilla but sent Modded payload)",
                    enabled = true,
                    matchBrands = emptyList(),
                    matchChannels = emptyList(),
                    actions = configAdapter.brandSpoofActions
                )
                val result = InspectionResult(
                    isViolated = true,
                    matchedRule = spoofRule,
                    detectionSource = "BRAND_SPOOF_CORRELATION",
                    matchedBrand = brand,
                    matchedChannel = detectedModdedChannel
                )
                handleViolation(player, profile, result)
                return result
            }
        }

        // 2. Strict Vanilla Mode Evaluation
        if (configAdapter.guardMode == GuardMode.STRICT_VANILLA) {
            val isVanilla = brand.equals("vanilla", ignoreCase = true)
            val hasModdedChannels = profile.registeredChannels.any { ch ->
                ch.startsWith("fabric:", ignoreCase = true) ||
                ch.startsWith("fml:", ignoreCase = true) ||
                ch.startsWith("forge:", ignoreCase = true) ||
                ch.startsWith("neoforge:", ignoreCase = true)
            }

            if (!isVanilla || hasModdedChannels) {
                val rule = configAdapter.getRule("mod_loaders") ?: DetectionRule(
                    id = "strict_vanilla",
                    name = "Modded Client ($brand)",
                    enabled = true,
                    matchBrands = emptyList(),
                    matchChannels = emptyList(),
                    actions = listOf(
                        ViolationAction.Kick("<red><bold>[Luminaris Security]</bold></red><newline><gray>เซิร์ฟเวอร์นี้อนุญาตเฉพาะ <green>Official Vanilla Client</green> เท่านั้น</gray>"),
                        ViolationAction.Alert(),
                        ViolationAction.Log
                    )
                )
                val result = InspectionResult(
                    isViolated = true,
                    matchedRule = rule,
                    detectionSource = if (!isVanilla) "STRICT_VANILLA_BRAND" else "STRICT_VANILLA_CHANNEL",
                    matchedBrand = brand,
                    matchedChannel = specificChannel
                )
                handleViolation(player, profile, result)
                return result
            }
        }

        // 2. Whitelist Only Mode Evaluation
        if (configAdapter.guardMode == GuardMode.WHITELIST_ONLY) {
            val isAllowed = configAdapter.getAllowedBrands().any { it.containsMatchIn(brand) }
            if (!isAllowed) {
                val rule = DetectionRule(
                    id = "whitelist_rejection",
                    name = "Unapproved Client ($brand)",
                    enabled = true,
                    matchBrands = emptyList(),
                    matchChannels = emptyList(),
                    actions = listOf(
                        ViolationAction.Kick("<red><bold>[Luminaris Security]</bold></red><newline><gray>Client ที่คุณใช้งาน (<yellow>$brand</yellow>) ไม่ได้อยู่ในรายการที่ได้รับอนุญาต</gray>"),
                        ViolationAction.Alert(),
                        ViolationAction.Log
                    )
                )
                val result = InspectionResult(
                    isViolated = true,
                    matchedRule = rule,
                    detectionSource = "WHITELIST_REJECTION",
                    matchedBrand = brand,
                    matchedChannel = specificChannel
                )
                handleViolation(player, profile, result)
                return result
            }
        }

        // 3. Configured Detection Rules Evaluation
        for (rule in configAdapter.getAllRules().values) {
            if (!rule.enabled) continue

            // A. Check Brand Match
            if (rule.matchesBrand(brand)) {
                val result = InspectionResult(
                    isViolated = true,
                    matchedRule = rule,
                    detectionSource = "BRAND",
                    matchedBrand = brand,
                    matchedChannel = null
                )
                handleViolation(player, profile, result)
                return result
            }

            // B. Check Specific Trigger Channel Match
            if (specificChannel != null && rule.matchesChannel(specificChannel)) {
                val result = InspectionResult(
                    isViolated = true,
                    matchedRule = rule,
                    detectionSource = "CHANNEL",
                    matchedBrand = brand,
                    matchedChannel = specificChannel
                )
                handleViolation(player, profile, result)
                return result
            }

            // C. Check All Listening Channels Match
            val matchedChannel = profile.registeredChannels.firstOrNull { rule.matchesChannel(it) }
            if (matchedChannel != null) {
                val result = InspectionResult(
                    isViolated = true,
                    matchedRule = rule,
                    detectionSource = "CHANNEL",
                    matchedBrand = brand,
                    matchedChannel = matchedChannel
                )
                handleViolation(player, profile, result)
                return result
            }
        }

        return InspectionResult.CLEAN
    }

    /**
     * Directly processes a violation triggered by active probing mechanisms (e.g. Sign Translation Probe).
     */
    fun handleViolationDirect(player: Player, profile: ClientProfile, result: InspectionResult) {
        handleViolation(player, profile, result)
    }

    /**
     * Handles violation execution once a disallowed client is detected.
     */
    private fun handleViolation(
        player: Player,
        profile: ClientProfile,
        result: InspectionResult
    ) {
        val rule = result.matchedRule ?: return
        profile.isPunished = true

        totalViolations.incrementAndGet()
        violationsByClient.computeIfAbsent(rule.name) { AtomicInteger(0) }.incrementAndGet()

        val brand = result.matchedBrand ?: profile.brand ?: "unknown"
        val channel = result.matchedChannel ?: "N/A"
        val source = result.detectionSource ?: "UNKNOWN"
        val ip = player.address?.address?.hostAddress ?: "unknown"

        executeActions(
            player = player,
            rule = rule,
            source = source,
            brand = brand,
            channel = channel,
            ip = ip
        )
    }

    /**
     * Executes the list of configured violation actions thread-safely across Folia and Paper.
     */
    private fun executeActions(
        player: Player,
        rule: DetectionRule,
        source: String,
        brand: String,
        channel: String,
        ip: String
    ) {
        val placeholders = mapOf(
            "<player>" to player.name,
            "<uuid>" to player.uniqueId.toString(),
            "<ip>" to ip,
            "<detected_client>" to rule.name,
            "<brand>" to brand,
            "<channel>" to channel,
            "<source>" to source
        )

        for (action in rule.actions) {
            when (action) {
                is ViolationAction.Log -> {
                    logViolation(player, rule, brand, channel, source, ip)
                }

                is ViolationAction.Alert -> {
                    if (configAdapter.isStaffAlertEnabled) {
                        val format = action.format ?: configAdapter.staffAlertFormat
                        val perm = action.permission ?: configAdapter.staffAlertPermission
                        val formattedMsg = replacePlaceholders(format, placeholders + ("<action>" to "DETECTED"))
                        val alertComponent = TranslationManager.parseComponent(formattedMsg)

                        for (staff in Bukkit.getOnlinePlayers()) {
                            if (staff.hasPermission(perm)) {
                                staff.sendMessage(alertComponent)
                            }
                        }
                    }
                }

                is ViolationAction.Command -> {
                    scheduler.runGlobal {
                        for (rawCommand in action.commands) {
                            val cmd = replacePlaceholders(rawCommand, placeholders + ("<action>" to "COMMAND"))
                            runCatching {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
                            }.onFailure { ex ->
                                plugin.logger.severe("[ClientGuard] Failed to execute console command '$cmd': ${ex.message}")
                            }
                        }
                    }
                }

                is ViolationAction.Ban -> {
                    val duration = action.duration ?: "permanent"
                    val reason = action.reason ?: "Using disallowed client/mod (${rule.name})"
                    val formattedReason = replacePlaceholders(reason, placeholders + ("<action>" to "BAN"))

                    scheduler.runGlobal {
                        val banCmd = if (duration.equals("permanent", ignoreCase = true)) {
                            "ban ${player.name} $formattedReason"
                        } else {
                            "tempban ${player.name} $duration $formattedReason"
                        }
                        runCatching {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), banCmd)
                        }
                    }

                    // Kick player after ban (delayed slightly to allow packet completion)
                    val kickReason = TranslationManager.parseComponent(formattedReason)
                    scheduler.runAtEntityDelayed(player, 10L) {
                        if (player.isOnline) {
                            debug("Kicking banned player ${player.name}")
                            player.kick(kickReason)
                        }
                    }
                }

                is ViolationAction.Kick -> {
                    val rawMsg = action.message ?: configAdapter.defaultKickMessage
                    val formattedKick = replacePlaceholders(rawMsg, placeholders + ("<action>" to "KICK"))
                    val kickComponent = TranslationManager.parseComponent(formattedKick)

                    scheduler.runAtEntityDelayed(player, 10L) {
                        if (player.isOnline) {
                            debug("Kicking player ${player.name} for rule '${rule.name}'")
                            player.kick(kickComponent)
                        }
                    }
                }
            }
        }
    }

    /**
     * Writes violation details to console and security log file asynchronously.
     */
    private fun logViolation(
        player: Player,
        rule: DetectionRule,
        brand: String,
        channel: String,
        source: String,
        ip: String
    ) {
        val timestamp = LocalDateTime.now().format(dateTimeFormatter)
        val logLine = "[$timestamp] [DETECTED] Player: ${player.name} (${player.uniqueId}) | IP: $ip | Client: ${rule.name} | Brand: $brand | Channel: $channel | Source: $source"

        if (configAdapter.isConsoleLoggingEnabled) {
            plugin.logger.warning("[ClientGuard] $logLine")
        }

        if (configAdapter.isFileLoggingEnabled) {
            scheduler.runAsync {
                synchronized(logLock) {
                    runCatching {
                        val logFile = File(plugin.dataFolder, "logs/${configAdapter.logFileName}")
                        PrintWriter(FileWriter(logFile, true)).use { writer ->
                            writer.println(logLine)
                        }
                    }.onFailure { ex ->
                        plugin.logger.severe("[ClientGuard] Failed to write to log file: ${ex.message}")
                    }
                }
            }
        }
    }

    /**
     * Utility method to replace key-value placeholders in a text string.
     */
    private fun replacePlaceholders(text: String, placeholders: Map<String, String>): String {
        var result = text
        for ((key, value) in placeholders) {
            result = result.replace(key, value)
        }
        return result
    }

    /**
     * Builds a detailed inspection report string for administrative commands.
     */
    fun buildInspectionReport(target: Player): String {
        val profile = profiles[target.uniqueId]
        val brand = target.clientBrandName ?: profile?.brand ?: "unknown"
        val channels = (target.listeningPluginChannels + (profile?.registeredChannels ?: emptySet())).toList()

        return buildString {
            append("<gradient:#00FFA3:#00B8FF><bold>[ClientGuard Report]</bold></gradient> <white>${target.name}</white>\n")
            append("<gray>UUID: <white>${target.uniqueId}</white></gray>\n")
            append("<gray>Client Brand: <yellow>$brand</yellow></gray>\n")
            append("<gray>Enforcement Mode: <aqua>${configAdapter.guardMode}</aqua></gray>\n")
            val isWhitelisted = configAdapter.isBypassed(target)
            val hasBypassPerm = target.hasPermission(configAdapter.bypassPermission)
            val bypassStatus = when {
                isWhitelisted -> "<green><bold>BYPASSED (Whitelisted)</bold></green>"
                hasBypassPerm && (!target.isOp || !configAdapter.isCheckOperatorsEnabled) -> "<green>BYPASSED (Permission)</green>"
                else -> "<gray>INSPECTED</gray>"
            }
            append("<gray>Bypass Status: $bypassStatus</gray>\n")
            append("<gray>Has Fabric Handshake: <white>${profile?.hasFabricChannels ?: false}</white></gray>\n")
            append("<gray>Punished in Session: <white>${profile?.isPunished ?: false}</white></gray>\n")
            val mods = profile?.discoveredMods?.toList() ?: emptyList()
            if (mods.isNotEmpty()) {
                append("<gray>Discovered Mods (<yellow>${mods.size}</yellow>): <white>${mods.joinToString(", ")}</white></gray>\n")
            }
            append("<gray>Listening Channels (<yellow>${channels.size}</yellow>):</gray>\n")
            if (channels.isEmpty()) {
                append("  <dark_gray>- (none)</dark_gray>\n")
            } else {
                for (ch in channels.take(15)) {
                    append("  <gray>• <white>$ch</white></gray>\n")
                }
                if (channels.size > 15) {
                    append("  <dark_gray>... and ${channels.size - 15} more</dark_gray>\n")
                }
            }
        }
    }

    fun getTotalViolationsCount(): Int = totalViolations.get()

    fun getViolationsByClientMap(): Map<String, Int> =
        violationsByClient.mapValues { it.value.get() }
}

