import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.db.DatabaseDriverFactory
import data.db.repository.VocabularyRepository
import data.model.VocabularyItem
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
import ui.ModeTabSelector
import ui.SettingsPanel
import ui.TabButton
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
        
        // Difficult words state
        var difficultWords by remember { mutableStateOf(listOf<VocabularyItem>()) }
        val vocabularyRepository = remember(driverFactory) { VocabularyRepository(driverFactory.createDriver()) }
        
        // Load difficult words when needed
        LaunchedEffect(activeTab, editorText) {
            if (activeTab == 0 && editorText.length > 20) {
                difficultWords = vocabularyRepository.getDifficultWords(limit = 5)
            }
        }
        
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
                    HorizontalDivider(color = Gray200)
                    
                    // Content based on selected tab - fills remaining space
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
                                difficultWords = difficultWords,
                                onWordSelected = { word ->
                                    // Word was selected from suggestion
                                    scope.launch {
                                        // Record practice attempt and check if auto-learned
                                        val autoLearned = vocabularyRepository.recordPractice(word)
                                        if (autoLearned) {
                                            // Refresh the list - word will be filtered out as it's now learned
                                            difficultWords = vocabularyRepository.getDifficultWords(limit = 5)
                                        }
                                    }
                                },
                                onDismissDifficultWords = { 
                                    // User dismissed the suggestion
                                },
                                onWordPracticed = { word ->
                                    scope.launch {
                                        val autoLearned = vocabularyRepository.recordPractice(word)
                                        if (autoLearned) {
                                            // Word was auto-marked as learned, refresh list
                                            difficultWords = vocabularyRepository.getDifficultWords(limit = 5)
                                        } else {
                                            // Just refresh to show updated practice count
                                            difficultWords = vocabularyRepository.getDifficultWords(limit = 5)
                                        }
                                    }
                                },
                                onMarkWordAsLearned = { word ->
                                    scope.launch {
                                        // Manually mark as learned
                                        vocabularyRepository.gradeWordAsLearned(word, autoLearnThreshold = 3)
                                        // Refresh the list
                                        difficultWords = vocabularyRepository.getDifficultWords(limit = 5)
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
                                difficultWords = difficultWords,
                                onDifficultWordSelected = { word ->
                                    scope.launch {
                                        val autoLearned = vocabularyRepository.recordPractice(word)
                                        if (autoLearned) {
                                            difficultWords = vocabularyRepository.getDifficultWords(limit = 5)
                                        }
                                    }
                                },
                                onDismissDifficultWords = { },
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
                    VerticalDivider(
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

expect fun getPlatformName(): String