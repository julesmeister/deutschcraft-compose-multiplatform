package data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Structured AI response for grammar and writing analysis.
 * This ensures consistent, parseable responses from the AI.
 */
@Serializable
data class GrammarAnalysisResponse(
    @SerialName("corrected_text")
    val correctedText: String,
    
    @SerialName("grammar_errors")
    val grammarErrors: List<GrammarError> = emptyList(),
    
    @SerialName("strengths")
    val strengths: List<WritingStrength> = emptyList(),
    
    @SerialName("stats")
    val stats: WritingStats,
    
    @SerialName("suggestions")
    val suggestions: List<WritingSuggestion> = emptyList(),
    
    @SerialName("learning_topics")
    val learningTopics: List<String> = emptyList()
)

@Serializable
data class GrammarError(
    @SerialName("original_text")
    val originalText: String,
    
    @SerialName("correction")
    val correction: String,
    
    @SerialName("error_type")
    val errorType: ErrorType,
    
    @SerialName("explanation")
    val explanation: String,
    
    @SerialName("position_start")
    val positionStart: Int = -1,
    
    @SerialName("position_end")
    val positionEnd: Int = -1,
    
    @SerialName("severity")
    val severity: ErrorSeverity = ErrorSeverity.MEDIUM
)

@Serializable
enum class ErrorType {
    @SerialName("grammar") GRAMMAR,
    @SerialName("spelling") SPELLING,
    @SerialName("punctuation") PUNCTUATION,
    @SerialName("word_order") WORD_ORDER,
    @SerialName("verb_conjugation") VERB_CONJUGATION,
    @SerialName("case") CASE,
    @SerialName("gender") GENDER,
    @SerialName("tense") TENSE,
    @SerialName("article") ARTICLE,
    @SerialName("preposition") PREPOSITION,
    @SerialName("style") STYLE,
    @SerialName("other") OTHER
}

@Serializable
enum class ErrorSeverity {
    @SerialName("low") LOW,
    @SerialName("medium") MEDIUM,
    @SerialName("high") HIGH,
    @SerialName("critical") CRITICAL
}

@Serializable
data class WritingStrength(
    @SerialName("aspect")
    val aspect: String,
    
    @SerialName("description")
    val description: String,
    
    @SerialName("examples")
    val examples: List<String> = emptyList()
)

@Serializable
data class WritingStats(
    @SerialName("word_count")
    val wordCount: Int,
    
    @SerialName("sentence_count")
    val sentenceCount: Int,
    
    @SerialName("vocabulary_diversity_score")
    val vocabularyDiversityScore: Double,
    
    @SerialName("complexity_score")
    val complexityScore: Double,
    
    @SerialName("estimated_cefr_level")
    val estimatedCefrLevel: CefrLevel,
    
    @SerialName("error_count")
    val errorCount: Int = 0
)

@Serializable
enum class CefrLevel {
    @SerialName("A1") A1,
    @SerialName("A2") A2,
    @SerialName("B1") B1,
    @SerialName("B2") B2,
    @SerialName("C1") C1,
    @SerialName("C2") C2
}

@Serializable
data class WritingSuggestion(
    @SerialName("type")
    val type: SuggestionType,
    
    @SerialName("description")
    val description: String,
    
    @SerialName("example_implementation")
    val exampleImplementation: String? = null
)

@Serializable
enum class SuggestionType {
    @SerialName("vocabulary") VOCABULARY,
    @SerialName("structure") STRUCTURE,
    @SerialName("style") STYLE,
    @SerialName("clarity") CLARITY,
    @SerialName("formality") FORMALITY
}

/**
 * Aggregated statistics for tracking user progress over time.
 */
@Serializable
data class UserProgressStats(
    @SerialName("total_writing_sessions")
    val totalWritingSessions: Int,
    
    @SerialName("total_words_written")
    val totalWordsWritten: Int,
    
    @SerialName("error_frequency_by_type")
    val errorFrequencyByType: Map<ErrorType, Int>,
    
    @SerialName("average_cefr_progression")
    val averageCefrProgression: Map<String, CefrLevel>,
    
    @SerialName("top_strengths")
    val topStrengths: List<String>,
    
    @SerialName("improvement_areas")
    val improvementAreas: List<String>
)
