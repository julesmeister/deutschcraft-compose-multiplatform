package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import service.OllamaService
import theme.*
import ui.components.m3.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SuggestionsPanel(
    selectedText: String,
    onApplySuggestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val ollamaService = remember { OllamaService() }
    
    var suggestionType by remember { mutableStateOf("improve") }
    var currentSuggestion by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var availableModels by remember { mutableStateOf(listOf("llama3.2")) }
    var selectedModel by remember { mutableStateOf("llama3.2") }
    var showSettings by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        val models = ollamaService.getAvailableModels()
        if (models.isNotEmpty()) {
            availableModels = models
            selectedModel = models.first()
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
                    text = "AI Suggestions",
                    style = MaterialTheme.typography.titleMedium,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { showSettings = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        // Settings Dialog
        if (showSettings) {
            SuggestionsSettingsDialog(
                availableModels = availableModels,
                selectedModel = selectedModel,
                onModelSelected = { selectedModel = it },
                onDismiss = { showSettings = false }
            )
        }
        
        // Suggestion Type Selector
        val types = listOf(
            Triple("improve", "Improve", Icons.Default.Edit),
            Triple("grammar", "Grammar", Icons.Default.Menu),
            Triple("rephrase", "Rephrase", Icons.Default.Refresh),
            Triple("expand", "Expand", Icons.Default.Add)
        )
        
        FlowRow(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.forEach { (type, label, icon) ->
                val isSelected = suggestionType == type
                FilterChip(
                    selected = isSelected,
                    onClick = { suggestionType = type },
                    label = { Text(label) },
                    leadingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
        
        Divider(color = Gray200)
        
        // Content Area
        if (selectedText.isEmpty()) {
            EmptyState()
        } else {
            SuggestionsContent(
                selectedText = selectedText,
                currentSuggestion = currentSuggestion,
                isGenerating = isGenerating
            )
        }
        
        Divider(color = Gray200)
        
        // Action Buttons
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isGenerating = true
                        currentSuggestion = ""
                        try {
                            ollamaService.generateSuggestion(
                                text = selectedText,
                                context = "Selected text for improvement",
                                model = selectedModel,
                                suggestionType = suggestionType
                            ).collect { chunk ->
                                currentSuggestion += chunk
                            }
                        } catch (e: Exception) {
                            currentSuggestion = "Error: ${e.message}"
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                enabled = selectedText.isNotEmpty() && !isGenerating,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = LocalContentColor.current
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isGenerating) "Generating..." else "Generate")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onApplySuggestion(currentSuggestion) },
                    enabled = currentSuggestion.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply")
                }
                OutlinedButton(
                    onClick = { /* Copy to clipboard */ },
                    enabled = currentSuggestion.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy")
                }
            }
        }
    }
}
@Composable
private fun EmptyState() {
    NoDataPlaceholder(
        message = "Select text to get AI suggestions. Works best with 1-3 sentences.",
        modifier = Modifier.padding(24.dp)
    )
}

@Composable
private fun SuggestionsContent(
    selectedText: String,
    currentSuggestion: String,
    isGenerating: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Selected text card
        DCCard(
            horizontalMargin = 16.dp,
            label = "SELECTED",
            labelColor = Gray500,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (selectedText.length > 150) selectedText.take(150) + "..." else selectedText,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600,
                modifier = Modifier.padding(16.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // AI Suggestion card
        DCCard(
            horizontalMargin = 16.dp,
            label = "AI SUGGESTION",
            labelColor = Indigo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (isGenerating && currentSuggestion.isEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShimmerEffect(height = 16.dp, widthFraction = 0.9f)
                        ShimmerEffect(height = 16.dp, widthFraction = 0.7f)
                        ShimmerEffect(height = 16.dp, widthFraction = 0.8f)
                    }
                } else {
                    Text(
                        text = currentSuggestion.ifEmpty { "Click Generate to get AI-powered suggestions" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray800,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
