package net.djrogers.aac4u.accessibility

/**
 * Scanning engine for auto-highlight sequential button selection.
 *
 * Scanning pattern:
 * 1. Highlights rows one at a time (row scanning)
 * 2. User presses switch → moves to column scanning within that row
 * 3. User presses switch again → selects the highlighted button
 *
 * Configurable:
 * - Scan speed (ms between advances)
 * - Scan pattern (linear, group)
 * - Auto-restart at end
 * - Number of loops before stopping
 *
 * TODO Phase 3: Implement scanning engine with configurable patterns.
 */
class ScanningEngine {
    // Placeholder — Phase 3 implementation
}

/**
 * Handles dwell/hover-to-select input for users with motor impairments.
 *
 * When a user holds their finger (or pointer) over a button for a
 * configurable duration, it triggers selection without requiring a tap.
 *
 * TODO Phase 2: Implement dwell selection with visual countdown feedback.
 */
class DwellSelectionHandler {
    // Placeholder — Phase 2 implementation
}

/**
 * Utilities for integrating with Android's built-in Switch Access service.
 *
 * Android Switch Access handles the scanning/highlighting automatically
 * if we set up proper focus order and semantics. This helper ensures
 * our Compose UI works correctly with the system service.
 *
 * TODO Phase 3: Implement switch access integration.
 */
class SwitchAccessHelper {
    // Placeholder — Phase 3 implementation
}
