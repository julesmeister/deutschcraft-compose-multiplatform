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
internal fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        NoDataPlaceholder(
            message = "Select text from the editor to get AI suggestions. Try selecting a sentence or paragraph.",
            icon = Icons.Default.Edit
        )
    }
}

@Composable
internal fun NoDataPlaceholder(
    message: String,
    icon: ImageVector = Icons.Default.Info,
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
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray400,
            textAlign = TextAlign.Center
        )
    }
}
