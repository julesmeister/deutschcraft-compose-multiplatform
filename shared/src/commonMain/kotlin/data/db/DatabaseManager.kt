package data.db

import app.cash.sqldelight.db.SqlDriver
import data.db.repository.GrammarMistakeRepository
import data.db.repository.StrengthRecordRepository
import data.db.repository.StudyEntryRepository
import data.db.repository.StudyTopicRepository
import data.db.repository.VocabularyRepository

/**
 * Central manager for all database repositories.
 * Provides a clean API for database operations across the app.
 */
class DatabaseManager(driverFactory: DatabaseDriverFactory) {
    private val driver: SqlDriver = driverFactory.createDriver()
    
    val entries: StudyEntryRepository = StudyEntryRepository(driver)
    val topics: StudyTopicRepository = StudyTopicRepository(driver)
    val mistakes: GrammarMistakeRepository = GrammarMistakeRepository(driver)
    val strengths: StrengthRecordRepository = StrengthRecordRepository(driver)
    val vocabulary: VocabularyRepository = VocabularyRepository(driver)
}
