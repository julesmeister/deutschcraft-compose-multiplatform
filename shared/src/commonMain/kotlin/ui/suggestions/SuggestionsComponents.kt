package ui.suggestions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.settings.FontSize
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import theme.*
import ui.suggestions.animations.TypewriterText
import ui.suggestions.animations.fadeTween
import ui.suggestions.animations.RotatingIcon


@Composable
internal fun EmptyState(
    message: String = "Select text from the editor to get AI suggestions. Try selecting a sentence or paragraph.",
    fontSize: FontSize = FontSize.MEDIUM
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "empty_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "empty_alpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .scale(scale)
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        NoDataPlaceholder(
            message = message,
            icon = Icons.Default.Edit,
            fontSize = fontSize
        )
    }
}

@Composable
internal fun NoDataPlaceholder(
    message: String,
    icon: ImageVector = Icons.Default.Info,
    fontSize: FontSize = FontSize.MEDIUM,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Gray300,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        val textSize = when (fontSize) {
            FontSize.SMALL -> 14.sp
            FontSize.MEDIUM -> 16.sp
            FontSize.LARGE -> 20.sp
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = textSize),
            color = Gray400,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun ChatSuggestionsContent(
    selectedText: String,
    currentSuggestion: String,
    isGenerating: Boolean,
    activeAction: String?,
    suggestions: List<String>,
    fontSize: FontSize,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    val baseFontSize = when (fontSize) {
        FontSize.SMALL -> 14f
        FontSize.MEDIUM -> 16f
        FontSize.LARGE -> 20f
    }
    
    println("[SEQ 9] ChatSuggestionsContent: START composition")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // FIX: Removed fillMaxHeight() + verticalScroll - parent provides bounded constraints
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selected text display (if any)
        AnimatedVisibility(
            visible = selectedText.isNotBlank(),
            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + 
                    fadeIn(animationSpec = fadeTween()),
            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
        ) {
            Surface(
                color = Gray100,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "SELECTED WORD/PHRASE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = (baseFontSize * 0.75).sp),
                        color = Gray500
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedText.length > 200) selectedText.take(200) + "..." else selectedText,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = (baseFontSize * 0.875).sp),
                        color = Gray700,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        // Current suggestion display
        AnimatedVisibility(
            visible = currentSuggestion.isNotBlank(),
            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + 
                    slideInVertically(animationSpec = fadeTween()) { it / 4 } +
                    fadeIn(animationSpec = fadeTween()),
            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
        ) {
            Surface(
                color = Indigo.copy(alpha = 0.05f),
                shape = MaterialTheme.shapes.small,
                border = androidx.compose.foundation.BorderStroke(1.dp, Indigo.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Action label
                    val label = when (activeAction) {
                        "title" -> "SUGGESTED TITLE"
                        "translate" -> "TRANSLATION"
                        "analyze" -> "WORD ANALYSIS"
                        "directions" -> "CONVERSATION DIRECTIONS"
                        else -> "AI SUGGESTION"
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = (baseFontSize * 0.875).sp),
                        color = Indigo,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TypewriterText(
                        text = currentSuggestion,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = baseFontSize.sp),
                        typingDelayMs = 8,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    // Action buttons with bounce animation
                    if (activeAction == "title") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var buttonVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(currentSuggestion) {
                                buttonVisible = false
                                kotlinx.coroutines.delay(300)
                                buttonVisible = true
                            }
                            val buttonScale by animateFloatAsState(
                                targetValue = if (buttonVisible) 1f else 0.8f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioHighBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "button_bounce"
                            )
                            Button(
                                onClick = onApply,
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                                modifier = Modifier
                                    .height(36.dp)
                                    .graphicsLayer { scaleX = buttonScale; scaleY = buttonScale }
                            ) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Use Title", fontSize = 14.sp)
                            }
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Dismiss", fontSize = 14.sp)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Dismiss", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
        
        // Suggestions list (for directions) with staggered animation
        AnimatedVisibility(
            visible = suggestions.isNotEmpty(),
            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + 
                    fadeIn(animationSpec = fadeTween()),
            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
        ) {
            Surface(
                color = Gray50,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SUGGESTED DIRECTIONS",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = (baseFontSize * 0.875).sp),
                        color = Indigo,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    suggestions.forEachIndexed { index, suggestion ->
                        val itemVisible = remember { mutableStateOf(false) }
                        LaunchedEffect(suggestions) {
                            kotlinx.coroutines.delay(index * 50L)
                            itemVisible.value = true
                        }
                        AnimatedVisibility(
                            visible = itemVisible.value,
                            enter = fadeIn(animationSpec = fadeTween()) + 
                                    slideInVertically(animationSpec = fadeTween()) { it / 3 }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = baseFontSize.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Indigo
                                )
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = baseFontSize.sp),
                                    color = Gray800,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        if (index < suggestions.size - 1) {
                            Divider(color = Gray200, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Dismiss", fontSize = 14.sp)
                    }
                }
            }
        }
        
        // Loading state with rotating animation
        AnimatedVisibility(
            visible = isGenerating && currentSuggestion.isBlank() && suggestions.isEmpty(),
            enter = fadeIn(animationSpec = fadeTween()),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                RotatingIcon(modifier = Modifier.size(32.dp)) { mod ->
                    CircularProgressIndicator(
                        color = Indigo, 
                        modifier = mod,
                        strokeWidth = 3.dp
                    )
                }
            }
        }
        
        // Empty state with hint
        AnimatedVisibility(
            visible = !isGenerating && currentSuggestion.isBlank() && suggestions.isEmpty(),
            enter = fadeIn(animationSpec = tween(400)) + 
                    slideInVertically(animationSpec = fadeTween()) { it / 4 },
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Text(
                text = "Select a word from chat or use the buttons below for AI assistance",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = (baseFontSize * 0.875).sp),
                color = Gray500,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
internal fun ChatContextualActions(
    selectedText: String,
    chatMessages: List<data.repository.ChatMessage>,
    isGenerating: Boolean,
    activeAction: String?,
    onSuggestTitle: () -> Unit,
    onSuggestDirections: () -> Unit,
    onTranslate: () -> Unit,
    onAnalyzeWord: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Chat management actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                icon = Icons.Default.Title,
                label = "Suggest Title",
                isActive = activeAction == "title",
                isLoading = isGenerating && activeAction == "title",
                onClick = onSuggestTitle,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                icon = Icons.Default.Explore,
                label = "Directions",
                isActive = activeAction == "directions",
                isLoading = isGenerating && activeAction == "directions",
                onClick = onSuggestDirections,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Row 2: Word-level actions (only enabled when text is selected)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                icon = Icons.Default.Translate,
                label = "Translate",
                isActive = activeAction == "translate",
                isLoading = isGenerating && activeAction == "translate",
                isEnabled = selectedText.isNotBlank(),
                onClick = onTranslate,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                icon = Icons.Default.MenuBook,
                label = "Analyze",
                isActive = activeAction == "analyze",
                isLoading = isGenerating && activeAction == "analyze",
                isEnabled = selectedText.isNotBlank(),
                onClick = onAnalyzeWord,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

