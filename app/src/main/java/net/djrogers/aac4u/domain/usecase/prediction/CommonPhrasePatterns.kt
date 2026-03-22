package net.djrogers.aac4u.domain.usecase.prediction

/**
 * Common English word-pair patterns for AAC prediction.
 * Used as a fallback when the user hasn't built up enough
 * personal usage data for bigram frequency prediction.
 *
 * Based on common AAC communication patterns and
 * high-frequency English word combinations.
 */
object CommonPhrasePatterns {

    /**
     * Map of word -> list of likely next words, ordered by probability.
     * Lowercase keys. Returns top candidates.
     */
    val patterns: Map<String, List<String>> = mapOf(
        // Pronouns → verbs/helpers
        "i" to listOf("want", "need", "like", "am", "can", "have", "do", "feel", "will", "was"),
        "you" to listOf("are", "can", "want", "need", "like", "have", "do", "will"),
        "he" to listOf("is", "was", "can", "will", "want", "need", "like", "have"),
        "she" to listOf("is", "was", "can", "will", "want", "need", "like", "have"),
        "it" to listOf("is", "was", "can", "will", "on", "in", "here"),
        "we" to listOf("are", "can", "want", "need", "will", "have", "do", "like"),
        "they" to listOf("are", "can", "want", "need", "will", "have", "do"),

        // Helpers → verbs
        "can" to listOf("I", "you", "we", "go", "have", "get", "see", "help", "play", "eat"),
        "will" to listOf("you", "go", "come", "be", "have", "get", "do", "help"),
        "do" to listOf("you", "it", "not", "want", "need", "like"),
        "did" to listOf("you", "it", "not", "he", "she"),
        "am" to listOf("happy", "sad", "hungry", "thirsty", "tired", "here", "not", "good"),
        "is" to listOf("it", "good", "bad", "here", "there", "not", "big", "little"),
        "are" to listOf("you", "we", "they", "not", "here", "there", "good"),
        "was" to listOf("it", "good", "bad", "here", "there", "not", "fun"),
        "have" to listOf("a", "some", "more", "it", "one", "you", "to"),
        "be" to listOf("good", "happy", "here", "there", "quiet"),

        // Common verbs → objects/places
        "want" to listOf("more", "it", "to", "that", "some", "help", "one", "food", "water"),
        "need" to listOf("help", "more", "it", "to", "some", "water", "food"),
        "like" to listOf("it", "that", "this", "to", "more"),
        "go" to listOf("to", "home", "outside", "there", "here", "away", "now"),
        "get" to listOf("it", "more", "some", "help", "one", "up", "down"),
        "see" to listOf("it", "that", "you", "here", "there"),
        "eat" to listOf("it", "more", "some", "food", "now"),
        "drink" to listOf("water", "juice", "milk", "more", "some"),
        "help" to listOf("me", "please", "now"),
        "put" to listOf("it", "on", "in", "down", "here", "there"),
        "open" to listOf("it", "please"),
        "close" to listOf("it", "please"),
        "come" to listOf("here", "please", "now", "with"),
        "play" to listOf("with", "more", "now", "outside"),
        "read" to listOf("it", "more", "please", "that"),
        "look" to listOf("here", "there", "at", "please"),
        "make" to listOf("it", "more", "one", "some"),
        "say" to listOf("please", "hello", "goodbye", "it", "yes", "no"),
        "tell" to listOf("me", "him", "her", "please"),
        "stop" to listOf("it", "please", "now", "that"),
        "turn" to listOf("on", "off", "it", "around"),
        "find" to listOf("it", "one", "more"),
        "feel" to listOf("happy", "sad", "good", "bad", "sick", "tired", "scared"),
        "work" to listOf("now", "here", "more", "please"),
        "give" to listOf("me", "it", "more", "please", "one"),
        "take" to listOf("it", "one", "me"),

        // Prepositions → locations/pronouns
        "to" to listOf("go", "the", "me", "you", "home", "school", "eat"),
        "in" to listOf("here", "there", "it", "the"),
        "on" to listOf("it", "the", "here", "there"),
        "with" to listOf("me", "you", "it", "that"),
        "for" to listOf("me", "you", "it", "that"),
        "up" to listOf("please", "now", "here", "there"),
        "down" to listOf("please", "now", "here", "there"),

        // Adjectives → nouns
        "more" to listOf("please", "water", "food", "play"),
        "big" to listOf("one", "it"),
        "little" to listOf("one", "it"),
        "good" to listOf("one", "morning", "job"),
        "bad" to listOf("one", "it"),
        "new" to listOf("one", "it"),
        "happy" to listOf("now", "today"),
        "sad" to listOf("now", "today"),

        // Social
        "thank" to listOf("you"),
        "thank you" to listOf("please", "more"),
        "yes" to listOf("please", "more", "I"),
        "no" to listOf("thank you", "more", "I", "not"),
        "please" to listOf("help", "more", "stop"),
        "hello" to listOf("how", "I", "my"),
        "goodbye" to listOf("see", "thank"),

        // Questions
        "what" to listOf("is", "do", "are", "can"),
        "where" to listOf("is", "are", "do", "can"),
        "who" to listOf("is", "are", "can", "did"),
        "when" to listOf("is", "do", "can", "will"),
        "why" to listOf("is", "do", "are", "not", "can"),
        "how" to listOf("are", "do", "is", "can", "many"),

        // Determiners
        "this" to listOf("is", "one", "here", "please"),
        "that" to listOf("is", "one", "there", "please"),
        "some" to listOf("more", "water", "food", "please"),
        "all" to listOf("done", "finished", "of", "good"),
        "the" to listOf("one", "big", "little", "good", "bad", "new", "old"),

        // Adverbs
        "not" to listOf("good", "now", "here", "want", "like", "happy"),
        "now" to listOf("please", "I", "we", "go"),
        "here" to listOf("please", "now", "is"),
        "there" to listOf("is", "it", "please"),
        "again" to listOf("please", "more", "now"),

        // Finished
        "finished" to listOf("now", "please", "thank you"),
    )

    /**
     * Get common next words for a given word.
     */
    fun getNextWords(word: String): List<String> {
        return patterns[word.lowercase()] ?: emptyList()
    }
}
