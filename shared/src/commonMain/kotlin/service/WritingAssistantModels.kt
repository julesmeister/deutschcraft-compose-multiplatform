package service

import data.model.*
import kotlinx.datetime.Instant

/**
 * Data models for WritingAssistantService.
 */

data class TextRevision(
    val originalText: String,
    val newText: String,
    val cursorPosition: Int,
    val newCursorPosition: Int,
    val action: SuggestionAction,
    val timestamp: Instant,
    val direction: RevisionDirection = RevisionDirection.UNDO,
    val isAutoFix: Boolean = false
)

enum class RevisionDirection { UNDO, REDO }

data class AutoApplyResult(
    val text: String,
    val appliedFixes: List<SentenceGrammarError>
)

data class DocumentAnalysis(
    val totalSentences: Int,
    val averageSentenceLength: Double,
    val repeatedPhrases: List<RepeatedPhrase>,
    val consistencyIssues: List<ConsistencyIssue>,
    val flowScore: Double,
    val overallLevel: CefrLevel,
    val suggestions: List<DocumentSuggestion>
)

data class RepeatedPhrase(val phrase: String, val count: Int)

data class ConsistencyIssue(
    val type: String,
    val description: String,
    val severity: ConsistencySeverity
)

enum class ConsistencySeverity { INFO, WARNING, ERROR }

data class DocumentSuggestion(
    val type: DocumentSuggestionType,
    val description: String
)

enum class DocumentSuggestionType { VARIETY, CLARITY, STRUCTURE, TONE }
