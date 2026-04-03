package data.settings

import kotlinx.coroutines.flow.*
import java.util.prefs.Preferences

class DesktopSettingsRepository : SettingsRepository {
    private val prefs = Preferences.userNodeForPackage(DesktopSettingsRepository::class.java)

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    override val themeMode: Flow<ThemeMode> = _themeMode.asStateFlow()

    private val _fontSize = MutableStateFlow(FontSize.MEDIUM)
    override val fontSize: Flow<FontSize> = _fontSize.asStateFlow()

    private val _autoSave = MutableStateFlow(true)
    override val autoSave: Flow<Boolean> = _autoSave.asStateFlow()

    private val _showSuggestions = MutableStateFlow(true)
    override val showSuggestions: Flow<Boolean> = _showSuggestions.asStateFlow()

    private val _enableGrammarCheck = MutableStateFlow(true)
    override val enableGrammarCheck: Flow<Boolean> = _enableGrammarCheck.asStateFlow()

    private val _selectedModel = MutableStateFlow("llama3.2")
    override val selectedModel: Flow<String> = _selectedModel.asStateFlow()

    private val _dailyGoalMinutes = MutableStateFlow(15)
    override val dailyGoalMinutes: Flow<Int> = _dailyGoalMinutes.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    override val notificationsEnabled: Flow<Boolean> = _notificationsEnabled.asStateFlow()

    init {
        _themeMode.value = ThemeMode.valueOf(prefs.get("theme_mode", "SYSTEM"))
        _fontSize.value = FontSize.valueOf(prefs.get("font_size", "MEDIUM"))
        _autoSave.value = prefs.getBoolean("auto_save", true)
        _showSuggestions.value = prefs.getBoolean("show_suggestions", true)
        _enableGrammarCheck.value = prefs.getBoolean("enable_grammar_check", true)
        _selectedModel.value = prefs.get("selected_model", "llama3.2")
        _dailyGoalMinutes.value = prefs.getInt("daily_goal_minutes", 15)
        _notificationsEnabled.value = prefs.getBoolean("notifications_enabled", true)
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.put("theme_mode", mode.name)
    }

    override suspend fun setFontSize(size: FontSize) {
        _fontSize.value = size
        prefs.put("font_size", size.name)
    }

    override suspend fun setAutoSave(enabled: Boolean) {
        _autoSave.value = enabled
        prefs.putBoolean("auto_save", enabled)
    }

    override suspend fun setShowSuggestions(enabled: Boolean) {
        _showSuggestions.value = enabled
        prefs.putBoolean("show_suggestions", enabled)
    }

    override suspend fun setEnableGrammarCheck(enabled: Boolean) {
        _enableGrammarCheck.value = enabled
        prefs.putBoolean("enable_grammar_check", enabled)
    }

    override suspend fun setSelectedModel(model: String) {
        _selectedModel.value = model
        prefs.put("selected_model", model)
    }

    override suspend fun setDailyGoalMinutes(minutes: Int) {
        _dailyGoalMinutes.value = minutes
        prefs.putInt("daily_goal_minutes", minutes)
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefs.putBoolean("notifications_enabled", enabled)
    }

    override suspend fun clearAll() {
        prefs.clear()
        _themeMode.value = ThemeMode.SYSTEM
        _fontSize.value = FontSize.MEDIUM
        _autoSave.value = true
        _showSuggestions.value = true
        _enableGrammarCheck.value = true
        _selectedModel.value = "llama3.2"
        _dailyGoalMinutes.value = 15
        _notificationsEnabled.value = true
    }
}
