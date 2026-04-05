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
        difficulty: CefrLevel,
        isUserDifficult: Boolean = false
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
                is_learned = 0L,
                is_user_difficult = if (isUserDifficult) 1L else 0L,
                practice_count = 0L,
                last_practiced = null
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
                    isLearned = row.is_learned == 1L,
                    isUserMarkedDifficult = row.is_user_difficult == 1L,
                    practiceCount = row.practice_count.toInt(),
                    lastPracticed = row.last_practiced?.let { Instant.fromEpochMilliseconds(it) }
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
                    isLearned = row.is_learned == 1L,
                    isUserMarkedDifficult = row.is_user_difficult == 1L,
                    practiceCount = row.practice_count.toInt(),
                    lastPracticed = row.last_practiced?.let { Instant.fromEpochMilliseconds(it) }
                )
            }
    }
    
    fun markAsDifficult(word: String) {
        queries.markAsDifficult(word)
    }
    
    fun unmarkAsDifficult(word: String) {
        queries.unmarkAsDifficult(word)
    }
    
    fun getDifficultWords(limit: Long = 10): List<VocabularyItem> {
        return queries.selectUserDifficultWords(limit)
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
                    isLearned = row.is_learned == 1L,
                    isUserMarkedDifficult = row.is_user_difficult == 1L,
                    practiceCount = row.practice_count.toInt(),
                    lastPracticed = row.last_practiced?.let { Instant.fromEpochMilliseconds(it) }
                )
            }
    }
    
    fun countDifficultWords(): Long {
        return queries.countUserDifficultWords().executeAsOne()
    }
    
    /**
     * Records a practice attempt for a word.
     * If the word has been practiced enough times (default: 3), it will be marked as learned.
     * @return true if the word was auto-marked as learned, false otherwise
     */
    fun recordPractice(word: String, autoLearnThreshold: Int = 3): Boolean {
        val timestamp = System.currentTimeMillis()
        queries.recordPractice(timestamp, word)
        
        // Check if we should auto-mark as learned
        val stats = queries.getWordPracticeStats(word).executeAsOneOrNull()
        if (stats != null && stats.practice_count >= autoLearnThreshold) {
            queries.markAsLearnedWithPractice(stats.practice_count, word)
            return true
        }
        return false
    }
    
    /**
     * Gets practice statistics for a word.
     */
    fun getPracticeStats(word: String): PracticeStats? {
        return queries.getWordPracticeStats(word).executeAsOneOrNull()?.let {
            PracticeStats(
                practiceCount = it.practice_count.toInt(),
                lastPracticed = it.last_practiced?.let { Instant.fromEpochMilliseconds(it) }
            )
        }
    }
    
    /**
     * Manually grades a word as learned with a specific practice count.
     */
    fun gradeWordAsLearned(word: String, practiceCount: Int) {
        queries.markAsLearnedWithPractice(practiceCount.toLong(), word)
    }
    
    data class PracticeStats(
        val practiceCount: Int,
        val lastPracticed: Instant?
    )
    
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
