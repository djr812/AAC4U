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

        // Seed database — this must complete before UI loads
        applicationScope.launch {
            databaseSeeder.seedIfEmpty()
        }

        // Apply TTS profile in background — don't block startup
        applicationScope.launch {
            try {
                tts.isReady.first { it }
                val profile = profileRepository.getActiveProfile().first()
                if (profile != null) {
                    tts.applyProfile(profile.ttsVoiceName, profile.ttsRate, profile.ttsPitch)
                }
            } catch (_: Exception) {
                // TTS not available — continue without it
            }
        }
    }
}
