package net.djrogers.aac4u.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.djrogers.aac4u.data.local.database.entity.*

@Dao
interface ButtonDao {
    @Query("SELECT * FROM buttons WHERE categoryId = :categoryId AND isVisible = 1 ORDER BY sortOrder ASC")
    fun getButtonsByCategory(categoryId: Long): Flow<List<ButtonEntity>>

    @Query("""
        SELECT b.* FROM buttons b 
        INNER JOIN categories c ON b.categoryId = c.id 
        WHERE c.profileId = :profileId AND b.isQuickPhrase = 1 AND b.isVisible = 1 
        ORDER BY b.usageCount DESC
    """)
    fun getQuickPhraseButtons(profileId: Long): Flow<List<ButtonEntity>>

    @Query("SELECT * FROM buttons WHERE id = :id")
    suspend fun getButtonById(id: Long): ButtonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertButton(button: ButtonEntity): Long

    @Update
    suspend fun updateButton(button: ButtonEntity)

    @Query("DELETE FROM buttons WHERE id = :id")
    suspend fun deleteButton(id: Long)

    @Update
    suspend fun updateButtons(buttons: List<ButtonEntity>)

    @Query("UPDATE buttons SET usageCount = usageCount + 1, lastUsedAt = :timestamp WHERE id = :buttonId")
    suspend fun incrementUsageCount(buttonId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("""
        SELECT b.* FROM buttons b 
        INNER JOIN categories c ON b.categoryId = c.id 
        WHERE c.profileId = :profileId AND (b.label LIKE '%' || :query || '%' OR b.phrase LIKE '%' || :query || '%')
        ORDER BY b.usageCount DESC
    """)
    fun searchButtons(profileId: Long, query: String): Flow<List<ButtonEntity>>
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE profileId = :profileId AND isVisible = 1 ORDER BY sortOrder ASC")
    fun getCategoriesByProfile(profileId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE profileId = :profileId AND vocabularyType = :type AND isVisible = 1 ORDER BY sortOrder ASC")
    fun getCategoriesByType(profileId: Long, type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: Long)

    @Update
    suspend fun updateCategories(categories: List<CategoryEntity>)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY name ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteProfile(id: Long)

    @Query("UPDATE profiles SET isActive = 0")
    suspend fun deactivateAllProfiles()

    @Query("UPDATE profiles SET isActive = 1 WHERE id = :profileId")
    suspend fun activateProfile(profileId: Long)

    @Transaction
    suspend fun setActiveProfile(profileId: Long) {
        deactivateAllProfiles()
        activateProfile(profileId)
    }
}

@Dao
interface PhraseHistoryDao {
    @Query("SELECT * FROM phrase_history WHERE profileId = :profileId ORDER BY timestamp DESC LIMIT :limit")
    fun getHistory(profileId: Long, limit: Int): Flow<List<PhraseHistoryEntity>>

    @Insert
    suspend fun insertEntry(entry: PhraseHistoryEntity)

    @Query("DELETE FROM phrase_history WHERE profileId = :profileId")
    suspend fun clearHistory(profileId: Long)

    @Query("SELECT * FROM phrase_history WHERE profileId = :profileId ORDER BY timestamp DESC")
    suspend fun exportHistory(profileId: Long): List<PhraseHistoryEntity>
}

@Dao
interface PredictionDao {
    @Query("""
        UPDATE predictions SET frequency = frequency + 1 
        WHERE profileId = :profileId AND buttonId = :buttonAId AND followingButtonId = :buttonBId
    """)
    suspend fun incrementFrequency(profileId: Long, buttonAId: Long, buttonBId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPrediction(prediction: PredictionEntity)

    @Transaction
    suspend fun recordSequence(profileId: Long, buttonAId: Long, buttonBId: Long) {
        val updated = incrementFrequency(profileId, buttonAId, buttonBId)
        if (updated == 0) {
            insertPrediction(
                PredictionEntity(
                    profileId = profileId,
                    buttonId = buttonAId,
                    followingButtonId = buttonBId,
                    frequency = 1
                )
            )
        }
    }

    @Query("""
        SELECT b.* FROM predictions p 
        INNER JOIN buttons b ON p.followingButtonId = b.id 
        WHERE p.profileId = :profileId AND p.buttonId = :lastButtonId AND b.isVisible = 1
        ORDER BY p.frequency DESC 
        LIMIT :limit
    """)
    fun getPredictions(profileId: Long, lastButtonId: Long, limit: Int): Flow<List<ButtonEntity>>

    @Query("""
        SELECT * FROM buttons b 
        INNER JOIN categories c ON b.categoryId = c.id 
        WHERE c.profileId = :profileId AND b.isVisible = 1 
        ORDER BY b.usageCount DESC 
        LIMIT :limit
    """)
    fun getMostUsedButtons(profileId: Long, limit: Int): Flow<List<ButtonEntity>>
}
