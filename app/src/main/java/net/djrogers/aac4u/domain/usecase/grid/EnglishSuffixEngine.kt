package net.djrogers.aac4u.domain.usecase.grid

/**
 * English suffix engine for AAC word modification.
 * Handles plurals, past tense, present participle, comparatives,
 * superlatives, and negation with proper English spelling rules.
 *
 * Includes common irregular forms and spelling rules for:
 * - Doubling final consonants (run → running, big → bigger)
 * - Dropping silent e (make → making, like → liked)
 * - Y → I changes (happy → happier, carry → carried)
 * - ES plurals (box → boxes, church → churches)
 * - Irregular verbs and adjectives
 */
object EnglishSuffixEngine {

    // ═══════════════════════════════════════
    // IRREGULAR FORMS
    // ═══════════════════════════════════════

    private val irregularPlurals = mapOf(
        "child" to "children", "person" to "people", "man" to "men",
        "woman" to "women", "mouse" to "mice", "foot" to "feet",
        "tooth" to "teeth", "goose" to "geese", "fish" to "fish",
        "sheep" to "sheep", "deer" to "deer", "I" to "we",
    )

    private val irregularPastTense = mapOf(
        "go" to "went", "get" to "got", "see" to "saw", "say" to "said",
        "make" to "made", "come" to "came", "take" to "took", "give" to "gave",
        "find" to "found", "tell" to "told", "feel" to "felt", "put" to "put",
        "read" to "read", "run" to "ran", "eat" to "ate", "drink" to "drank",
        "sit" to "sat", "stand" to "stood", "have" to "had", "do" to "did",
        "be" to "was", "is" to "was", "am" to "was", "are" to "were",
        "can" to "could", "will" to "would",
    )

    private val irregularPresentParticiple = mapOf(
        "be" to "being", "is" to "being", "am" to "being", "are" to "being",
        "die" to "dying", "lie" to "lying", "tie" to "tying",
    )

    private val irregularComparative = mapOf(
        "good" to "better", "bad" to "worse", "far" to "farther",
        "little" to "less", "more" to "more",
    )

    private val irregularSuperlative = mapOf(
        "good" to "best", "bad" to "worst", "far" to "farthest",
        "little" to "least", "more" to "most",
    )

    private val irregularNegation = mapOf(
        "can" to "can't", "will" to "won't", "do" to "don't",
        "did" to "didn't", "is" to "isn't", "am" to "I'm not",
        "are" to "aren't", "was" to "wasn't", "were" to "weren't",
        "have" to "haven't", "has" to "hasn't", "had" to "hadn't",
        "would" to "wouldn't", "could" to "couldn't", "should" to "shouldn't",
    )

    // Short vowel + single consonant words that double the final consonant
    private val doublingWords = setOf(
        "run", "sit", "stop", "get", "put", "cut", "hit", "let", "set",
        "big", "hot", "sad", "mad", "wet", "thin", "fit", "win", "swim",
        "shop", "drop", "trip", "skip", "step", "plan", "chat", "clap",
        "grab", "snap", "wrap", "drag", "plug", "hug", "rub", "tug",
    )

    // Words ending in consonant clusters that do NOT double
    private val noDoubleEndings = setOf(
        "help", "want", "need", "look", "work", "walk", "talk", "turn",
        "open", "close", "play", "read", "feel", "tell", "find",
        "fast", "slow", "loud", "quiet", "hard", "soft",
    )

    private val vowels = setOf('a', 'e', 'i', 'o', 'u')

    // ═══════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════

    /**
     * Add plural suffix (+s / +es).
     */
    fun addPlural(word: String): String {
        val lower = word.lowercase()

        // Check irregulars
        irregularPlurals[lower]?.let { return matchCase(word, it) }

        // ES rules
        if (lower.endsWith("s") || lower.endsWith("x") || lower.endsWith("z") ||
            lower.endsWith("ch") || lower.endsWith("sh")) {
            return word + "es"
        }

        // Consonant + y → ies
        if (lower.endsWith("y") && lower.length > 1 && lower[lower.length - 2] !in vowels) {
            return word.dropLast(1) + "ies"
        }

        // F/FE → ves (knife → knives, leaf → leaves)
        if (lower.endsWith("fe")) {
            return word.dropLast(2) + "ves"
        }
        if (lower.endsWith("f") && !lower.endsWith("ff")) {
            return word.dropLast(1) + "ves"
        }

        return word + "s"
    }

    /**
     * Add past tense suffix (+ed).
     */
    fun addPastTense(word: String): String {
        val lower = word.lowercase()

        // Check irregulars
        irregularPastTense[lower]?.let { return matchCase(word, it) }

        // Already ends in e → just add d
        if (lower.endsWith("e")) {
            return word + "d"
        }

        // Consonant + y → ied
        if (lower.endsWith("y") && lower.length > 1 && lower[lower.length - 2] !in vowels) {
            return word.dropLast(1) + "ied"
        }

        // Double final consonant
        if (shouldDouble(lower)) {
            return word + word.last() + "ed"
        }

        return word + "ed"
    }

    /**
     * Add present participle suffix (+ing).
     */
    fun addPresentParticiple(word: String): String {
        val lower = word.lowercase()

        // Check irregulars
        irregularPresentParticiple[lower]?.let { return matchCase(word, it) }

        // Silent e → drop e, add ing
        if (lower.endsWith("e") && !lower.endsWith("ee") && !lower.endsWith("ye")) {
            return word.dropLast(1) + "ing"
        }

        // IE → ying
        if (lower.endsWith("ie")) {
            return word.dropLast(2) + "ying"
        }

        // Double final consonant
        if (shouldDouble(lower)) {
            return word + word.last() + "ing"
        }

        return word + "ing"
    }

    /**
     * Add comparative suffix (+er).
     */
    fun addComparative(word: String): String {
        val lower = word.lowercase()

        // Check irregulars
        irregularComparative[lower]?.let { return matchCase(word, it) }

        // Silent e → just add r
        if (lower.endsWith("e")) {
            return word + "r"
        }

        // Consonant + y → ier
        if (lower.endsWith("y") && lower.length > 1 && lower[lower.length - 2] !in vowels) {
            return word.dropLast(1) + "ier"
        }

        // Double final consonant for short adjectives
        if (shouldDouble(lower)) {
            return word + word.last() + "er"
        }

        // Multi-syllable words → "more X" (but we just append for simplicity)
        return word + "er"
    }

    /**
     * Add superlative suffix (+est).
     */
    fun addSuperlative(word: String): String {
        val lower = word.lowercase()

        // Check irregulars
        irregularSuperlative[lower]?.let { return matchCase(word, it) }

        // Silent e → just add st
        if (lower.endsWith("e")) {
            return word + "st"
        }

        // Consonant + y → iest
        if (lower.endsWith("y") && lower.length > 1 && lower[lower.length - 2] !in vowels) {
            return word.dropLast(1) + "iest"
        }

        // Double final consonant
        if (shouldDouble(lower)) {
            return word + word.last() + "est"
        }

        return word + "est"
    }

    /**
     * Add negation (+n't).
     */
    fun addNegation(word: String): String {
        val lower = word.lowercase()

        // Check irregulars (most negations are irregular)
        irregularNegation[lower]?.let { return matchCase(word, it) }

        // Default: append n't
        return word + "n't"
    }

    // ═══════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════

    /**
     * Determine if the final consonant should be doubled.
     * Rule: short words (1 syllable) ending in single vowel + single consonant.
     */
    private fun shouldDouble(word: String): Boolean {
        if (word.length < 2) return false

        // Known doubling words
        if (word in doublingWords) return true

        // Known non-doubling words
        if (word in noDoubleEndings) return false

        val lastChar = word.last()
        val secondLast = word[word.length - 2]

        // Don't double w, x, y
        if (lastChar in setOf('w', 'x', 'y')) return false

        // Must end in consonant
        if (lastChar in vowels) return false

        // Second-to-last must be a vowel
        if (secondLast !in vowels) return false

        // For short words (roughly 1 syllable, ≤4 chars), double
        if (word.length <= 4) return true

        return false
    }

    /**
     * Match the case pattern of the original word to the result.
     */
    private fun matchCase(original: String, result: String): String {
        if (original.isEmpty() || result.isEmpty()) return result

        // ALL CAPS
        if (original == original.uppercase() && original.length > 1) {
            return result.uppercase()
        }

        // Title Case
        if (original[0].isUpperCase()) {
            return result.replaceFirstChar { it.uppercase() }
        }

        return result
    }
}
