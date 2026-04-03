package data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "deutschcraft_settings")

class SettingsRepository(private val context: Context) : data.settings.SettingsRepository {
    private val dataStore = context.dataStore
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        val SELECTED_MODEL = stringPreferencesKey("selected_model")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
        val SHOW_SUGGESTIONS = booleanPreferencesKey("show_suggestions")
        val LAST_DOCUMENT_PATH = stringPreferencesKey("last_document_path")
        val ENABLE_GRAMMAR_CHECK = booleanPreferencesKey("enable_grammar_check")
        val ENABLE_AUTO_CORRECT = booleanPreferencesKey("enable_auto_correct")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
        val USER_PROFILE = stringPreferencesKey("user_profile_json")
    }

    // Theme settings
    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val themeString = preferences[THEME_MODE] ?: "SYSTEM"
        ThemeMode.valueOf(themeString)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
        }
    }

    // Editor settings
    val fontSize: Flow<FontSize> = dataStore.data.map { preferences ->
        val sizeString = preferences[FONT_SIZE] ?: "MEDIUM"
        FontSize.valueOf(sizeString)
    }

    suspend fun setFontSize(size: FontSize) {
        dataStore.edit { preferences ->
            preferences[FONT_SIZE] = size.name
        }
    }

    val autoSave: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_SAVE] ?: true
    }

    suspend fun setAutoSave(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_SAVE] = enabled
        }
    }

    // AI settings
    val selectedModel: Flow<String> = dataStore.data.map { preferences ->
        preferences[SELECTED_MODEL] ?: "llama3.2"
    }

    suspend fun setSelectedModel(model: String) {
        dataStore.edit { preferences ->
            preferences[SELECTED_MODEL] = model
        }
    }

    val showSuggestions: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_SUGGESTIONS] ?: true
    }

    suspend fun setShowSuggestions(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_SUGGESTIONS] = enabled
        }
    }

    val enableGrammarCheck: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ENABLE_GRAMMAR_CHECK] ?: true
    }

    suspend fun setEnableGrammarCheck(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ENABLE_GRAMMAR_CHECK] = enabled
        }
    }

    // Document settings
    val lastDocumentPath: Flow<String?> = dataStore.data.map { preferences ->
        preferences[LAST_DOCUMENT_PATH]
    }

    suspend fun setLastDocumentPath(path: String?) {
        dataStore.edit { preferences ->
            if (path != null) {
                preferences[LAST_DOCUMENT_PATH] = path
            } else {
                preferences.remove(LAST_DOCUMENT_PATH)
            }
        }
    }

    // Learning settings
    val dailyGoalMinutes: Flow<Int> = dataStore.data.map { preferences ->
        preferences[DAILY_GOAL_MINUTES] ?: 15
    }

    suspend fun setDailyGoalMinutes(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[DAILY_GOAL_MINUTES] = minutes
        }
    }

    // Notifications
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    // User profile
    suspend fun saveUserProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[USER_PROFILE] = json.encodeToString(profile)
        }
    }

    suspend fun getUserProfile(): UserProfile? {
        val profileJson = dataStore.data.first()[USER_PROFILE]
        return profileJson?.let {
            try {
                json.decodeFromString(UserProfile.serializer(), it)
            } catch (e: Exception) {
                null
            }
        }
    }

    // Clear all settings
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

@kotlinx.serialization.Serializable
data class UserProfile(
    val username: String,
    val targetLanguage: String = "German",
    val nativeLanguage: String = "English",
    val currentLevel: String = "A1",
    val learningGoals: List<String> = emptyList()
)
