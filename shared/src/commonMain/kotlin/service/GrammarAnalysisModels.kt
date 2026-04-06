package service

import data.model.*
import kotlinx.datetime.Instant

/**
 * Data models for GrammarAnalysisService.
 */

data class GrammarAnalysisResult(
    val entryId: Long,
    val analysis: GrammarAnalysisResponse,
    val progressStats: UserProgressStats,
    val recurringMistakes: List<GrammarError>
)

data class QuickAnalysis(
    val correctedText: String,
    val errorCount: Int,
    val topErrors: List<Pair<ErrorType, Int>>,
    val cefrLevel: CefrLevel
)

data class MistakePatterns(
    val mostFrequentTypes: List<Pair<ErrorType, Int>>,
    val recurringMistakes: List<GrammarMistake>,
    val recentMistakes: List<GrammarMistake>,
    val suggestion: String
)

data class StudyHistoryEntry(
    val id: Long,
    val preview: String,
    val type: EntryType,
    val timestamp: Instant,
    val topic: String?,
    val errorCount: Int,
    val cefrLevel: CefrLevel?
)

data class LearningRecommendation(
    val type: RecommendationType,
    val title: String,
    val description: String,
    val priority: Priority,
    val estimatedTime: String
)

enum class RecommendationType {
    FOCUSED_PRACTICE,
    REVIEW,
    VOCABULARY,
    GRAMMAR_LESSON,
    WRITING_PROMPT
}

enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}
