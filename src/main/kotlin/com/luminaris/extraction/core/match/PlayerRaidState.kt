package com.luminaris.extraction.core.match

/**
 * Tactical lifecycle state of a player during an extraction match/raid.
 */
enum class PlayerRaidState {
    /** Actively exploring or engaging in the match world. */
    IN_RAID,

    /** Inside an extraction evacuation zone awaiting extraction timer. */
    EXTRACTING,

    /** Successfully evacuated through an extraction zone. */
    EXTRACTED,

    /** Eliminated during the match by damage or combat. */
    KILLED_IN_ACTION,

    /** Failed to evacuate before raid countdown timer reached zero. */
    MISSING_IN_ACTION;

    /**
     * Identifies whether this player state is final/terminal (no further in-match actions possible).
     */
    val isTerminal: Boolean
        get() = this == EXTRACTED || this == KILLED_IN_ACTION || this == MISSING_IN_ACTION

    companion object {
        /** Shorthand alias for MISSING_IN_ACTION */
        val MIA = MISSING_IN_ACTION

        /** Shorthand alias for KILLED_IN_ACTION */
        val KIA = KILLED_IN_ACTION
    }
}
