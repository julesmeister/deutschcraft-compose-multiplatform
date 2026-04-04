package ui.grammar.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.model.*
import theme.*

@Composable
fun OverviewTab(analysis: GrammarAnalysisResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats card
        Card(
            colors = CardDefaults.cardColors(containerColor = Gray50),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Writing Statistics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                StatRow("Word Count", analysis.stats.wordCount.toString())
                StatRow("Sentences", analysis.stats.sentenceCount.toString())
                StatRow("CEFR Level", analysis.stats.estimatedCefrLevel.name)
                StatRow("Vocabulary Diversity", "${(analysis.stats.vocabularyDiversityScore * 100).toInt()}%")
                StatRow("Complexity Score", "${(analysis.stats.complexityScore * 100).toInt()}%")
            }
        }

        // Corrected text
        if (analysis.correctedText != analysis.grammarErrors.firstOrNull()?.originalText) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Corrected Version",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = analysis.correctedText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700
                    )
                }
            }
        }

        // Suggestions
        if (analysis.suggestions.isNotEmpty()) {
            Text(
                text = "Suggestions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            analysis.suggestions.forEach { suggestion ->
                SuggestionChip(suggestion)
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray600
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Gray800
        )
    }
}

@Composable
fun SuggestionChip(suggestion: WritingSuggestion) {
    Surface(
        color = when (suggestion.type) {
            SuggestionType.VOCABULARY -> Color(0xFFE3F2FD)
            SuggestionType.STRUCTURE -> Color(0xFFFFF3E0)
            SuggestionType.STYLE -> Color(0xFFF3E5F5)
            SuggestionType.CLARITY -> Color(0xFFE8F5E9)
            SuggestionType.FORMALITY -> Color(0xFFFCE4EC)
        },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = suggestion.type.name.replace("_", " ").lowercase()
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = Gray600
            )
            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodySmall,
                color = Gray800
            )
        }
    }
}
