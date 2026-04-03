package data.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val fontSize: Flow<FontSize>
    val autoSave: Flow<Boolean>
    val showSuggestions: Flow<Boolean>
    val enableGrammarCheck: Flow<Boolean>
    val selectedModel: Flow<String>
    val dailyGoalMinutes: Flow<Int>
    val notificationsEnabled: Flow<Boolean>
    
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setFontSize(size: FontSize)
    suspend fun setAutoSave(enabled: Boolean)
    suspend fun setShowSuggestions(enabled: Boolean)
    suspend fun setEnableGrammarCheck(enabled: Boolean)
    suspend fun setSelectedModel(model: String)
    suspend fun setDailyGoalMinutes(minutes: Int)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun clearAll()
}
