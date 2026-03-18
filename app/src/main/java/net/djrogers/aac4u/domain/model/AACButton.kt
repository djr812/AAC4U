package net.djrogers.aac4u.domain.model

/**
 * Represents a single AAC communication button.
 * This is the core unit of the app — each button speaks a word or phrase.
 */
data class AACButton(
    val id: Long = 0,
    val categoryId: Long,
    val label: String,
    val phrase: String,
    val imagePath: String? = null,
    val imageType: ImageType = ImageType.BUNDLED,
    val sortOrder: Int = 0,
    val isVisible: Boolean = true,
    val backgroundColor: String? = null, // Hex colour e.g. "#FF5722"
    val usageCount: Int = 0,
    val lastUsedAt: Long? = null,
    val isQuickPhrase: Boolean = false
)

enum class ImageType {
    BUNDLED,    // Shipped with APK (ARASAAC core set)
    ARASAAC,    // Downloaded ARASAAC symbol
    CUSTOM      // User-uploaded image
}
