package net.djrogers.aac4u.ui.settings

import android.speech.tts.Voice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.djrogers.aac4u.data.tts.AACTextToSpeech
import net.djrogers.aac4u.domain.model.UserProfile
import net.djrogers.aac4u.domain.repository.ProfileRepository
import java.util.Locale
import javax.inject.Inject

data class TtsSettingsState(
    val savedVoiceName: String? = null,
    val savedRate: Float = 1.0f,
    val savedPitch: Float = 1.0f,

    val previewVoiceName: String? = null,
    val previewRate: Float = 1.0f,
    val previewPitch: Float = 1.0f,

    val offlineVoices: List<VoiceInfo> = emptyList(),
    val onlineOnlyVoices: List<VoiceInfo> = emptyList(),

    val isTtsReady: Boolean = false,
    val isSpeaking: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val showSavedToast: Boolean = false,
    val isLoading: Boolean = true
)

data class VoiceInfo(
    val name: String,
    val friendlyName: String,
    val isOffline: Boolean
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val tts: AACTextToSpeech,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TtsSettingsState())
    val state: StateFlow<TtsSettingsState> = _state.asStateFlow()

    private var activeProfile: UserProfile? = null

    init {
        loadSettings()
        observeTtsState()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            profileRepository.getActiveProfile().collect { profile ->
                if (profile != null) {
                    activeProfile = profile
                    _state.update { state ->
                        state.copy(
                            savedVoiceName = profile.ttsVoiceName,
                            savedRate = profile.ttsRate,
                            savedPitch = profile.ttsPitch,
                            previewVoiceName = profile.ttsVoiceName,
                            previewRate = profile.ttsRate,
                            previewPitch = profile.ttsPitch,
                            isLoading = false
                        )
                    }
                    loadVoices()
                }
            }
        }
    }

    private fun loadVoices() {
        val allVoices = tts.getAllVoices()

        val englishVoices = allVoices.filter { voice ->
            voice.locale.language == Locale.ENGLISH.language
        }

        val offlineSorted = englishVoices
            .filter { !it.isNetworkConnectionRequired }
            .sortedWith(compareBy({ it.locale.displayCountry }, { it.name }))

        val onlineSorted = englishVoices
            .filter { it.isNetworkConnectionRequired }
            .sortedWith(compareBy({ it.locale.displayCountry }, { it.name }))

        _state.update { state ->
            state.copy(
                offlineVoices = buildNumberedVoices(offlineSorted, isOffline = true),
                onlineOnlyVoices = buildNumberedVoices(onlineSorted, isOffline = false)
            )
        }
    }

    /**
     * Number voices per country: Australia 1, Australia 2, US 1, US 2, etc.
     * If a country only has one voice, skip the number.
     */
    private fun buildNumberedVoices(voices: List<Voice>, isOffline: Boolean): List<VoiceInfo> {
        // First pass: count voices per country
        val countsByCountry = voices.groupBy { it.locale.displayCountry.ifEmpty { "English" } }

        // Second pass: assign numbers
        val counters = mutableMapOf<String, Int>()
        return voices.map { voice ->
            val country = voice.locale.displayCountry.ifEmpty { "English" }
            val count = counters.getOrDefault(country, 0) + 1
            counters[country] = count

            val totalForCountry = countsByCountry[country]?.size ?: 1
            val friendlyName = if (totalForCountry > 1) {
                "$country $count"
            } else {
                country
            }

            VoiceInfo(
                name = voice.name,
                friendlyName = friendlyName,
                isOffline = isOffline
            )
        }
    }

    private fun observeTtsState() {
        viewModelScope.launch {
            tts.isReady.collect { ready ->
                _state.update { it.copy(isTtsReady = ready) }
                if (ready) loadVoices()
            }
        }
        viewModelScope.launch {
            tts.isSpeaking.collect { speaking ->
                _state.update { it.copy(isSpeaking = speaking) }
            }
        }
    }

    fun setPreviewVoice(voiceName: String?) {
        _state.update { state ->
            state.copy(
                previewVoiceName = voiceName,
                hasUnsavedChanges = hasChanges(voiceName, state.previewRate, state.previewPitch),
                showSavedToast = false
            )
        }
    }

    fun setPreviewRate(rate: Float) {
        val rounded = (rate * 10).toInt() / 10f
        _state.update { state ->
            state.copy(
                previewRate = rounded,
                hasUnsavedChanges = hasChanges(state.previewVoiceName, rounded, state.previewPitch),
                showSavedToast = false
            )
        }
    }

    fun setPreviewPitch(pitch: Float) {
        val rounded = (pitch * 10).toInt() / 10f
        _state.update { state ->
            state.copy(
                previewPitch = rounded,
                hasUnsavedChanges = hasChanges(state.previewVoiceName, state.previewRate, rounded),
                showSavedToast = false
            )
        }
    }

    fun testVoice() {
        val currentState = _state.value
        tts.applyProfile(
            currentState.previewVoiceName,
            currentState.previewRate,
            currentState.previewPitch
        )
        tts.speakPhrase("Hello, this is how I will sound. I can help you communicate.")
    }

    fun stopTest() {
        tts.stop()
    }

    fun saveSettings() {
        val profile = activeProfile ?: return
        val currentState = _state.value

        viewModelScope.launch {
            val updatedProfile = profile.copy(
                ttsVoiceName = currentState.previewVoiceName,
                ttsRate = currentState.previewRate,
                ttsPitch = currentState.previewPitch
            )
            profileRepository.updateProfile(updatedProfile)
            activeProfile = updatedProfile

            tts.applyProfile(
                updatedProfile.ttsVoiceName,
                updatedProfile.ttsRate,
                updatedProfile.ttsPitch
            )

            _state.update { state ->
                state.copy(
                    savedVoiceName = currentState.previewVoiceName,
                    savedRate = currentState.previewRate,
                    savedPitch = currentState.previewPitch,
                    hasUnsavedChanges = false,
                    showSavedToast = true
                )
            }

            delay(2000)
            _state.update { it.copy(showSavedToast = false) }
        }
    }

    fun discardChanges() {
        val currentState = _state.value
        tts.applyProfile(
            currentState.savedVoiceName,
            currentState.savedRate,
            currentState.savedPitch
        )
        _state.update { state ->
            state.copy(
                previewVoiceName = state.savedVoiceName,
                previewRate = state.savedRate,
                previewPitch = state.savedPitch,
                hasUnsavedChanges = false,
                showSavedToast = false
            )
        }
    }

    private fun hasChanges(voiceName: String?, rate: Float, pitch: Float): Boolean {
        val currentState = _state.value
        return voiceName != currentState.savedVoiceName ||
                rate != currentState.savedRate ||
                pitch != currentState.savedPitch
    }
}
