package ui.suggestions

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.model.WritingAnalysis
import data.settings.FontSize
import theme.*

@Composable
internal fun WritingAssistantHeader(
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
                modifier = Modifier.size(28.dp).scale(scale)
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
                IconButton(onClick = onRequestAnalysis, modifier = Modifier.size(32.dp)) {
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
internal fun WritingAssistantQuickActions(
    analysis: WritingAnalysis?,
    onRequestAnalysis: () -> Unit,
    fontSize: FontSize
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            analysis?.let {
                if (it.learningOpportunities.isNotEmpty()) {
                    AssistChip(
                        onClick = { },
                        label = { Text("${it.learningOpportunities.size} to learn") },
                        leadingIcon = {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
                
                if (it.grammarErrors.isNotEmpty()) {
                    AssistChip(
                        onClick = { },
                        label = { Text("${it.grammarErrors.size} fixes") },
                        leadingIcon = {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = AssistChipDefaults.assistChipColors(leadingIconContentColor = Error)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(onClick = onRequestAnalysis, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "Analyze again", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
internal fun CategoryFilterChips(
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
internal fun CategoryHeader(
    category: SuggestionCategory,
    count: Int,
    fontSize: FontSize
) {
    val (icon, colors) = getCategoryVisuals(category)
    
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
        Surface(color = colors.first.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.first,
                modifier = Modifier.padding(4.dp).size(16.dp)
            )
        }
        
        Text(
            text = category.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium.copy(fontSize = textSize),
            color = Gray700,
            fontWeight = FontWeight.SemiBold
        )
        
        Badge(containerColor = colors.first.copy(alpha = 0.2f), contentColor = colors.first) {
            Text(text = count.toString(), style = MaterialTheme.typography.labelSmall.copy(fontSize = textSize - 2.sp))
        }
    }
}
