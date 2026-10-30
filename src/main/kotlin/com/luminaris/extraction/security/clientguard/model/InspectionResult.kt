package com.luminaris.extraction.security.clientguard.model

import java.util.UUID

/**
 * Inspection result of a player's client brand and registered channels.
 */
data class InspectionResult(
    val isViolated: Boolean,
    val matchedRule: DetectionRule? = null,
    val detectionSource: String? = null,
    val matchedBrand: String? = null,
    val matchedChannel: String? = null
) {
    companion object {
        val CLEAN = InspectionResult(isViolated = false)
    }
}

/**
 * In-memory client profile for an active player session.
 */
data class ClientProfile(
    val playerUuid: UUID,
    val playerName: String,
    var brand: String? = null,
    val registeredChannels: MutableSet<String> = java.util.concurrent.ConcurrentHashMap.newKeySet(),
    val discoveredMods: MutableSet<String> = java.util.concurrent.ConcurrentHashMap.newKeySet(),
    var hasFabricChannels: Boolean = false,
    val firstSeenTimestamp: Long = System.currentTimeMillis(),
    var isPunished: Boolean = false
)

