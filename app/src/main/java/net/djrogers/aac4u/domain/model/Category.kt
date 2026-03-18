package net.djrogers.aac4u.domain.model

/**
 * A category groups related AAC buttons together.
 * Categories are either CORE (always visible) or FRINGE (context-specific).
 */
data class Category(
    val id: Long = 0,
    val profileId: Long,
    val name: String,
    val iconPath: String? = null,
    val sortOrder: Int = 0,
    val isVisible: Boolean = true,
    val vocabularyType: VocabularyType = VocabularyType.FRINGE,
    val parentCategoryId: Long? = null // For sub-categories (keep depth to 1 level max)
)
