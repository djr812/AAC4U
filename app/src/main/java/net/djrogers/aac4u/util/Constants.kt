package net.djrogers.aac4u.util

/**
 * App-wide constants.
 */
object Constants {
    // Grid limits
    const val MIN_GRID_COLUMNS = 2
    const val MAX_GRID_COLUMNS = 10
    const val DEFAULT_GRID_COLUMNS = 4

    // TTS limits
    const val MIN_SPEECH_RATE = 0.5f
    const val MAX_SPEECH_RATE = 2.0f
    const val DEFAULT_SPEECH_RATE = 1.0f
    const val MIN_PITCH = 0.5f
    const val MAX_PITCH = 2.0f
    const val DEFAULT_PITCH = 1.0f

    // Dwell selection
    const val MIN_DWELL_TIME_MS = 300L
    const val MAX_DWELL_TIME_MS = 5000L
    const val DEFAULT_DWELL_TIME_MS = 1500L

    // Scanning
    const val MIN_SCAN_SPEED_MS = 500L
    const val MAX_SCAN_SPEED_MS = 5000L
    const val DEFAULT_SCAN_SPEED_MS = 2000L

    // Predictions
    const val DEFAULT_PREDICTION_COUNT = 5

    // Backup
    const val BACKUP_FILE_EXTENSION = ".aac4u"
    const val BACKUP_MIME_TYPE = "application/zip"

    // Symbols
    const val SYMBOL_DIRECTORY = "symbols"
    const val ARASAAC_CORE_DIRECTORY = "symbols/arasaac_core"
    const val CUSTOM_IMAGES_DIRECTORY = "custom"
    const val SYMBOL_SIZE_PX = 300
}
