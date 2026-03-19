package net.djrogers.aac4u.data.local.database

import net.djrogers.aac4u.data.local.database.entity.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds the database with a default profile, core vocabulary, and starter
 * fringe categories on first launch.
 *
 * Vocabulary design notes:
 * - Core words are the ~40 most frequently used words in AAC research
 *   (based on common AAC core word lists used in speech-language pathology)
 * - Fringe categories cover the most common communication contexts
 * - Each fringe category has 12-16 starter words (fills a 4×4 grid nicely)
 * - All buttons are text-only for now — images added in a later phase
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val database: AAC4UDatabase
) {
    /**
     * Seeds the database if no profiles exist (first launch).
     * Call this from the Application class or a startup use case.
     */
    suspend fun seedIfEmpty() {
        val profileDao = database.profileDao()
        val categoryDao = database.categoryDao()
        val buttonDao = database.buttonDao()

        // Check if data already exists
        val existingProfile = profileDao.getProfileById(1)
        if (existingProfile != null) return

        // Create default profile
        val profileId = profileDao.insertProfile(
            ProfileEntity(
                name = "Default",
                gridColumns = 4,
                gridRows = 4,
                buttonPaddingDp = 4,
                showLabels = true,
                labelPosition = "BELOW",
                inputMethod = "TAP",
                feedbackMode = "BOTH",
                ttsRate = 1.0f,
                ttsPitch = 1.0f,
                isActive = true,
                highContrastEnabled = false,
                dwellTimeMs = 1500,
                scanSpeedMs = 2000
            )
        )

        // ── CORE VOCABULARY ──
        // These are always visible regardless of selected category.
        val coreId = categoryDao.insertCategory(
            CategoryEntity(
                profileId = profileId,
                name = "Core",
                sortOrder = 0,
                isVisible = true,
                vocabularyType = "CORE"
            )
        )

        val coreWords = listOf(
            // Pronouns & people
            "I", "you", "he", "she", "it", "we", "they",
            // High-frequency verbs
            "want", "go", "like", "need", "help",
            "have", "make", "do", "get", "see",
            // Key modifiers
            "more", "not", "all", "some",
            // Social essentials
            "yes", "no", "please", "thank you",
            "hello", "goodbye",
            // Connectors
            "and", "the", "is", "my", "that",
            // Core questions
            "what", "where", "who", "when", "why", "how"
        )

        coreWords.forEachIndexed { index, word ->
            buttonDao.insertButton(
                ButtonEntity(
                    categoryId = coreId,
                    label = word,
                    phrase = word,
                    sortOrder = index,
                    isVisible = true,
                    isQuickPhrase = false
                )
            )
        }

        // ── FRINGE CATEGORIES ──

        // -- Feelings --
        val feelingsId = categoryDao.insertCategory(
            CategoryEntity(
                profileId = profileId,
                name = "Feelings",
                sortOrder = 1,
                isVisible = true,
                vocabularyType = "FRINGE"
            )
        )
        seedButtons(buttonDao, feelingsId, listOf(
            "happy", "sad", "angry", "scared", "tired",
            "hungry", "thirsty", "sick", "hurt", "bored",
            "excited", "worried", "confused", "surprised", "proud", "calm"
        ))

        // -- Actions --
        val actionsId = categoryDao.insertCategory(
            CategoryEntity(
                profileId = profileId,
                name = "Actions",
                sortOrder = 2,
                isVisible = true,
                vocabularyType = "FRINGE"
            )
        )
        seedButtons(buttonDao, actionsId, listOf(
            "eat", "drink", "play", "read", "watch",
            "listen", "walk", "run", "sit", "stand",
            "open", "close", "give", "take", "put", "stop"
        ))

        // -- Food & Drink --
        val foodId = categoryDao.insertCategory(
            CategoryEntity(
                profileId = profileId,
                name = "Food & Drink",
                sortOrder = 3,
                isVisible = true,
                vocabularyType = "FRINGE"
            )
        )
        seedButtons(buttonDao, foodId, listOf(
            "water", "juice", "milk", "apple", "banana",
            "bread", "chicken", "rice", "pasta", "cheese",
            "biscuit", "yoghurt", "sandwich", "pizza", "soup", "snack"
        ))

        // -- People --
        val peopleId = categoryDao.insertCategory(
            CategoryEntity(
                profileId = profileId,
                name = "People",
                sortOrder = 4,
                isVisible = true,
                vocabularyType = "FRINGE"
            )
        )
        seedButtons(buttonDao, peopleId, listOf(
            "mum", "dad", "brother", "sister", "grandma",
            "grandpa", "friend", "teacher", "doctor", "baby",
            "family", "carer", "therapist", "pet"
        ))

        // -- Places --
        val placesId = categoryDao.insertCategory(
            CategoryEntity(
                profileId = profileId,
                name = "Places",
                sortOrder = 5,
                isVisible = true,
                vocabularyType = "FRINGE"
            )
        )
        seedButtons(buttonDao, placesId, listOf(
            "home", "school", "park", "shop", "hospital",
            "bathroom", "bedroom", "kitchen", "outside", "car",
            "playground", "pool", "library", "restaurant"
        ))

        // -- Things --
        val thingsId = categoryDao.insertCategory(
            CategoryEntity(
                profileId = profileId,
                name = "Things",
                sortOrder = 6,
                isVisible = true,
                vocabularyType = "FRINGE"
            )
        )
        seedButtons(buttonDao, thingsId, listOf(
            "phone", "tablet", "book", "toy", "ball",
            "chair", "table", "bed", "TV", "computer",
            "cup", "plate", "bag", "shoes", "clothes", "money"
        ))

        // -- Descriptions --
        val descriptionsId = categoryDao.insertCategory(
            CategoryEntity(
                profileId = profileId,
                name = "Descriptions",
                sortOrder = 7,
                isVisible = true,
                vocabularyType = "FRINGE"
            )
        )
        seedButtons(buttonDao, descriptionsId, listOf(
            "big", "small", "hot", "cold", "good",
            "bad", "new", "old", "fast", "slow",
            "loud", "quiet", "hard", "easy", "same", "different"
        ))

        // -- Time --
        val timeId = categoryDao.insertCategory(
            CategoryEntity(
                profileId = profileId,
                name = "Time",
                sortOrder = 8,
                isVisible = true,
                vocabularyType = "FRINGE"
            )
        )
        seedButtons(buttonDao, timeId, listOf(
            "now", "later", "today", "tomorrow", "yesterday",
            "morning", "afternoon", "night", "soon", "wait",
            "before", "after", "always", "never", "again", "finished"
        ))

        // -- Questions --
        val questionsId = categoryDao.insertCategory(
            CategoryEntity(
                profileId = profileId,
                name = "Questions",
                sortOrder = 9,
                isVisible = true,
                vocabularyType = "FRINGE"
            )
        )
        seedButtons(buttonDao, questionsId, listOf(
            "what is that?", "where is it?", "who is that?",
            "when is it?", "why?", "how?",
            "can I?", "is it?", "do you?",
            "what happened?", "where are we going?", "are you okay?"
        ))

        // -- Social --
        val socialId = categoryDao.insertCategory(
            CategoryEntity(
                profileId = profileId,
                name = "Social",
                sortOrder = 10,
                isVisible = true,
                vocabularyType = "FRINGE"
            )
        )
        seedButtons(buttonDao, socialId, listOf(
            "hello", "goodbye", "please", "thank you",
            "sorry", "excuse me", "well done", "I love you",
            "how are you?", "my turn", "your turn", "let's go",
            "come here", "look at this", "I don't know", "wait please"
        ))

        // ── QUICK PHRASES ──
        // These are pre-built sentences for rapid communication.
        val quickId = categoryDao.insertCategory(
            CategoryEntity(
                profileId = profileId,
                name = "Quick Phrases",
                sortOrder = 11,
                isVisible = true,
                vocabularyType = "FRINGE"
            )
        )
        val quickPhrases = listOf(
            "I need help",
            "I'm hungry",
            "I'm thirsty",
            "I need the bathroom",
            "I don't feel well",
            "I'm in pain",
            "I don't understand",
            "Can you repeat that?",
            "I want to go home",
            "Leave me alone please",
            "I'm happy",
            "I'm tired",
            "Can I have more?",
            "I'm finished",
            "I don't like that",
            "I want something else"
        )
        quickPhrases.forEachIndexed { index, phrase ->
            buttonDao.insertButton(
                ButtonEntity(
                    categoryId = quickId,
                    label = phrase,
                    phrase = phrase,
                    sortOrder = index,
                    isVisible = true,
                    isQuickPhrase = true
                )
            )
        }
    }

    private suspend fun seedButtons(
        buttonDao: net.djrogers.aac4u.data.local.database.dao.ButtonDao,
        categoryId: Long,
        words: List<String>
    ) {
        words.forEachIndexed { index, word ->
            buttonDao.insertButton(
                ButtonEntity(
                    categoryId = categoryId,
                    label = word,
                    phrase = word,
                    sortOrder = index,
                    isVisible = true,
                    isQuickPhrase = false
                )
            )
        }
    }
}
