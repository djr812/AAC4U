package net.djrogers.aac4u

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.djrogers.aac4u.data.local.database.DatabaseSeeder
import net.djrogers.aac4u.data.tts.AACTextToSpeech
import net.djrogers.aac4u.domain.repository.ProfileRepository
import javax.inject.Inject

@HiltAndroidApp
class AAC4UApplication : Application() {

    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    @Inject
    lateinit var profileRepository: ProfileRepository

    @Inject
    lateinit var tts: AACTextToSpeech

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            // Seed database with default data on first launch
            databaseSeeder.seedIfEmpty()

            // Wait for TTS to be ready, then apply the saved profile voice settings
            tts.isReady.first { it }

            val profile = profileRepository.getActiveProfile().first()
            if (profile != null) {
                tts.applyProfile(
                    profile.ttsVoiceName,
                    profile.ttsRate,
                    profile.ttsPitch
                )
            }
        }
    }
}