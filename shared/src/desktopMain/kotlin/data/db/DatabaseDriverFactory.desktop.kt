package data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import data.settings.DesktopSettingsRepository
import java.io.File

actual class DatabaseDriverFactory {
    val settingsRepository: DesktopSettingsRepository by lazy { DesktopSettingsRepository() }
    
    actual fun createDriver(): SqlDriver {
        val databasePath = getDatabasePath()
        val driver = JdbcSqliteDriver("jdbc:sqlite:$databasePath")
        
        // Create schema if needed
        if (!File(databasePath).exists()) {
            StudyDatabaseSqlDelight.Schema.create(driver)
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
