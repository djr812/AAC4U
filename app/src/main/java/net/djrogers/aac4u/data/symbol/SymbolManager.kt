package net.djrogers.aac4u.data.symbol

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import net.djrogers.aac4u.domain.model.ImageType
import net.djrogers.aac4u.util.Constants
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages loading symbol images from different sources:
 * - Bundled ARASAAC symbols (shipped in assets/)
 * - Custom user-uploaded images (stored in internal files/)
 *
 * Returns file paths that Coil can load directly.
 */
@Singleton
class SymbolManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val customDir: File by lazy {
        File(context.filesDir, Constants.CUSTOM_IMAGES_DIRECTORY).also { it.mkdirs() }
    }

    /**
     * Resolve a symbol's display path based on its type and stored path.
     */
    fun resolveImagePath(imagePath: String?, imageType: ImageType): String? {
        if (imagePath == null) return null

        return when (imageType) {
            ImageType.BUNDLED -> "file:///android_asset/${Constants.ARASAAC_CORE_DIRECTORY}/$imagePath"
            ImageType.ARASAAC -> "file:///android_asset/${Constants.SYMBOL_DIRECTORY}/$imagePath"
            ImageType.CUSTOM -> File(customDir, imagePath).absolutePath
        }
    }

    /**
     * Save a custom image and return its filename for storage in the database.
     */
    fun saveCustomImage(sourceBytes: ByteArray, filename: String): String {
        val file = File(customDir, filename)
        file.writeBytes(sourceBytes)
        return filename
    }

    /**
     * Delete a custom image.
     */
    fun deleteCustomImage(filename: String): Boolean {
        val file = File(customDir, filename)
        return file.delete()
    }

    /**
     * List all bundled ARASAAC symbol filenames in the core set.
     */
    fun listBundledSymbols(): List<String> {
        return try {
            context.assets.list(Constants.ARASAAC_CORE_DIRECTORY)?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Check if the bundled symbol set is available.
     */
    fun hasBundledSymbols(): Boolean {
        return listBundledSymbols().isNotEmpty()
    }
}
