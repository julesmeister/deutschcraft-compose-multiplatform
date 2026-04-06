package service

import data.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data models for AI analysis responses and context.
 */

data class AnalysisResult(
    val analysis: WritingAnalysis,
    val confidence: Double,
    val source: AnalysisSource
)

enum class AnalysisSource {
    AI,
    LOCAL_RULES,
    LOCAL_FALLBACK,
    HYBRID
}

data class AnalysisContext(
    val previousSentences: List<String> = emptyList(),
    val userPreferences: UserPreferences = UserPreferences(),
    val documentTopic: String? = null
)

@Serializable
data class AiAnalysisResponse(
    val grammarErrors: List<AiGrammarError> = emptyList(),
    val vocabularyInsights: AiVocabularyInsights? = null,
    val styleSuggestions: List<AiStyleSuggestion> = emptyList(),
    val learningOpportunities: List<AiLearningOpportunity> = emptyList(),
    val nextWordSuggestions: List<AiNextWordSuggestion> = emptyList(),
    val sentenceLevel: String = "A2",
    val score: Double = 70.0,
    val confidence: Double = 0.7
) {
    fun toWritingAnalysis(sentence: String): WritingAnalysis {
        return WritingAnalysis(
            sentence = sentence,
            timestamp = kotlinx.datetime.Clock.System.now(),
            grammarErrors = grammarErrors.map { it.toSentenceGrammarError() },
            vocabularyInsights = vocabularyInsights?.toVocabularyInsights(),
            styleSuggestions = styleSuggestions.map { it.toStyleSuggestion() },
            learningOpportunities = learningOpportunities.map { it.toLearningOpportunity() },
            nextWordSuggestions = nextWordSuggestions.map { it.toNextWordSuggestion() },
            structureAnalysis = null,
            sentenceLevel = parseLevel(sentenceLevel),
            score = score
        )
    }
    
    private fun parseLevel(level: String): CefrLevel {
        return try {
            CefrLevel.valueOf(level.uppercase())
        } catch (_: Exception) {
            CefrLevel.A2
        }
    }
}

@Serializable
data class AiGrammarError(
    @SerialName("originalFragment") val originalFragment: String,
    @SerialName("correction") val correction: String,
    @SerialName("errorType") val errorType: String,
    @SerialName("explanation") val explanation: String,
    @SerialName("severity") val severity: String,
    @SerialName("canAutoFix") val canAutoFix: Boolean
) {
    fun toSentenceGrammarError(): SentenceGrammarError {
        return SentenceGrammarError(
            originalFragment = originalFragment,
            correction = correction,
            errorType = try {
                ErrorType.valueOf(errorType.uppercase())
            } catch (_: Exception) { ErrorType.OTHER },
            explanation = explanation,
            positionStart = 0,
            positionEnd = originalFragment.length,
            severity = try {
                ErrorSeverity.valueOf(severity.uppercase())
            } catch (_: Exception) { ErrorSeverity.MEDIUM },
            canAutoFix = canAutoFix
        )
    }
}

@Serializable
data class AiVocabularyInsights(
    val advancedWords: List<String> = emptyList(),
    val repeatedWords: List<String> = emptyList(),
    val suggestedAlternatives: List<AiWordAlternative> = emptyList()
) {
    fun toVocabularyInsights(): VocabularyInsights {
        return VocabularyInsights(
            wordsUsed = emptyList(),
            advancedVocabulary = advancedWords,
            repeatedWords = repeatedWords.map { RepeatedWord(it, 2, emptyList()) },
            suggestedAlternatives = suggestedAlternatives.map { 
                WordAlternative(
                    original = it.original,
                    alternatives = it.alternatives.map { alt -> 
                        AlternativeWithContext(alt, "", "", CefrLevel.B1)
                    }
                )
            },
            diversityScore = 0.7,
            complexityScore = 0.5
        )
    }
}

@Serializable
data class AiWordAlternative(
    val original: String,
    val alternatives: List<String>,
    val context: String = ""
)

@Serializable
data class AiStyleSuggestion(
    val type: String,
    val currentVersion: String,
    val suggestedVersion: String,
    val explanation: String
) {
    fun toStyleSuggestion(): StyleSuggestion {
        return StyleSuggestion(
            type = try {
                StyleSuggestionType.valueOf(type.uppercase())
            } catch (_: Exception) { StyleSuggestionType.CLARITY },
            description = explanation,
            currentVersion = currentVersion,
            suggestedVersion = suggestedVersion,
            explanation = explanation
        )
    }
}

@Serializable
data class AiLearningOpportunity(
    val concept: String,
    val explanation: String,
    val examples: List<String>,
    val difficulty: String
) {
    fun toLearningOpportunity(): LearningOpportunity {
        return LearningOpportunity(
            concept = concept,
            conceptType = ConceptType.GRAMMAR_PATTERN,
            currentAttempt = "",
            explanation = explanation,
            examples = examples,
            relatedExercises = emptyList(),
            difficulty = try {
                CefrLevel.valueOf(difficulty.uppercase())
            } catch (_: Exception) { CefrLevel.A2 }
        )
    }
}

@Serializable
data class AiNextWordSuggestion(
    val suggestedWord: String,
    val context: String,
    val confidence: Double
) {
    fun toNextWordSuggestion(): NextWordSuggestion {
        return NextWordSuggestion(
            suggestedWord = suggestedWord,
            wordType = WordSuggestionType.CONTINUATION,
            context = context,
            meaning = "",
            grammaticalCase = null,
            exampleSentence = "",
            difficulty = CefrLevel.A2,
            confidence = confidence.toFloat()
        )
    }
}

@Serializable
data class StreamingSuggestion(
    val type: String,
    val title: String,
    val description: String,
    val priority: Int
)

sealed class SuggestionStreamEvent {
    data class Suggestion(val suggestion: StreamingSuggestion) : SuggestionStreamEvent()
    data class Error(val message: String) : SuggestionStreamEvent()
    object Complete : SuggestionStreamEvent()
}
