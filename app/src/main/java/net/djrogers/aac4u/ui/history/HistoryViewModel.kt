package net.djrogers.aac4u.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.djrogers.aac4u.data.tts.AACTextToSpeech
import net.djrogers.aac4u.domain.model.PhraseHistoryEntry
import net.djrogers.aac4u.domain.repository.PhraseHistoryRepository
import net.djrogers.aac4u.domain.repository.ProfileRepository
import javax.inject.Inject

data class HistoryUiState(
    val entries: List<PhraseHistoryEntry> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val showClearConfirmation: Boolean = false,
    val speakingEntryId: Long? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val phraseHistoryRepository: PhraseHistoryRepository,
    private val profileRepository: ProfileRepository,
    private val tts: AACTextToSpeech
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    private var historyJob: Job? = null
    private var activeProfileId: Long? = null

    init {
        loadHistory()
        observeTts()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val profile = profileRepository.getActiveProfile().first() ?: return@launch
            activeProfileId = profile.id
            collectHistory(profile.id, "")
        }
    }

    private fun collectHistory(profileId: Long, query: String) {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            val flow = if (query.isBlank()) {
                phraseHistoryRepository.getHistory(profileId, 50)
            } else {
                phraseHistoryRepository.searchHistory(profileId, query, 50)
            }

            flow.collect { entries ->
                _state.update { it.copy(entries = entries, isLoading = false) }
            }
        }
    }

    private fun observeTts() {
        viewModelScope.launch {
            tts.isSpeaking.collect { speaking ->
                if (!speaking) {
                    _state.update { it.copy(speakingEntryId = null) }
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        val profileId = activeProfileId ?: return
        collectHistory(profileId, query)
    }

    fun speakEntry(entry: PhraseHistoryEntry) {
        _state.update { it.copy(speakingEntryId = entry.id) }
        tts.speakPhrase(entry.fullPhrase)
    }

    fun stopSpeaking() {
        tts.stop()
        _state.update { it.copy(speakingEntryId = null) }
    }

    fun deleteEntry(entry: PhraseHistoryEntry) {
        viewModelScope.launch {
            phraseHistoryRepository.deleteEntry(entry.id)
        }
    }

    fun showClearConfirmation() {
        _state.update { it.copy(showClearConfirmation = true) }
    }

    fun hideClearConfirmation() {
        _state.update { it.copy(showClearConfirmation = false) }
    }

    fun clearAllHistory() {
        val profileId = activeProfileId ?: return
        viewModelScope.launch {
            phraseHistoryRepository.clearHistory(profileId)
            _state.update { it.copy(showClearConfirmation = false) }
        }
    }
}
