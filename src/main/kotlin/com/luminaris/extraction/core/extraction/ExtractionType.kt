package com.luminaris.extraction.core.extraction

/**
 * Operational extraction classification rules for evacuation zones.
 */
enum class ExtractionType {
    /**
     * Unconditionally open for all players and squads throughout the match duration.
     */
    ALWAYS_OPEN,

    /**
     * Restricted strictly to authorized squad or team identifiers.
     */
    TEAM_SPECIFIC,

    /**
     * Requires a specific keycard, quest item, or resource present in the player's inventory.
     */
    CONDITIONAL_ITEM,

    /**
     * Only operational during a specific elapsed match timeframe [timeStartSeconds, timeEndSeconds].
     */
    TIMED_WINDOW
}
