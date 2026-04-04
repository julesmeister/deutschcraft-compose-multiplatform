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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.settings.FontSize
import theme.*
import ui.chat.debugConstraints
import ui.suggestions.animations.StaggeredAnimatedContainer
import ui.suggestions.animations.PulsingDots
import ui.suggestions.animations.fadeTween
import ui.suggestions.animations.TypewriterText
import ui.suggestions.animations.BounceIn
import ui.suggestions.animations.StaggeredFadeIn
import ui.suggestions.animations.PulsingIcon

@Composable
internal fun AutoSuggestionsContent(
    suggestions: List<String>,
    fontSize: FontSize = FontSize.MEDIUM,
    onSuggestionClick: (String) -> Unit,
) {
    val baseFontSize = when (fontSize) {
        FontSize.SMALL -> 14f
        FontSize.MEDIUM -> 16f
        FontSize.LARGE -> 20f
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Success.copy(alpha = 0.08f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Suggested Replies",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = (baseFontSize * 0.875).sp),
                    color = Success,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Suggestions with staggered animation
            StaggeredAnimatedContainer(
                itemCount = suggestions.size,
                modifier = Modifier.fillMaxWidth()
            ) { index, itemModifier ->
                val suggestion = suggestions[index]
                if (index > 0) {
                    Divider(color = Gray200, modifier = Modifier.padding(vertical = 8.dp))
                }

                OutlinedButton(
                    onClick = { onSuggestionClick(suggestion) },
                    modifier = itemModifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Gray800
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Gray300)
                    )
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = baseFontSize.sp),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
                    )
                }
            }
        }
    }
}

@Composable
internal fun SuggestionsContent(
    selectedText: String,
    isFullDocument: Boolean = false,
    currentSuggestion: String,
    isGenerating: Boolean,
    activeAction: String?,
    suggestions: List<String>,
    fontSize: FontSize = FontSize.MEDIUM,
    onApply: () -> Unit,
    onAppend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Convert FontSize enum to base sp value
    val baseFontSize = when (fontSize) {
        FontSize.SMALL -> 14f
        FontSize.MEDIUM -> 16f
        FontSize.LARGE -> 20f
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()  // FIX: Use fillMaxHeight for bounded constraints
            .verticalScroll(rememberScrollState())
            .debugConstraints("SuggestionsContent Column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Results section with animated visibility
        AnimatedVisibility(
            visible = isGenerating || currentSuggestion.isNotEmpty() || suggestions.isNotEmpty(),
            enter = expandVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                expandFrom = Alignment.Top
            ) + fadeIn(animationSpec = fadeTween()),
            exit = shrinkVertically(
                animationSpec = tween(200),
                shrinkTowards = Alignment.Top
            ) + fadeOut(animationSpec = tween(200))
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Indigo.copy(alpha = 0.05f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with action type
                    activeAction?.let { action ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val (icon, label) = when (action) {
                                "grammar" -> Pair(Icons.Default.CheckCircle, "Grammar Check")
                                "improve" -> Pair(Icons.Default.Edit, "Improved Version")
                                "rephrase" -> Pair(Icons.Default.Refresh, "Rephrased")
                                "continue" -> Pair(Icons.Default.AddCircle, "Suggestions to Continue")
                                else -> Pair(Icons.Default.Star, "AI Suggestion")
                            }
                            Icon(icon, contentDescription = null, tint = Indigo, modifier = Modifier.size(18.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = (baseFontSize * 0.875).sp),
                                color = Indigo,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Loading state with pulsing dots
                    if (isGenerating && currentSuggestion.isEmpty() && suggestions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            PulsingDots(
                                dotCount = 3,
                                dotSize = 8.dp,
                                color = Indigo
                            )
                        }
                    }
                    
                    // Single suggestion result with typewriter effect
                    if (currentSuggestion.isNotEmpty()) {
                        TypewriterText(
                            text = currentSuggestion,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = baseFontSize.sp),
                            typingDelayMs = 8,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Action buttons for single suggestion with bounce
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            var applyButtonVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(currentSuggestion) {
                                applyButtonVisible = false
                                kotlinx.coroutines.delay(400)
                                applyButtonVisible = true
                            }
                            val applyButtonScale by animateFloatAsState(
                                targetValue = if (applyButtonVisible) 1f else 0.8f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioHighBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "apply_bounce"
                            )
                            Button(
                                onClick = onApply,
                                modifier = Modifier
                                    .weight(1f)
                                    .graphicsLayer { scaleX = applyButtonScale; scaleY = applyButtonScale },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Indigo
                                )
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Apply")
                            }
                        }
                    }
                    
                    // Multiple suggestions (for "suggest more") with staggered fade-in
                    if (suggestions.isNotEmpty()) {
                        StaggeredFadeIn(
                            itemCount = suggestions.size,
                            staggerDelayMs = 60
                        ) { index, itemModifier ->
                            val suggestion = suggestions[index]
                            if (index > 0) {
                                Divider(color = Gray200, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            
                            Column(modifier = itemModifier.fillMaxWidth()) {
                                Text(
                                    text = "${index + 1}. $suggestion",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = baseFontSize.sp),
                                    color = Gray800,
                                    lineHeight = (baseFontSize * 1.5).sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    var useButtonVisible by remember(index) { mutableStateOf(false) }
                                    LaunchedEffect(index) {
                                        useButtonVisible = false
                                        kotlinx.coroutines.delay(200 + index * 100L)
                                        useButtonVisible = true
                                    }
                                    val useButtonScale by animateFloatAsState(
                                        targetValue = if (useButtonVisible) 1f else 0.8f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        ),
                                        label = "use_bounce_$index"
                                    )
                                    Button(
                                        onClick = { onAppend(suggestion) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .graphicsLayer { scaleX = useButtonScale; scaleY = useButtonScale },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Indigo
                                        )
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Use This")
                                    }
                                }
                            }
                        }
                    }
                    
                    // Dismiss button
                    if (!isGenerating && (currentSuggestion.isNotEmpty() || suggestions.isNotEmpty())) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Dismiss", color = Gray500)
                        }
                    }
                }
            }
        }
        
        // Hint when no results yet - with animated entrance
        AnimatedVisibility(
            visible = !isGenerating && currentSuggestion.isEmpty() && suggestions.isEmpty(),
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + 
                    slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { it / 3 },
            exit = fadeOut(animationSpec = tween(200)) + 
                   slideOutVertically(animationSpec = tween(200)) { it / 3 }
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Gray50
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PulsingIcon(modifier = Modifier.size(24.dp)) { mod ->
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = mod
                        )
                    }
                    Text(
                        text = "Choose an action below to get AI assistance for your selected text.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = (baseFontSize * 0.875).sp),
                        color = Gray500
                    )
                }
            }
        }
    }
}
