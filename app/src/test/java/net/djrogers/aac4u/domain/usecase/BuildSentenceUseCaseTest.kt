package net.djrogers.aac4u.domain.usecase

import net.djrogers.aac4u.domain.usecase.grid.BuildSentenceUseCase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests for the sentence builder — a core piece of AAC logic.
 */
class BuildSentenceUseCaseTest {

    private lateinit var useCase: BuildSentenceUseCase

    @BeforeEach
    fun setup() {
        useCase = BuildSentenceUseCase()
    }

    @Test
    fun `addPart adds word to sentence`() {
        val result = useCase.addPart("hello")
        assertEquals(listOf("hello"), result)
    }

    @Test
    fun `addPart builds multi-word sentence`() {
        useCase.addPart("I")
        useCase.addPart("want")
        val result = useCase.addPart("juice")
        assertEquals(listOf("I", "want", "juice"), result)
    }

    @Test
    fun `buildSentence joins parts with spaces`() {
        useCase.addPart("I")
        useCase.addPart("want")
        useCase.addPart("more")
        assertEquals("I want more", useCase.buildSentence())
    }

    @Test
    fun `removeLastPart removes most recent word`() {
        useCase.addPart("I")
        useCase.addPart("want")
        useCase.addPart("oops")
        val result = useCase.removeLastPart()
        assertEquals(listOf("I", "want"), result)
    }

    @Test
    fun `removeLastPart on empty list returns empty`() {
        val result = useCase.removeLastPart()
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `clear empties the sentence`() {
        useCase.addPart("I")
        useCase.addPart("want")
        val result = useCase.clear()
        assertEquals(emptyList<String>(), result)
        assertEquals("", useCase.buildSentence())
    }

    @Test
    fun `buildSentence on empty returns empty string`() {
        assertEquals("", useCase.buildSentence())
    }

    @Test
    fun `getCurrentParts returns copy not reference`() {
        useCase.addPart("hello")
        val parts = useCase.getCurrentParts()
        assertEquals(listOf("hello"), parts)
        // Modifying returned list shouldn't affect internal state
        useCase.addPart("world")
        assertEquals(listOf("hello"), parts) // Original list unchanged
    }
}
