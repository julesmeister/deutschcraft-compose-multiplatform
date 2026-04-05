package ui.suggestions

import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.settings.FontSize
import theme.*
import ui.components.m3.*


@Composable
internal fun EmptyState(
    message: String = "Select text from the editor to get AI suggestions. Try selecting a sentence or paragraph.",
    fontSize: FontSize = FontSize.MEDIUM
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "empty_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "empty_alpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .scale(scale)
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        NoDataPlaceholder(
            message = message,
            icon = Icons.Default.Edit,
            fontSize = fontSize
        )
    }
}

@Composable
internal fun NoDataPlaceholder(
    message: String,
    icon: ImageVector = Icons.Default.Info,
    fontSize: FontSize = FontSize.MEDIUM,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        val textSize = when (fontSize) {
            FontSize.EXTRA_SMALL -> 12.sp
            FontSize.SMALL -> 14.sp
            FontSize.MEDIUM -> 16.sp
            FontSize.LARGE -> 20.sp
            FontSize.EXTRA_LARGE -> 24.sp
            FontSize.HUGE -> 28.sp
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = textSize),
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun ChatContextualActions(
    selectedText: String,
    chatMessages: List<data.repository.ChatMessage>,
    isGenerating: Boolean,
    activeAction: String?,
    onSuggestTitle: () -> Unit,
    onSuggestDirections: () -> Unit,
    onTranslate: () -> Unit,
    onAnalyzeWord: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Chat management actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                icon = Icons.Default.Title,
                label = "Suggest Title",
                isActive = activeAction == "title",
                isLoading = isGenerating && activeAction == "title",
                onClick = onSuggestTitle,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                icon = Icons.Default.Explore,
                label = "Directions",
                isActive = activeAction == "directions",
                isLoading = isGenerating && activeAction == "directions",
                onClick = onSuggestDirections,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Row 2: Word-level actions (only enabled when text is selected)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                icon = Icons.Default.Translate,
                label = "Translate",
                isActive = activeAction == "translate",
                isLoading = isGenerating && activeAction == "translate",
                isEnabled = selectedText.isNotBlank(),
                onClick = onTranslate,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                icon = Icons.Default.MenuBook,
                label = "Analyze",
                isActive = activeAction == "analyze",
                isLoading = isGenerating && activeAction == "analyze",
                isEnabled = selectedText.isNotBlank(),
                onClick = onAnalyzeWord,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

