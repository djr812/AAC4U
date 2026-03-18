package net.djrogers.aac4u.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-level preferences stored via DataStore.
 * For simple key-value settings that don't belong in Room.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aac4u_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val LAST_ACTIVE_PROFILE_ID = longPreferencesKey("last_active_profile_id")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val SYMBOLS_DOWNLOADED = booleanPreferencesKey("symbols_downloaded")
    }

    val lastActiveProfileId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[LAST_ACTIVE_PROFILE_ID]
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED] ?: false
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[FIRST_LAUNCH] ?: true
    }

    suspend fun setLastActiveProfileId(id: Long) {
        context.dataStore.edit { prefs ->
            prefs[LAST_ACTIVE_PROFILE_ID] = id
        }
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { prefs ->
            prefs[FIRST_LAUNCH] = false
        }
    }

    suspend fun setSymbolsDownloaded() {
        context.dataStore.edit { prefs ->
            prefs[SYMBOLS_DOWNLOADED] = true
        }
    }
}
