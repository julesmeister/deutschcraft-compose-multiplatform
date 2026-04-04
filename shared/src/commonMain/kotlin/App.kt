import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.db.DatabaseDriverFactory
import data.repository.ChatMessage
import data.repository.EssayDraft
import data.settings.ThemeMode
import data.settings.FontSize
import androidx.compose.foundation.isSystemInDarkTheme
import theme.DeutschCraftTheme
import theme.Gray200
import theme.Gray50
import theme.Gray100
import theme.Gray600
import theme.Gray700
import theme.Indigo
import ui.EditorPanel
import ui.chat.debugConstraints
import ui.ChatPanelWithPersistence
import ui.GrammarAnalysisPanel
import ui.SettingsPanel
import ui.suggestions.SuggestionsPanel
import ui.suggestions.SuggestionsPanelMode
import kotlinx.coroutines.*

@Composable
fun App(driverFactory: DatabaseDriverFactory) {
    val scope = rememberCoroutineScope()
    val settingsRepo = remember(driverFactory) { driverFactory.settingsRepository }
    
    var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
    var fontSize by remember { mutableStateOf(FontSize.MEDIUM) }
    
    LaunchedEffect(Unit) {
        settingsRepo.themeMode.collect { themeMode = it }
    }
    LaunchedEffect(Unit) {
        settingsRepo.fontSize.collect { fontSize = it }
    }
    
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    DeutschCraftTheme(darkTheme = darkTheme) {
        var editorText by remember { mutableStateOf("") }
        var selectedText by remember { mutableStateOf("") }
        var activeTab by remember { mutableStateOf(0) } // 0 = Editor, 1 = Chat, 2 = Analysis, 3 = Settings
        var showRightPanel by remember { mutableStateOf(true) }
        var chatSelectedText by remember { mutableStateOf("") }
        var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
        var chatTitleSuggestion by remember { mutableStateOf<String?>(null) }
        var chatAutoSuggestions by remember { mutableStateOf(listOf<String>()) }
        var suggestionError by remember { mutableStateOf<String?>(null) }
        
        // Essay state
        var currentEssayId by remember { mutableStateOf<Long?>(null) }
        var currentEssayTitle by remember { mutableStateOf<String?>(null) }
        var showOpenEssayDialog by remember { mutableStateOf(false) }
        var showSaveEssayDialog by remember { mutableStateOf(false) }
        var essayTitleInput by remember { mutableStateOf("") }
        var savedEssays by remember { mutableStateOf(listOf<EssayDraft>()) }
        
        val essayRepository = remember(driverFactory) { driverFactory.essayRepository }
        
        Scaffold { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Left Panel (Editor/Chat) - Takes available space
                Column(
                    modifier = if (showRightPanel) Modifier.weight(2f) else Modifier.fillMaxSize()
                ) {
                    // Tab Selector with right panel toggle
                    ModeTabSelector(
                        activeTab = activeTab,
                        onTabSelected = { activeTab = it },
                        showRightPanel = showRightPanel,
                        onToggleRightPanel = { showRightPanel = !showRightPanel }
                    )
                    Divider(color = Gray200)
                    
                    // Content based on selected tab - fills remaining space
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().debugConstraints("App.kt Content Box")) {
                        when (activeTab) {
                            0 -> EditorPanel(
                                text = editorText,
                                onTextChange = { editorText = it },
                                onSelectionChange = { selectedText = it },
                                onAnalyzeClick = { activeTab = 2 },
                                errorMessage = suggestionError,
                                onErrorDismiss = { suggestionError = null },
                                fontSize = fontSize,
                                currentEssayTitle = currentEssayTitle,
                                onOpenEssay = { showOpenEssayDialog = true },
                                onSaveEssay = { 
                                    if (currentEssayId != null) {
                                        // Update existing
                                        scope.launch {
                                            essayRepository.updateEssay(currentEssayId!!, currentEssayTitle ?: "", editorText)
                                        }
                                    } else {
                                        // Show save dialog for new essay
                                        essayTitleInput = editorText.take(50)
                                        showSaveEssayDialog = true
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                            1 -> ChatPanelWithPersistence(
                                editorText = editorText,
                                driverFactory = driverFactory,
                                fontSize = fontSize,
                                onSelectionChange = { chatSelectedText = it },
                                onMessagesChange = { chatMessages = it },
                                onAutoSuggestionsChange = { chatAutoSuggestions = it },
                                suggestedTitle = chatTitleSuggestion,
                                onTitleApplied = { chatTitleSuggestion = null },
                                modifier = Modifier.fillMaxSize()
                            )
                            2 -> GrammarAnalysisPanel(
                                text = editorText,
                                driverFactory = driverFactory,
                                modifier = Modifier.fillMaxSize()
                            )
                            3 -> SettingsPanel(
                                driverFactory = driverFactory,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                
                // Right Panel - Suggestions (collapsible)
                if (showRightPanel) {
                    // Divider
                    Divider(
                        modifier = Modifier.fillMaxHeight().width(1.dp),
                        color = Gray200
                    )
                    
                    // Suggestions Panel
                    SuggestionsPanel(
                        selectedText = if (activeTab == 1) chatSelectedText else selectedText,
                        fullText = editorText,
                        onApplySuggestion = { suggestion ->
                            if (activeTab == 1) {
                                // In chat mode, apply as title
                                chatTitleSuggestion = suggestion
                            } else {
                                // In editor mode, apply to editor
                                if (suggestion.isNotBlank() && !suggestion.startsWith("Error:")) {
                                    scope.launch {
                                        editorText = suggestion
                                    }
                                }
                            }
                        },
                        onAppendSuggestion = { suggestion ->
                            if (suggestion.isNotBlank() && !suggestion.startsWith("Error:")) {
                                scope.launch {
                                    editorText = if (editorText.endsWith(" ") || editorText.isEmpty()) {
                                        editorText + suggestion
                                    } else {
                                        editorText + " " + suggestion
                                    }
                                }
                            }
                        },
                        onError = { errorMsg ->
                            suggestionError = errorMsg
                        },
                        fontSize = fontSize,
                        mode = if (activeTab == 1) SuggestionsPanelMode.CHAT else SuggestionsPanelMode.EDITOR,
                        chatMessages = chatMessages,
                        autoSuggestions = chatAutoSuggestions,
                        onTitleSuggestion = { chatTitleSuggestion = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Load saved essays when dialog opens
        LaunchedEffect(showOpenEssayDialog) {
            if (showOpenEssayDialog) {
                savedEssays = essayRepository.getAllEssays()
            }
        }
        
        // Open Essay Dialog
        if (showOpenEssayDialog) {
            AlertDialog(
                onDismissRequest = { showOpenEssayDialog = false },
                title = { Text("Open Essay") },
                text = {
                    if (savedEssays.isEmpty()) {
                        Text("No saved essays yet. Write something and save it first!")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            savedEssays.forEach { essay ->
                                Card(
                                    onClick = {
                                        editorText = essay.content
                                        currentEssayId = essay.id
                                        currentEssayTitle = essay.title
                                        showOpenEssayDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = essay.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = essay.content.take(60) + if (essay.content.length > 60) "..." else "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Gray600
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showOpenEssayDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Save Essay Dialog
        if (showSaveEssayDialog) {
            AlertDialog(
                onDismissRequest = { showSaveEssayDialog = false },
                title = { Text("Save Essay") },
                text = {
                    OutlinedTextField(
                        value = essayTitleInput,
                        onValueChange = { essayTitleInput = it },
                        label = { Text("Essay Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val newId = essayRepository.createEssay(essayTitleInput, editorText)
                                currentEssayId = newId
                                currentEssayTitle = essayTitleInput
                                showSaveEssayDialog = false
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveEssayDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ModeTabSelector(
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    showRightPanel: Boolean = true,
    onToggleRightPanel: () -> Unit = {}
) {
    Surface(
        color = Gray50,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Editor Tab
            TabButton(
                icon = Icons.Default.Edit,
                label = "Editor",
                isSelected = activeTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            
            // Chat Tab
            TabButton(
                icon = Icons.Default.Chat,
                label = "Chat",
                isSelected = activeTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
            
            // Analysis Tab
            TabButton(
                icon = Icons.Default.Assessment,
                label = "Analysis",
                isSelected = activeTab == 2,
                onClick = { onTabSelected(2) },
                modifier = Modifier.weight(1f)
            )
            
            // Settings Tab
            TabButton(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = activeTab == 3,
                onClick = { onTabSelected(3) },
                modifier = Modifier.weight(1f)
            )
            
            // Right panel toggle button
            IconButton(
                onClick = onToggleRightPanel,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (showRightPanel) Icons.Default.Menu else Icons.Default.MenuOpen,
                    contentDescription = if (showRightPanel) "Hide suggestions" else "Show suggestions",
                    tint = Gray600,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Indigo else Gray100
    val contentColor = if (isSelected) androidx.compose.ui.graphics.Color.White else Gray700
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

expect fun getPlatformName(): String