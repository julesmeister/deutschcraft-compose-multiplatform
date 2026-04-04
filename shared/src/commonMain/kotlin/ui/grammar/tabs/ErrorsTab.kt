package ui.grammar.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.model.ErrorSeverity
import data.model.GrammarError

@Composable
fun ErrorsTab(errors: List<GrammarError>) {
    if (errors.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "No errors found!",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = "Great job with your German writing!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.Gray500
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            errors.forEach { error ->
                ErrorCard(error)
            }
        }
    }
}

@Composable
fun ErrorCard(error: GrammarError) {
    val severityColor = when (error.severity) {
        ErrorSeverity.LOW -> Color(0xFFFFB74D)
        ErrorSeverity.MEDIUM -> Color(0xFFFF9800)
        ErrorSeverity.HIGH -> Color(0xFFF57C00)
        ErrorSeverity.CRITICAL -> Color(0xFFD32F2F)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, severityColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = severityColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = error.errorType.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = severityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = severityColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = error.severity.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Original vs Correction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Original",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.Gray500
                    )
                    Text(
                        text = "\"${error.originalText}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD32F2F)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = theme.Gray400,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Correction",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.Gray500
                    )
                    Text(
                        text = "\"${error.correction}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF388E3C)
                    )
                }
            }

            // Explanation
            if (error.explanation.isNotBlank()) {
                Text(
                    text = error.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.Gray600
                )
            }
        }
    }
}
