package data.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import data.model.CefrLevel
import data.model.EntryType
import data.model.ErrorType
import data.model.GrammarMistake
import data.model.StrengthRecord
import data.model.StudyEntry
import data.model.StudyTopic
import data.model.TopicCategory
import data.model.VocabularyItem
import data.repository.ChatRepository
import data.repository.RoomStudyRepository
import data.settings.SettingsRepository

/**
 * Android implementation using Room for persistence.
 * Provides access to Room repositories and maintains compatibility with common code.
 */
actual class DatabaseDriverFactory(private val context: Context) {
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    val studyRepository: RoomStudyRepository by lazy {
        RoomStudyRepository(database)
    }

    val chatRepository: ChatRepository by lazy {
        ChatRepository(database)
    }

    val settingsRepository: AndroidSettingsRepository by lazy {
        AndroidSettingsRepository(context)
    }

    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(StudyDatabaseSqlDelight.Schema, context, "study_database.db")
    }
}

/**
 * Room-based implementation of StudyDatabase for Android.
 * Wraps RoomStudyRepository to provide the StudyDatabase interface.
 */
class RoomStudyDatabase(context: Context) : StudyDatabase(
    driverFactory = DatabaseDriverFactory(context)
) {
    private val database = AppDatabase.getDatabase(context)
    private val roomRepo = RoomStudyRepository(database)

    override fun insertStudyEntry(content: String, type: EntryType, topic: String?): Long {
        return kotlinx.coroutines.runBlocking {
            roomRepo.insertStudyEntry(content, type, topic)
        }
    }

    override fun updateEntryAnalysis(entryId: Long, correctedContent: String, analysisJson: String) {
        kotlinx.coroutines.runBlocking {
            roomRepo.updateEntryAnalysis(entryId, correctedContent, analysisJson)
        }
    }

    override fun getRecentEntries(limit: Long): List<StudyEntry> {
        return kotlinx.coroutines.runBlocking {
            roomRepo.getRecentEntries(limit.toInt())
        }
    }

    override fun getEntriesByTopic(topic: String): List<StudyEntry> {
        return kotlinx.coroutines.runBlocking {
            roomRepo.getEntriesByTopic(topic)
        }
    }

    override fun recordGrammarMistake(
        originalText: String,
        correction: String,
        errorType: ErrorType,
        explanation: String,
        studyEntryId: Long
    ) {
        kotlinx.coroutines.runBlocking {
            roomRepo.recordGrammarMistake(originalText, correction, errorType, explanation, studyEntryId)
        }
    }

    override fun getMistakesByType(errorType: ErrorType, limit: Long): List<GrammarMistake> {
        return kotlinx.coroutines.runBlocking {
            roomRepo.getMistakesByType(errorType, limit.toInt())
        }
    }

    override fun getTopMistakeTypes(limit: Long): List<Pair<ErrorType, Int>> {
        return kotlinx.coroutines.runBlocking {
            roomRepo.getTopMistakeTypes(limit.toInt())
        }
    }

    override fun getRecentMistakes(limit: Long): List<GrammarMistake> {
        return kotlinx.coroutines.runBlocking {
            roomRepo.getRecentMistakes(limit.toInt())
        }
    }

    override fun getMistakeStats(): MistakeStats {
        return kotlinx.coroutines.runBlocking {
            roomRepo.getMistakeStats()
        }
    }

    override fun recordTopicEncounter(name: String, category: TopicCategory) {
        kotlinx.coroutines.runBlocking {
            roomRepo.recordTopicEncounter(name, category)
        }
    }

    override fun getTopTopics(limit: Long): List<StudyTopic> {
        return kotlinx.coroutines.runBlocking {
            roomRepo.getTopTopics(limit.toInt())
        }
    }

    override fun getTopicsByCategory(category: TopicCategory): List<StudyTopic> {
        return kotlinx.coroutines.runBlocking {
            roomRepo.getTopicsByCategory(category)
        }
    }

    override fun recordStrength(aspect: String, description: String, confidenceScore: Double) {
        kotlinx.coroutines.runBlocking {
            roomRepo.recordStrength(aspect, description, confidenceScore)
        }
    }

    override fun getStrengths(limit: Long): List<StrengthRecord> {
        return kotlinx.coroutines.runBlocking {
            roomRepo.getStrengths(limit.toInt())
        }
    }

    override fun getTopStrengths(limit: Long): List<Pair<String, Int>> {
        return kotlinx.coroutines.runBlocking {
            roomRepo.getTopStrengths(limit.toInt())
        }
    }

    override fun recordVocabulary(
        word: String,
        context: String,
        translation: String?,
        difficulty: CefrLevel
    ) {
        kotlinx.coroutines.runBlocking {
            roomRepo.recordVocabulary(word, context, translation, difficulty)
        }
    }

    override fun markVocabularyAsLearned(word: String) {
        kotlinx.coroutines.runBlocking {
            roomRepo.markVocabularyAsLearned(word)
        }
    }

    override fun getVocabularyByDifficulty(difficulty: CefrLevel, limit: Long): List<VocabularyItem> {
        return kotlinx.coroutines.runBlocking {
            roomRepo.getVocabularyByDifficulty(difficulty, limit.toInt())
        }
    }

    override fun getUnlearnedVocabulary(limit: Long): List<VocabularyItem> {
        return kotlinx.coroutines.runBlocking {
            roomRepo.getUnlearnedVocabulary(limit.toInt())
        }
    }

    override fun getVocabularyStats(): VocabularyStats {
        return kotlinx.coroutines.runBlocking {
            roomRepo.getVocabularyStats()
        }
    }

    override fun getStudySessionCount(): Long {
        return kotlinx.coroutines.runBlocking {
            roomRepo.getStudySessionCount()
        }
    }

    override fun getTotalWordsWritten(): Long {
        return kotlinx.coroutines.runBlocking {
            roomRepo.getTotalWordsWritten()
        }
    }
}
