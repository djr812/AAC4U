package net.djrogers.aac4u.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.djrogers.aac4u.data.tts.AACTextToSpeech
import net.djrogers.aac4u.domain.model.UserProfile
import net.djrogers.aac4u.domain.repository.ProfileRepository
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val tts: AACTextToSpeech
) : ViewModel() {

    private val _activeProfile = MutableStateFlow<UserProfile?>(null)
    val activeProfile: StateFlow<UserProfile?> = _activeProfile.asStateFlow()

    // Track which profile ID we last welcomed, to avoid repeating
    private var lastWelcomedProfileId: Long? = null
    private var hasSpokenStartup = false

    init {
        observeActiveProfile()
    }

    private fun observeActiveProfile() {
        viewModelScope.launch {
            profileRepository.getActiveProfile().collect { profile ->
                _activeProfile.value = profile

                if (profile != null && profile.name != "Default") {
                    // Wait for TTS to be ready before speaking
                    tts.isReady.first { it }

                    if (!hasSpokenStartup) {
                        // First time — startup welcome
                        hasSpokenStartup = true
                        lastWelcomedProfileId = profile.id
                        speakWelcome(profile.name)
                    } else if (profile.id != lastWelcomedProfileId) {
                        // Profile switched
                        lastWelcomedProfileId = profile.id
                        speakWelcome(profile.name)
                    }
                }
            }
        }
    }

    private fun speakWelcome(profileName: String) {
        tts.speakPhrase("Welcome, $profileName")
    }
}
