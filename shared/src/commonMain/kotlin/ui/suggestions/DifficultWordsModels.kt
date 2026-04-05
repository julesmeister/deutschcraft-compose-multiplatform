package ui.suggestions

import data.model.VocabularyItem
import kotlin.random.Random

/**
 * Data class representing a word practice suggestion.
 */
data class WordPracticeSuggestion(
    val word: String,
    val context: String,
    val translation: String?,
    val prompt: String
)

/**
 * Generates a practice prompt for a given word.
 */
fun generatePracticePrompt(word: VocabularyItem, seed: Int = 0): String {
    val prompts = listOf(
        "Try to use \"${word.word}\" in a sentence about your day.",
        "Write a sentence using \"${word.word}\" that shows its meaning.",
        "Can you think of a situation where you'd use \"${word.word}\"?",
        "Use \"${word.word}\" to describe something you see around you.",
        "Create a question that includes the word \"${word.word}\".",
        "Write about a time when you encountered \"${word.word}\".",
        "Describe your favorite activity using \"${word.word}\".",
        "How would you explain \"${word.word}\" to a friend?"
    )

    return prompts[(seed + word.word.hashCode()).absoluteValue % prompts.size]
}

/**
 * Extension property to get the absolute value of an Int.
 */
val Int.absoluteValue: Int get() = if (this < 0) -this else this

/**
 * Determines if it is an appropriate moment to suggest difficult words.
 * This is based on heuristics like:
 * - User has written enough text (at least a few sentences)
 * - Some time has passed since last suggestion
 * - User has difficult words to practice
 */
fun shouldSuggestDifficultWords(
    textLength: Int,
    wordCount: Int,
    difficultWordCount: Int,
    lastSuggestionTime: Long? = null,
    minWordsSinceLastSuggestion: Int = 50
): Boolean {
    // Need at least some content to suggest words
    if (textLength < 20 || wordCount < 5) return false

    // Need difficult words to suggest
    if (difficultWordCount == 0) return false

    // Check time/words since last suggestion
    lastSuggestionTime?.let { lastTime ->
        val timeSinceLastSuggestion = System.currentTimeMillis() - lastTime
        // Don't show more than once per 5 minutes
        if (timeSinceLastSuggestion < 5 * 60 * 1000) return false
    }

    // Random chance to avoid being too intrusive (30% chance when conditions are met)
    return Random.Default.nextFloat() < 0.3f
}
