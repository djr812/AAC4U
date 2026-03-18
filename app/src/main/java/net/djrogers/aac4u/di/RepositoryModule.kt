package net.djrogers.aac4u.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.djrogers.aac4u.data.repository.*
import net.djrogers.aac4u.domain.repository.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindButtonRepository(impl: ButtonRepositoryImpl): ButtonRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindPhraseHistoryRepository(impl: PhraseHistoryRepositoryImpl): PhraseHistoryRepository

    @Binds
    @Singleton
    abstract fun bindPredictionRepository(impl: PredictionRepositoryImpl): PredictionRepository
}
