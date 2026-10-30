package com.luminaris.extraction.security.clientguard.model

/**
 * Operating mode for Client Guard enforcement.
 */
enum class GuardMode {
    /**
     * Blocks known cheat clients, suspicious channels, and blacklisted brands,
     * while allowing clean Fabric/mod loaders (e.g. Sodium/Iris FPS mods).
     */
    SMART_HYBRID,

    /**
     * Enforces pure vanilla clients only; completely blocks Fabric, Forge, and any mod loaders.
     * Guarantees 100% prevention of Meteor and Wurst, as they cannot execute without Fabric.
     */
    STRICT_VANILLA,

    /**
     * Permits only client brands explicitly matching the allowed_brands regex patterns.
     */
    WHITELIST_ONLY;

    companion object {
        fun fromString(value: String): GuardMode {
            return entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) } ?: SMART_HYBRID
        }
    }
}

