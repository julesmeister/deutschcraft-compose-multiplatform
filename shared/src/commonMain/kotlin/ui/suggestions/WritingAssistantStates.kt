package ui.suggestions

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.model.WritingAnalysis
import data.settings.FontSize
import theme.*

@Composable
internal fun EmptyWritingState(fontSize: FontSize) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EditNote,
                contentDescription = null,
                tint = Gray400,
                modifier = Modifier.size(56.dp)
            )
            
            val textSize = when (fontSize) {
                FontSize.EXTRA_SMALL -> 12.sp
                FontSize.SMALL -> 14.sp
                FontSize.MEDIUM -> 16.sp
                FontSize.LARGE -> 18.sp
                FontSize.EXTRA_LARGE -> 20.sp
                FontSize.HUGE -> 22.sp
            }
            
            Text(
                text = "Start writing in German...",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = textSize),
                color = Gray600,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Text(
                text = "Suggestions will appear here when you complete a sentence",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
internal fun AnalyzingState(fontSize: FontSize) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "analyze")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
            
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp,
                    color = Indigo
                )
                
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Indigo,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            val textSize = when (fontSize) {
                FontSize.EXTRA_SMALL -> 12.sp
                FontSize.SMALL -> 14.sp
                FontSize.MEDIUM -> 16.sp
                FontSize.LARGE -> 18.sp
                FontSize.EXTRA_LARGE -> 20.sp
                FontSize.HUGE -> 22.sp
            }
            
            Text(
                text = "Analyzing your sentence...",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = textSize),
                color = Gray700
            )
            
            val analyzingMessages = listOf(
                "Checking grammar patterns...",
                "Looking up vocabulary...",
                "Analyzing sentence structure...",
                "Finding learning opportunities..."
            )
            
            var messageIndex by remember { mutableStateOf(0) }
            
            LaunchedEffect(Unit) {
                while (true) {
                    delay(1500)
                    messageIndex = (messageIndex + 1) % analyzingMessages.size
                }
            }
            
            AnimatedContent(
                targetState = messageIndex,
                transitionSpec = { fadeIn() + slideInVertically { it } togetherWith fadeOut() + slideOutVertically { -it } }
            ) { index ->
                Text(
                    text = analyzingMessages[index],
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }
    }
}

@Composable
internal fun NoSuggestionsState(
    analysis: WritingAnalysis,
    fontSize: FontSize
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                color = Emerald.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Emerald,
                    modifier = Modifier.padding(16.dp).size(40.dp)
                )
            }
            
            val textSize = when (fontSize) {
                FontSize.EXTRA_SMALL -> 14.sp
                FontSize.SMALL -> 16.sp
                FontSize.MEDIUM -> 18.sp
                FontSize.LARGE -> 20.sp
                FontSize.EXTRA_LARGE -> 22.sp
                FontSize.HUGE -> 24.sp
            }
            
            Text(
                text = "Great sentence!",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = textSize),
                color = Gray800,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "No suggestions needed. Your ${analysis.sentenceLevel.name} level sentence looks good.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            analysis.vocabularyInsights?.let { vocab ->
                if (vocab.advancedVocabulary.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✨ Advanced words used: ${vocab.advancedVocabulary.take(3).joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Indigo,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
