package data.db.repository

import app.cash.sqldelight.db.SqlDriver
import data.db.StudyDatabaseSqlDelight
import data.model.StrengthRecord
import kotlinx.datetime.Instant

/**
 * Repository for strength record operations.
 */
class StrengthRecordRepository(driver: SqlDriver) {
    private val database = StudyDatabaseSqlDelight(driver)
    private val queries = database.strengthRecordQueries
    
    fun record(aspect: String, description: String, confidenceScore: Double = 1.0) {
        queries.insertStrength(
            aspect = aspect,
            description = description,
            timestamp = System.currentTimeMillis(),
            confidence_score = confidenceScore
        )
    }
    
    fun getRecent(limit: Long = 20): List<StrengthRecord> {
        return queries.selectRecentStrengths(limit)
            .executeAsList()
            .map { row ->
                StrengthRecord(
                    id = row.id,
                    aspect = row.aspect,
                    description = row.description,
                    timestamp = Instant.fromEpochMilliseconds(row.timestamp),
                    confidenceScore = row.confidence_score
                )
            }
    }
    
    fun getTop(limit: Long = 5): List<Pair<String, Int>> {
        return queries.selectTopStrengths(limit)
            .executeAsList()
            .map { it.aspect to it.count.toInt() }
    }
}
