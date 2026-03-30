package net.djrogers.aac4u

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import coil.ImageLoader
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

    @Inject
    lateinit var imageLoader: ImageLoader

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

        // Register memory pressure listener
        registerComponentCallbacks(memoryCallbacks)
    }

    /**
     * Respond to system memory pressure by trimming image caches.
     * Critical for 3GB devices where memory is tight.
     */
    private val memoryCallbacks = object : ComponentCallbacks2 {
        override fun onTrimMemory(level: Int) {
            when {
                level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                    // System is critically low — clear all caches
                    imageLoader.memoryCache?.clear()
                }
                level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                    // System is low — trim to half
                    imageLoader.memoryCache?.trimMemory(level)
                }
                level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                    // App went to background — trim aggressively
                    imageLoader.memoryCache?.clear()
                }
            }
        }

        override fun onConfigurationChanged(newConfig: Configuration) {}
        override fun onLowMemory() {
            imageLoader.memoryCache?.clear()
        }
    }

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
