package net.djrogers.aac4u.data.local.database

import net.djrogers.aac4u.data.local.database.entity.*
import net.djrogers.aac4u.data.symbol.SymbolManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val database: AAC4UDatabase,
    private val symbolManager: SymbolManager
) {
    suspend fun seedIfEmpty() {
        val profileDao = database.profileDao()
        val categoryDao = database.categoryDao()
        val buttonDao = database.buttonDao()

        val existingProfile = profileDao.getProfileById(1)
        if (existingProfile != null) return

        val profileId = profileDao.insertProfile(
            ProfileEntity(
                name = "Default",
                gridColumns = 4, gridRows = 4, buttonPaddingDp = 4,
                showLabels = true, labelPosition = "BELOW",
                inputMethod = "TAP", feedbackMode = "BOTH",
                ttsRate = 1.0f, ttsPitch = 1.0f,
                isActive = true, highContrastEnabled = false,
                dwellTimeMs = 1500, scanSpeedMs = 2000
            )
        )

        // ── CORE VOCABULARY (100+ high-frequency words) ──
        val coreId = categoryDao.insertCategory(
            CategoryEntity(
                profileId = profileId, name = "Core", sortOrder = 0,
                isVisible = true, vocabularyType = "CORE"
            )
        )

        // All core words in a meaningful order matching the grouped display
        val coreWords = listOf(
            // Pronouns
            "I", "me", "my", "mine", "you", "it", "he", "she", "we", "they",
            // Verbs (action words)
            "go", "stop", "turn", "make", "look", "see", "find", "put",
            "open", "close", "eat", "drink", "get", "help", "want", "need",
            "say", "tell", "come", "read", "like", "feel", "color", "let's",
            "work", "play", "finished",
            // Adjectives (descriptive words)
            "more", "one", "big", "little", "fast", "slow", "same", "different",
            "pretty", "red", "blue", "yellow", "good", "bad", "new", "old", "happy", "sad",
            // Preverbs (helping words)
            "be", "is", "am", "are", "was", "were", "do", "did", "can", "have", "will",
            // Prepositions (placing words)
            "on", "off", "in", "out", "up", "down", "to", "for", "under", "with",
            // Question words
            "what", "when", "where", "who", "why", "how",
            // Interjections (social words)
            "yes", "no", "thank you", "please", "hello", "goodbye",
            // Determiners (pointer words)
            "this", "that", "some", "all", "the",
            // Adverbs
            "not", "now", "here", "there", "away", "again",
            // Conjunctions
            "and", "but"
        )

        coreWords.forEachIndexed { index, word ->
            val symbolPath = symbolManager.getSymbolForWord(word)
            buttonDao.insertButton(
                ButtonEntity(
                    categoryId = coreId, label = word, phrase = word,
                    imagePath = symbolPath, imageType = "BUNDLED",
                    sortOrder = index, isVisible = true, isQuickPhrase = false
                )
            )
        }

        // ── FRINGE CATEGORIES ──

        val feelingsId = categoryDao.insertCategory(
            CategoryEntity(profileId = profileId, name = "Feelings", sortOrder = 1, isVisible = true, vocabularyType = "FRINGE")
        )
        seedButtons(buttonDao, feelingsId, listOf(
            "happy", "sad", "angry", "scared", "tired",
            "hungry", "thirsty", "sick", "hurt", "bored",
            "excited", "worried", "confused", "surprised", "proud", "calm"
        ))

        val actionsId = categoryDao.insertCategory(
            CategoryEntity(profileId = profileId, name = "Actions", sortOrder = 2, isVisible = true, vocabularyType = "FRINGE")
        )
        seedButtons(buttonDao, actionsId, listOf(
            "eat", "drink", "play", "read", "watch",
            "listen", "walk", "run", "sit", "stand",
            "open", "close", "give", "take", "put", "stop"
        ))

        val foodId = categoryDao.insertCategory(
            CategoryEntity(profileId = profileId, name = "Food & Drink", sortOrder = 3, isVisible = true, vocabularyType = "FRINGE")
        )
        seedButtons(buttonDao, foodId, listOf(
            "water", "juice", "milk", "apple", "banana",
            "bread", "chicken", "rice", "pasta", "cheese",
            "biscuit", "yoghurt", "sandwich", "pizza", "soup", "snack"
        ))

        val peopleId = categoryDao.insertCategory(
            CategoryEntity(profileId = profileId, name = "People", sortOrder = 4, isVisible = true, vocabularyType = "FRINGE")
        )
        seedButtons(buttonDao, peopleId, listOf(
            "mum", "dad", "brother", "sister", "grandma",
            "grandpa", "friend", "teacher", "doctor", "baby",
            "family", "carer", "therapist", "pet"
        ))

        val placesId = categoryDao.insertCategory(
            CategoryEntity(profileId = profileId, name = "Places", sortOrder = 5, isVisible = true, vocabularyType = "FRINGE")
        )
        seedButtons(buttonDao, placesId, listOf(
            "home", "school", "park", "shop", "hospital",
            "bathroom", "bedroom", "kitchen", "outside", "car",
            "playground", "pool", "library", "restaurant"
        ))

        val thingsId = categoryDao.insertCategory(
            CategoryEntity(profileId = profileId, name = "Things", sortOrder = 6, isVisible = true, vocabularyType = "FRINGE")
        )
        seedButtons(buttonDao, thingsId, listOf(
            "phone", "tablet", "book", "toy", "ball",
            "chair", "table", "bed", "TV", "computer",
            "cup", "plate", "bag", "shoes", "clothes", "money"
        ))

        val descriptionsId = categoryDao.insertCategory(
            CategoryEntity(profileId = profileId, name = "Descriptions", sortOrder = 7, isVisible = true, vocabularyType = "FRINGE")
        )
        seedButtons(buttonDao, descriptionsId, listOf(
            "big", "small", "hot", "cold", "good",
            "bad", "new", "old", "fast", "slow",
            "loud", "quiet", "hard", "easy", "same", "different"
        ))

        val timeId = categoryDao.insertCategory(
            CategoryEntity(profileId = profileId, name = "Time", sortOrder = 8, isVisible = true, vocabularyType = "FRINGE")
        )
        seedButtons(buttonDao, timeId, listOf(
            "now", "later", "today", "tomorrow", "yesterday",
            "morning", "afternoon", "night", "soon", "wait",
            "before", "after", "always", "never", "again", "finished"
        ))

        val questionsId = categoryDao.insertCategory(
            CategoryEntity(profileId = profileId, name = "Questions", sortOrder = 9, isVisible = true, vocabularyType = "FRINGE")
        )
        seedButtons(buttonDao, questionsId, listOf(
            "what is that?", "where is it?", "who is that?",
            "when is it?", "why?", "how?",
            "can I?", "is it?", "do you?",
            "what happened?", "where are we going?", "are you okay?"
        ))

        val socialId = categoryDao.insertCategory(
            CategoryEntity(profileId = profileId, name = "Social", sortOrder = 10, isVisible = true, vocabularyType = "FRINGE")
        )
        seedButtons(buttonDao, socialId, listOf(
            "hello", "goodbye", "please", "thank you",
            "sorry", "excuse me", "well done", "I love you",
            "how are you?", "my turn", "your turn", "let's go",
            "come here", "look at this", "I don't know", "wait please"
        ))

        val quickId = categoryDao.insertCategory(
            CategoryEntity(profileId = profileId, name = "Quick Phrases", sortOrder = 11, isVisible = true, vocabularyType = "FRINGE")
        )
        val quickPhrases = listOf(
            "I need help", "I'm hungry", "I'm thirsty",
            "I need the bathroom", "I don't feel well", "I'm in pain",
            "I don't understand", "Can you repeat that?",
            "I want to go home", "Leave me alone please",
            "I'm happy", "I'm tired", "Can I have more?",
            "I'm finished", "I don't like that", "I want something else"
        )
        quickPhrases.forEachIndexed { index, phrase ->
            val symbolPath = symbolManager.getSymbolForWord(phrase)
            buttonDao.insertButton(
                ButtonEntity(
                    categoryId = quickId, label = phrase, phrase = phrase,
                    imagePath = symbolPath, imageType = "BUNDLED",
                    sortOrder = index, isVisible = true, isQuickPhrase = true
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
            val symbolPath = symbolManager.getSymbolForWord(word)
            buttonDao.insertButton(
                ButtonEntity(
                    categoryId = categoryId, label = word, phrase = word,
                    imagePath = symbolPath, imageType = "BUNDLED",
                    sortOrder = index, isVisible = true, isQuickPhrase = false
                )
            )
        }
    }
}
