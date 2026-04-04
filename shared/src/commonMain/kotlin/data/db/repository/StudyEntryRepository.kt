package data.db.repository

import app.cash.sqldelight.db.SqlDriver
import data.db.DbEntryType
import data.db.StudyDatabaseSqlDelight
import data.db.TableNames
import data.model.EntryType
import data.model.StudyEntry
import kotlinx.datetime.Instant

/**
 * Repository for study entry operations.
 */
class StudyEntryRepository(driver: SqlDriver) {
    private val database = StudyDatabaseSqlDelight(driver)
    private val queries = database.studyEntryQueries
    
    fun insert(content: String, type: EntryType, topic: String? = null): Long {
        queries.insertStudyEntry(
            content = content,
            type = type.name,
            timestamp = System.currentTimeMillis(),
            topic = topic,
            corrected_content = null,
            analysis_json = null
        )
        return queries.lastInsertRowId().executeAsOne()
    }
    
    fun updateAnalysis(entryId: Long, correctedContent: String, analysisJson: String) {
        queries.updateAnalysis(
            corrected_content = correctedContent,
            analysis_json = analysisJson,
            id = entryId
        )
    }
    
    fun getRecent(limit: Long = 50): List<StudyEntry> {
        return queries.selectRecentEntries(limit)
            .executeAsList()
            .map { row ->
                StudyEntry(
                    id = row.id,
                    content = row.content,
                    type = EntryType.valueOf(row.type),
                    timestamp = Instant.fromEpochMilliseconds(row.timestamp),
                    topic = row.topic,
                    correctedContent = row.corrected_content,
                    analysisJson = row.analysis_json
                )
            }
    }
    
    fun getByTopic(topic: String): List<StudyEntry> {
        return queries.selectEntriesByTopic(topic)
            .executeAsList()
            .map { row ->
                StudyEntry(
                    id = row.id,
                    content = row.content,
                    type = EntryType.valueOf(row.type),
                    timestamp = Instant.fromEpochMilliseconds(row.timestamp),
                    topic = row.topic,
                    correctedContent = row.corrected_content,
                    analysisJson = row.analysis_json
                )
            }
    }
    
    fun getCount(): Long {
        return queries.countEntries().executeAsOne()
    }
    
    fun getTotalWords(): Long {
        return queries.sumWordCount().executeAsOne() ?: 0L
    }

    // Data cleanup methods
    fun deleteEntriesBeforeDate(timestamp: Long): Long {
        val count = countEntriesBeforeDate(timestamp)
        queries.deleteEntriesBeforeDate(timestamp)
        return count
    }

    fun deleteEntriesByTopic(topic: String): Long {
        val count = countEntriesByTopic(topic)
        queries.deleteEntriesByTopic(topic)
        return count
    }

    fun countEntriesBeforeDate(timestamp: Long): Long {
        return queries.countEntriesBeforeDate(timestamp).executeAsOne() ?: 0L
    }

    fun countEntriesByTopic(topic: String): Long {
        return queries.countEntriesByTopic(topic).executeAsOne() ?: 0L
    }

    fun getOldestEntryDate(): Long? {
        return queries.getOldestEntryDate().executeAsOne()
    }
}
