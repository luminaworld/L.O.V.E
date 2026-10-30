package com.luminaris.extraction.security.clientguard.model

/**
 * Granular violation actions executed upon detection of disallowed clients or mods.
 */
sealed interface ViolationAction {

    /**
     * Disconnects the player from the server with an optional custom MiniMessage payload.
     */
    data class Kick(val message: String? = null) : ViolationAction

    /**
     * Bans the player for a given duration (or permanent) and kicks them.
     */
    data class Ban(
        val duration: String? = null,
        val reason: String? = null
    ) : ViolationAction

    /**
     * Broadcasts a notification to staff members holding the specified permission.
     */
    data class Alert(
        val format: String? = null,
        val permission: String? = null
    ) : ViolationAction

    /**
     * Dispatches one or more console commands safely on Folia's global scheduler.
     */
    data class Command(val commands: List<String>) : ViolationAction

    /**
     * Writes violation details to the console and security log file.
     */
    data object Log : ViolationAction
}

