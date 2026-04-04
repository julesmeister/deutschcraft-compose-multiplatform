package data.db.repository

import app.cash.sqldelight.db.SqlDriver
import data.db.StudyDatabaseSqlDelight
import data.model.StudyTopic
import data.model.TopicCategory
import kotlinx.datetime.Instant

/**
 * Repository for study topic operations.
 */
class StudyTopicRepository(driver: SqlDriver) {
    private val database = StudyDatabaseSqlDelight(driver)
    private val queries = database.studyTopicQueries
    
    fun recordEncounter(name: String, category: TopicCategory) {
        val now = System.currentTimeMillis()
        val existing = queries.selectTopicByName(name).executeAsOneOrNull()
        
        if (existing == null) {
            queries.insertTopic(
                name = name,
                category = category.name,
                first_encountered = now,
                last_encountered = now,
                frequency = 1
            )
        } else {
            queries.updateTopicFrequency(
                last_encountered = now,
                frequency = existing.frequency + 1,
                id = existing.id
            )
        }
    }
    
    fun getTop(limit: Long = 10): List<StudyTopic> {
        return queries.selectTopTopics(limit)
            .executeAsList()
            .map { row ->
                StudyTopic(
                    id = row.id,
                    name = row.name,
                    category = TopicCategory.valueOf(row.category),
                    firstEncountered = Instant.fromEpochMilliseconds(row.first_encountered),
                    lastEncountered = Instant.fromEpochMilliseconds(row.last_encountered),
                    frequency = row.frequency.toInt()
                )
            }
    }
    
    fun getByCategory(category: TopicCategory): List<StudyTopic> {
        return queries.selectTopicsByCategory(category.name)
            .executeAsList()
            .map { row ->
                StudyTopic(
                    id = row.id,
                    name = row.name,
                    category = TopicCategory.valueOf(row.category),
                    firstEncountered = Instant.fromEpochMilliseconds(row.first_encountered),
                    lastEncountered = Instant.fromEpochMilliseconds(row.last_encountered),
                    frequency = row.frequency.toInt()
                )
            }
    }
}
