package service

import data.model.*
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Writing progress tracking and report generation.
 * Tracks metrics, generates reports, and exports data.
 */
class WritingAnalytics(
    private val service: WritingAssistantService
) {
    private val sessionHistory = mutableListOf<WritingSession>()
    private val dailyStats = mutableMapOf<LocalDate, DailyWritingStats>()
    private var currentSession: WritingSession? = null
    
    private val json = Json { 
        prettyPrint = true 
        ignoreUnknownKeys = true
    }
    
    /**
     * Start tracking a new writing session.
     */
    fun startSession(title: String = "Untitled", topic: String? = null) {
        currentSession = WritingSession(
            id = generateSessionId(),
            title = title,
            topic = topic,
            startTime = Clock.System.now(),
            wordCount = 0,
            sentenceAnalyses = mutableListOf(),
            suggestionsAccepted = 0,
            suggestionsRejected = 0,
            autoFixesApplied = 0
        )
    }
    
    /**
     * End current session and save stats.
     */
    fun endSession(finalText: String) {
        currentSession?.let { session ->
            val endedSession = session.copy(
                endTime = Clock.System.now(),
                wordCount = finalText.split(Regex("\\s+")).size,
                finalText = finalText
            )
            sessionHistory.add(endedSession)
            updateDailyStats(endedSession)
            currentSession = null
        }
    }
    
    /**
     * Record an accepted suggestion.
     */
    fun recordSuggestionAccepted(suggestion: QuickSuggestion) {
        currentSession?.let { session ->
            val updatedAnalyses = session.sentenceAnalyses.toMutableList()
            currentSession = session.copy(suggestionsAccepted = session.suggestionsAccepted + 1)
        }
    }
    
    /**
     * Record an auto-fix being applied.
     */
    fun recordAutoFix(error: SentenceGrammarError) {
        currentSession?.let { session ->
            currentSession = session.copy(autoFixesApplied = session.autoFixesApplied + 1)
        }
    }
    
    /**
     * Record sentence analysis for tracking.
     */
    fun recordAnalysis(analysis: WritingAnalysis) {
        currentSession?.let { session ->
            val analyses = session.sentenceAnalyses.toMutableList().apply { add(analysis) }
            currentSession = session.copy(sentenceAnalyses = analyses)
        }
    }
    
    /**
     * Generate comprehensive progress report.
     */
    fun generateProgressReport(
        startDate: Instant? = null,
        endDate: Instant? = null
    ): WritingProgressReport {
        val sessions = sessionHistory.filter { session ->
            val afterStart = startDate?.let { session.startTime >= it } ?: true
            val beforeEnd = endDate?.let { session.endTime?.let { end -> end <= it } ?: true } ?: true
            afterStart && beforeEnd
        }
        
        if (sessions.isEmpty()) {
            return WritingProgressReport(
                period = DatePeriod(startDate, endDate),
                totalSessions = 0,
                totalWords = 0,
                totalTimeMinutes = 0,
                averageWpm = 0.0,
                levelProgression = emptyList(),
                commonErrors = emptyList(),
                improvementAreas = emptyList(),
                strengths = emptyList(),
                suggestionsAcceptedRate = 0.0,
                writingStreakDays = calculateStreak()
            )
        }
        
        val totalWords = sessions.sumOf { it.wordCount }
        val totalTimeMs = sessions.mapNotNull { 
            it.endTime?.minus(it.startTime) 
        }.sumOf { it.inWholeMilliseconds }
        val totalTimeMinutes = totalTimeMs / 60000.0
        
        // Calculate all metrics
        val allAnalyses = sessions.flatMap { it.sentenceAnalyses }
        val levelProgression = calculateLevelProgression(sessions)
        val commonErrors = findCommonErrors(allAnalyses)
        val (improvements, strengths) = analyzeImprovements(allAnalyses)
        
        val totalSuggestions = sessions.sumOf { it.suggestionsAccepted + it.suggestionsRejected }
        val acceptedSuggestions = sessions.sumOf { it.suggestionsAccepted }
        
        return WritingProgressReport(
            period = DatePeriod(startDate, endDate),
            totalSessions = sessions.size,
            totalWords = totalWords,
            totalTimeMinutes = totalTimeMinutes.toInt(),
            averageWpm = if (totalTimeMinutes > 0) totalWords / totalTimeMinutes else 0.0,
            levelProgression = levelProgression,
            commonErrors = commonErrors,
            improvementAreas = improvements,
            strengths = strengths,
            suggestionsAcceptedRate = if (totalSuggestions > 0) acceptedSuggestions.toDouble() / totalSuggestions else 0.0,
            writingStreakDays = calculateStreak(),
            favoriteSuggestions = findFavoriteSuggestionTypes(sessions),
            sessionBreakdown = sessions.map { it.toSummary() }
        )
    }
    
    /**
     * Export report to JSON.
     */
    fun exportToJson(report: WritingProgressReport): String {
        return json.encodeToString(report)
    }
    
    /**
     * Export report to Markdown for human reading.
     */
    fun exportToMarkdown(report: WritingProgressReport): String {
        return buildString {
            appendLine("# German Writing Progress Report")
            appendLine()
            appendLine("**Period:** ${formatPeriod(report.period)}")
            appendLine("**Generated:** ${Clock.System.now()}")
            appendLine()
            
            appendLine("## Summary")
            appendLine("- **Total Sessions:** ${report.totalSessions}")
            appendLine("- **Total Words:** ${report.totalWords}")
            appendLine("- **Total Time:** ${report.totalTimeMinutes} minutes")
            appendLine("- **Average WPM:** ${String.format("%.1f", report.averageWpm)}")
            appendLine("- **Writing Streak:** ${report.writingStreakDays} days")
            appendLine("- **Suggestion Acceptance:** ${String.format("%.1f", report.suggestionsAcceptedRate * 100)}%")
            appendLine()
            
            if (report.levelProgression.isNotEmpty()) {
                appendLine("## CEFR Level Progression")
                report.levelProgression.forEach { level ->
                    appendLine("- **${level.date}:** ${level.level} (${level.averageScore}/100)")
                }
                appendLine()
            }
            
            if (report.strengths.isNotEmpty()) {
                appendLine("## Strengths")
                report.strengths.forEach { strength ->
                    appendLine("- ${strength.description}")
                }
                appendLine()
            }
            
            if (report.improvementAreas.isNotEmpty()) {
                appendLine("## Areas for Improvement")
                report.improvementAreas.forEach { area ->
                    appendLine("- **${area.category}:** ${area.description}")
                    appendLine("  - Frequency: ${area.frequency}x")
                    if (area.recommendation.isNotEmpty()) {
                        appendLine("  - Tip: ${area.recommendation}")
                    }
                }
                appendLine()
            }
            
            if (report.commonErrors.isNotEmpty()) {
                appendLine("## Most Common Error Types")
                report.commonErrors.take(5).forEach { error ->
                    appendLine("- **${error.type}:** ${error.count} occurrences")
                }
                appendLine()
            }
            
            appendLine("---")
            appendLine("*Keep practicing! Consistency is key to language mastery.*")
        }
    }
    
    /**
     * Export session data to CSV format.
     */
    fun exportToCsv(): String {
        val header = "Session ID,Title,Topic,Start Time,End Time,Words,Sentences,Suggestions Accepted,Suggestions Rejected,Auto Fixes,Average Score,Level"
        
        val rows = sessionHistory.map { session ->
            val analyses = session.sentenceAnalyses
            val avgScore = if (analyses.isNotEmpty()) analyses.map { it.score }.average() else 0.0
            val level = if (analyses.isNotEmpty()) analyses.last().sentenceLevel else CefrLevel.A1
            
            buildString {
                append("${session.id},")
                append("\"${session.title}\",")
                append("\"${session.topic ?: ""}\",")
                append("${session.startTime},")
                append("${session.endTime ?: "ongoing"},")
                append("${session.wordCount},")
                append("${analyses.size},")
                append("${session.suggestionsAccepted},")
                append("${session.suggestionsRejected},")
                append("${session.autoFixesApplied},")
                append("${String.format("%.1f", avgScore)},")
                append("${level}")
            }
        }
        
        return (listOf(header) + rows).joinToString("\n")
    }
    
    /**
     * Get quick daily stats.
     */
    fun getDailyStats(date: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date): DailyWritingStats? {
        return dailyStats[date]
    }
    
    /**
     * Get streak information.
     */
    fun getStreakInfo(): StreakInfo {
        return StreakInfo(
            currentStreak = calculateStreak(),
            longestStreak = calculateLongestStreak(),
            lastWritingDate = sessionHistory.lastOrNull()?.endTime?.toLocalDateTime(TimeZone.currentSystemDefault())?.date,
            totalDaysWritten = dailyStats.size
        )
    }
    
    // Private helper methods
    
    private fun updateDailyStats(session: WritingSession) {
        val date = session.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val existing = dailyStats[date] ?: DailyWritingStats(date, 0, 0, 0, 0, 0.0, 0)
        
        val sessionMinutes = session.endTime?.let { 
            (it - session.startTime).inWholeMinutes 
        } ?: 0
        
        dailyStats[date] = existing.copy(
            sessions = existing.sessions + 1,
            totalWords = existing.totalWords + session.wordCount,
            totalMinutes = existing.totalMinutes + sessionMinutes.toInt(),
            sentencesAnalyzed = existing.sentencesAnalyzed + session.sentenceAnalyses.size,
            suggestionsAccepted = existing.suggestionsAccepted + session.suggestionsAccepted,
            averageScore = ((existing.averageScore * existing.sessions) + 
                session.sentenceAnalyses.map { it.score }.averageOrZero()) / (existing.sessions + 1)
        )
    }
    
    private fun calculateLevelProgression(sessions: List<WritingSession>): List<LevelProgress> {
        return sessions
            .groupBy { it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date }
            .map { (date, daySessions) ->
                val allAnalyses = daySessions.flatMap { it.sentenceAnalyses }
                val avgScore = allAnalyses.map { it.score }.averageOrZero()
                val avgLevel = if (allAnalyses.isNotEmpty()) {
                    val avgOrdinal = allAnalyses.map { it.sentenceLevel.ordinal }.average()
                    CefrLevel.entries.getOrNull(avgOrdinal.toInt()) ?: CefrLevel.A1
                } else CefrLevel.A1
                
                LevelProgress(date.toString(), avgLevel, avgScore)
            }
            .sortedBy { it.date }
    }
    
    private fun findCommonErrors(analyses: List<WritingAnalysis>): List<ErrorFrequency> {
        return analyses
            .flatMap { it.grammarErrors }
            .groupBy { it.errorType }
            .map { (type, errors) -> ErrorFrequency(type.name, errors.size) }
            .sortedByDescending { it.count }
    }
    
    private fun analyzeImprovements(analyses: List<WritingAnalysis>): Pair<List<ImprovementArea>, List<Strength>> {
        val errors = analyses.flatMap { it.grammarErrors }
        
        val improvements = errors
            .groupBy { it.errorType }
            .map { (type, errs) ->
                ImprovementArea(
                    category = type.name.lowercase().replace("_", " "),
                    description = "Recurring ${type.name.lowercase()} errors",
                    frequency = errs.size,
                    recommendation = generateRecommendation(type)
                )
            }
            .sortedByDescending { it.frequency }
            .take(3)
        
        val strengths = mutableListOf<Strength>()
        
        // Detect strengths
        val vocabDiversity = analyses.mapNotNull { 
            it.vocabularyInsights?.diversityScore 
        }.averageOrZero()
        
        if (vocabDiversity > 0.7) {
            strengths.add(Strength("Rich vocabulary variety", "You use a diverse range of words"))
        }
        
        val avgScore = analyses.map { it.score }.averageOrZero()
        if (avgScore > 80) {
            strengths.add(Strength("Strong grammar foundation", "Consistently high accuracy"))
        }
        
        val complexSentences = analyses.count { 
            it.structureAnalysis?.sentenceType == SentenceType.COMPLEX 
        }
        if (complexSentences > analyses.size / 3) {
            strengths.add(Strength("Complex sentence structures", "Comfortable with advanced grammar"))
        }
        
        return improvements to strengths
    }
    
    private fun findFavoriteSuggestionTypes(sessions: List<WritingSession>): Map<String, Int> {
        // This would need to track suggestion types - simplified for now
        return emptyMap()
    }
    
    private fun calculateStreak(): Int {
        if (dailyStats.isEmpty()) return 0
        
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        var streak = 0
        var checkDate = today
        
        while (dailyStats.containsKey(checkDate) || checkDate == today) {
            if (dailyStats.containsKey(checkDate)) streak++
            checkDate = checkDate.minus(1, DateTimeUnit.DAY)
        }
        
        return streak
    }
    
    private fun calculateLongestStreak(): Int {
        if (dailyStats.isEmpty()) return 0
        
        val sortedDates = dailyStats.keys.sorted()
        var maxStreak = 0
        var currentStreak = 0
        var previousDate: LocalDate? = null
        
        for (date in sortedDates) {
            if (previousDate == null || date == previousDate.plus(1, DateTimeUnit.DAY)) {
                currentStreak++
            } else {
                maxStreak = maxOf(maxStreak, currentStreak)
                currentStreak = 1
            }
            previousDate = date
        }
        
        return maxOf(maxStreak, currentStreak)
    }
    
    private fun generateRecommendation(type: ErrorType): String {
        return when (type) {
            ErrorType.VERB_CONJUGATION -> "Practice verb conjugations with online exercises"
            ErrorType.WORD_ORDER -> "Remember: verb second in statements, final in subordinate clauses"
            ErrorType.CASE -> "Review the four German cases and their prepositions"
            ErrorType.GENDER -> "Learn noun genders with their articles together"
            ErrorType.SPELLING -> "Use spell-check and read your text aloud"
            else -> "Review this grammar topic in your textbook"
        }
    }
    
    private fun generateSessionId(): String {
        return "${Clock.System.now().toEpochMilliseconds()}"
    }
    
    private fun formatPeriod(period: DatePeriod): String {
        return when {
            period.start == null && period.end == null -> "All Time"
            period.start == null -> "Until ${period.end}"
            period.end == null -> "From ${period.start}"
            else -> "${period.start} to ${period.end}"
        }
    }
    
    private fun List<Double>.averageOrZero(): Double = 
        if (isEmpty()) 0.0 else average()
}

// Data classes

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
        date = startTime.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.toString(),
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
