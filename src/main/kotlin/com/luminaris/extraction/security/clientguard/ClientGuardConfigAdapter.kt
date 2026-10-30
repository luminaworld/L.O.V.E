package com.luminaris.extraction.security.clientguard

import com.luminaris.extraction.security.clientguard.model.DetectionRule
import com.luminaris.extraction.security.clientguard.model.GuardMode
import com.luminaris.extraction.security.clientguard.model.ViolationAction
import com.luminaris.extraction.storage.ConfigAdapter
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

/**
 * Concrete configuration adapter for client_guard.yml managing anti-mod & cheat detection settings.
 */
class ClientGuardConfigAdapter(plugin: Plugin) : ConfigAdapter(plugin, "client_guard.yml") {

    @Volatile
    var isGuardEnabled: Boolean = true
        private set

    @Volatile
    var isDebugEnabled: Boolean = false
        private set

    @Volatile
    var isCheckOperatorsEnabled: Boolean = true
        private set

    @Volatile
    var guardMode: GuardMode = GuardMode.SMART_HYBRID
        private set

    @Volatile
    var delayedCheckTicks: Long = 4L
        private set

    @Volatile
    var bypassPermission: String = "luminaris.security.clientguard.bypass"
        private set

    @Volatile
    var defaultKickMessage: String = "<red><bold>[Security]</bold> Disallowed client detected: <yellow><detected_client></yellow></red>"
        private set

    @Volatile
    var isStaffAlertEnabled: Boolean = true
        private set

    @Volatile
    var staffAlertPermission: String = "luminaris.security.clientguard.alert"
        private set

    @Volatile
    var staffAlertFormat: String = "<red>[Security Alert]</red> <white><player></white> joined with <yellow><detected_client></yellow>"
        private set

    @Volatile
    var isConsoleLoggingEnabled: Boolean = true
        private set

    @Volatile
    var isFileLoggingEnabled: Boolean = true
        private set

    @Volatile
    var logFileName: String = "client_guard.log"
        private set

    @Volatile
    var isBrandSpoofDetectionEnabled: Boolean = true
        private set

    @Volatile
    var isSignProbeEnabled: Boolean = true
        private set

    @Volatile
    var signProbeDelayTicks: Long = 6L
        private set

    val brandSpoofActions = mutableListOf<ViolationAction>()
    val blacklistedModNamespaces = mutableSetOf<String>()
    val bypassedPlayers = ConcurrentHashMap.newKeySet<String>()

    private val allowedBrandsList = mutableListOf<Regex>()
    private val detectionRules = ConcurrentHashMap<String, DetectionRule>()

    override fun onLoaded(config: YamlConfiguration) {
        isGuardEnabled = config.getBoolean("enabled", true)
        isDebugEnabled = config.getBoolean("debug", true)
        isCheckOperatorsEnabled = config.getBoolean("check_operators", true)
        guardMode = GuardMode.fromString(config.getString("mode", "SMART_HYBRID") ?: "SMART_HYBRID")
        delayedCheckTicks = config.getLong("delayed_check_ticks", 4L).coerceAtLeast(1L)
        bypassPermission = config.getString("bypass_permission", "luminaris.security.clientguard.bypass") ?: "luminaris.security.clientguard.bypass"
        defaultKickMessage = config.getString(
            "default_kick_message",
            "<red><bold>[Security]</bold> Disallowed client detected: <yellow><detected_client></yellow></red>"
        ) ?: "<red><bold>[Security]</bold> Disallowed client detected: <yellow><detected_client></yellow></red>"

        isStaffAlertEnabled = config.getBoolean("staff_alert.enabled", true)
        staffAlertPermission = config.getString("staff_alert.permission", "luminaris.security.clientguard.alert") ?: "luminaris.security.clientguard.alert"
        staffAlertFormat = config.getString(
            "staff_alert.format",
            "<red>[Security Alert]</red> <white><player></white> joined with <yellow><detected_client></yellow>"
        ) ?: "<red>[Security Alert]</red> <white><player></white> joined with <yellow><detected_client></yellow>"

        isConsoleLoggingEnabled = config.getBoolean("logging.console", true)
        isFileLoggingEnabled = config.getBoolean("logging.file", true)
        logFileName = config.getString("logging.file_name", "client_guard.log") ?: "client_guard.log"

        isBrandSpoofDetectionEnabled = config.getBoolean("brand_spoof_detection.enabled", true)
        isSignProbeEnabled = config.getBoolean("sign_probe.enabled", true)
        signProbeDelayTicks = config.getLong("sign_probe.delay_ticks", 6L).coerceAtLeast(1L)

        // Load Brand Spoof Actions
        brandSpoofActions.clear()
        val spoofActionsList = config.getMapList("brand_spoof_detection.actions")
        for (actionMap in spoofActionsList) {
            val type = (actionMap["type"] as? String)?.uppercase() ?: continue
            when (type) {
                "KICK" -> brandSpoofActions.add(ViolationAction.Kick(actionMap["message"] as? String))
                "BAN" -> brandSpoofActions.add(ViolationAction.Ban(actionMap["duration"] as? String, actionMap["reason"] as? String))
                "ALERT" -> brandSpoofActions.add(ViolationAction.Alert(actionMap["format"] as? String, actionMap["permission"] as? String))
                "COMMAND" -> {
                    val cmds = when (val c = actionMap["commands"] ?: actionMap["command"]) {
                        is List<*> -> c.mapNotNull { it?.toString() }
                        is String -> listOf(c)
                        else -> emptyList()
                    }
                    if (cmds.isNotEmpty()) brandSpoofActions.add(ViolationAction.Command(cmds))
                }
                "LOG" -> brandSpoofActions.add(ViolationAction.Log)
            }
        }
        if (brandSpoofActions.isEmpty()) {
            brandSpoofActions.add(ViolationAction.Kick("<red><bold>[Security]</bold> ตรวจพบการปลอมแปลง Client Brand (Brand Spoofing)</red>"))
            brandSpoofActions.add(ViolationAction.Alert())
            brandSpoofActions.add(ViolationAction.Log)
        }

        // Load Blacklisted Mod Namespaces
        blacklistedModNamespaces.clear()
        val modBlacklist = config.getStringList("mod_namespaces.blacklist")
        if (modBlacklist.isNotEmpty()) {
            blacklistedModNamespaces.addAll(modBlacklist.map { it.lowercase().trim() })
        } else {
            blacklistedModNamespaces.addAll(com.luminaris.extraction.security.clientguard.parser.ModRegisterParser.DEFAULT_BLACKLIST_MODS)
        }

        // Load Bypassed Players Whitelist
        bypassedPlayers.clear()
        val bypassList = config.getStringList("bypassed_players")
        if (bypassList.isNotEmpty()) {
            bypassedPlayers.addAll(bypassList.map { it.lowercase().trim() })
        }

        // Load Allowed Brands
        allowedBrandsList.clear()
        val rawAllowed = config.getStringList("allowed_brands")
        for (pattern in rawAllowed) {
            runCatching { Regex(pattern) }.onSuccess { allowedBrandsList.add(it) }
        }

        // Load Detection Rules
        detectionRules.clear()
        val detectionsSec = config.getConfigurationSection("detections")
        if (detectionsSec != null) {
            for (key in detectionsSec.getKeys(false)) {
                val ruleSec = detectionsSec.getConfigurationSection(key) ?: continue
                val rule = parseDetectionRule(key, ruleSec)
                detectionRules[key] = rule
            }
        }
    }

    private fun parseDetectionRule(id: String, section: ConfigurationSection): DetectionRule {
        val name = section.getString("name", id) ?: id
        val enabled = section.getBoolean("enabled", true)

        val brandPatterns = section.getStringList("match_brands").mapNotNull { pattern ->
            runCatching { Regex(pattern) }.getOrNull()
        }

        val channelPatterns = section.getStringList("match_channels").mapNotNull { pattern ->
            runCatching { Regex(pattern) }.getOrNull()
        }

        val actions = mutableListOf<ViolationAction>()
        val actionsList = section.getMapList("actions")
        for (actionMap in actionsList) {
            val type = (actionMap["type"] as? String)?.uppercase() ?: continue
            when (type) {
                "KICK" -> {
                    val msg = actionMap["message"] as? String
                    actions.add(ViolationAction.Kick(msg))
                }
                "BAN" -> {
                    val duration = actionMap["duration"] as? String
                    val reason = actionMap["reason"] as? String
                    actions.add(ViolationAction.Ban(duration, reason))
                }
                "ALERT" -> {
                    val fmt = actionMap["format"] as? String
                    val perm = actionMap["permission"] as? String
                    actions.add(ViolationAction.Alert(fmt, perm))
                }
                "COMMAND" -> {
                    val cmds = when (val c = actionMap["commands"] ?: actionMap["command"]) {
                        is List<*> -> c.mapNotNull { it?.toString() }
                        is String -> listOf(c)
                        else -> emptyList()
                    }
                    if (cmds.isNotEmpty()) {
                        actions.add(ViolationAction.Command(cmds))
                    }
                }
                "LOG" -> {
                    actions.add(ViolationAction.Log)
                }
            }
        }

        // If no actions configured, default to KICK and ALERT
        if (actions.isEmpty()) {
            actions.add(ViolationAction.Kick())
            actions.add(ViolationAction.Alert())
            actions.add(ViolationAction.Log)
        }

        return DetectionRule(
            id = id,
            name = name,
            enabled = enabled,
            matchBrands = brandPatterns,
            matchChannels = channelPatterns,
            actions = actions
        )
    }

    fun getAllowedBrands(): List<Regex> = allowedBrandsList.toList()

    fun getAllRules(): Map<String, DetectionRule> = detectionRules.toMap()

    fun getRule(id: String): DetectionRule? = detectionRules[id]

    /**
     * Checks if a player is in the bypass whitelist by username or UUID.
     */
    fun isBypassed(player: org.bukkit.entity.Player): Boolean {
        return bypassedPlayers.contains(player.name.lowercase().trim()) ||
               bypassedPlayers.contains(player.uniqueId.toString().lowercase().trim())
    }

    /**
     * Checks if a username or UUID is in the bypass whitelist.
     */
    fun isBypassed(nameOrUuid: String): Boolean {
        return bypassedPlayers.contains(nameOrUuid.lowercase().trim())
    }

    /**
     * Adds a player username or UUID to the bypass list and persists to client_guard.yml.
     * @return true if newly added, false if already present.
     */
    fun addBypassedPlayer(nameOrUuid: String): Boolean {
        val normalized = nameOrUuid.lowercase().trim()
        val added = bypassedPlayers.add(normalized)
        if (added) {
            saveBypassedPlayers()
        }
        return added
    }

    /**
     * Removes a player username or UUID from the bypass list and persists to client_guard.yml.
     * @return true if removed, false if not present.
     */
    fun removeBypassedPlayer(nameOrUuid: String): Boolean {
        val normalized = nameOrUuid.lowercase().trim()
        val removed = bypassedPlayers.remove(normalized)
        if (removed) {
            saveBypassedPlayers()
        }
        return removed
    }

    /**
     * Retrieves an immutable set of all currently bypassed players/UUIDs.
     */
    fun getBypassedPlayers(): Set<String> = bypassedPlayers.toSet()

    private fun saveBypassedPlayers() {
        set("bypassed_players", bypassedPlayers.toList().sorted())
        save()
    }
}

