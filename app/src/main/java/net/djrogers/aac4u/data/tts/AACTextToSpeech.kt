package net.djrogers.aac4u.data.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Wraps Android's TextToSpeech API with AAC-specific behaviour.
 *
 * Key design decisions:
 * - Queues full phrases, not individual words
 * - Filters to offline-only voices by default (reliability > quality)
 * - Exposes speaking state for UI feedback
 * - Handles initialisation failures gracefully
 */
class AACTextToSpeech(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialised = false

    // Observable speaking state for UI (show "speaking" indicator, highlight words, etc.)
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            isInitialised = status == TextToSpeech.SUCCESS
            _isReady.value = isInitialised

            if (isInitialised) {
                setupUtteranceListener()
            }
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            @Deprecated("Deprecated in API level 21")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
            }
        })
    }

    /**
     * Speak a full phrase. Flushes any currently speaking audio by default.
     */
    fun speakPhrase(phrase: String, flush: Boolean = true) {
        if (!isInitialised || phrase.isBlank()) return

        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val utteranceId = UUID.randomUUID().toString()

        tts?.speak(phrase, queueMode, null, utteranceId)
    }

    /**
     * Speak a sentence built from multiple parts.
     */
    fun speakSentence(parts: List<String>, flush: Boolean = true) {
        val sentence = parts.joinToString(" ").trim()
        speakPhrase(sentence, flush)
    }

    /**
     * Stop any currently speaking audio immediately.
     */
    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    /**
     * Set speech rate. Range: 0.5 (half speed) to 2.0 (double speed). Default: 1.0.
     */
    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
    }

    /**
     * Set pitch. Range: 0.5 (low) to 2.0 (high). Default: 1.0.
     */
    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
    }

    /**
     * Select a specific voice by name. Pass null for system default.
     */
    fun setVoice(voiceName: String?) {
        if (voiceName == null) {
            tts?.voice = tts?.defaultVoice
            return
        }
        val voice = getAvailableOfflineVoices().find { it.name == voiceName }
        if (voice != null) {
            tts?.voice = voice
        }
    }

    /**
     * Get all available voices that work offline.
     * Offline is critical — AAC users depend on this to communicate.
     */
    fun getAvailableOfflineVoices(): List<Voice> {
        return tts?.voices
            ?.filter { !it.isNetworkConnectionRequired }
            ?.sortedBy { it.name }
            ?: emptyList()
    }

    /**
     * Get ALL available voices (including network-required).
     * Show these in settings but warn user about offline availability.
     */
    fun getAllVoices(): List<Voice> {
        return tts?.voices?.sortedBy { it.name } ?: emptyList()
    }

    /**
     * Apply a user profile's TTS settings.
     */
    fun applyProfile(ttsVoiceName: String?, ttsRate: Float, ttsPitch: Float) {
        setVoice(ttsVoiceName)
        setSpeechRate(ttsRate)
        setPitch(ttsPitch)
    }

    /**
     * Clean up TTS resources. Call when the app is being destroyed.
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialised = false
        _isReady.value = false
        _isSpeaking.value = false
    }
}
