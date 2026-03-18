package net.djrogers.aac4u.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import net.djrogers.aac4u.data.local.database.dao.*
import net.djrogers.aac4u.data.local.database.entity.*

@Database(
    entities = [
        ProfileEntity::class,
        CategoryEntity::class,
        ButtonEntity::class,
        PhraseHistoryEntity::class,
        PredictionEntity::class
    ],
    version = 1,
    exportSchema = true // Always export — needed for migration testing
)
abstract class AAC4UDatabase : RoomDatabase() {
    abstract fun buttonDao(): ButtonDao
    abstract fun categoryDao(): CategoryDao
    abstract fun profileDao(): ProfileDao
    abstract fun phraseHistoryDao(): PhraseHistoryDao
    abstract fun predictionDao(): PredictionDao

    companion object {
        const val DATABASE_NAME = "aac4u.db"
    }
}
