package net.djrogers.aac4u.data.local.database.mapper

import net.djrogers.aac4u.data.local.database.entity.*
import net.djrogers.aac4u.domain.model.*

// ── Button mappers ──

fun ButtonEntity.toDomain(): AACButton = AACButton(
    id = id,
    categoryId = categoryId,
    label = label,
    phrase = phrase,
    imagePath = imagePath,
    imageType = ImageType.valueOf(imageType),
    sortOrder = sortOrder,
    isVisible = isVisible,
    backgroundColor = backgroundColor,
    usageCount = usageCount,
    lastUsedAt = lastUsedAt,
    isQuickPhrase = isQuickPhrase
)

fun AACButton.toEntity(): ButtonEntity = ButtonEntity(
    id = id,
    categoryId = categoryId,
    label = label,
    phrase = phrase,
    imagePath = imagePath,
    imageType = imageType.name,
    sortOrder = sortOrder,
    isVisible = isVisible,
    backgroundColor = backgroundColor,
    usageCount = usageCount,
    lastUsedAt = lastUsedAt,
    isQuickPhrase = isQuickPhrase
)

// ── Category mappers ──

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    profileId = profileId,
    name = name,
    iconPath = iconPath,
    sortOrder = sortOrder,
    isVisible = isVisible,
    vocabularyType = VocabularyType.valueOf(vocabularyType),
    parentCategoryId = parentCategoryId
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    profileId = profileId,
    name = name,
    iconPath = iconPath,
    sortOrder = sortOrder,
    isVisible = isVisible,
    vocabularyType = vocabularyType.name,
    parentCategoryId = parentCategoryId
)

// ── Profile mappers ──

fun ProfileEntity.toDomain(): UserProfile = UserProfile(
    id = id,
    name = name,
    gridConfig = GridConfig(
        columns = gridColumns,
        rows = gridRows,
        buttonPaddingDp = buttonPaddingDp,
        showLabels = showLabels,
        labelPosition = LabelPosition.valueOf(labelPosition)
    ),
    inputMethod = InputMethod.valueOf(inputMethod),
    feedbackMode = FeedbackMode.valueOf(feedbackMode),
    ttsVoiceName = ttsVoiceName,
    ttsRate = ttsRate,
    ttsPitch = ttsPitch,
    isActive = isActive,
    highContrastEnabled = highContrastEnabled,
    dwellTimeMs = dwellTimeMs,
    scanSpeedMs = scanSpeedMs
)

fun UserProfile.toEntity(): ProfileEntity = ProfileEntity(
    id = id,
    name = name,
    gridColumns = gridConfig.columns,
    gridRows = gridConfig.rows,
    buttonPaddingDp = gridConfig.buttonPaddingDp,
    showLabels = gridConfig.showLabels,
    labelPosition = gridConfig.labelPosition.name,
    inputMethod = inputMethod.name,
    feedbackMode = feedbackMode.name,
    ttsVoiceName = ttsVoiceName,
    ttsRate = ttsRate,
    ttsPitch = ttsPitch,
    isActive = isActive,
    highContrastEnabled = highContrastEnabled,
    dwellTimeMs = dwellTimeMs,
    scanSpeedMs = scanSpeedMs
)

// ── PhraseHistory mappers ──

fun PhraseHistoryEntity.toDomain(): PhraseHistoryEntry = PhraseHistoryEntry(
    id = id,
    profileId = profileId,
    fullPhrase = fullPhrase,
    timestamp = timestamp,
    wasEdited = wasEdited
)

fun PhraseHistoryEntry.toEntity(): PhraseHistoryEntity = PhraseHistoryEntity(
    id = id,
    profileId = profileId,
    fullPhrase = fullPhrase,
    timestamp = timestamp,
    wasEdited = wasEdited
)
