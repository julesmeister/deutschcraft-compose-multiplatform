package service

import data.model.*
import kotlinx.datetime.*
import kotlinx.serialization.Serializable

/**
 * Data models for WritingAnalytics.
 */

@Serializable
data class WritingSession(
    val id: String,
    val title: String,
    val topic: String?,
    val startTime: Instant,
    val endTime: Instant? = null,
    val wordCount: Int,
    val sentenceAnalyses: List<WritingAnalysis>,
    val suggestionsAccepted: Int,
    val suggestionsRejected: Int,
    val autoFixesApplied: Int,
    val finalText: String? = null
) {
    fun toSummary(): SessionSummary = SessionSummary(
        id = id,
        title = title,
        date = startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString(),
        wordCount = wordCount,
        durationMinutes = endTime?.let { (it - startTime).inWholeMinutes } ?: 0,
        averageScore = if (sentenceAnalyses.isNotEmpty()) sentenceAnalyses.map { it.score }.average() else 0.0
    )
}

@Serializable
data class SessionSummary(
    val id: String,
    val title: String,
    val date: String,
    val wordCount: Int,
    val durationMinutes: Long,
    val averageScore: Double
)

@Serializable
data class DailyWritingStats(
    val date: LocalDate,
    val sessions: Int,
    val totalWords: Int,
    val totalMinutes: Int,
    val sentencesAnalyzed: Int,
    val averageScore: Double,
    val suggestionsAccepted: Int
)

@Serializable
data class WritingProgressReport(
    val period: DatePeriod,
    val totalSessions: Int,
    val totalWords: Int,
    val totalTimeMinutes: Int,
    val averageWpm: Double,
    val levelProgression: List<LevelProgress>,
    val commonErrors: List<ErrorFrequency>,
    val improvementAreas: List<ImprovementArea>,
    val strengths: List<Strength>,
    val suggestionsAcceptedRate: Double,
    val writingStreakDays: Int,
    val favoriteSuggestions: Map<String, Int> = emptyMap(),
    val sessionBreakdown: List<SessionSummary> = emptyList()
)

@Serializable
data class DatePeriod(
    val start: Instant?,
    val end: Instant?
)

@Serializable
data class LevelProgress(
    val date: String,
    val level: CefrLevel,
    val averageScore: Double
)

@Serializable
data class ErrorFrequency(
    val type: String,
    val count: Int
)

@Serializable
data class ImprovementArea(
    val category: String,
    val description: String,
    val frequency: Int,
    val recommendation: String
)

@Serializable
data class Strength(
    val name: String,
    val description: String
)

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastWritingDate: LocalDate?,
    val totalDaysWritten: Int
)
