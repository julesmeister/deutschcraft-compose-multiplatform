package ui.suggestions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import service.OllamaService

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
