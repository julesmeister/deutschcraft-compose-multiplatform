package data.db.repository

import app.cash.sqldelight.db.SqlDriver
import data.db.StudyDatabaseSqlDelight
import data.model.ErrorType
import data.model.GrammarMistake
import kotlinx.datetime.Instant

/**
 * Repository for grammar mistake operations.
 */
class GrammarMistakeRepository(driver: SqlDriver) {
    private val database = StudyDatabaseSqlDelight(driver)
    private val queries = database.grammarMistakeQueries
    
    fun record(
        originalText: String,
        correction: String,
        errorType: ErrorType,
        explanation: String,
        studyEntryId: Long
    ) {
        val now = System.currentTimeMillis()
        
        val similarMistake = queries.findSimilarMistake(
            original_text = originalText,
            error_type = errorType.name
        ).executeAsOneOrNull()
        
        if (similarMistake != null) {
            queries.incrementRecurrence(
                recurrence_count = similarMistake.recurrence_count + 1,
                id = similarMistake.id
            )
        } else {
            queries.insertMistake(
                original_text = originalText,
                correction = correction,
                error_type = errorType.name,
                explanation = explanation,
                timestamp = now,
                study_entry_id = studyEntryId,
                recurrence_count = 1
            )
        }
    }
    
    fun getByType(errorType: ErrorType, limit: Long = 100): List<GrammarMistake> {
        return queries.selectMistakesByType(errorType.name, limit)
            .executeAsList()
            .map { row ->
                GrammarMistake(
                    id = row.id,
                    originalText = row.original_text,
                    correction = row.correction,
                    errorType = ErrorType.valueOf(row.error_type),
                    explanation = row.explanation,
                    timestamp = Instant.fromEpochMilliseconds(row.timestamp),
                    studyEntryId = row.study_entry_id,
                    recurrenceCount = row.recurrence_count.toInt()
                )
            }
    }
    
    fun getRecent(limit: Long = 50): List<GrammarMistake> {
        return queries.selectRecentMistakes(limit)
            .executeAsList()
            .map { row ->
                GrammarMistake(
                    id = row.id,
                    originalText = row.original_text,
                    correction = row.correction,
                    errorType = ErrorType.valueOf(row.error_type),
                    explanation = row.explanation,
                    timestamp = Instant.fromEpochMilliseconds(row.timestamp),
                    studyEntryId = row.study_entry_id,
                    recurrenceCount = row.recurrence_count.toInt()
                )
            }
    }
    
    fun getTopTypes(limit: Long = 5): List<Pair<ErrorType, Int>> {
        return queries.selectTopErrorTypes(limit)
            .executeAsList()
            .map { 
                ErrorType.valueOf(it.error_type) to it.count.toInt()
            }
    }
    
    fun getStats(): MistakeStats {
        val total = queries.countTotalMistakes().executeAsOne()
        val recurring = queries.countRecurringMistakes().executeAsOne()
        val byType = queries.selectAllErrorTypesWithCount()
            .executeAsList()
            .associate { ErrorType.valueOf(it.error_type) to it.count.toInt() }
        
        return MistakeStats(total, recurring, byType)
    }
    
    data class MistakeStats(
        val total: Long,
        val recurring: Long,
        val byType: Map<ErrorType, Int>
    )
}
