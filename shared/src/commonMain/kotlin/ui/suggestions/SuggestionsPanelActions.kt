package ui.suggestions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import service.OllamaService
import data.repository.ChatMessage
import data.settings.FontSize

/**
 * Action handlers for the SuggestionsPanel.
 * These are extracted to reduce the size of the main composable.
 */

/**
 * Data class holding all action callbacks for Chat mode
 */
internal data class ChatActions(
    val onSuggestTitle: () -> Unit,
    val onSuggestDirections: () -> Unit,
    val onTranslate: () -> Unit,
    val onAnalyzeWord: () -> Unit
)

/**
 * Data class holding all action callbacks for Editor mode
 */
internal data class EditorActions(
    val onCheckGrammar: () -> Unit,
    val onImprove: () -> Unit,
    val onRephrase: () -> Unit,
    val onSuggestMore: () -> Unit
)

/**
 * Creates action handlers for Chat mode
 */
@Composable
internal fun rememberChatActions(
    selectedText: String,
    chatMessages: List<ChatMessage>,
    selectedModel: String,
    setGenerating: (Boolean) -> Unit,
    setActiveAction: (String?) -> Unit,
    setCurrentSuggestion: (String) -> Unit,
    setSuggestions: (List<String>) -> Unit,
    onTitleSuggestion: (String) -> Unit,
    onError: (String?) -> Unit
): ChatActions {
    val scope = rememberCoroutineScope()
    val ollamaService = remember { OllamaService() }
    
    return remember(selectedText, chatMessages, selectedModel) {
        ChatActions(
            onSuggestTitle = {
                if (chatMessages.isNotEmpty()) {
                    setActiveAction("title")
                    scope.launch {
                        setGenerating(true)
                        setCurrentSuggestion("")
                        setSuggestions(emptyList())
                        try {
                            val title = ollamaService.suggestChatTitle(
                                messages = chatMessages,
                                model = selectedModel
                            )
                            setCurrentSuggestion(title)
                            onTitleSuggestion(title)
                        } catch (e: Exception) {
                            val errorMsg = e.message ?: "Unknown error"
                            setCurrentSuggestion("Error: $errorMsg")
                            onError(errorMsg)
                        } finally {
                            setGenerating(false)
                        }
                    }
                }
            },
            onSuggestDirections = {
                if (chatMessages.isNotEmpty()) {
                    setActiveAction("directions")
                    scope.launch {
                        setGenerating(true)
                        setCurrentSuggestion("")
                        setSuggestions(emptyList())
                        try {
                            val directions = ollamaService.suggestConversationDirections(
                                messages = chatMessages,
                                model = selectedModel
                            )
                            setSuggestions(directions)
                        } catch (e: Exception) {
                            val errorMsg = e.message ?: "Unknown error"
                            setCurrentSuggestion("Error: $errorMsg")
                            onError(errorMsg)
                        } finally {
                            setGenerating(false)
                        }
                    }
                }
            },
            onTranslate = {
                if (selectedText.isNotBlank()) {
                    setActiveAction("translate")
                    scope.launch {
                        setGenerating(true)
                        setCurrentSuggestion("")
                        setSuggestions(emptyList())
                        try {
                            val translation = ollamaService.translateGermanWord(
                                word = selectedText,
                                context = "",
                                model = selectedModel
                            )
                            setCurrentSuggestion(translation)
                        } catch (e: Exception) {
                            val errorMsg = e.message ?: "Unknown error"
                            setCurrentSuggestion("Error: $errorMsg")
                            onError(errorMsg)
                        } finally {
                            setGenerating(false)
                        }
                    }
                }
            },
            onAnalyzeWord = {
                if (selectedText.isNotBlank()) {
                    setActiveAction("analyze")
                    scope.launch {
                        setGenerating(true)
                        setCurrentSuggestion("")
                        setSuggestions(emptyList())
                        try {
                            val analysis = ollamaService.analyzeGermanWord(
                                word = selectedText,
                                model = selectedModel
                            )
                            setCurrentSuggestion(analysis)
                        } catch (e: Exception) {
                            val errorMsg = e.message ?: "Unknown error"
                            setCurrentSuggestion("Error: $errorMsg")
                            onError(errorMsg)
                        } finally {
                            setGenerating(false)
                        }
                    }
                }
            }
        )
    }
}

/**
 * Creates action handlers for Editor mode
 */
@Composable
internal fun rememberEditorActions(
    selectedText: String,
    fullText: String,
    selectedModel: String,
    setGenerating: (Boolean) -> Unit,
    setActiveAction: (String?) -> Unit,
    setCurrentSuggestion: (String) -> Unit,
    setSuggestions: (List<String>) -> Unit,
    onError: (String?) -> Unit
): EditorActions {
    val scope = rememberCoroutineScope()
    val ollamaService = remember { OllamaService() }
    
    return remember(selectedText, fullText, selectedModel) {
        EditorActions(
            onCheckGrammar = {
                if (fullText.isNotBlank()) {
                    setActiveAction("grammar")
                    var accumulated = ""
                    generateSuggestion(
                        scope, ollamaService, selectedText, fullText,
                        "grammar", selectedModel,
                        onStart = { setGenerating(true); accumulated = ""; setCurrentSuggestion(""); setSuggestions(emptyList()) },
                        onChunk = { chunk -> accumulated += chunk; setCurrentSuggestion(accumulated) },
                        onComplete = { setGenerating(false) },
                        onError = { errorMsg ->
                            setCurrentSuggestion("Error: $errorMsg")
                            setGenerating(false)
                            onError(errorMsg)
                        }
                    )
                }
            },
            onImprove = {
                if (fullText.isNotBlank()) {
                    setActiveAction("improve")
                    var accumulated = ""
                    generateSuggestion(
                        scope, ollamaService, selectedText, fullText,
                        "improve", selectedModel,
                        onStart = { setGenerating(true); accumulated = ""; setCurrentSuggestion(""); setSuggestions(emptyList()) },
                        onChunk = { chunk -> accumulated += chunk; setCurrentSuggestion(accumulated) },
                        onComplete = { setGenerating(false) },
                        onError = { errorMsg ->
                            setCurrentSuggestion("Error: $errorMsg")
                            setGenerating(false)
                            onError(errorMsg)
                        }
                    )
                }
            },
            onRephrase = {
                if (fullText.isNotBlank()) {
                    setActiveAction("rephrase")
                    var accumulated = ""
                    generateSuggestion(
                        scope, ollamaService, selectedText, fullText,
                        "rephrase", selectedModel,
                        onStart = { setGenerating(true); accumulated = ""; setCurrentSuggestion(""); setSuggestions(emptyList()) },
                        onChunk = { chunk -> accumulated += chunk; setCurrentSuggestion(accumulated) },
                        onComplete = { setGenerating(false) },
                        onError = { errorMsg ->
                            setCurrentSuggestion("Error: $errorMsg")
                            setGenerating(false)
                            onError(errorMsg)
                        }
                    )
                }
            },
            onSuggestMore = {
                if (fullText.isNotBlank()) {
                    setActiveAction("continue")
                    scope.launch {
                        setGenerating(true)
                        setCurrentSuggestion("")
                        setSuggestions(emptyList())
                        try {
                            val result = ollamaService.suggestContinuation(
                                fullText = fullText,
                                selectedText = selectedText,
                                model = selectedModel
                            )
                            setSuggestions(result)
                        } catch (e: Exception) {
                            val errorMsg = e.message ?: "Unknown error"
                            setCurrentSuggestion("Error: $errorMsg")
                            onError(errorMsg)
                        } finally {
                            setGenerating(false)
                        }
                    }
                }
            }
        )
    }
}
