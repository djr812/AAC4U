package net.djrogers.aac4u.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.djrogers.aac4u.data.local.database.AAC4UDatabase
import net.djrogers.aac4u.data.local.database.DatabaseSeeder
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabaseSeeder(database: AAC4UDatabase): DatabaseSeeder {
        return DatabaseSeeder(database)
    }
}
