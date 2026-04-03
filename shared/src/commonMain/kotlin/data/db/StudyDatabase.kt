package data.db

import app.cash.sqldelight.db.SqlDriver
import data.model.*
import kotlinx.datetime.Instant

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

class StudyDatabase(driverFactory: DatabaseDriverFactory) {
    private val driver = driverFactory.createDriver()
    private val database = StudyDatabaseSqlDelight(driver)
    
    private val studyEntryQueries = database.studyEntryQueries
    private val studyTopicQueries = database.studyTopicQueries
    private val grammarMistakeQueries = database.grammarMistakeQueries
    private val strengthRecordQueries = database.strengthRecordQueries
    private val vocabularyQueries = database.vocabularyQueries
    
    // ==================== Study Entries ====================
    
    fun insertStudyEntry(content: String, type: EntryType, topic: String? = null): Long {
        studyEntryQueries.insertStudyEntry(
            content = content,
            type = type.name,
            timestamp = System.currentTimeMillis(),
            topic = topic,
            corrected_content = null,
            analysis_json = null
        )
        return studyEntryQueries.lastInsertRowId().executeAsOne()
    }
    
    fun updateEntryAnalysis(entryId: Long, correctedContent: String, analysisJson: String) {
        studyEntryQueries.updateAnalysis(
            corrected_content = correctedContent,
            analysis_json = analysisJson,
            id = entryId
        )
    }
    
    fun getRecentEntries(limit: Long = 50): List<StudyEntry> {
        return studyEntryQueries.selectRecentEntries(limit)
            .executeAsList()
            .map { it.toStudyEntry() }
    }
    
    fun getEntriesByTopic(topic: String): List<StudyEntry> {
        return studyEntryQueries.selectEntriesByTopic(topic)
            .executeAsList()
            .map { it.toStudyEntry() }
    }
    
    // ==================== Study Topics ====================
    
    fun recordTopicEncounter(name: String, category: TopicCategory) {
        val now = System.currentTimeMillis()
        val existing = studyTopicQueries.selectTopicByName(name).executeAsOneOrNull()
        
        if (existing == null) {
            studyTopicQueries.insertTopic(
                name = name,
                category = category.name,
                first_encountered = now,
                last_encountered = now,
                frequency = 1
            )
        } else {
            studyTopicQueries.updateTopicFrequency(
                last_encountered = now,
                frequency = existing.frequency + 1,
                id = existing.id
            )
        }
    }
    
    fun getTopTopics(limit: Long = 10): List<StudyTopic> {
        return studyTopicQueries.selectTopTopics(limit)
            .executeAsList()
            .map { it.toStudyTopic() }
    }
    
    fun getTopicsByCategory(category: TopicCategory): List<StudyTopic> {
        return studyTopicQueries.selectTopicsByCategory(category.name)
            .executeAsList()
            .map { it.toStudyTopic() }
    }
    
    // ==================== Grammar Mistakes ====================
    
    fun recordGrammarMistake(
        originalText: String,
        correction: String,
        errorType: ErrorType,
        explanation: String,
        studyEntryId: Long
    ) {
        val now = System.currentTimeMillis()
        
        // Check for recurring mistakes
        val similarMistake = grammarMistakeQueries.findSimilarMistake(
            original_text = originalText,
            error_type = errorType.name
        ).executeAsOneOrNull()
        
        if (similarMistake != null) {
            grammarMistakeQueries.incrementRecurrence(
                recurrence_count = similarMistake.recurrence_count + 1,
                id = similarMistake.id
            )
        } else {
            grammarMistakeQueries.insertMistake(
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
    
    fun getMistakesByType(errorType: ErrorType, limit: Long = 100): List<GrammarMistake> {
        return grammarMistakeQueries.selectMistakesByType(errorType.name, limit)
            .executeAsList()
            .map { it.toGrammarMistake() }
    }
    
    fun getTopMistakeTypes(limit: Long = 5): List<Pair<ErrorType, Int>> {
        return grammarMistakeQueries.selectTopErrorTypes(limit)
            .executeAsList()
            .map { 
                ErrorType.valueOf(it.error_type) to it.count.toInt()
            }
    }
    
    fun getRecentMistakes(limit: Long = 50): List<GrammarMistake> {
        return grammarMistakeQueries.selectRecentMistakes(limit)
            .executeAsList()
            .map { it.toGrammarMistake() }
    }
    
    fun getMistakeStats(): MistakeStats {
        val total = grammarMistakeQueries.countTotalMistakes().executeAsOne()
        val recurring = grammarMistakeQueries.countRecurringMistakes().executeAsOne()
        val byType = grammarMistakeQueries.selectAllErrorTypesWithCount()
            .executeAsList()
            .associate { ErrorType.valueOf(it.error_type) to it.count.toInt() }
        
        return MistakeStats(total, recurring, byType)
    }
    
    // ==================== Strength Records ====================
    
    fun recordStrength(aspect: String, description: String, confidenceScore: Double = 1.0) {
        strengthRecordQueries.insertStrength(
            aspect = aspect,
            description = description,
            timestamp = System.currentTimeMillis(),
            confidence_score = confidenceScore
        )
    }
    
    fun getStrengths(limit: Long = 20): List<StrengthRecord> {
        return strengthRecordQueries.selectRecentStrengths(limit)
            .executeAsList()
            .map { it.toStrengthRecord() }
    }
    
    fun getTopStrengths(limit: Long = 5): List<Pair<String, Int>> {
        return strengthRecordQueries.selectTopStrengths(limit)
            .executeAsList()
            .map { it.aspect to it.count.toInt() }
    }
    
    // ==================== Vocabulary ====================
    
    fun recordVocabulary(
        word: String,
        context: String,
        translation: String?,
        difficulty: CefrLevel
    ) {
        val now = System.currentTimeMillis()
        val existing = vocabularyQueries.selectVocabularyByWord(word).executeAsOneOrNull()
        
        if (existing == null) {
            vocabularyQueries.insertVocabulary(
                word = word,
                context = context,
                translation = translation,
                difficulty = difficulty.name,
                first_seen = now,
                encounter_count = 1,
                is_learned = false
            )
        } else {
            vocabularyQueries.incrementEncounter(
                encounter_count = existing.encounter_count + 1,
                id = existing.id
            )
        }
    }
    
    fun markVocabularyAsLearned(word: String) {
        vocabularyQueries.markAsLearned(word)
    }
    
    fun getVocabularyByDifficulty(difficulty: CefrLevel, limit: Long = 100): List<VocabularyItem> {
        return vocabularyQueries.selectByDifficulty(difficulty.name, limit)
            .executeAsList()
            .map { it.toVocabularyItem() }
    }
    
    fun getUnlearnedVocabulary(limit: Long = 100): List<VocabularyItem> {
        return vocabularyQueries.selectUnlearned(limit)
            .executeAsList()
            .map { it.toVocabularyItem() }
    }
    
    fun getVocabularyStats(): VocabularyStats {
        val total = vocabularyQueries.countTotal().executeAsOne()
        val learned = vocabularyQueries.countLearned().executeAsOne()
        return VocabularyStats(total.toInt(), learned.toInt())
    }
    
    // ==================== Analytics ====================
    
    fun getStudySessionCount(): Long {
        return studyEntryQueries.countEntries().executeAsOne()
    }
    
    fun getTotalWordsWritten(): Long {
        return studyEntryQueries.sumWordCount().executeAsOne() ?: 0L
    }
}

// ==================== Mapper Functions ====================

private fun SelectRecentEntries.toStudyEntry(): StudyEntry {
    return StudyEntry(
        id = this.id,
        content = this.content,
        type = EntryType.valueOf(this.type),
        timestamp = Instant.fromEpochMilliseconds(this.timestamp),
        topic = this.topic,
        correctedContent = this.corrected_content,
        analysisJson = this.analysis_json
    )
}

private fun SelectTopTopics.toStudyTopic(): StudyTopic {
    return StudyTopic(
        id = this.id,
        name = this.name,
        category = TopicCategory.valueOf(this.category),
        firstEncountered = Instant.fromEpochMilliseconds(this.first_encountered),
        lastEncountered = Instant.fromEpochMilliseconds(this.last_encountered),
        frequency = this.frequency.toInt()
    )
}

private fun SelectRecentMistakes.toGrammarMistake(): GrammarMistake {
    return GrammarMistake(
        id = this.id,
        originalText = this.original_text,
        correction = this.correction,
        errorType = ErrorType.valueOf(this.error_type),
        explanation = this.explanation,
        timestamp = Instant.fromEpochMilliseconds(this.timestamp),
        studyEntryId = this.study_entry_id,
        recurrenceCount = this.recurrence_count.toInt()
    )
}

private fun SelectMistakesByType.toGrammarMistake(): GrammarMistake {
    return GrammarMistake(
        id = this.id,
        originalText = this.original_text,
        correction = this.correction,
        errorType = ErrorType.valueOf(this.error_type),
        explanation = this.explanation,
        timestamp = Instant.fromEpochMilliseconds(this.timestamp),
        studyEntryId = this.study_entry_id,
        recurrenceCount = this.recurrence_count.toInt()
    )
}

private fun SelectRecentStrengths.toStrengthRecord(): StrengthRecord {
    return StrengthRecord(
        id = this.id,
        aspect = this.aspect,
        description = this.description,
        timestamp = Instant.fromEpochMilliseconds(this.timestamp),
        confidenceScore = this.confidence_score
    )
}

private fun SelectByDifficulty.toVocabularyItem(): VocabularyItem {
    return VocabularyItem(
        id = this.id,
        word = this.word,
        context = this.context,
        translation = this.translation,
        difficulty = CefrLevel.valueOf(this.difficulty),
        firstSeen = Instant.fromEpochMilliseconds(this.first_seen),
        encounterCount = this.encounter_count.toInt(),
        isLearned = this.is_learned
    )
}

private fun SelectUnlearned.toVocabularyItem(): VocabularyItem {
    return VocabularyItem(
        id = this.id,
        word = this.word,
        context = this.context,
        translation = this.translation,
        difficulty = CefrLevel.valueOf(this.difficulty),
        firstSeen = Instant.fromEpochMilliseconds(this.first_seen),
        encounterCount = this.encounter_count.toInt(),
        isLearned = this.is_learned
    )
}

// ==================== Stats Data Classes ====================

data class MistakeStats(
    val totalMistakes: Long,
    val recurringMistakes: Long,
    val byType: Map<ErrorType, Int>
)

data class VocabularyStats(
    val totalWords: Int,
    val learnedWords: Int
) {
    val progressPercentage: Int
        get() = if (totalWords > 0) (learnedWords * 100 / totalWords) else 0
}
