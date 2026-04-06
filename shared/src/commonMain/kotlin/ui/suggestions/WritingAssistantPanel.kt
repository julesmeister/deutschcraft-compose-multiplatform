package ui.suggestions

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.model.*
import data.settings.FontSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import theme.*

/**
 * Comprehensive writing assistant panel that shows real-time suggestions
 * as the user writes. Automatically triggers on sentence completion.
 */
@Composable
fun WritingAssistantPanel(
    currentAnalysis: Flow<WritingAnalysis?>,
    quickSuggestions: Flow<List<QuickSuggestion>>,
    isAnalyzing: Flow<Boolean>,
    fontSize: FontSize,
    onApplySuggestion: (SuggestionAction) -> Unit,
    onDismissSuggestion: () -> Unit,
    onShowExercise: (PracticeExercise) -> Unit,
    onRequestAnalysis: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analysis by currentAnalysis.collectAsState(initial = null)
    val suggestions by quickSuggestions.collectAsState(initial = emptyList())
    val analyzing by isAnalyzing.collectAsState(initial = false)
    
    var selectedCategory by remember { mutableStateOf<SuggestionCategory?>(null) }
    var expandedSuggestionId by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header with status
        WritingAssistantHeader(
            analysis = analysis,
            isAnalyzing = analyzing,
            onRequestAnalysis = onRequestAnalysis,
            fontSize = fontSize
        )
        
        Divider(color = Gray200)
        
        // Main content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                analyzing -> {
                    AnalyzingState(fontSize = fontSize)
                }
                analysis == null -> {
                    EmptyWritingState(fontSize = fontSize)
                }
                suggestions.isEmpty() -> {
                    NoSuggestionsState(analysis = analysis!!, fontSize = fontSize)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Filter chips for categories
                        item {
                            CategoryFilterChips(
                                suggestions = suggestions,
                                selectedCategory = selectedCategory,
                                onCategorySelected = { selectedCategory = it }
                            )
                        }
                        
                        // Grouped suggestions by category
                        val grouped = if (selectedCategory != null) {
                            mapOf(selectedCategory!! to suggestions.filter { it.type == selectedCategory })
                        } else {
                            suggestions.groupBy { it.type }
                        }
                        
                        grouped.forEach { (category, categorySuggestions) ->
                            item {
                                CategoryHeader(
                                    category = category,
                                    count = categorySuggestions.size,
                                    fontSize = fontSize
                                )
                            }
                            
                            items(categorySuggestions, key = { it.id }) { suggestion ->
                                SuggestionCard(
                                    suggestion = suggestion,
                                    isExpanded = expandedSuggestionId == suggestion.id,
                                    fontSize = fontSize,
                                    onExpandToggle = {
                                        expandedSuggestionId = if (expandedSuggestionId == suggestion.id) null else suggestion.id
                                    },
                                    onApply = { onApplySuggestion(suggestion.action) },
                                    onDismiss = onDismissSuggestion,
                                    onShowExercise = { 
                                        if (suggestion.action.type == ActionType.OPEN_EXERCISE) {
                                            // Would create exercise from context
                                        }
                                    }
                                )
                            }
                        }
                        
                        // Analysis details section
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            AnalysisDetailsSection(
                                analysis = analysis!!,
                                fontSize = fontSize,
                                onShowExercise = onShowExercise
                            )
                        }
                    }
                }
            }
        }
        
        Divider(color = Gray200)
        
        // Bottom quick actions
        WritingAssistantQuickActions(
            analysis = analysis,
            onRequestAnalysis = onRequestAnalysis,
            fontSize = fontSize
        )
    }
}

@Composable
private fun WritingAssistantHeader(
    analysis: WritingAnalysis?,
    isAnalyzing: Boolean,
    onRequestAnalysis: () -> Unit,
    fontSize: FontSize
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Surface(
        color = colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Animated icon
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isAnalyzing) 1.2f else 1f,
                animationSpec = infiniteTween(
                    durationMillis = if (isAnalyzing) 800 else 2000,
                    easing = FastOutSlowInEasing
                ),
                label = "icon_scale"
            )
            
            Icon(
                imageVector = if (isAnalyzing) Icons.Default.Psychology else Icons.Default.EditNote,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier
                    .size(28.dp)
                    .scale(scale)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAnalyzing) "Analyzing your writing..." else "Writing Assistant",
                    style = MaterialTheme.typography.titleSmall,
                    color = colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (analysis != null) {
                    Text(
                        text = "CEFR ${analysis.sentenceLevel.name} • Score: ${analysis.score.toInt()}/100",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            if (!isAnalyzing) {
                IconButton(
                    onClick = onRequestAnalysis,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reanalyze",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterChips(
    suggestions: List<QuickSuggestion>,
    selectedCategory: SuggestionCategory?,
    onCategorySelected: (SuggestionCategory?) -> Unit
) {
    val categories = suggestions.map { it.type }.distinct()
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") },
                modifier = Modifier.height(32.dp)
            )
        }
        
        items(categories) { category ->
            val colors = getCategoryColors(category)
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(if (selectedCategory == category) null else category) },
                label = { 
                    Text(
                        category.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.first,
                    selectedLabelColor = colors.second
                ),
                modifier = Modifier.height(32.dp)
            )
        }
    }
}

@Composable
private fun CategoryHeader(
    category: SuggestionCategory,
    count: Int,
    fontSize: FontSize
) {
    val (icon, colors) = when (category) {
        SuggestionCategory.GRAMMAR_FIX -> Icons.Default.Error to (Error to Color.White)
        SuggestionCategory.VOCABULARY -> Icons.Default.MenuBook to (Indigo to Color.White)
        SuggestionCategory.STYLE -> Icons.Default.Brush to (Violet to Color.White)
        SuggestionCategory.STRUCTURE -> Icons.Default.AccountTree to (Blue600 to Color.White)
        SuggestionCategory.NEXT_WORD -> Icons.Default.AddComment to (Emerald to Color.White)
        SuggestionCategory.LEARNING -> Icons.Default.School to (Amber to Color.Black)
        SuggestionCategory.CONTINUATION -> Icons.Default.DoubleArrow to (Cyan to Color.Black)
    }
    
    val textSize = when (fontSize) {
        FontSize.EXTRA_SMALL -> 11.sp
        FontSize.SMALL -> 12.sp
        FontSize.MEDIUM -> 13.sp
        FontSize.LARGE -> 14.sp
        FontSize.EXTRA_LARGE -> 15.sp
        FontSize.HUGE -> 16.sp
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    ) {
        Surface(
            color = colors.first.copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.first,
                modifier = Modifier
                    .padding(4.dp)
                    .size(16.dp)
            )
        }
        
        Text(
            text = category.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium.copy(fontSize = textSize),
            color = Gray700,
            fontWeight = FontWeight.SemiBold
        )
        
        Badge(
            containerColor = colors.first.copy(alpha = 0.2f),
            contentColor = colors.first
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = textSize - 2.sp)
            )
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: QuickSuggestion,
    isExpanded: Boolean,
    fontSize: FontSize,
    onExpandToggle: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    onShowExercise: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "card_scale"
    )
    
    val (icon, colors) = getCategoryVisuals(suggestion.type)
    
    val textSize = when (fontSize) {
        FontSize.EXTRA_SMALL -> 11.sp
        FontSize.SMALL -> 12.sp
        FontSize.MEDIUM -> 13.sp
        FontSize.LARGE -> 14.sp
        FontSize.EXTRA_LARGE -> 15.sp
        FontSize.HUGE -> 16.sp
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onExpandToggle
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = colors.first.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = colors.first.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.first,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(18.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = suggestion.title,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = textSize),
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    Text(
                        text = suggestion.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = textSize - 1.sp),
                        color = Gray600,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Preview/Action section
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (suggestion.preview != null) {
                        Surface(
                            color = Gray100,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = suggestion.preview,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = textSize - 1.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                ),
                                color = Gray700,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    
                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onApply,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.first
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = when (suggestion.action.type) {
                                    ActionType.REPLACE -> Icons.Default.Edit
                                    ActionType.INSERT -> Icons.Default.Add
                                    ActionType.APPEND -> Icons.Default.AddComment
                                    ActionType.DELETE -> Icons.Default.Delete
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (suggestion.action.type) {
                                    ActionType.REPLACE -> "Apply"
                                    ActionType.INSERT -> "Insert"
                                    ActionType.APPEND -> "Add"
                                    ActionType.DELETE -> "Remove"
                                    ActionType.OPEN_EXERCISE -> "Practice"
                                    ActionType.SHOW_INFO -> "Learn"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = textSize)
                            )
                        }
                        
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.heightIn(min = 36.dp),
                            border = ButtonDefaults.outlinedButtonBorder,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Gray600
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisDetailsSection(
    analysis: WritingAnalysis,
    fontSize: FontSize,
    onShowExercise: (PracticeExercise) -> Unit
) {
    val textSize = when (fontSize) {
        FontSize.EXTRA_SMALL -> 11.sp
        FontSize.SMALL -> 12.sp
        FontSize.MEDIUM -> 13.sp
        FontSize.LARGE -> 14.sp
        FontSize.EXTRA_LARGE -> 15.sp
        FontSize.HUGE -> 16.sp
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Gray50
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Analysis Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Gray800
            )
            
            // Vocabulary insights
            analysis.vocabularyInsights?.let { vocab ->
                DetailRow(
                    icon = Icons.Default.MenuBook,
                    label = "Vocabulary",
                    value = "${vocab.wordsUsed.size} words • Diversity: ${(vocab.diversityScore * 100).toInt()}%",
                    fontSize = textSize
                )
            }
            
            // Structure
            analysis.structureAnalysis?.let { structure ->
                DetailRow(
                    icon = Icons.Default.AccountTree,
                    label = "Structure",
                    value = "${structure.sentenceType.name.lowercase().replaceFirstChar { it.uppercase() }} • ${structure.clauses.size} clause(s)",
                    fontSize = textSize
                )
            }
            
            // Learning opportunities count
            if (analysis.learningOpportunities.isNotEmpty()) {
                DetailRow(
                    icon = Icons.Default.School,
                    label = "Learning",
                    value = "${analysis.learningOpportunities.size} opportunities",
                    fontSize = textSize,
                    onClick = { 
                        // Show first learning opportunity
                        analysis.learningOpportunities.firstOrNull()?.let { 
                            // Would navigate to learning detail
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = if (onClick != null) {
            Modifier
                .clickable(onClick = onClick)
                .padding(vertical = 2.dp)
        } else {
            Modifier.padding(vertical = 2.dp)
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Indigo,
            modifier = Modifier.size(18.dp)
        )
        
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = fontSize),
            color = Gray600,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = fontSize),
            color = Gray800
        )
    }
}

@Composable
private fun EmptyWritingState(fontSize: FontSize) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EditNote,
                contentDescription = null,
                tint = Gray400,
                modifier = Modifier.size(56.dp)
            )
            
            val textSize = when (fontSize) {
                FontSize.EXTRA_SMALL -> 12.sp
                FontSize.SMALL -> 14.sp
                FontSize.MEDIUM -> 16.sp
                FontSize.LARGE -> 18.sp
                FontSize.EXTRA_LARGE -> 20.sp
                FontSize.HUGE -> 22.sp
            }
            
            Text(
                text = "Start writing in German...",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = textSize),
                color = Gray600,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Text(
                text = "Suggestions will appear here when you complete a sentence",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun AnalyzingState(fontSize: FontSize) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "analyze")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
            
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp,
                    color = Indigo
                )
                
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Indigo,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            val textSize = when (fontSize) {
                FontSize.EXTRA_SMALL -> 12.sp
                FontSize.SMALL -> 14.sp
                FontSize.MEDIUM -> 16.sp
                FontSize.LARGE -> 18.sp
                FontSize.EXTRA_LARGE -> 20.sp
                FontSize.HUGE -> 22.sp
            }
            
            Text(
                text = "Analyzing your sentence...",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = textSize),
                color = Gray700
            )
            
            val analyzingMessages = listOf(
                "Checking grammar patterns...",
                "Looking up vocabulary...",
                "Analyzing sentence structure...",
                "Finding learning opportunities..."
            )
            
            var messageIndex by remember { mutableStateOf(0) }
            
            LaunchedEffect(Unit) {
                while (true) {
                    delay(1500)
                    messageIndex = (messageIndex + 1) % analyzingMessages.size
                }
            }
            
            AnimatedContent(
                targetState = messageIndex,
                transitionSpec = { fadeIn() + slideInVertically { it } togetherWith fadeOut() + slideOutVertically { -it } }
            ) { index ->
                Text(
                    text = analyzingMessages[index],
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }
    }
}

@Composable
private fun NoSuggestionsState(
    analysis: WritingAnalysis,
    fontSize: FontSize
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                color = Emerald.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Emerald,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(40.dp)
                )
            }
            
            val textSize = when (fontSize) {
                FontSize.EXTRA_SMALL -> 14.sp
                FontSize.SMALL -> 16.sp
                FontSize.MEDIUM -> 18.sp
                FontSize.LARGE -> 20.sp
                FontSize.EXTRA_LARGE -> 22.sp
                FontSize.HUGE -> 24.sp
            }
            
            Text(
                text = "Great sentence!",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = textSize),
                color = Gray800,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "No suggestions needed. Your ${analysis.sentenceLevel.name} level sentence looks good.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            analysis.vocabularyInsights?.let { vocab ->
                if (vocab.advancedVocabulary.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✨ Advanced words used: ${vocab.advancedVocabulary.take(3).joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Indigo,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun WritingAssistantQuickActions(
    analysis: WritingAnalysis?,
    onRequestAnalysis: () -> Unit,
    fontSize: FontSize
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            analysis?.let {
                // Quick action chips based on analysis
                if (it.learningOpportunities.isNotEmpty()) {
                    AssistChip(
                        onClick = { /* Show learning */ },
                        label = { Text("${it.learningOpportunities.size} to learn") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                
                if (it.grammarErrors.isNotEmpty()) {
                    AssistChip(
                        onClick = { /* Focus on grammar */ },
                        label = { Text("${it.grammarErrors.size} fixes") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            leadingIconContentColor = Error
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(
                onClick = onRequestAnalysis,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Analyze again",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Helper functions
private fun getCategoryColors(category: SuggestionCategory): Pair<Color, Color> {
    return when (category) {
        SuggestionCategory.GRAMMAR_FIX -> Error to Color.White
        SuggestionCategory.VOCABULARY -> Indigo to Color.White
        SuggestionCategory.STYLE -> Violet to Color.White
        SuggestionCategory.STRUCTURE -> Blue600 to Color.White
        SuggestionCategory.NEXT_WORD -> Emerald to Color.White
        SuggestionCategory.LEARNING -> Amber to Color.Black
        SuggestionCategory.CONTINUATION -> Cyan to Color.Black
    }
}

private fun getCategoryVisuals(category: SuggestionCategory): Pair<ImageVector, Pair<Color, Color>> {
    return when (category) {
        SuggestionCategory.GRAMMAR_FIX -> Icons.Default.Error to (Error to Color.White)
        SuggestionCategory.VOCABULARY -> Icons.Default.MenuBook to (Indigo to Color.White)
        SuggestionCategory.STYLE -> Icons.Default.Brush to (Violet to Color.White)
        SuggestionCategory.STRUCTURE -> Icons.Default.AccountTree to (Blue600 to Color.White)
        SuggestionCategory.NEXT_WORD -> Icons.Default.AddComment to (Emerald to Color.White)
        SuggestionCategory.LEARNING -> Icons.Default.School to (Amber to Color.Black)
        SuggestionCategory.CONTINUATION -> Icons.Default.DoubleArrow to (Cyan to Color.Black)
    }
}

// Extension for delay
import kotlinx.coroutines.delay
