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
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.settings.FontSize
import theme.*
import ui.suggestions.animations.TypewriterText
import ui.suggestions.animations.fadeTween
import ui.suggestions.animations.RotatingIcon

@Composable
internal fun ChatSuggestionsContent(
    selectedText: String,
    currentSuggestion: String,
    isGenerating: Boolean,
    activeAction: String?,
    suggestions: List<String>,
    fontSize: FontSize,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    val baseFontSize = when (fontSize) {
        FontSize.EXTRA_SMALL -> 12f
        FontSize.SMALL -> 14f
        FontSize.MEDIUM -> 16f
        FontSize.LARGE -> 20f
        FontSize.EXTRA_LARGE -> 24f
        FontSize.HUGE -> 28f
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
                    fadeIn(animationSpec = ui.animations.fadeTween()),
            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
        ) {
            val colorScheme = MaterialTheme.colorScheme
            Surface(
                color = colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "SELECTED WORD/PHRASE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = (baseFontSize * 0.75).sp),
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedText.length > 200) selectedText.take(200) + "..." else selectedText,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = (baseFontSize * 0.875).sp),
                        color = colorScheme.onSurface,
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
                    slideInVertically(animationSpec = ui.animations.fadeTween()) { it / 4 } +
                    fadeIn(animationSpec = ui.animations.fadeTween()),
            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
        ) {
            val colorScheme = MaterialTheme.colorScheme
            val isError = currentSuggestion.contains("failed", ignoreCase = true) || 
                          currentSuggestion.contains("error", ignoreCase = true)
            Surface(
                color = if (isError) M3RedContainer else M3SurfaceContainer,
                shape = MaterialTheme.shapes.small,
                border = if (isError) 
                    androidx.compose.foundation.BorderStroke(1.dp, M3RedColor.copy(alpha = 0.5f))
                else 
                    androidx.compose.foundation.BorderStroke(1.dp, M3Outline),
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
                        color = if (isError) M3RedColor else M3Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ui.animations.TypewriterText(
                        text = currentSuggestion,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = baseFontSize.sp,
                            color = if (isError) M3RedColor else M3OnSurface
                        ),
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (isError && onRetry != null) {
                                Button(
                                    onClick = onRetry,
                                    modifier = Modifier.height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = M3RedColor,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry", fontSize = 14.sp)
                                }
                            }
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.height(36.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 1.dp,
                                    brush = androidx.compose.ui.graphics.SolidColor(M3Outline)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = M3OnSurface
                                )
                            ) {
                                Text("Dismiss", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
        
        // Suggestions list (for directions) with staggered animation
        AnimatedVisibility(
            visible = suggestions.isNotEmpty(),
            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + 
                    fadeIn(animationSpec = ui.animations.fadeTween()),
            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
        ) {
            val colorScheme = MaterialTheme.colorScheme
            Surface(
                color = colorScheme.surface,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SUGGESTED DIRECTIONS",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = (baseFontSize * 0.875).sp),
                        color = colorScheme.primary,
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
                            enter = fadeIn(animationSpec = ui.animations.fadeTween()) + 
                                    slideInVertically(animationSpec = ui.animations.fadeTween()) { it / 3 }
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
                                    color = colorScheme.primary
                                )
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = baseFontSize.sp),
                                    color = colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        if (index < suggestions.size - 1) {
                            Divider(color = colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
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
            enter = fadeIn(animationSpec = ui.animations.fadeTween()),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ui.animations.RotatingIcon(modifier = Modifier.size(32.dp)) { mod ->
                    CircularProgressIndicator(
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
            val colorScheme = MaterialTheme.colorScheme
            Text(
                text = "Select a word from chat or use the buttons below for AI assistance",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = (baseFontSize * 0.875).sp),
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
