package data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import data.repository.ChatRepository
import data.repository.EssayRepository
import data.repository.SqlDelightChatRepository
import data.repository.SqlDelightEssayRepository
import data.settings.DesktopSettingsRepository
import data.settings.SettingsRepository
import java.io.File

actual class DatabaseDriverFactory {
    actual val databaseManager: DatabaseManager by lazy { DatabaseManager(this) }
    
    // Single driver instance shared across all repositories
    private val driver: SqlDriver by lazy { createDriverInternal() }
    
    actual val chatRepository: ChatRepository by lazy { SqlDelightChatRepository(driver) }
    actual val essayRepository: EssayRepository by lazy { SqlDelightEssayRepository(driver) }
    actual val settingsRepository: SettingsRepository by lazy { DesktopSettingsRepository() }
    
    actual fun createDriver(): SqlDriver = driver
    
    private fun createDriverInternal(): SqlDriver {
        val databasePath = getDatabasePath()
        val driver = JdbcSqliteDriver("jdbc:sqlite:$databasePath")
        
        // Create schema if needed - wrapped in try-catch to handle "already exists" gracefully
        try {
            StudyDatabaseSqlDelight.Schema.create(driver)
        } catch (e: Exception) {
            // Schema likely already exists, which is fine
            println("[Database] Schema creation note: ${e.message}")
        }
        
        return driver
    }
    
    private fun getDatabasePath(): String {
        val appDir = when {
            System.getProperty("os.name").lowercase().contains("windows") -> {
                File(System.getenv("APPDATA") ?: System.getProperty("user.home"), "DeutschCraft")
            }
            System.getProperty("os.name").lowercase().contains("mac") -> {
                File(System.getProperty("user.home"), "Library/Application Support/DeutschCraft")
            }
            else -> {
                File(System.getProperty("user.home"), ".config/deutschcraft")
            }
        }
        
        appDir.mkdirs()
        return File(appDir, "study_database.db").absolutePath
    }
}
