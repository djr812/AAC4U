package net.djrogers.aac4u.domain.usecase.grid

import javax.inject.Inject

/**
 * Manages building a sentence from multiple button taps.
 * The sentence bar shows accumulated words before the user hits "speak".
 *
 * This is a pure domain class with no Android dependencies —
 * ideal for sharing via KMP when porting to iOS.
 */
class BuildSentenceUseCase @Inject constructor() {

    private val parts = mutableListOf<String>()

    /**
     * Add a word/phrase to the sentence being built.
     */
    fun addPart(phrase: String): List<String> {
        parts.add(phrase)
        return parts.toList()
    }

    /**
     * Remove the last added part (backspace equivalent).
     */
    fun removeLastPart(): List<String> {
        if (parts.isNotEmpty()) {
            parts.removeAt(parts.lastIndex)
        }
        return parts.toList()
    }

    fun replaceLastPart(newPart: String): List<String> {
        if (parts.isNotEmpty()) {
            parts[parts.size - 1] = newPart
        }
        return parts.toList()
    }

    /**
     * Get the full sentence as a single string for TTS.
     */
    fun buildSentence(): String {
        return parts.joinToString(" ")
    }

    /**
     * Clear the sentence bar after speaking.
     */
    fun clear(): List<String> {
        parts.clear()
        return parts.toList()
    }

    /**
     * Get current parts (for UI display).
     */
    fun getCurrentParts(): List<String> = parts.toList()
}
