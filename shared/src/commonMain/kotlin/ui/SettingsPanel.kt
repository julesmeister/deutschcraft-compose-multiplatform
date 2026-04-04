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
import theme.Gray500
import theme.Gray600
import theme.Gray700
import theme.Gray800
import theme.Gray50
import theme.Indigo
import theme.Gray400

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

        // Danger Zone
        Spacer(modifier = Modifier.height(16.dp))
        SettingsSection(title = "Data Management", isDanger = true) {
            Button(
                onClick = {
                    scope.launch {
                        settingsRepo.clearAll()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFEE2E2),
                    contentColor = Color(0xFFDC2626)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All Settings")
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    isDanger: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isDanger) Color(0xFFDC2626) else Indigo
        )

        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    control: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Indigo.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Indigo,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Gray800
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }

        control()
    }
}

@Composable
private fun SettingsToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsRow(
        icon = icon,
        title = title,
        subtitle = subtitle
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Indigo,
                checkedTrackColor = Indigo.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun ThemeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeMode.values().forEach { mode ->
            val isSelected = mode == selected
            Surface(
                color = if (isSelected) Indigo else Gray100,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { onSelect(mode) }
            ) {
                Text(
                    text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Color.White else Gray700,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun FontSizeSelector(
    selected: FontSize,
    onSelect: (FontSize) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FontSize.values().forEach { size ->
            val isSelected = size == selected
            Surface(
                color = if (isSelected) Indigo else Gray100,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { onSelect(size) }
            ) {
                Text(
                    text = when (size) {
                        FontSize.SMALL -> "A"
                        FontSize.MEDIUM -> "A+"
                        FontSize.LARGE -> "A++"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = when (size) {
                            FontSize.SMALL -> 12.sp
                            FontSize.MEDIUM -> 14.sp
                            FontSize.LARGE -> 16.sp
                        }
                    ),
                    color = if (isSelected) Color.White else Gray700,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ModelSelector(
    selected: String,
    availableModels: List<String>,
    isLoading: Boolean,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            color = Gray100,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.clickable(enabled = !isLoading) { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Gray600
                    )
                    Text(
                        text = "Loading...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600
                    )
                } else {
                    Text(
                        text = selected,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray800
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Gray600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableModels.forEach { model ->
                DropdownMenuItem(
                    text = { Text(model) },
                    onClick = {
                        onSelect(model)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DailyGoalSelector(
    minutes: Int,
    onSelect: (Int) -> Unit
) {
    val options = listOf(5, 10, 15, 20, 30, 45, 60)

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { mins ->
            val isSelected = mins == minutes
            Surface(
                color = if (isSelected) Indigo else Gray100,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { onSelect(mins) }
            ) {
                Text(
                    text = "${mins}m",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Color.White else Gray700,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
