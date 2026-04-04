package ui.suggestions

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.settings.FontSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import service.OllamaService
import theme.*

internal fun generateSuggestion(
    scope: CoroutineScope,
    ollamaService: OllamaService,
    selectedText: String,
    fullText: String,
    suggestionType: String,
    model: String,
    onStart: () -> Unit,
    onChunk: (String) -> Unit,
    onComplete: () -> Unit,
    onError: (String) -> Unit
) {
    scope.launch {
        onStart()
        try {
            ollamaService.generateSuggestion(
                text = selectedText,
                context = "Full document context: ${fullText.take(500)}",
                model = model,
                suggestionType = suggestionType
            ).collect { chunk ->
                onChunk(chunk)
            }
            onComplete()
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error")
        }
    }
}

@Composable
internal fun EmptyState(
    message: String = "Select text from the editor to get AI suggestions. Try selecting a sentence or paragraph.",
    fontSize: FontSize = FontSize.MEDIUM
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Gray300,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        val textSize = when (fontSize) {
            FontSize.SMALL -> 14.sp
            FontSize.MEDIUM -> 16.sp
            FontSize.LARGE -> 20.sp
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = textSize),
            color = Gray400,
            textAlign = TextAlign.Center
        )
    }
}
