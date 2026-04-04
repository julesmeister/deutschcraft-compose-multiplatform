package data.db.repository

import app.cash.sqldelight.db.SqlDriver
import data.db.StudyDatabaseSqlDelight
import data.model.CefrLevel
import data.model.VocabularyItem
import kotlinx.datetime.Instant

/**
 * Repository for vocabulary operations.
 */
class VocabularyRepository(driver: SqlDriver) {
    private val database = StudyDatabaseSqlDelight(driver)
    private val queries = database.vocabularyQueries
    
    fun record(
        word: String,
        context: String,
        translation: String?,
        difficulty: CefrLevel
    ) {
        val now = System.currentTimeMillis()
        val existing = queries.selectVocabularyByWord(word).executeAsOneOrNull()
        
        if (existing == null) {
            queries.insertVocabulary(
                word = word,
                context = context,
                translation = translation,
                difficulty = difficulty.name,
                first_seen = now,
                encounter_count = 1,
                is_learned = 0L
            )
        } else {
            queries.incrementEncounter(
                encounter_count = existing.encounter_count + 1,
                id = existing.id
            )
        }
    }
    
    fun markAsLearned(word: String) {
        queries.markAsLearned(word)
    }
    
    fun getByDifficulty(difficulty: CefrLevel, limit: Long = 100): List<VocabularyItem> {
        return queries.selectByDifficulty(difficulty.name, limit)
            .executeAsList()
            .map { row ->
                VocabularyItem(
                    id = row.id,
                    word = row.word,
                    context = row.context,
                    translation = row.translation,
                    difficulty = CefrLevel.valueOf(row.difficulty),
                    firstSeen = Instant.fromEpochMilliseconds(row.first_seen),
                    encounterCount = row.encounter_count.toInt(),
                    isLearned = row.is_learned == 1L
                )
            }
    }
    
    fun getUnlearned(limit: Long = 100): List<VocabularyItem> {
        return queries.selectUnlearned(limit)
            .executeAsList()
            .map { row ->
                VocabularyItem(
                    id = row.id,
                    word = row.word,
                    context = row.context,
                    translation = row.translation,
                    difficulty = CefrLevel.valueOf(row.difficulty),
                    firstSeen = Instant.fromEpochMilliseconds(row.first_seen),
                    encounterCount = row.encounter_count.toInt(),
                    isLearned = row.is_learned == 1L
                )
            }
    }
    
    fun getStats(): VocabularyStats {
        val total = queries.countTotal().executeAsOne()
        val learned = queries.countLearned().executeAsOne()
        return VocabularyStats(total.toInt(), learned.toInt())
    }
    
    data class VocabularyStats(
        val totalWords: Int,
        val learnedWords: Int
    ) {
        val progressPercentage: Int
            get() = if (totalWords > 0) (learnedWords * 100 / totalWords) else 0
    }

    // Data cleanup methods
    fun deleteLearnedVocabulary(): Long {
        val count = queries.countLearned().executeAsOne()
        queries.deleteLearnedVocabulary()
        return count
    }

    fun deleteVocabularyBeforeDate(timestamp: Long): Long {
        val count = queries.countVocabularyBeforeDate(timestamp).executeAsOne() ?: 0L
        queries.deleteVocabularyBeforeDate(timestamp)
        return count
    }

    fun countVocabularyBeforeDate(timestamp: Long): Long {
        return queries.countVocabularyBeforeDate(timestamp).executeAsOne() ?: 0L
    }

    fun resetVocabulary(): Long {
        val count = queries.countTotal().executeAsOne()
        queries.resetVocabulary()
        return count
    }
}
