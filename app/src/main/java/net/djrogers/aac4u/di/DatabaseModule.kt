package net.djrogers.aac4u.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.djrogers.aac4u.data.local.database.AAC4UDatabase
import net.djrogers.aac4u.data.local.database.dao.*
import net.djrogers.aac4u.data.local.database.migration.Migrations
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AAC4UDatabase {
        return Room.databaseBuilder(
            context,
            AAC4UDatabase::class.java,
            AAC4UDatabase.DATABASE_NAME
        )
            .addMigrations(*Migrations.ALL)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideButtonDao(database: AAC4UDatabase): ButtonDao = database.buttonDao()

    @Provides
    fun provideCategoryDao(database: AAC4UDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideProfileDao(database: AAC4UDatabase): ProfileDao = database.profileDao()

    @Provides
    fun providePhraseHistoryDao(database: AAC4UDatabase): PhraseHistoryDao = database.phraseHistoryDao()

    @Provides
    fun providePredictionDao(database: AAC4UDatabase): PredictionDao = database.predictionDao()
}
