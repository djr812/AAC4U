package net.djrogers.aac4u.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val gridColumns: Int = 4,
    val gridRows: Int = 4,
    val buttonPaddingDp: Int = 4,
    val showLabels: Boolean = true,
    val labelPosition: String = "BELOW",
    val inputMethod: String = "TAP",
    val feedbackMode: String = "BOTH",
    val ttsVoiceName: String? = null,
    val ttsRate: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val isActive: Boolean = false,
    val highContrastEnabled: Boolean = false,
    val dwellTimeMs: Long = 1500,
    val scanSpeedMs: Long = 2000
)

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val name: String,
    val iconPath: String? = null,
    val sortOrder: Int = 0,
    val isVisible: Boolean = true,
    val vocabularyType: String = "FRINGE",
    val parentCategoryId: Long? = null
)

@Entity(
    tableName = "buttons",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class ButtonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val label: String,
    val phrase: String,
    val imagePath: String? = null,
    val imageType: String = "BUNDLED",
    val sortOrder: Int = 0,
    val isVisible: Boolean = true,
    val backgroundColor: String? = null,
    val usageCount: Int = 0,
    val lastUsedAt: Long? = null,
    val isQuickPhrase: Boolean = false
)

@Entity(
    tableName = "phrase_history",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class PhraseHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val fullPhrase: String,
    val timestamp: Long = System.currentTimeMillis(),
    val wasEdited: Boolean = false
)

@Entity(
    tableName = "predictions",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("profileId"),
        Index(value = ["profileId", "buttonId", "followingButtonId"], unique = true)
    ]
)
data class PredictionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val buttonId: Long,
    val followingButtonId: Long,
    val frequency: Int = 1
)
