package com.luminaris.extraction.security.clientguard.parser

import java.util.Locale

/**
 * High-performance parser extracting mod namespaces from minecraft:register channel payloads.
 * Identifies Fabric/Forge mod signatures and detects brand spoofing attempts.
 */
class ModRegisterParser {

    companion object {
        val BUILTIN_NAMESPACES = setOf(
            "minecraft", "neoforge", "forge", "fml", "c", "fabric", "bungeecord", "velocity"
        )

        val DEFAULT_BLACKLIST_MODS = setOf(
            "forgewurst", "forgehax", "wurst", "baritone", "meteor", "meteor-client",
            "aristois", "impact", "liquidbounce", "freecam", "xray", "seedcracker",
            "worlddownloader", "wdl", "inertia", "bleachhack", "futureclient",
            "rusherhack", "thunderhack", "kami", "lambda"
        )
    }

    data class ParseResult(
        val hasFabricChannels: Boolean,
        val detectedBlacklistMods: Set<String>,
        val allDiscoveredMods: Set<String>
    )

    /**
     * Parses a raw channel payload string (or comma/null-separated channel list).
     */
    fun parseChannels(
        channelsData: String,
        blacklist: Set<String> = DEFAULT_BLACKLIST_MODS
    ): ParseResult {
        var hasFabric = false
        val detectedBlacklist = mutableSetOf<String>()
        val allDiscovered = mutableSetOf<String>()

        // Split channels separated by null bytes (\0), spaces, or commas
        val parts = channelsData.split(Regex("[\\u0000\\s,]+"))
        for (part in parts) {
            val trimmed = part.trim().lowercase(Locale.ROOT)
            if (trimmed.isEmpty()) continue

            if (trimmed.startsWith("fabric-") || trimmed.startsWith("fabric:") || trimmed.startsWith("fabricloader")) {
                hasFabric = true
            }

            val colonIdx = trimmed.indexOf(':')
            if (colonIdx <= 0) continue

            val namespace = trimmed.substring(0, colonIdx)
            if (BUILTIN_NAMESPACES.contains(namespace) || namespace.startsWith("fabric-") || namespace.startsWith("fabricloader")) {
                continue
            }

            allDiscovered.add(namespace)

            if (blacklist.contains(namespace)) {
                detectedBlacklist.add(namespace)
            }
        }

        return ParseResult(
            hasFabricChannels = hasFabric,
            detectedBlacklistMods = detectedBlacklist,
            allDiscoveredMods = allDiscovered
        )
    }
}

