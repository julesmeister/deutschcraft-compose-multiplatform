package ui.suggestions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.model.*
import data.settings.FontSize
import kotlinx.coroutines.flow.Flow
import theme.Gray200

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
        WritingAssistantHeader(
            analysis = analysis,
            isAnalyzing = analyzing,
            onRequestAnalysis = onRequestAnalysis,
            fontSize = fontSize
        )
        
        Divider(color = Gray200)
        
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            when {
                analyzing -> AnalyzingState(fontSize = fontSize)
                analysis == null -> EmptyWritingState(fontSize = fontSize)
                suggestions.isEmpty() -> NoSuggestionsState(analysis = analysis!!, fontSize = fontSize)
                else -> SuggestionsList(
                    analysis = analysis!!,
                    suggestions = suggestions,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    expandedSuggestionId = expandedSuggestionId,
                    onExpandToggle = { expandedSuggestionId = if (expandedSuggestionId == it) null else it },
                    onApply = onApplySuggestion,
                    onDismiss = onDismissSuggestion,
                    onShowExercise = onShowExercise,
                    fontSize = fontSize
                )
            }
        }
        
        Divider(color = Gray200)
        
        WritingAssistantQuickActions(
            analysis = analysis,
            onRequestAnalysis = onRequestAnalysis,
            fontSize = fontSize
        )
    }
}

@Composable
private fun SuggestionsList(
    analysis: WritingAnalysis,
    suggestions: List<QuickSuggestion>,
    selectedCategory: SuggestionCategory?,
    onCategorySelected: (SuggestionCategory?) -> Unit,
    expandedSuggestionId: String?,
    onExpandToggle: (String) -> Unit,
    onApply: (SuggestionAction) -> Unit,
    onDismiss: () -> Unit,
    onShowExercise: (PracticeExercise) -> Unit,
    fontSize: FontSize
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CategoryFilterChips(
                suggestions = suggestions,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }
        
        val grouped = if (selectedCategory != null) {
            mapOf(selectedCategory to suggestions.filter { it.type == selectedCategory })
        } else {
            suggestions.groupBy { it.type }
        }
        
        grouped.forEach { (category, categorySuggestions) ->
            item {
                CategoryHeader(category = category, count = categorySuggestions.size, fontSize = fontSize)
            }
            
            items(categorySuggestions, key = { it.id }) { suggestion ->
                SuggestionCard(
                    suggestion = suggestion,
                    isExpanded = expandedSuggestionId == suggestion.id,
                    fontSize = fontSize,
                    onExpandToggle = { onExpandToggle(suggestion.id) },
                    onApply = { onApply(suggestion.action) },
                    onDismiss = onDismiss,
                    onShowExercise = onShowExercise
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            AnalysisDetailsSection(
                analysis = analysis,
                fontSize = fontSize,
                onShowExercise = onShowExercise
            )
        }
    }
}
