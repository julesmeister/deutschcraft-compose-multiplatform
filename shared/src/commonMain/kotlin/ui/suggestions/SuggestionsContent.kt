package ui.suggestions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.*
import ui.components.m3.ShimmerEffect

@Composable
internal fun SuggestionsContent(
    selectedText: String,
    currentSuggestion: String,
    isGenerating: Boolean,
    activeAction: String?,
    suggestions: List<String>,
    onApply: () -> Unit,
    onAppend: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Selected text card - compact
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Gray50
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "SELECTED TEXT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (selectedText.length > 200) selectedText.take(200) + "..." else selectedText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray700,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // Results section
        if (isGenerating || currentSuggestion.isNotEmpty() || suggestions.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Indigo.copy(alpha = 0.05f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Indigo.copy(alpha = 0.3f))
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
                                style = MaterialTheme.typography.labelMedium,
                                color = Indigo,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Loading state
                    if (isGenerating && currentSuggestion.isEmpty() && suggestions.isEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ShimmerEffect(height = 16.dp, widthFraction = 0.9f)
                            ShimmerEffect(height = 16.dp, widthFraction = 0.7f)
                            ShimmerEffect(height = 16.dp, widthFraction = 0.8f)
                        }
                    }
                    
                    // Single suggestion result
                    if (currentSuggestion.isNotEmpty()) {
                        Text(
                            text = currentSuggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray800,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Action buttons for single suggestion
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = onApply,
                                modifier = Modifier.weight(1f),
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
                    
                    // Multiple suggestions (for "suggest more")
                    if (suggestions.isNotEmpty()) {
                        suggestions.forEachIndexed { index, suggestion ->
                            if (index > 0) {
                                Divider(color = Gray200, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            
                            Text(
                                text = "${index + 1}. $suggestion",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray800,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Button(
                                    onClick = onAppend,
                                    modifier = Modifier.weight(1f),
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
        
        // Hint when no results yet
        if (!isGenerating && currentSuggestion.isEmpty() && suggestions.isEmpty()) {
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
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Gray400,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Choose an action below to get AI assistance for your selected text.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
            }
        }
    }
}
