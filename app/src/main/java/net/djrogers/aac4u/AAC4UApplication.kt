package net.djrogers.aac4u

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
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
class AAC4UApplication : Application(), ImageLoaderFactory {

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

    override fun newImageLoader(): ImageLoader = imageLoader

    override fun onCreate() {
        super.onCreate()

        Log.d("AAC4U_DEBUG", "Application onCreate started")

        applicationScope.launch {
            databaseSeeder.seedIfEmpty()
            Log.d("AAC4U_DEBUG", "Database seeding complete")
        }

        applicationScope.launch {
            refreshMissingSymbols()
            logImageDiagnostics()
        }

        applicationScope.launch {
            try {
                tts.isReady.first { it }
                val profile = profileRepository.getActiveProfile().first()
                if (profile != null) {
                    tts.applyProfile(profile.ttsVoiceName, profile.ttsRate, profile.ttsPitch)
                }
            } catch (_: Exception) {}
        }

        registerComponentCallbacks(memoryCallbacks)
    }

    private suspend fun logImageDiagnostics() {
        try {
            val buttonDao = database.buttonDao()
            val categoryDao = database.categoryDao()
            val profileDao = database.profileDao()

            val profile = profileDao.getActiveProfile().first() ?: run {
                Log.w("AAC4U_DEBUG", "No active profile found!")
                return
            }

            Log.d("AAC4U_DEBUG", "Active profile: ${profile.name} (id=${profile.id})")

            val categories = categoryDao.getAllCategoriesByProfile(profile.id).first()
            Log.d("AAC4U_DEBUG", "Categories: ${categories.size}")

            var totalButtons = 0
            var withImage = 0
            var withoutImage = 0
            val samplePaths = mutableListOf<String>()

            for (category in categories) {
                val buttons = buttonDao.getAllButtonsByCategory(category.id).first()
                totalButtons += buttons.size

                for (button in buttons) {
                    if (button.imagePath != null) {
                        withImage++
                        if (samplePaths.size < 5) {
                            samplePaths.add("'${button.label}' -> '${button.imagePath}'")
                        }
                    } else {
                        withoutImage++
                    }
                }
            }

            Log.d("AAC4U_DEBUG", "Total buttons: $totalButtons, with image: $withImage, without image: $withoutImage")

            for (sample in samplePaths) {
                Log.d("AAC4U_DEBUG", "Sample path: $sample")
            }

            // Test asset access
            try {
                val testAssets = assets.list("symbols/arasaac_core")
                Log.d("AAC4U_DEBUG", "Asset folder has ${testAssets?.size ?: 0} files")
                if (testAssets != null && testAssets.isNotEmpty()) {
                    Log.d("AAC4U_DEBUG", "First asset: ${testAssets[0]}")
                    val stream = assets.open("symbols/arasaac_core/${testAssets[0]}")
                    val bytes = stream.available()
                    stream.close()
                    Log.d("AAC4U_DEBUG", "Successfully opened asset, size: $bytes bytes")
                }
            } catch (e: Exception) {
                Log.e("AAC4U_DEBUG", "Failed to access assets: ${e.message}")
            }

            Log.d("AAC4U_DEBUG", "ImageLoader cache size: ${imageLoader.memoryCache?.size ?: "null"}")
            Log.d("AAC4U_DEBUG", "ImageLoader cache max: ${imageLoader.memoryCache?.maxSize ?: "null"}")

        } catch (e: Exception) {
            Log.e("AAC4U_DEBUG", "Diagnostics failed: ${e.message}", e)
        }
    }

    private val memoryCallbacks = object : ComponentCallbacks2 {
        override fun onTrimMemory(level: Int) {
            when {
                level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> imageLoader.memoryCache?.clear()
                level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> imageLoader.memoryCache?.trimMemory(level)
                level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> imageLoader.memoryCache?.clear()
            }
        }
        override fun onConfigurationChanged(newConfig: Configuration) {}
        override fun onLowMemory() { imageLoader.memoryCache?.clear() }
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
        } catch (_: Exception) {}
    }
}
