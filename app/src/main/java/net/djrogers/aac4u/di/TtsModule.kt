package net.djrogers.aac4u.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.djrogers.aac4u.data.tts.AACTextToSpeech
import net.djrogers.aac4u.data.tts.TtsManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TtsModule {

    @Provides
    @Singleton
    fun provideAACTextToSpeech(ttsManager: TtsManager): AACTextToSpeech {
        return ttsManager.tts
    }
}
