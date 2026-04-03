package data.repository

import data.db.AppDatabase
import data.db.entity.*
import data.model.*
import data.model.ErrorType
import data.model.TopicCategory
import kotlinx.datetime.Instant

/**
 * Room-based implementation of study database operations for Android.
 * Replaces SQLDelight with Room on Android platform.
 */
class RoomStudyRepository(private val database: AppDatabase) {
    private val studyEntryDao = database.studyEntryDao()
    private val grammarMistakeDao = database.grammarMistakeDao()
    private val studyTopicDao = database.studyTopicDao()
    private val strengthRecordDao = database.strengthRecordDao()
    private val vocabularyDao = database.vocabularyDao()

    // ==================== Study Entries ====================

    suspend fun insertStudyEntry(content: String, type: EntryType, topic: String? = null): Long {
        val entity = StudyEntryEntity(
            content = content,
            type = type.name,
            timestamp = kotlinx.datetime.Clock.System.now(),
            topic = topic
        )
        return studyEntryDao.insert(entity)
    }

    suspend fun updateEntryAnalysis(entryId: Long, correctedContent: String, analysisJson: String) {
        studyEntryDao.updateAnalysis(entryId, correctedContent, analysisJson)
    }

    suspend fun getRecentEntries(limit: Int = 50): List<StudyEntry> {
        return studyEntryDao.getRecentEntries(limit).map { it.toStudyEntry() }
    }

    suspend fun getEntriesByTopic(topic: String): List<StudyEntry> {
        return studyEntryDao.getEntriesByTopic(topic).map { it.toStudyEntry() }
    }

    // ==================== Grammar Mistakes ====================

    suspend fun recordGrammarMistake(
        originalText: String,
        correction: String,
        errorType: ErrorType,
        explanation: String,
        studyEntryId: Long
    ) {
        // Check for recurring mistake
        val similar = grammarMistakeDao.findSimilarMistake(originalText, errorType.name)

        if (similar != null) {
            grammarMistakeDao.updateRecurrenceCount(similar.id, similar.recurrenceCount + 1)
        } else {
            val entity = GrammarMistakeEntity(
                originalText = originalText,
                correction = correction,
                errorType = errorType.name,
                explanation = explanation,
                timestamp = kotlinx.datetime.Clock.System.now(),
                studyEntryId = studyEntryId,
                recurrenceCount = 1
            )
            grammarMistakeDao.insert(entity)
        }
    }

    suspend fun getMistakesByType(errorType: ErrorType, limit: Int = 100): List<GrammarMistake> {
        return grammarMistakeDao.getMistakesByType(errorType.name, limit).map { it.toGrammarMistake() }
    }

    suspend fun getTopMistakeTypes(limit: Int = 5): List<Pair<ErrorType, Int>> {
        return grammarMistakeDao.getTopErrorTypes(limit).map {
            ErrorType.valueOf(it.errorType) to it.count.toInt()
        }
    }

    suspend fun getRecentMistakes(limit: Int = 50): List<GrammarMistake> {
        return grammarMistakeDao.getRecentMistakes(limit).map { it.toGrammarMistake() }
    }

    suspend fun getMistakeStats(): MistakeStats {
        val total = grammarMistakeDao.countTotal()
        val recurring = grammarMistakeDao.countRecurring()
        val byType = grammarMistakeDao.getAllErrorTypesWithCount().associate {
            ErrorType.valueOf(it.errorType) to it.count.toInt()
        }
        return MistakeStats(total, recurring, byType)
    }

    // ==================== Study Topics ====================

    suspend fun recordTopicEncounter(name: String, category: TopicCategory) {
        val now = kotlinx.datetime.Clock.System.now()
        val existing = studyTopicDao.getTopicByName(name)

        if (existing == null) {
            val entity = StudyTopicEntity(
                name = name,
                category = category.name,
                firstEncountered = now,
                lastEncountered = now,
                frequency = 1
            )
            studyTopicDao.insert(entity)
        } else {
            studyTopicDao.updateFrequency(existing.id, existing.frequency + 1, now)
        }
    }

    suspend fun getTopTopics(limit: Int = 10): List<StudyTopic> {
        return studyTopicDao.getTopTopics(limit).map { it.toStudyTopic() }
    }

    suspend fun getTopicsByCategory(category: TopicCategory): List<StudyTopic> {
        return studyTopicDao.getTopicsByCategory(category.name).map { it.toStudyTopic() }
    }

    // ==================== Strength Records ====================

    suspend fun recordStrength(aspect: String, description: String, confidenceScore: Double = 1.0) {
        val entity = StrengthRecordEntity(
            aspect = aspect,
            description = description,
            timestamp = kotlinx.datetime.Clock.System.now(),
            confidenceScore = confidenceScore
        )
        strengthRecordDao.insert(entity)
    }

    suspend fun getStrengths(limit: Int = 20): List<StrengthRecord> {
        return strengthRecordDao.getRecentStrengths(limit).map { it.toStrengthRecord() }
    }

    suspend fun getTopStrengths(limit: Int = 5): List<Pair<String, Int>> {
        return strengthRecordDao.getTopStrengths(limit).map { it.aspect to it.count.toInt() }
    }

    // ==================== Vocabulary ====================

    suspend fun recordVocabulary(
        word: String,
        context: String,
        translation: String?,
        difficulty: CefrLevel
    ) {
        val now = kotlinx.datetime.Clock.System.now()
        val existing = vocabularyDao.getVocabularyByWord(word)

        if (existing == null) {
            val entity = VocabularyItemEntity(
                word = word,
                context = context,
                translation = translation,
                difficulty = difficulty.name,
                firstSeen = now,
                encounterCount = 1,
                isLearned = false
            )
            vocabularyDao.insert(entity)
        } else {
            vocabularyDao.updateEncounterCount(existing.id, existing.encounterCount + 1)
        }
    }

    suspend fun markVocabularyAsLearned(word: String) {
        vocabularyDao.markAsLearned(word)
    }

    suspend fun getVocabularyByDifficulty(difficulty: CefrLevel, limit: Int = 100): List<VocabularyItem> {
        return vocabularyDao.getByDifficulty(difficulty.name, limit).map { it.toVocabularyItem() }
    }

    suspend fun getUnlearnedVocabulary(limit: Int = 100): List<VocabularyItem> {
        return vocabularyDao.getUnlearned(limit).map { it.toVocabularyItem() }
    }

    suspend fun getVocabularyStats(): VocabularyStats {
        val total = vocabularyDao.countTotal().toInt()
        val learned = vocabularyDao.countLearned().toInt()
        return VocabularyStats(total, learned)
    }

    // ==================== Analytics ====================

    suspend fun getStudySessionCount(): Long {
        return studyEntryDao.countEntries()
    }

    suspend fun getTotalWordsWritten(): Long {
        // Sum word counts from all entries
        val entries = studyEntryDao.getRecentEntries(Int.MAX_VALUE)
        return entries.sumOf { it.content.split(Regex("\\s+")).size.toLong() }
    }
}

// ==================== Mapper Functions ====================

private fun StudyEntryEntity.toStudyEntry(): StudyEntry {
    return StudyEntry(
        id = this.id,
        content = this.content,
        type = EntryType.valueOf(this.type),
        timestamp = this.timestamp,
        topic = this.topic,
        correctedContent = this.correctedContent,
        analysisJson = this.analysisJson
    )
}

private fun GrammarMistakeEntity.toGrammarMistake(): GrammarMistake {
    return GrammarMistake(
        id = this.id,
        originalText = this.originalText,
        correction = this.correction,
        errorType = ErrorType.valueOf(this.errorType),
        explanation = this.explanation,
        timestamp = this.timestamp,
        studyEntryId = this.studyEntryId,
        recurrenceCount = this.recurrenceCount
    )
}

private fun StudyTopicEntity.toStudyTopic(): StudyTopic {
    return StudyTopic(
        id = this.id,
        name = this.name,
        category = TopicCategory.valueOf(this.category),
        firstEncountered = this.firstEncountered,
        lastEncountered = this.lastEncountered,
        frequency = this.frequency
    )
}

private fun StrengthRecordEntity.toStrengthRecord(): StrengthRecord {
    return StrengthRecord(
        id = this.id,
        aspect = this.aspect,
        description = this.description,
        timestamp = this.timestamp,
        confidenceScore = this.confidenceScore
    )
}

private fun VocabularyItemEntity.toVocabularyItem(): VocabularyItem {
    return VocabularyItem(
        id = this.id,
        word = this.word,
        context = this.context,
        translation = this.translation,
        difficulty = CefrLevel.valueOf(this.difficulty),
        firstSeen = this.firstSeen,
        encounterCount = this.encounterCount,
        isLearned = this.isLearned
    )
}
