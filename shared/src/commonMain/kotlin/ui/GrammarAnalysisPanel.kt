package ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.model.*
import service.*
import theme.*
import ui.components.SegmentedSelector
import ui.grammar.tabs.ErrorsTab
import ui.grammar.tabs.OverviewTab
import ui.grammar.tabs.StrengthsTab
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
            ProgressOverviewCard(
                stats, 
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        }

        // Analysis Results
        analysisResult?.let { result ->
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Tab selection
                val tabs = listOf(
                    "Overview",
                    "Errors (${result.analysis.grammarErrors.size})",
                    "Strengths"
                )
                
                SegmentedSelector(
                    options = tabs,
                    selectedIndex = selectedTab,
                    onSelect = { selectedTab = it },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )

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
