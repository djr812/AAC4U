package net.djrogers.aac4u.data.symbol

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.djrogers.aac4u.util.Constants
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages loading symbol images from bundled assets, custom storage,
 * and live ARASAAC API lookups.
 *
 * Symbol resolution order:
 * 1. Check symbol_mapping.json for an exact word match (bundled)
 * 2. Check for a sanitized filename match in the bundled assets folder
 * 3. Check if a previously downloaded symbol exists in local storage
 * 4. Return null for synchronous calls (use searchAndDownloadSymbol for async API lookup)
 */
@Singleton
class SymbolManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AAC4U_SYMBOL"
        private const val API_BASE = "https://api.arasaac.org/v1"
        private const val STATIC_BASE = "https://static.arasaac.org/pictograms"
        private const val SYMBOL_SIZE = 300
        private const val DOWNLOADED_DIR = "downloaded_symbols"
    }

    private val customDir: File by lazy {
        File(context.filesDir, Constants.CUSTOM_IMAGES_DIRECTORY).also { it.mkdirs() }
    }

    private val downloadedDir: File by lazy {
        File(context.filesDir, DOWNLOADED_DIR).also { it.mkdirs() }
    }

    private var symbolMapping: Map<String, String>? = null

    // Phrase search overrides — maps phrases to searchable single words
    private val phraseOverrides = mapOf(
        "thank you" to "thank",
        "excuse me" to "excuse",
        "well done" to "congratulations",
        "I love you" to "love",
        "my turn" to "turn",
        "your turn" to "turn",
        "let's go" to "go",
        "come here" to "come",
        "look at this" to "look",
        "I don't know" to "doubt",
        "wait please" to "wait",
        "I need help" to "help",
        "I'm hungry" to "hungry",
        "I'm thirsty" to "thirsty",
        "I need the bathroom" to "bathroom",
        "I don't feel well" to "sick",
        "I'm in pain" to "pain",
        "I don't understand" to "confused",
        "Can you repeat that?" to "repeat",
        "I want to go home" to "home",
        "Leave me alone please" to "alone",
        "I'm happy" to "happy",
        "I'm tired" to "tired",
        "Can I have more?" to "more",
        "I'm finished" to "finished",
        "I don't like that" to "dislike",
        "I want something else" to "different",
        "mum" to "mother",
        "dad" to "father",
        "grandma" to "grandmother",
        "grandpa" to "grandfather",
        "TV" to "television",
    )

    /**
     * Get the image URI for a word/phrase (synchronous — bundled + local only).
     * Returns a URI string that Coil can load, or null if no symbol found.
     */
    fun getSymbolForWord(word: String): String? {
        // 1. Check bundled mapping
        val mapping = getMapping()
        val filename = mapping[word]
            ?: mapping[word.lowercase()]
            ?: mapping[word.lowercase().trim()]

        if (filename != null) {
            return "file:///android_asset/${Constants.ARASAAC_CORE_DIRECTORY}/$filename"
        }

        // 2. Check sanitized filename in bundled assets
        val sanitized = sanitizeFilename(word)
        val assetPath = "${Constants.ARASAAC_CORE_DIRECTORY}/$sanitized"
        if (assetExists(assetPath)) {
            return "file:///android_asset/$assetPath"
        }

        // 3. Check previously downloaded symbols
        val downloadedFile = File(downloadedDir, sanitized)
        if (downloadedFile.exists() && downloadedFile.length() > 100) {
            return downloadedFile.absolutePath
        }

        return null
    }

    /**
     * Search ARASAAC API and download a symbol for the given word.
     * This is an async/suspend function that requires internet.
     * Returns the local file path if successful, null if not found or offline.
     */
    suspend fun searchAndDownloadSymbol(word: String): String? = withContext(Dispatchers.IO) {
        // First check if we already have it
        val existing = getSymbolForWord(word)
        if (existing != null) return@withContext existing

        try {
            // Determine search term
            val searchTerm = phraseOverrides[word]
                ?: word.lowercase().trim().replace(Regex("[?!.,']"), "")

            Log.d(TAG, "Searching ARASAAC for: '$searchTerm'")

            // Search the API
            val pictogramId = searchArasaac(searchTerm)

            if (pictogramId != null) {
                Log.d(TAG, "Found pictogram ID $pictogramId for '$searchTerm'")

                // Download the image
                val filename = sanitizeFilename(word)
                val success = downloadPictogram(pictogramId, filename)

                if (success) {
                    val filePath = File(downloadedDir, filename).absolutePath
                    Log.d(TAG, "Downloaded symbol to: $filePath")
                    return@withContext filePath
                }
            } else {
                Log.d(TAG, "No ARASAAC result for '$searchTerm'")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to search/download symbol for '$word': ${e.message}")
        }

        null
    }

    /**
     * Resolve a stored image path to a loadable URI.
     */
    fun resolveImagePath(imagePath: String?): String? {
        if (imagePath == null) return null
        if (imagePath.startsWith("file:///")) return imagePath
        if (imagePath.startsWith("/")) return imagePath

        val assetPath = "${Constants.ARASAAC_CORE_DIRECTORY}/$imagePath"
        if (assetExists(assetPath)) {
            return "file:///android_asset/$assetPath"
        }

        val downloadedFile = File(downloadedDir, imagePath)
        if (downloadedFile.exists()) return downloadedFile.absolutePath

        val customFile = File(customDir, imagePath)
        if (customFile.exists()) return customFile.absolutePath

        return null
    }

    fun saveCustomImage(sourceBytes: ByteArray, filename: String): String {
        val file = File(customDir, filename)
        file.writeBytes(sourceBytes)
        return filename
    }

    fun deleteCustomImage(filename: String): Boolean {
        return File(customDir, filename).delete()
    }

    fun listBundledSymbols(): List<String> {
        return try {
            context.assets.list(Constants.ARASAAC_CORE_DIRECTORY)?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun hasBundledSymbols(): Boolean = listBundledSymbols().isNotEmpty()

    fun getMapping(): Map<String, String> {
        if (symbolMapping == null) {
            symbolMapping = loadMapping()
        }
        return symbolMapping ?: emptyMap()
    }

    // ══════════════════════════════════════
    // PRIVATE HELPERS
    // ══════════════════════════════════════

    private fun loadMapping(): Map<String, String> {
        return try {
            val jsonString = context.assets.open("symbols/symbol_mapping.json")
                .bufferedReader()
                .use { it.readText() }

            val jsonElement = Json.parseToJsonElement(jsonString)
            jsonElement.jsonObject.mapValues { it.value.jsonPrimitive.content }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun assetExists(path: String): Boolean {
        return try {
            context.assets.open(path).use { true }
        } catch (e: Exception) {
            false
        }
    }

    private fun sanitizeFilename(word: String): String {
        return word.lowercase()
            .replace(" ", "_")
            .replace("?", "")
            .replace("!", "")
            .replace(".", "")
            .replace("'", "")
            .replace(",", "") + ".png"
    }

    /**
     * Search the ARASAAC API for a word and return the best matching pictogram ID.
     */
    private fun searchArasaac(searchTerm: String): Int? {
        val url = URL("$API_BASE/pictograms/en/search/$searchTerm")
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        try {
            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = Json.parseToJsonElement(responseText).jsonArray
                if (jsonArray.isNotEmpty()) {
                    val firstResult = jsonArray[0].jsonObject
                    return firstResult["_id"]?.jsonPrimitive?.content?.toIntOrNull()
                }
            }
        } finally {
            connection.disconnect()
        }

        return null
    }

    /**
     * Download a pictogram image from ARASAAC's static server.
     * Tries multiple URL formats.
     */
    private fun downloadPictogram(pictogramId: Int, filename: String): Boolean {
        val urls = listOf(
            "$STATIC_BASE/$pictogramId/${pictogramId}_$SYMBOL_SIZE.png",
            "$STATIC_BASE/$pictogramId/${pictogramId}_500.png",
            "$STATIC_BASE/$pictogramId.png",
            "$API_BASE/pictograms/$pictogramId?download=false&resolution=$SYMBOL_SIZE",
        )

        for (downloadUrl in urls) {
            try {
                val connection = URL(downloadUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.instanceFollowRedirects = true

                try {
                    if (connection.responseCode == 200) {
                        val bytes = connection.inputStream.readBytes()
                        if (bytes.size > 100) {
                            val file = File(downloadedDir, filename)
                            file.writeBytes(bytes)
                            return true
                        }
                    }
                } finally {
                    connection.disconnect()
                }
            } catch (e: Exception) {
                continue
            }
        }

        return false
    }
}