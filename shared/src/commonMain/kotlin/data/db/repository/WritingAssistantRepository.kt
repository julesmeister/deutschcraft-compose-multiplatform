package data.db.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import data.db.DbPatternType
import data.db.DbSuggestionCategory
import data.db.DbCefrLevel
import data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository for managing writing analysis data, patterns, and suggestion history.
 */
class WritingAssistantRepository(private val database: Database) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // ==================== Writing Analysis ====================
    
    suspend fun saveAnalysis(analysis: WritingAnalysis): Long = withContext(Dispatchers.IO) {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val analysisData = json.encodeToString(analysis)
        
        database.writingAnalysisQueries.insertAnalysis(
            sentence = analysis.sentence,
            timestamp = timestamp,
            sentence_level = analysis.sentenceLevel.name,
            score = analysis.score,
            analysis_data = analysisData
        )
        
        database.writingAnalysisQueries.lastInsertRowId().executeAsOne()
    }
    
    fun getRecentAnalyses(limit: Long = 50): Flow<List<WritingAnalysis>> {
        return database.writingAnalysisQueries
            .getRecentAnalyses(limit)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    json.decodeFromString<WritingAnalysis>(row.analysis_data)
                }
            }
    }
    
    suspend fun getAnalysisById(id: Long): WritingAnalysis? = withContext(Dispatchers.IO) {
        database.writingAnalysisQueries
            .getAnalysisById(id)
            .executeAsOneOrNull()
            ?.let { json.decodeFromString<WritingAnalysis>(it.analysis_data) }
    }
    
    suspend fun deleteOldAnalyses(olderThan: Instant) = withContext(Dispatchers.IO) {
        val cutoffMillis = olderThan.toEpochMilliseconds()
        database.writingAnalysisQueries.deleteOldAnalyses(cutoffMillis)
    }
    
    // ==================== Writing Patterns ====================
    
    suspend fun saveOrUpdatePattern(pattern: WritingPattern): Long = withContext(Dispatchers.IO) {
        val existing = database.writingPatternQueries
            .getPatternByTextAndType(pattern.pattern, pattern.patternType.name)
            .executeAsOneOrNull()
        
        val now = Clock.System.now().toEpochMilliseconds()
        
        if (existing != null) {
            // Update existing pattern
            database.writingPatternQueries.updatePattern(
                id = existing.id,
                frequency = existing.frequency + pattern.frequency,
                last_seen = now
            )
            existing.id
        } else {
            // Insert new pattern
            database.writingPatternQueries.insertPattern(
                pattern_type = pattern.patternType.name,
                pattern = pattern.pattern,
                frequency = pattern.frequency,
                first_seen = pattern.firstSeen.toEpochMilliseconds(),
                last_seen = pattern.lastSeen.toEpochMilliseconds(),
                examples_json = json.encodeToString(pattern.examples)
            )
            database.writingPatternQueries.lastInsertRowId().executeAsOne()
        }
    }
    
    fun getPatternsByType(type: PatternType, limit: Long = 20): Flow<List<WritingPattern>> {
        return database.writingPatternQueries
            .getPatternsByType(type.name, limit)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    WritingPattern(
                        id = row.id,
                        patternType = PatternType.valueOf(row.pattern_type),
                        pattern = row.pattern,
                        frequency = row.frequency.toInt(),
                        firstSeen = Instant.fromEpochMilliseconds(row.first_seen),
                        lastSeen = Instant.fromEpochMilliseconds(row.last_seen),
                        examples = json.decodeFromString(row.examples_json ?: "[]")
                    )
                }
            }
    }
    
    fun getMostFrequentPatterns(limit: Long = 20): Flow<List<WritingPattern>> {
        return database.writingPatternQueries
            .getMostFrequentPatterns(limit)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    WritingPattern(
                        id = row.id,
                        patternType = PatternType.valueOf(row.pattern_type),
                        pattern = row.pattern,
                        frequency = row.frequency.toInt(),
                        firstSeen = Instant.fromEpochMilliseconds(row.first_seen),
                        lastSeen = Instant.fromEpochMilliseconds(row.last_seen),
                        examples = json.decodeFromString(row.examples_json ?: "[]")
                    )
                }
            }
    }
    
    suspend fun getFavoriteTransitionWords(limit: Long = 10): List<String> = withContext(Dispatchers.IO) {
        database.writingPatternQueries
            .getPatternsByType(PatternType.TRANSITION_WORD.name, limit)
            .executeAsList()
            .map { it.pattern }
    }
    
    // ==================== Suggestion History ====================
    
    suspend fun recordSuggestion(suggestion: QuickSuggestion, context: String, wasApplied: Boolean): Long = withContext(Dispatchers.IO) {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        
        database.suggestionHistoryQueries.insertSuggestion(
            suggestion_type = suggestion.type.name,
            suggestion_text = suggestion.title,
            was_applied = if (wasApplied) 1 else 0,
            context = context,
            timestamp = timestamp
        )
        
        database.suggestionHistoryQueries.lastInsertRowId().executeAsOne()
    }
    
    fun getSuggestionHistory(limit: Long = 100): Flow<List<SuggestionRecord>> {
        return database.suggestionHistoryQueries
            .getRecentSuggestions(limit)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    SuggestionRecord(
                        id = row.id,
                        suggestionType = SuggestionCategory.valueOf(row.suggestion_type),
                        suggestionText = row.suggestion_text,
                        wasApplied = row.was_applied != 0L,
                        context = row.context ?: "",
                        timestamp = Instant.fromEpochMilliseconds(row.timestamp)
                    )
                }
            }
    }
    
    suspend fun getMostHelpfulSuggestionTypes(limit: Long = 10): List<SuggestionCategory> = withContext(Dispatchers.IO) {
        database.suggestionHistoryQueries
            .getMostAppliedSuggestions(limit)
            .executeAsList()
            .mapNotNull { row ->
                try {
                    SuggestionCategory.valueOf(row.suggestion_type)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
    }
    
    suspend fun getSuggestionApplicationRate(): Double = withContext(Dispatchers.IO) {
        val total = database.suggestionHistoryQueries.countAllSuggestions().executeAsOne()
        val applied = database.suggestionHistoryQueries.countAppliedSuggestions().executeAsOne()
        
        if (total > 0) applied.toDouble() / total else 0.0
    }
    
    // ==================== Statistics ====================
    
    suspend fun getWritingStatistics(): WritingStatistics = withContext(Dispatchers.IO) {
        val totalAnalyses = database.writingAnalysisQueries.countAnalyses().executeAsOne()
        val avgScore = database.writingAnalysisQueries.getAverageScore().executeAsOne() ?: 0.0
        
        // Get level distribution
        val levelCounts = database.writingAnalysisQueries
            .getLevelDistribution()
            .executeAsList()
            .associate { it.sentence_level to it.COUNT }
        
        WritingStatistics(
            totalAnalyses = totalAnalyses.toInt(),
            averageScore = avgScore,
            levelDistribution = levelCounts.mapKeys { CefrLevel.valueOf(it.key) },
            totalPatterns = database.writingPatternQueries.countPatterns().executeAsOne().toInt(),
            totalSuggestions = database.suggestionHistoryQueries.countAllSuggestions().executeAsOne().toInt()
        )
    }
    
    // ==================== Cleanup ====================
    
    suspend fun clearOldData(olderThanDays: Int = 30) = withContext(Dispatchers.IO) {
        val cutoff = Clock.System.now().minus(kotlinx.datetime.DateTimePeriod(days = olderThanDays))
        val cutoffMillis = cutoff.toEpochMilliseconds()
        
        database.writingAnalysisQueries.deleteOldAnalyses(cutoffMillis)
        database.suggestionHistoryQueries.deleteOldSuggestions(cutoffMillis)
    }
}

/**
 * Data class for suggestion history records.
 */
data class SuggestionRecord(
    val id: Long,
    val suggestionType: SuggestionCategory,
    val suggestionText: String,
    val wasApplied: Boolean,
    val context: String,
    val timestamp: Instant
)

/**
 * Statistics about user's writing patterns and assistant usage.
 */
data class WritingStatistics(
    val totalAnalyses: Int,
    val averageScore: Double,
    val levelDistribution: Map<CefrLevel, Long>,
    val totalPatterns: Int,
    val totalSuggestions: Int
)
