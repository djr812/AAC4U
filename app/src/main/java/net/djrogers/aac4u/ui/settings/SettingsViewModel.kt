package net.djrogers.aac4u.ui.settings

import android.speech.tts.Voice
import android.util.Log
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
    val displayName: String,
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
        Log.d("AAC4U_VOICE", "SettingsViewModel init called")
        loadSettings()
        observeTtsState()
    }

    private fun loadSettings() {
        Log.d("AAC4U_VOICE", "loadSettings called")
        viewModelScope.launch {
            profileRepository.getActiveProfile().collect { profile ->
                Log.d("AAC4U_VOICE", "Active profile received: ${profile?.name}")
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
        Log.d("AAC4U_VOICE", "loadVoices called")

        val allVoices = tts.getAllVoices()
        Log.d("AAC4U_VOICE", "Total voices from TTS engine: ${allVoices.size}")

        val englishVoices = allVoices.filter { voice ->
            voice.locale.language == Locale.ENGLISH.language
        }
        Log.d("AAC4U_VOICE", "English voices: ${englishVoices.size}")

        val offlineSorted = englishVoices
            .filter { !it.isNetworkConnectionRequired }
            .sortedWith(compareBy({ it.locale.displayCountry }, { it.name }))

        val onlineSorted = englishVoices
            .filter { it.isNetworkConnectionRequired }
            .sortedWith(compareBy({ it.locale.displayCountry }, { it.name }))

        val offlineVoices = buildDisplayNames(offlineSorted, isOffline = true)
        val onlineOnlyVoices = buildDisplayNames(onlineSorted, isOffline = false)

        Log.d("AAC4U_VOICE", "Offline: ${offlineVoices.size}, Online: ${onlineOnlyVoices.size}")
        offlineVoices.forEach { Log.d("AAC4U_VOICE", "Offline: '${it.displayName}' (${it.name})") }

        _state.update { state ->
            state.copy(
                offlineVoices = offlineVoices,
                onlineOnlyVoices = onlineOnlyVoices
            )
        }
    }

    /**
     * Build display names with per-country numbering.
     * If a country has only one voice, just show "English (Australia)".
     * If multiple, show "English (Australia) 1", "English (Australia) 2", etc.
     */
    private fun buildDisplayNames(voices: List<Voice>, isOffline: Boolean): List<VoiceInfo> {
        // First pass: count how many voices per displayName
        val counts = voices.groupBy { it.locale.displayName }.mapValues { it.value.size }

        // Second pass: assign numbers where needed
        val counters = mutableMapOf<String, Int>()
        return voices.map { voice ->
            val baseName = voice.locale.displayName
            val total = counts[baseName] ?: 1
            val count = (counters[baseName] ?: 0) + 1
            counters[baseName] = count

            val displayName = if (total > 1) {
                "$baseName $count"
            } else {
                baseName
            }

            VoiceInfo(
                name = voice.name,
                displayName = displayName,
                isOffline = isOffline
            )
        }
    }

    private fun observeTtsState() {
        viewModelScope.launch {
            tts.isReady.collect { ready ->
                Log.d("AAC4U_VOICE", "TTS isReady changed to: $ready")
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
