package data.db

import app.cash.sqldelight.db.SqlDriver
import data.repository.ChatRepository
import data.settings.SettingsRepository

expect class DatabaseDriverFactory {
    val databaseManager: DatabaseManager
    val chatRepository: ChatRepository
    val settingsRepository: SettingsRepository
    fun createDriver(): SqlDriver
}
