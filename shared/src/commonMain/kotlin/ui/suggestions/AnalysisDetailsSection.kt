package ui.suggestions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.model.PracticeExercise
import data.model.WritingAnalysis
import data.settings.FontSize
import theme.*

@Composable
internal fun AnalysisDetailsSection(
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
        colors = CardDefaults.cardColors(containerColor = Gray50),
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
            
            analysis.vocabularyInsights?.let { vocab ->
                DetailRow(
                    icon = Icons.Default.MenuBook,
                    label = "Vocabulary",
                    value = "${vocab.wordsUsed.size} words • Diversity: ${(vocab.diversityScore * 100).toInt()}%",
                    fontSize = textSize
                )
            }
            
            analysis.structureAnalysis?.let { structure ->
                DetailRow(
                    icon = Icons.Default.AccountTree,
                    label = "Structure",
                    value = "${structure.sentenceType.name.lowercase().replaceFirstChar { it.uppercase() }} • ${structure.clauses.size} clause(s)",
                    fontSize = textSize
                )
            }
            
            if (analysis.learningOpportunities.isNotEmpty()) {
                DetailRow(
                    icon = Icons.Default.School,
                    label = "Learning",
                    value = "${analysis.learningOpportunities.size} opportunities",
                    fontSize = textSize,
                    onClick = { 
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
internal fun DetailRow(
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
            Modifier.clickable(onClick = onClick).padding(vertical = 2.dp)
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
