package com.luminaris.extraction.security.clientguard.model

/**
 * Definition of a single client/mod detection rule.
 */
data class DetectionRule(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val matchBrands: List<Regex>,
    val matchChannels: List<Regex>,
    val actions: List<ViolationAction>
) {
    /**
     * Checks whether the given client brand string matches any configured regex pattern.
     */
    fun matchesBrand(brand: String): Boolean {
        if (matchBrands.isEmpty()) return false
        return matchBrands.any { it.containsMatchIn(brand) }
    }

    /**
     * Checks whether the given channel string matches any configured regex pattern.
     */
    fun matchesChannel(channel: String): Boolean {
        if (matchChannels.isEmpty()) return false
        return matchChannels.any { it.containsMatchIn(channel) }
    }
}

