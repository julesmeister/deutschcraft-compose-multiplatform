package ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.settings.FontSize
import data.settings.SettingsRepository
import data.settings.ThemeMode
import kotlinx.coroutines.launch
import service.OllamaService

@Composable
fun AppearanceSection(
    themeMode: ThemeMode,
    fontSize: FontSize,
    settingsRepo: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    SettingsSection(title = "Appearance") {
        SettingsRow(
            icon = Icons.Default.DarkMode,
            title = "Theme",
            subtitle = "Choose your preferred theme"
        ) {
            ThemeSelector(
                selected = themeMode,
                onSelect = { scope.launch { settingsRepo.setThemeMode(it) } }
            )
        }

        SettingsRow(
            icon = Icons.Default.TextFields,
            title = "Font Size",
            subtitle = "Editor text size"
        ) {
            FontSizeSelector(
                selected = fontSize,
                onSelect = { scope.launch { settingsRepo.setFontSize(it) } }
            )
        }
    }
}

@Composable
fun EditorSection(
    autoSave: Boolean,
    showSuggestions: Boolean,
    enableGrammarCheck: Boolean,
    settingsRepo: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    SettingsSection(title = "Editor") {
        SettingsToggleRow(
            icon = Icons.Default.Save,
            title = "Auto Save",
            subtitle = "Automatically save your work",
            checked = autoSave,
            onCheckedChange = { scope.launch { settingsRepo.setAutoSave(it) } }
        )

        SettingsToggleRow(
            icon = Icons.Default.Lightbulb,
            title = "AI Suggestions",
            subtitle = "Show inline writing suggestions",
            checked = showSuggestions,
            onCheckedChange = { scope.launch { settingsRepo.setShowSuggestions(it) } }
        )

        SettingsToggleRow(
            icon = Icons.Default.Spellcheck,
            title = "Grammar Check",
            subtitle = "Highlight grammar errors",
            checked = enableGrammarCheck,
            onCheckedChange = { scope.launch { settingsRepo.setEnableGrammarCheck(it) } }
        )
    }
}

@Composable
fun AiSection(
    selectedModel: String,
    availableModels: List<String>,
    isLoadingModels: Boolean,
    settingsRepo: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    SettingsSection(title = "AI & Language") {
        SettingsRow(
            icon = Icons.Default.SmartToy,
            title = "AI Model",
            subtitle = "Select your preferred model"
        ) {
            ModelSelector(
                selected = selectedModel,
                availableModels = availableModels,
                isLoading = isLoadingModels,
                onSelect = { scope.launch { settingsRepo.setSelectedModel(it) } }
            )
        }
    }
}

@Composable
fun LearningSection(
    dailyGoalMinutes: Int,
    notificationsEnabled: Boolean,
    settingsRepo: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    SettingsSection(title = "Learning Goals") {
        SettingsRow(
            icon = Icons.Default.Schedule,
            title = "Daily Goal",
            subtitle = "Minutes of practice per day"
        ) {
            DailyGoalSelector(
                minutes = dailyGoalMinutes,
                onSelect = { scope.launch { settingsRepo.setDailyGoalMinutes(it) } }
            )
        }

        SettingsToggleRow(
            icon = Icons.Default.Notifications,
            title = "Reminders",
            subtitle = "Daily learning reminders",
            checked = notificationsEnabled,
            onCheckedChange = { scope.launch { settingsRepo.setNotificationsEnabled(it) } }
        )
    }
}
