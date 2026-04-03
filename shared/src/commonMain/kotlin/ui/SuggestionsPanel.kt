package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import service.OllamaService
import theme.*

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
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "AI Suggestions",
                    style = MaterialTheme.typography.titleMedium,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            }
        }
        
        // Model Selector
        Column(modifier = Modifier.padding(12.dp)) {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedModel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Model") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    availableModels.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = {
                                selectedModel = model
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        
        // Suggestion Type Selector
        val types = listOf(
            Triple("improve", "Improve", Icons.Default.Edit),
            Triple("grammar", "Grammar", Icons.Default.Menu),
            Triple("rephrase", "Rephrase", Icons.Default.Refresh),
            Triple("expand", "Expand", Icons.Default.OpenInNew)
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
                        imageVector = Icons.Default.Star,
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
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy")
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = Gray100,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Select text to get AI suggestions",
            style = MaterialTheme.typography.titleMedium,
            color = Gray800,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Works best with 1-3 sentences",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500
        )
    }
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
        Text(
            text = "Selected text",
            style = MaterialTheme.typography.labelSmall,
            color = Gray500
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = Gray100,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (selectedText.length > 150) selectedText.take(150) + "..." else selectedText,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600,
                modifier = Modifier.padding(12.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "AI Suggestion",
            style = MaterialTheme.typography.labelSmall,
            color = Gray500
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = Indigo.copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Indigo.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                if (isGenerating && currentSuggestion.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Indigo
                    )
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
