package service

import data.model.*

/**
 * Generates quick-action suggestions from writing analysis.
 */
class SuggestionGenerator {
    
    fun generate(analysis: WritingAnalysis): List<QuickSuggestion> {
        val suggestions = mutableListOf<QuickSuggestion>()
        
        // Grammar fixes (highest priority)
        analysis.grammarErrors
            .filter { it.canAutoFix }
            .take(2)
            .forEachIndexed { index, error ->
                suggestions.add(
                    QuickSuggestion(
                        id = "grammar_$index",
                        type = SuggestionCategory.GRAMMAR_FIX,
                        title = "Fix: ${error.errorType.name.lowercase().replace("_", " ")}",
                        description = error.explanation.take(60),
                        action = SuggestionAction(
                            type = ActionType.REPLACE,
                            value = error.correction,
                            position = error.positionStart
                        ),
                        preview = "\"${error.originalFragment}\" → \"${error.correction}\"",
                        priority = 100 - index * 10
                    )
                )
            }
        
        // Vocabulary alternatives
        analysis.vocabularyInsights?.suggestedAlternatives?.take(2)?.forEachIndexed { index, alt ->
            alt.alternatives.take(1).forEach { alternative ->
                suggestions.add(
                    QuickSuggestion(
                        id = "vocab_${index}",
                        type = SuggestionCategory.VOCABULARY,
                        title = "Try: ${alternative.word}",
                        description = alternative.meaning,
                        action = SuggestionAction(
                            type = ActionType.REPLACE,
                            value = alternative.word,
                            position = null
                        ),
                        preview = "Instead of \"${alt.original}\"",
                        priority = 80 - index * 5
                    )
                )
            }
        }
        
        // Style improvements
        analysis.styleSuggestions.take(2).forEachIndexed { index, style ->
            suggestions.add(
                QuickSuggestion(
                    id = "style_$index",
                    type = SuggestionCategory.STYLE,
                    title = style.type.name.lowercase().replace("_", " ").capitalize(),
                    description = style.description,
                    action = SuggestionAction(
                        type = ActionType.REPLACE,
                        value = style.suggestedVersion,
                        position = null
                    ),
                    preview = style.currentVersion.take(30),
                    priority = 60 - index * 5
                )
            )
        }
        
        // Next word suggestions
        analysis.nextWordSuggestions.take(3).forEachIndexed { index, word ->
            suggestions.add(
                QuickSuggestion(
                    id = "next_$index",
                    type = SuggestionCategory.NEXT_WORD,
                    title = word.suggestedWord,
                    description = word.context,
                    action = SuggestionAction(
                        type = ActionType.APPEND,
                        value = " ${word.suggestedWord}",
                        position = null
                    ),
                    preview = word.exampleSentence.take(40),
                    priority = (word.confidence * 50).toInt() + 30
                )
            )
        }
        
        // Learning opportunities
        analysis.learningOpportunities.take(2).forEachIndexed { index, learning ->
            suggestions.add(
                QuickSuggestion(
                    id = "learn_$index",
                    type = SuggestionCategory.LEARNING,
                    title = "Learn: ${learning.concept}",
                    description = learning.explanation.take(50),
                    action = SuggestionAction(
                        type = ActionType.SHOW_INFO,
                        value = learning.concept,
                        position = null
                    ),
                    preview = learning.examples.firstOrNull()?.take(40) ?: "",
                    priority = 40 - index * 5
                )
            )
        }
        
        // Continuation suggestions
        suggestions.add(
            QuickSuggestion(
                id = "continue_1",
                type = SuggestionCategory.CONTINUATION,
                title = "Add an example",
                description = "Illustrate your point with 'zum Beispiel'",
                action = SuggestionAction(
                    type = ActionType.APPEND,
                    value = " Zum Beispiel, ",
                    position = null
                ),
                preview = "Continue with an example",
                priority = 25
            )
        )
        
        suggestions.add(
            QuickSuggestion(
                id = "continue_2",
                type = SuggestionCategory.CONTINUATION,
                title = "Add contrast",
                description = "Show another perspective with 'aber' or 'jedoch'",
                action = SuggestionAction(
                    type = ActionType.APPEND,
                    value = " Aber ",
                    position = null
                ),
                preview = "Contrast your statement",
                priority = 20
            )
        )
        
        return suggestions.sortedByDescending { it.priority }
    }
    
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
