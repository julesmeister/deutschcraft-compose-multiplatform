package data.db.dao

import androidx.room.*
import data.db.entity.*
import kotlinx.datetime.Instant

@Dao
interface StudyEntryDao {
    @Insert
    suspend fun insert(entry: StudyEntryEntity): Long

    @Query("SELECT * FROM study_entries ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentEntries(limit: Int): List<StudyEntryEntity>

    @Query("SELECT * FROM study_entries WHERE topic = :topic ORDER BY timestamp DESC")
    suspend fun getEntriesByTopic(topic: String): List<StudyEntryEntity>

    @Query("UPDATE study_entries SET correctedContent = :correctedContent, analysisJson = :analysisJson WHERE id = :id")
    suspend fun updateAnalysis(id: Long, correctedContent: String, analysisJson: String)

    @Query("SELECT COUNT(*) FROM study_entries")
    suspend fun countEntries(): Long

    @Query("SELECT * FROM study_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): StudyEntryEntity?

    @Delete
    suspend fun delete(entry: StudyEntryEntity)
}

@Dao
interface GrammarMistakeDao {
    @Insert
    suspend fun insert(mistake: GrammarMistakeEntity): Long

    @Query("SELECT * FROM grammar_mistakes ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMistakes(limit: Int): List<GrammarMistakeEntity>

    @Query("SELECT * FROM grammar_mistakes WHERE errorType = :errorType ORDER BY recurrenceCount DESC, timestamp DESC LIMIT :limit")
    suspend fun getMistakesByType(errorType: String, limit: Int): List<GrammarMistakeEntity>

    @Query("SELECT * FROM grammar_mistakes WHERE originalText = :originalText AND errorType = :errorType LIMIT 1")
    suspend fun findSimilarMistake(originalText: String, errorType: String): GrammarMistakeEntity?

    @Query("UPDATE grammar_mistakes SET recurrenceCount = :recurrenceCount WHERE id = :id")
    suspend fun updateRecurrenceCount(id: Long, recurrenceCount: Int)

    @Query("SELECT errorType, COUNT(*) as count FROM grammar_mistakes GROUP BY errorType ORDER BY count DESC LIMIT :limit")
    suspend fun getTopErrorTypes(limit: Int): List<ErrorTypeCount>

    @Query("SELECT COUNT(*) FROM grammar_mistakes")
    suspend fun countTotal(): Long

    @Query("SELECT COUNT(*) FROM grammar_mistakes WHERE recurrenceCount > 1")
    suspend fun countRecurring(): Long

    @Query("SELECT errorType, COUNT(*) as count FROM grammar_mistakes GROUP BY errorType")
    suspend fun getAllErrorTypesWithCount(): List<ErrorTypeCount>
}

data class ErrorTypeCount(
    val errorType: String,
    val count: Long
)

@Dao
interface StudyTopicDao {
    @Insert
    suspend fun insert(topic: StudyTopicEntity): Long

    @Query("SELECT * FROM study_topics WHERE name = :name LIMIT 1")
    suspend fun getTopicByName(name: String): StudyTopicEntity?

    @Query("SELECT * FROM study_topics ORDER BY frequency DESC, lastEncountered DESC LIMIT :limit")
    suspend fun getTopTopics(limit: Int): List<StudyTopicEntity>

    @Query("SELECT * FROM study_topics WHERE category = :category ORDER BY frequency DESC")
    suspend fun getTopicsByCategory(category: String): List<StudyTopicEntity>

    @Query("UPDATE study_topics SET frequency = :frequency, lastEncountered = :lastEncountered WHERE id = :id")
    suspend fun updateFrequency(id: Long, frequency: Int, lastEncountered: Instant)

    @Query("SELECT * FROM study_topics WHERE id = :id")
    suspend fun getTopicById(id: Long): StudyTopicEntity?
}

@Dao
interface StrengthRecordDao {
    @Insert
    suspend fun insert(record: StrengthRecordEntity): Long

    @Query("SELECT * FROM strength_records ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentStrengths(limit: Int): List<StrengthRecordEntity>

    @Query("SELECT aspect, COUNT(*) as count FROM strength_records GROUP BY aspect ORDER BY count DESC LIMIT :limit")
    suspend fun getTopStrengths(limit: Int): List<StrengthCount>

    @Query("SELECT * FROM strength_records WHERE aspect = :aspect ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestByAspect(aspect: String): StrengthRecordEntity?
}

data class StrengthCount(
    val aspect: String,
    val count: Long
)

@Dao
interface VocabularyDao {
    @Insert
    suspend fun insert(vocab: VocabularyItemEntity): Long

    @Query("SELECT * FROM vocabulary_items WHERE word = :word LIMIT 1")
    suspend fun getVocabularyByWord(word: String): VocabularyItemEntity?

    @Query("SELECT * FROM vocabulary_items WHERE difficulty = :difficulty ORDER BY encounterCount DESC LIMIT :limit")
    suspend fun getByDifficulty(difficulty: String, limit: Int): List<VocabularyItemEntity>

    @Query("SELECT * FROM vocabulary_items WHERE isLearned = 0 ORDER BY encounterCount DESC, firstSeen DESC LIMIT :limit")
    suspend fun getUnlearned(limit: Int): List<VocabularyItemEntity>

    @Query("UPDATE vocabulary_items SET encounterCount = :count WHERE id = :id")
    suspend fun updateEncounterCount(id: Long, count: Int)

    @Query("UPDATE vocabulary_items SET isLearned = 1 WHERE word = :word")
    suspend fun markAsLearned(word: String)

    @Query("SELECT COUNT(*) FROM vocabulary_items")
    suspend fun countTotal(): Long

    @Query("SELECT COUNT(*) FROM vocabulary_items WHERE isLearned = 1")
    suspend fun countLearned(): Long
}

@Dao
interface ChatSessionDao {
    @Insert
    suspend fun insert(session: ChatSessionEntity): Long

    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    suspend fun getAllSessions(): List<ChatSessionEntity>

    @Query("SELECT * FROM chat_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): ChatSessionEntity?

    @Query("UPDATE chat_sessions SET updatedAt = :updatedAt, title = :title, category = :category WHERE id = :id")
    suspend fun updateSession(id: Long, updatedAt: Instant, title: String?, category: String?)

    @Query("SELECT * FROM chat_sessions WHERE category = :category ORDER BY updatedAt DESC")
    suspend fun getSessionsByCategory(category: String): List<ChatSessionEntity>

    @Query("SELECT DISTINCT category FROM chat_sessions WHERE category IS NOT NULL ORDER BY category")
    suspend fun getAllCategories(): List<String?>

    @Delete
    suspend fun delete(session: ChatSessionEntity)

    @Query("DELETE FROM chat_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ChatMessageDao {
    @Insert
    suspend fun insert(message: ChatMessageEntity): Long

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesForSession(sessionId: Long): List<ChatMessageEntity>

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Long)

    @Query("SELECT * FROM chat_messages WHERE id = :id")
    suspend fun getMessageById(id: Long): ChatMessageEntity?

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<ChatMessageEntity>
}
