package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.db.DatabaseDriverFactory
import data.settings.FontSize
import data.settings.ThemeMode
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import service.OllamaService
import theme.Gray800
import theme.Indigo
import ui.chat.debugConstraints
import ui.data.DataManagementPanel
import ui.settings.AppearanceSection
import ui.settings.EditorSection
import ui.settings.AiSection
import ui.settings.LearningSection

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
            .verticalScroll(rememberScrollState())
            .debugConstraints("SettingsPanel Column")
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
        AppearanceSection(
            themeMode = themeMode,
            fontSize = fontSize,
            settingsRepo = settingsRepo
        )

        // Editor Section
        EditorSection(
            autoSave = autoSave,
            showSuggestions = showSuggestions,
            enableGrammarCheck = enableGrammarCheck,
            settingsRepo = settingsRepo
        )

        // AI Section
        AiSection(
            selectedModel = selectedModel,
            availableModels = availableModels,
            isLoadingModels = isLoadingModels,
            settingsRepo = settingsRepo
        )

        // Learning Section
        LearningSection(
            dailyGoalMinutes = dailyGoalMinutes,
            notificationsEnabled = notificationsEnabled,
            settingsRepo = settingsRepo
        )

        // Data Management Section
        Spacer(modifier = Modifier.height(16.dp))
        DataManagementPanel(
            driverFactory = driverFactory,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
