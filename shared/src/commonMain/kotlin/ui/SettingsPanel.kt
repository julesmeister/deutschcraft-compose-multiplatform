package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.db.DatabaseDriverFactory
import data.settings.FontSize
import data.settings.SettingsRepository
import data.settings.ThemeMode
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import service.OllamaService
import theme.Gray100
import theme.Gray200
import theme.Gray300
import theme.Gray400
import theme.Gray500
import theme.Gray600
import theme.Gray700
import theme.Gray800
import theme.Gray50
import theme.Indigo
import ui.settings.SettingsSection
import ui.settings.SettingsRow
import ui.settings.SettingsToggleRow
import ui.settings.ThemeSelector
import ui.settings.FontSizeSelector
import ui.settings.ModelSelector
import ui.data.DataManagementPanel

@Composable
fun SettingsPanel(
    driverFactory: DatabaseDriverFactory,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val settingsRepo = remember(driverFactory) { driverFactory.settingsRepository }
    val ollamaService = remember { OllamaService() }

    // Settings state
    var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
    var fontSize by remember { mutableStateOf(FontSize.MEDIUM) }
    var autoSave by remember { mutableStateOf(true) }
    var showSuggestions by remember { mutableStateOf(true) }
    var enableGrammarCheck by remember { mutableStateOf(true) }
    var selectedModel by remember { mutableStateOf("llama3.2") }
    var dailyGoalMinutes by remember { mutableStateOf(15) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    
    // Available models from Ollama
    var availableModels by remember { mutableStateOf(listOf("llama3.2")) }
    var isLoadingModels by remember { mutableStateOf(true) }

    // Load settings and fetch models
    LaunchedEffect(Unit) {
        settingsRepo.themeMode.collect { themeMode = it }
    }
    LaunchedEffect(Unit) {
        settingsRepo.fontSize.collect { fontSize = it }
    }
    LaunchedEffect(Unit) {
        settingsRepo.autoSave.collect { autoSave = it }
    }
    LaunchedEffect(Unit) {
        settingsRepo.showSuggestions.collect { showSuggestions = it }
    }
    LaunchedEffect(Unit) {
        settingsRepo.enableGrammarCheck.collect { enableGrammarCheck = it }
    }
    LaunchedEffect(Unit) {
        settingsRepo.selectedModel.collect { selectedModel = it }
    }
    LaunchedEffect(Unit) {
        settingsRepo.dailyGoalMinutes.collect { dailyGoalMinutes = it }
    }
    LaunchedEffect(Unit) {
        settingsRepo.notificationsEnabled.collect { notificationsEnabled = it }
    }
    LaunchedEffect(Unit) {
        isLoadingModels = true
        try {
            val models = ollamaService.getAvailableModels()
            if (models.isNotEmpty()) {
                availableModels = models
            }
        } finally {
            isLoadingModels = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = Indigo,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Gray800
            )
        }

        // Appearance Section
        SettingsSection(title = "Appearance") {
            // Theme Mode
            SettingsRow(
                icon = Icons.Default.DarkMode,
                title = "Theme",
                subtitle = "Choose your preferred theme"
            ) {
                ThemeSelector(
                    selected = themeMode,
                    onSelect = {
                        scope.launch { settingsRepo.setThemeMode(it) }
                    }
                )
            }

            // Font Size
            SettingsRow(
                icon = Icons.Default.TextFields,
                title = "Font Size",
                subtitle = "Editor text size"
            ) {
                FontSizeSelector(
                    selected = fontSize,
                    onSelect = {
                        scope.launch { settingsRepo.setFontSize(it) }
                    }
                )
            }
        }

        // Editor Section
        SettingsSection(title = "Editor") {
            // Auto Save
            SettingsToggleRow(
                icon = Icons.Default.Save,
                title = "Auto Save",
                subtitle = "Automatically save your work",
                checked = autoSave,
                onCheckedChange = {
                    scope.launch { settingsRepo.setAutoSave(it) }
                }
            )

            // Show Suggestions
            SettingsToggleRow(
                icon = Icons.Default.Lightbulb,
                title = "AI Suggestions",
                subtitle = "Show inline writing suggestions",
                checked = showSuggestions,
                onCheckedChange = {
                    scope.launch { settingsRepo.setShowSuggestions(it) }
                }
            )

            // Grammar Check
            SettingsToggleRow(
                icon = Icons.Default.Spellcheck,
                title = "Grammar Check",
                subtitle = "Highlight grammar errors",
                checked = enableGrammarCheck,
                onCheckedChange = {
                    scope.launch { settingsRepo.setEnableGrammarCheck(it) }
                }
            )
        }

        // AI Section
        SettingsSection(title = "AI & Language") {
            // Selected Model
            SettingsRow(
                icon = Icons.Default.SmartToy,
                title = "AI Model",
                subtitle = "Select your preferred model"
            ) {
                ModelSelector(
                    selected = selectedModel,
                    availableModels = availableModels,
                    isLoading = isLoadingModels,
                    onSelect = {
                        scope.launch { settingsRepo.setSelectedModel(it) }
                    }
                )
            }
        }

        // Learning Section
        SettingsSection(title = "Learning Goals") {
            // Daily Goal
            SettingsRow(
                icon = Icons.Default.Schedule,
                title = "Daily Goal",
                subtitle = "Minutes of practice per day"
            ) {
                DailyGoalSelector(
                    minutes = dailyGoalMinutes,
                    onSelect = {
                        scope.launch { settingsRepo.setDailyGoalMinutes(it) }
                    }
                )
            }

            // Notifications
            SettingsToggleRow(
                icon = Icons.Default.Notifications,
                title = "Reminders",
                subtitle = "Daily learning reminders",
                checked = notificationsEnabled,
                onCheckedChange = {
                    scope.launch { settingsRepo.setNotificationsEnabled(it) }
                }
            )
        }

        // Data Management Section
        Spacer(modifier = Modifier.height(16.dp))
        DataManagementPanel(
            driverFactory = driverFactory,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
