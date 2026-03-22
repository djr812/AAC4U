package net.djrogers.aac4u

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.djrogers.aac4u.data.local.database.AAC4UDatabase
import net.djrogers.aac4u.data.local.database.DatabaseSeeder
import net.djrogers.aac4u.data.symbol.SymbolManager
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

    @Inject
    lateinit var database: AAC4UDatabase

    @Inject
    lateinit var symbolManager: SymbolManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Seed database on first launch
        applicationScope.launch {
            databaseSeeder.seedIfEmpty()
        }

        // Refresh symbol paths for any buttons missing images
        applicationScope.launch {
            refreshMissingSymbols()
        }

        // Apply TTS profile in background
        applicationScope.launch {
            try {
                tts.isReady.first { it }
                val profile = profileRepository.getActiveProfile().first()
                if (profile != null) {
                    tts.applyProfile(profile.ttsVoiceName, profile.ttsRate, profile.ttsPitch)
                }
            } catch (_: Exception) {
                // TTS not available
            }
        }
    }

    /**
     * Scans all buttons in the database. For any button that has no imagePath,
     * checks if a symbol is now available (either bundled or in the mapping)
     * and updates the button if found. This handles the case where new symbols
     * are added to the assets after the database was already seeded.
     */
    private suspend fun refreshMissingSymbols() {
        try {
            val buttonDao = database.buttonDao()
            val categoryDao = database.categoryDao()
            val profileDao = database.profileDao()

            val allProfiles = profileDao.getAllProfiles().first()

            for (profile in allProfiles) {
                val categories = categoryDao.getAllCategoriesByProfile(profile.id).first()

                for (category in categories) {
                    val buttons = buttonDao.getAllButtonsByCategory(category.id).first()

                    for (button in buttons) {
                        if (button.imagePath == null) {
                            val symbolPath = symbolManager.getSymbolForWord(button.label)
                            if (symbolPath != null) {
                                buttonDao.updateButton(button.copy(imagePath = symbolPath))
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Non-critical — symbols will just stay text-only
        }
    }
}