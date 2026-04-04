package ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.model.*
import service.*
import theme.*
import kotlinx.coroutines.launch

@Composable
fun GrammarAnalysisPanel(
    text: String,
    driverFactory: data.db.DatabaseDriverFactory,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val analysisService = remember { GrammarAnalysisService(driverFactory) }
    
    var analysisResult by remember { mutableStateOf<AnalysisResult?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var progressStats by remember { mutableStateOf<UserProgressStats?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    
    // Load initial stats
    LaunchedEffect(Unit) {
        progressStats = analysisService.getProgressStats()
    }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with analyze button
        Surface(
            color = Gray50,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Grammar Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                
                Text(
                    text = if (text.isBlank()) "Write some text in the editor to get started" else "${text.split(Regex("\\s+")).size} words ready for analysis",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
                
                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            scope.launch {
                                isAnalyzing = true
                                try {
                                    analysisResult = analysisService.analyzeAndRecord(text)
                                    progressStats = analysisService.getProgressStats()
                                } finally {
                                    isAnalyzing = false
                                }
                            }
                        }
                    },
                    enabled = text.isNotBlank() && !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Indigo,
                        disabledContainerColor = Gray300
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        if (isAnalyzing) "Analyzing..." else "Analyze Text",
                        color = Color.White
                    )
                }
            }
        }
        
        // Progress Overview
        progressStats?.let { stats ->
            ProgressOverviewCard(stats, modifier = Modifier.fillMaxWidth())
        }
        
        // Analysis Results
        analysisResult?.let { result ->
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = Indigo
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Overview") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Errors (${result.analysis.grammarErrors.size})") }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Strengths") }
                        )
                    }
                    
                    // Tab content
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    ) {
                        when (selectedTab) {
                            0 -> OverviewTab(result.analysis)
                            1 -> ErrorsTab(result.analysis.grammarErrors)
                            2 -> StrengthsTab(result.analysis.strengths)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressOverviewCard(stats: UserProgressStats, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Indigo.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Indigo,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Your Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gray800
                    )
                    Text(
                        text = "Keep up the great work!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
            }
            
            Divider(color = Gray200)
            
            // Stats in a row with dividers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                M3StatItem(
                    icon = Icons.Default.Edit,
                    value = stats.totalWritingSessions.toString(),
                    label = "Sessions",
                    accentColor = Indigo
                )
                
                VerticalDivider(color = Gray200, modifier = Modifier.height(40.dp))
                
                M3StatItem(
                    icon = Icons.Default.MenuBook,
                    value = stats.totalWordsWritten.toString(),
                    label = "Words",
                    accentColor = Color(0xFF4CAF50)
                )
                
                VerticalDivider(color = Gray200, modifier = Modifier.height(40.dp))
                
                M3StatItem(
                    icon = Icons.Default.School,
                    value = stats.errorFrequencyByType.size.toString(),
                    label = "Focus Areas",
                    accentColor = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
private fun M3StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    accentColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Gray800
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Gray500
        )
    }
}

@Composable
private fun OverviewTab(analysis: GrammarAnalysisResponse) {
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
private fun StatRow(label: String, value: String) {
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
private fun SuggestionChip(suggestion: WritingSuggestion) {
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

@Composable
private fun ErrorsTab(errors: List<GrammarError>) {
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
                    color = Gray500
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
private fun ErrorCard(error: GrammarError) {
    val severityColor = when (error.severity) {
        ErrorSeverity.LOW -> Color(0xFFFFB74D)
        ErrorSeverity.MEDIUM -> Color(0xFFFF9800)
        ErrorSeverity.HIGH -> Color(0xFFF57C00)
        ErrorSeverity.CRITICAL -> Color(0xFFD32F2F)
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, severityColor.copy(alpha = 0.3f))
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
                        color = Gray500
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
                    tint = Gray400,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Correction",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray500
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
                    color = Gray600
                )
            }
        }
    }
}

@Composable
private fun StrengthsTab(strengths: List<WritingStrength>) {
    if (strengths.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No strengths identified yet. Keep writing!",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            strengths.forEach { strength ->
                StrengthCard(strength)
            }
        }
    }
}

@Composable
private fun StrengthCard(strength: WritingStrength) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = strength.aspect,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32)
                )
            }
            
            Text(
                text = strength.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray700
            )
            
            if (strength.examples.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Examples:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray500
                    )
                    strength.examples.forEach { example ->
                        Text(
                            text = "• $example",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                }
            }
        }
    }
}
