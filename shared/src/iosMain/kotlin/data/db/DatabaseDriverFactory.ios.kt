package data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import data.repository.ChatRepository
import data.repository.EssayRepository
import data.repository.SqlDelightChatRepository
import data.repository.SqlDelightEssayRepository
import data.settings.IosSettingsRepository
import data.settings.SettingsRepository

actual class DatabaseDriverFactory {
    actual val databaseManager: DatabaseManager by lazy { DatabaseManager(this) }
    actual val chatRepository: ChatRepository by lazy { SqlDelightChatRepository(createDriver()) }
    actual val essayRepository: EssayRepository by lazy { SqlDelightEssayRepository(createDriver()) }
    actual val settingsRepository: SettingsRepository by lazy { IosSettingsRepository() }

    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(StudyDatabaseSqlDelight.Schema, "study_database.db")
    }
}
