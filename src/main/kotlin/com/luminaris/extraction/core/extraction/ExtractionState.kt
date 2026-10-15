package com.luminaris.extraction.core.extraction

/**
 * Tactical lifecycle state of a player during an extraction match.
 */
enum class ExtractionState {
    /** Active in the match environment. */
    IN_MATCH,

    /** Inside an extraction zone with an active countdown. */
    EXTRACTING,

    /** Successfully evacuated through an extraction zone. */
    SURVIVED,

    /** Missing in action (e.g., match timer expired before extraction). */
    MIA,

    /** Killed in action during the match. */
    KIA
}
