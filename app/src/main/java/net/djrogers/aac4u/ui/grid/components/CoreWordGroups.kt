package net.djrogers.aac4u.ui.grid.components

import androidx.compose.ui.graphics.Color

/**
 * Core word groups based on the PRC-Saltillo 100 High-Frequency Core Words list.
 * Each group has a name, colour, and list of words.
 */
data class CoreWordGroup(
    val name: String,
    val color: Color,
    val textColor: Color = Color(0xFF212121),
    val words: List<String>
)

object CoreWordGroups {
    val INTERJECTIONS = CoreWordGroup(
        name = "Social",
        color = Color(0xFFFFCDD2), // Pink
        words = listOf("yes", "no", "thank you", "please", "hello", "goodbye")
    )

    val PRONOUNS = CoreWordGroup(
        name = "Pronouns",
        color = Color(0xFFBBDEFB), // Light blue
        words = listOf("I", "me", "my", "mine", "you", "it", "he", "she", "we", "they")
    )

    val QUESTION_WORDS = CoreWordGroup(
        name = "Questions",
        color = Color(0xFFE0E0E0), // Warm grey
        words = listOf("what", "when", "where", "who", "why", "how")
    )

    val PREVERBS = CoreWordGroup(
        name = "Helpers",
        color = Color(0xFFE1BEE7), // Lavender
        words = listOf("be", "is", "am", "are", "was", "were", "do", "did", "can", "have", "will")
    )

    val VERBS = CoreWordGroup(
        name = "Actions",
        color = Color(0xFFC8E6C9), // Green
        words = listOf(
            "go", "stop", "turn", "make", "look", "see", "find", "put",
            "open", "close", "eat", "drink", "get", "help", "want", "need",
            "say", "tell", "come", "read", "like", "feel", "color", "let's",
            "work", "play", "finished"
        )
    )

    val ADJECTIVES = CoreWordGroup(
        name = "Describing",
        color = Color(0xFFFFF9C4), // Yellow
        words = listOf(
            "more", "one", "big", "little", "fast", "slow", "same", "different",
            "pretty", "red", "blue", "yellow", "good", "bad", "new", "old", "happy", "sad"
        )
    )

    val PREPOSITIONS = CoreWordGroup(
        name = "Where",
        color = Color(0xFFB2EBF2), // Cyan
        words = listOf("on", "off", "in", "out", "up", "down", "to", "for", "under", "with")
    )

    val DETERMINERS = CoreWordGroup(
        name = "Pointers",
        color = Color(0xFFFFE0B2), // Peach
        words = listOf("this", "that", "some", "all", "the")
    )

    val CONJUNCTIONS = CoreWordGroup(
        name = "Joining",
        color = Color(0xFFDCEDC8), // Lime
        words = listOf("and", "but")
    )

    val ADVERBS = CoreWordGroup(
        name = "When/How",
        color = Color(0xFFFFCCBC), // Coral
        words = listOf("not", "now", "here", "there", "away", "again")
    )

    val ALL_GROUPS = listOf(
        PRONOUNS, VERBS, ADJECTIVES, PREVERBS,
        PREPOSITIONS, QUESTION_WORDS, INTERJECTIONS,
        DETERMINERS, ADVERBS, CONJUNCTIONS
    )

    /**
     * Get the group colour for a given core word.
     */
    fun colorForWord(word: String): Color {
        for (group in ALL_GROUPS) {
            if (word in group.words || word.lowercase() in group.words.map { it.lowercase() }) {
                return group.color
            }
        }
        return Color(0xFFCFD8DC) // Default blue-grey
    }

    /**
     * Get all unique core words across all groups.
     */
    fun allWords(): List<String> {
        return ALL_GROUPS.flatMap { it.words }.distinct()
    }
}
