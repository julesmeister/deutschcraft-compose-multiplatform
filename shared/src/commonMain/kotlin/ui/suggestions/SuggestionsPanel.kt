package ui.suggestions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import theme.*
import data.repository.ChatMessage
import ui.chat.debugConstraints
import ui.components.m3.M3IconBox
import ui.suggestions.animations.PulsingIcon
import ui.suggestions.animations.RotatingIcon

import ui.suggestions.SuggestionsPanelMode
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
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Surface(
            color = Indigo,
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
                        tint = Indigo,
                        bg = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
                        modifier = mod
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (mode == SuggestionsPanelMode.CHAT) "Chat Assistant" else "AI Assistant",
                    style = MaterialTheme.typography.titleMedium,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        
        // Content Area with animated transitions
        val textToAnalyze = if (selectedText.isEmpty()) fullText else selectedText
        
        println("[SEQ 1] SuggestionsPanel: Starting AnimatedContent composition")
        
        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + 
                slideInHorizontally { it / 4 }) togetherWith
                (fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + 
                slideOutHorizontally { -it / 4 })
                using SizeTransform { _, targetSize -> targetSize }
            },
            modifier = Modifier.weight(1f)
        ) { targetMode ->
            when (targetMode) {
                SuggestionsPanelMode.CHAT -> {
                    println("[SEQ 2] SuggestionsPanel: CHAT mode branch")
                    if (chatMessages.isEmpty()) {
                        println("[SEQ 3] SuggestionsPanel: Empty state - no messages")
                        EmptyState(
                            message = "Start a conversation to get AI assistance",
                            fontSize = fontSize
                        )
                    } else {
                        println("[SEQ 4] SuggestionsPanel: Entering CHAT content Box")
                        // FIX: Remove verticalScroll from here - it's handled inside ChatSuggestionsContent
                        Box(modifier = Modifier.fillMaxSize()) {
                            println("[SEQ 5] SuggestionsPanel: Inside Box, building Column")
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                            // Auto-generated follow-up suggestions (shown when AI replies)
                            if (autoSuggestions.isNotEmpty()) {
                                println("[SEQ 6] SuggestionsPanel: AutoSuggestionsContent composition")
                                AutoSuggestionsContent(
                                    suggestions = autoSuggestions,
                                    fontSize = fontSize,
                                    onSuggestionClick = { suggestion ->
                                        onAppendSuggestion(suggestion)
                                    }
                                )
                            }
                            
                            // Manual suggestion results
                            println("[SEQ 7] SuggestionsPanel: ChatSuggestionsContent composition START")
                            ChatSuggestionsContent(
                                selectedText = selectedText,
                                currentSuggestion = currentSuggestion,
                                isGenerating = isGenerating,
                                activeAction = activeAction,
                                suggestions = suggestions,
                                fontSize = fontSize,
                                onApply = { onApplySuggestion(currentSuggestion) },
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
        
        Divider(color = Gray200)
        
        // Contextual Action Buttons
        AnimatedVisibility(
            visible = if (mode == SuggestionsPanelMode.CHAT) chatMessages.isNotEmpty() else fullText.isNotEmpty(),
            enter = fadeIn() + slideInVertically { it / 2 }
        ) {
            if (mode == SuggestionsPanelMode.CHAT) {
                // Chat-specific actions
                ChatContextualActions(
                    selectedText = selectedText,
                    chatMessages = chatMessages,
                    isGenerating = isGenerating,
                    activeAction = activeAction,
                    onSuggestTitle = {
                        activeAction = "title"
                        scope.launch {
                            isGenerating = true
                            currentSuggestion = ""
                            suggestions = emptyList()
                            try {
                                val title = ollamaService.suggestChatTitle(
                                    messages = chatMessages,
                                    model = selectedModel
                                )
                                currentSuggestion = title
                                onTitleSuggestion(title)
                            } catch (e: Exception) {
                                val errorMsg = e.message ?: "Unknown error"
                                currentSuggestion = "Error: $errorMsg"
                                onError(errorMsg)
                            } finally {
                                isGenerating = false
                            }
                        }
                    },
                    onSuggestDirections = {
                        activeAction = "directions"
                        scope.launch {
                            isGenerating = true
                            currentSuggestion = ""
                            suggestions = emptyList()
                            try {
                                val directions = ollamaService.suggestConversationDirections(
                                    messages = chatMessages,
                                    model = selectedModel
                                )
                                suggestions = directions
                            } catch (e: Exception) {
                                val errorMsg = e.message ?: "Unknown error"
                                currentSuggestion = "Error: $errorMsg"
                                onError(errorMsg)
                            } finally {
                                isGenerating = false
                            }
                        }
                    },
                    onTranslate = {
                        if (selectedText.isNotBlank()) {
                            activeAction = "translate"
                            scope.launch {
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
                    },
                    onAnalyzeWord = {
                        if (selectedText.isNotBlank()) {
                            activeAction = "analyze"
                            scope.launch {
                                isGenerating = true
                                currentSuggestion = ""
                                suggestions = emptyList()
                                try {
                                    val analysis = ollamaService.analyzeGermanWord(
                                        word = selectedText,
                                        model = selectedModel
                                    )
                                    currentSuggestion = analysis
                                } catch (e: Exception) {
                                    val errorMsg = e.message ?: "Unknown error"
                                    currentSuggestion = "Error: $errorMsg"
                                    onError(errorMsg)
                                } finally {
                                    isGenerating = false
                                }
                            }
                        }
                    }
                )
            } else {
                // Editor actions (original)
                ContextualActions(
                    selectedText = textToAnalyze,
                    isGenerating = isGenerating,
                    activeAction = activeAction,
                    onCheckGrammar = {
                        activeAction = "grammar"
                        generateSuggestion(
                            scope, ollamaService, textToAnalyze, fullText, 
                            "grammar", selectedModel,
                            onStart = { isGenerating = true; currentSuggestion = ""; suggestions = emptyList() },
                            onChunk = { currentSuggestion += it },
                            onComplete = { isGenerating = false },
                            onError = { errorMsg ->
                                currentSuggestion = "Error: $errorMsg"
                                isGenerating = false
                                onError(errorMsg)
                            }
                        )
                    },
                    onImprove = {
                        activeAction = "improve"
                        generateSuggestion(
                            scope, ollamaService, textToAnalyze, fullText,
                            "improve", selectedModel,
                            onStart = { isGenerating = true; currentSuggestion = ""; suggestions = emptyList() },
                            onChunk = { currentSuggestion += it },
                            onComplete = { isGenerating = false },
                            onError = { errorMsg ->
                                currentSuggestion = "Error: $errorMsg"
                                isGenerating = false
                                onError(errorMsg)
                            }
                        )
                    },
                    onRephrase = {
                        activeAction = "rephrase"
                        generateSuggestion(
                            scope, ollamaService, textToAnalyze, fullText,
                            "rephrase", selectedModel,
                            onStart = { isGenerating = true; currentSuggestion = ""; suggestions = emptyList() },
                            onChunk = { currentSuggestion += it },
                            onComplete = { isGenerating = false },
                            onError = { errorMsg ->
                                currentSuggestion = "Error: $errorMsg"
                                isGenerating = false
                                onError(errorMsg)
                            }
                        )
                    },
                    onSuggestMore = {
                        activeAction = "continue"
                        scope.launch {
                            isGenerating = true
                            currentSuggestion = ""
                            suggestions = emptyList()
                            try {
                                val result = ollamaService.suggestContinuation(fullText, textToAnalyze, selectedModel)
                                suggestions = result
                            } catch (e: Exception) {
                                val errorMsg = e.message ?: "Unknown error"
                                currentSuggestion = "Error: $errorMsg"
                                onError(errorMsg)
                            } finally {
                                isGenerating = false
                            }
                        }
                    }
                )
            }
        }
    }
}
