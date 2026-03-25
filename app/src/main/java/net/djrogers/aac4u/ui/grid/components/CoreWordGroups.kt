package net.djrogers.aac4u.ui.grid.components

import androidx.compose.ui.graphics.Color

data class CoreWordGroup(
    val name: String,
    val label: String,
    val color: Color,
    val symbolWord: String,
    val textColor: Color = Color(0xFF212121),
    val words: List<String>
)

object CoreWordGroups {
    val INTERJECTIONS = CoreWordGroup(
        name = "Social",
        label = "Social",
        color = Color(0xFFFFCDD2),
        symbolWord = "hello",
        words = listOf("yes", "no", "thank you", "please", "hello", "goodbye")
    )

    val PRONOUNS = CoreWordGroup(
        name = "Pronouns",
        label = "Pronouns",
        color = Color(0xFFBBDEFB),
        symbolWord = "I",
        words = listOf("I", "me", "my", "mine", "you", "it", "he", "she", "we", "they")
    )

    val QUESTION_WORDS = CoreWordGroup(
        name = "Questions",
        label = "Questions",
        color = Color(0xFFE0E0E0),
        symbolWord = "what",
        words = listOf("what", "when", "where", "who", "why", "how")
    )

    val PREVERBS = CoreWordGroup(
        name = "Helpers",
        label = "Helpers",
        color = Color(0xFFE1BEE7),
        symbolWord = "can",
        words = listOf("be", "is", "am", "are", "was", "were", "do", "did", "can", "have", "will")
    )

    val VERBS = CoreWordGroup(
        name = "Actions",
        label = "Verbs",
        color = Color(0xFFC8E6C9),
        symbolWord = "go",
        words = listOf(
            "go", "stop", "turn", "make", "look", "see", "find", "put",
            "open", "close", "eat", "drink", "get", "help", "want", "need",
            "say", "tell", "come", "read", "like", "feel", "color", "let's",
            "work", "play", "finished"
        )
    )

    val ADJECTIVES = CoreWordGroup(
        name = "Describing",
        label = "Describing",
        color = Color(0xFFFFF9C4),
        symbolWord = "happy",
        words = listOf(
            "more", "one", "big", "little", "fast", "slow", "same", "different",
            "pretty", "red", "blue", "yellow", "good", "bad", "new", "old", "happy", "sad"
        )
    )

    val PREPOSITIONS = CoreWordGroup(
        name = "Where",
        label = "Where",
        color = Color(0xFFB2EBF2),
        symbolWord = "in",
        words = listOf("on", "off", "in", "out", "up", "down", "to", "for", "under", "with")
    )

    val DETERMINERS = CoreWordGroup(
        name = "Pointers",
        label = "Pointers",
        color = Color(0xFFFFE0B2),
        symbolWord = "this",
        words = listOf("this", "that", "some", "all", "the")
    )

    val CONJUNCTIONS = CoreWordGroup(
        name = "Joining",
        label = "Joining",
        color = Color(0xFFDCEDC8),
        symbolWord = "and",
        words = listOf("and", "but")
    )

    val ADVERBS = CoreWordGroup(
        name = "When/How",
        label = "When",
        color = Color(0xFFFFCCBC),
        symbolWord = "now",
        words = listOf("not", "now", "here", "there", "away", "again")
    )

    val ALL_GROUPS = listOf(
        PRONOUNS, VERBS, ADJECTIVES, PREVERBS,
        PREPOSITIONS, QUESTION_WORDS, INTERJECTIONS,
        DETERMINERS, ADVERBS, CONJUNCTIONS
    )

    private val hexToGroup: Map<String, CoreWordGroup> = mapOf(
        "#BBDEFB" to PRONOUNS,
        "#C8E6C9" to VERBS,
        "#FFF9C4" to ADJECTIVES,
        "#E1BEE7" to PREVERBS,
        "#B2EBF2" to PREPOSITIONS,
        "#E0E0E0" to QUESTION_WORDS,
        "#FFCDD2" to INTERJECTIONS,
        "#FFE0B2" to DETERMINERS,
        "#DCEDC8" to CONJUNCTIONS,
        "#FFCCBC" to ADVERBS
    )

    fun colorForWord(word: String): Color {
        for (group in ALL_GROUPS) {
            if (word in group.words || word.lowercase() in group.words.map { it.lowercase() }) {
                return group.color
            }
        }
        return Color(0xFFCFD8DC)
    }

    fun groupForButton(label: String, backgroundColor: String?): CoreWordGroup? {
        for (group in ALL_GROUPS) {
            if (label.lowercase() in group.words.map { it.lowercase() }) return group
        }
        if (backgroundColor != null) {
            return hexToGroup[backgroundColor.uppercase()] ?: hexToGroup[backgroundColor]
        }
        return null
    }

    fun allWords(): List<String> = ALL_GROUPS.flatMap { it.words }.distinct()
}
