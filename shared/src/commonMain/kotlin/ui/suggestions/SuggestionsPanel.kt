package ui.suggestions

import androidx.compose.animation.*
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
import theme.*
import ui.components.m3.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsPanel(
    selectedText: String,
    fullText: String,
    onApplySuggestion: (String) -> Unit,
    onAppendSuggestion: (String) -> Unit,
    onError: (String?) -> Unit = {},
    fontSize: FontSize = FontSize.MEDIUM,
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
                M3IconBox(
                    icon = Icons.Default.Star,
                    tint = Indigo,
                    bg = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "AI Assistant",
                    style = MaterialTheme.typography.titleMedium,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        
        // Content Area
        val textToAnalyze = if (selectedText.isEmpty()) fullText else selectedText
        
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
                onAppend = { onAppendSuggestion(currentSuggestion) },
                onDismiss = { 
                    currentSuggestion = ""
                    activeAction = null
                    suggestions = emptyList()
                }
            )
        }
        
        Divider(color = Gray200)
        
        // Contextual Action Buttons - show when there's text to analyze
        AnimatedVisibility(
            visible = fullText.isNotEmpty(),
            enter = fadeIn() + slideInVertically { it / 2 }
        ) {
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
