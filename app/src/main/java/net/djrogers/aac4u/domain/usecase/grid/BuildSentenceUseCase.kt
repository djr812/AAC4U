package net.djrogers.aac4u.domain.usecase.grid

import javax.inject.Inject

/**
 * Manages building a sentence from multiple button taps.
 * Supports selecting and replacing individual words in the sentence.
 */
class BuildSentenceUseCase @Inject constructor() {

    private val parts = mutableListOf<String>()

    fun addPart(phrase: String): List<String> {
        parts.add(phrase)
        return parts.toList()
    }

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
     * Replace the word at a specific index.
     */
    fun replacePartAt(index: Int, newPart: String): List<String> {
        if (index in parts.indices) {
            parts[index] = newPart
        }
        return parts.toList()
    }

    /**
     * Remove the word at a specific index.
     */
    fun removePartAt(index: Int): List<String> {
        if (index in parts.indices) {
            parts.removeAt(index)
        }
        return parts.toList()
    }

    fun buildSentence(): String {
        return parts.joinToString(" ")
    }

    fun clear(): List<String> {
        parts.clear()
        return parts.toList()
    }

    fun getCurrentParts(): List<String> = parts.toList()
}
