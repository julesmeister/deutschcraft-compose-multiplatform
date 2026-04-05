package ui.suggestions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import service.OllamaService
import data.settings.FontSize
import data.repository.ChatMessage
import ui.components.m3.M3IconBox
import ui.suggestions.animations.PulsingIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsPanel(
    selectedText: String,
    fullText: String,
    onApplySuggestion: (String) -> Unit,
    onAppendSuggestion: (String) -> Unit,
    onError: (String?) -> Unit = {},
    fontSize: FontSize = FontSize.MEDIUM,
    mode: SuggestionsPanelMode = SuggestionsPanelMode.EDITOR,
    chatMessages: List<ChatMessage> = emptyList(),
    autoSuggestions: List<String> = emptyList(),
    onTitleSuggestion: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val ollamaService = remember { OllamaService() }
    
    var currentSuggestion by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var activeAction by remember { mutableStateOf<String?>(null) }
    var suggestions by remember { mutableStateOf(listOf<String>()) }
    var selectedModel by remember { mutableStateOf("llama3.2") }
    
    // Load available models on startup
    LaunchedEffect(Unit) {
        try {
            val models = ollamaService.getAvailableModels()
            if (models.isNotEmpty()) {
                selectedModel = models.first()
            }
        } catch (e: Exception) {
            // Keep default model
        }
    }
    
    // Auto-translate when a word is selected from chat (only in CHAT mode)
    LaunchedEffect(selectedText, mode) {
        if (mode == SuggestionsPanelMode.CHAT && selectedText.isNotBlank() && !isGenerating) {
            activeAction = "translate"
            isGenerating = true
            currentSuggestion = ""
            suggestions = emptyList()
            try {
                val translation = ollamaService.translateGermanWord(
                    word = selectedText,
                    context = "",
                    model = selectedModel
                )
                currentSuggestion = translation
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error"
                currentSuggestion = "Error: $errorMsg"
                onError(errorMsg)
            } finally {
                isGenerating = false
            }
        }
    }
    
    // State setters for action handlers
    val setGenerating: (Boolean) -> Unit = { isGenerating = it }
    val setActiveAction: (String?) -> Unit = { activeAction = it }
    val setCurrentSuggestion: (String) -> Unit = { currentSuggestion = it }
    val setSuggestions: (List<String>) -> Unit = { suggestions = it }
    
    // Create action handlers
    val chatActions = rememberChatActions(
        selectedText = selectedText,
        chatMessages = chatMessages,
        selectedModel = selectedModel,
        setGenerating = setGenerating,
        setActiveAction = setActiveAction,
        setCurrentSuggestion = setCurrentSuggestion,
        setSuggestions = setSuggestions,
        onTitleSuggestion = onTitleSuggestion,
        onError = onError
    )
    
    val editorActions = rememberEditorActions(
        selectedText = selectedText,
        fullText = fullText,
        selectedModel = selectedModel,
        setGenerating = setGenerating,
        setActiveAction = setActiveAction,
        setCurrentSuggestion = setCurrentSuggestion,
        setSuggestions = setSuggestions,
        onError = onError
    )
    
    val colorScheme = MaterialTheme.colorScheme
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Surface(
            color = colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PulsingIcon(modifier = Modifier.size(36.dp)) { mod ->
                    M3IconBox(
                        icon = if (mode == SuggestionsPanelMode.CHAT) Icons.Default.Chat else Icons.Default.Star,
                        tint = colorScheme.primary,
                        bg = colorScheme.onPrimaryContainer,
                        modifier = mod
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (mode == SuggestionsPanelMode.CHAT) "Chat Assistant" else "AI Assistant",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onPrimaryContainer,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        
        // Content Area with animated transitions
        val textToAnalyze = if (selectedText.isEmpty()) fullText else selectedText
        
        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + 
                slideInHorizontally { it / 4 }) togetherWith
                (fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + 
                slideOutHorizontally { -it / 4 }) using
                SizeTransform { _, _ ->
                    tween(durationMillis = 200)
                }
            },
            modifier = Modifier.weight(1f)
        ) { targetMode ->
            when (targetMode) {
                SuggestionsPanelMode.CHAT -> {
                    if (chatMessages.isEmpty()) {
                        EmptyState(
                            message = "Start a conversation to get AI assistance",
                            fontSize = fontSize
                        )
                    } else {
                        ChatModeContent(
                            selectedText = selectedText,
                            chatMessages = chatMessages,
                            autoSuggestions = autoSuggestions,
                            currentSuggestion = currentSuggestion,
                            isGenerating = isGenerating,
                            activeAction = activeAction,
                            suggestions = suggestions,
                            fontSize = fontSize,
                            onAppendSuggestion = onAppendSuggestion,
                            onApplySuggestion = onApplySuggestion,
                            onDismiss = {
                                currentSuggestion = ""
                                activeAction = null
                                suggestions = emptyList()
                            }
                        )
                    }
                }
                SuggestionsPanelMode.EDITOR -> {
                    if (fullText.isEmpty()) {
                        EmptyState(
                            message = "Start writing in the editor to get AI suggestions",
                            fontSize = fontSize
                        )
                    } else {
                        SuggestionsContent(
                            selectedText = textToAnalyze,
                            isFullDocument = selectedText.isEmpty(),
                            currentSuggestion = currentSuggestion,
                            isGenerating = isGenerating,
                            activeAction = activeAction,
                            suggestions = suggestions,
                            fontSize = fontSize,
                            onApply = { onApplySuggestion(currentSuggestion) },
                            onAppend = { suggestion -> onAppendSuggestion(suggestion) },
                            onDismiss = { 
                                currentSuggestion = ""
                                activeAction = null
                                suggestions = emptyList()
                            }
                        )
                    }
                }
            }
        }
        
        Divider(color = colorScheme.outlineVariant)
        
        // Contextual Action Buttons
        AnimatedVisibility(
            visible = if (mode == SuggestionsPanelMode.CHAT) chatMessages.isNotEmpty() else fullText.isNotEmpty(),
            enter = fadeIn() + slideInVertically { it / 2 }
        ) {
            if (mode == SuggestionsPanelMode.CHAT) {
                ChatContextualActions(
                    selectedText = selectedText,
                    chatMessages = chatMessages,
                    isGenerating = isGenerating,
                    activeAction = activeAction,
                    onSuggestTitle = chatActions.onSuggestTitle,
                    onSuggestDirections = chatActions.onSuggestDirections,
                    onTranslate = chatActions.onTranslate,
                    onAnalyzeWord = chatActions.onAnalyzeWord
                )
            } else {
                EditorContextualActions(
                    selectedText = textToAnalyze,
                    isGenerating = isGenerating,
                    activeAction = activeAction,
                    onCheckGrammar = editorActions.onCheckGrammar,
                    onImprove = editorActions.onImprove,
                    onRephrase = editorActions.onRephrase,
                    onSuggestMore = editorActions.onSuggestMore
                )
            }
        }
    }
}

@Composable
private fun ChatModeContent(
    selectedText: String,
    chatMessages: List<ChatMessage>,
    autoSuggestions: List<String>,
    currentSuggestion: String,
    isGenerating: Boolean,
    activeAction: String?,
    suggestions: List<String>,
    fontSize: FontSize,
    onAppendSuggestion: (String) -> Unit,
    onApplySuggestion: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Auto-generated follow-up suggestions (shown when AI replies)
            if (autoSuggestions.isNotEmpty()) {
                AutoSuggestionsContent(
                    suggestions = autoSuggestions,
                    fontSize = fontSize,
                    onSuggestionClick = onAppendSuggestion
                )
            }
            
            // Manual suggestion results
            ChatSuggestionsContent(
                selectedText = selectedText,
                currentSuggestion = currentSuggestion,
                isGenerating = isGenerating,
                activeAction = activeAction,
                suggestions = suggestions,
                fontSize = fontSize,
                onApply = { onApplySuggestion(currentSuggestion) },
                onDismiss = onDismiss
            )
        }
    }
}
