package ui.chat

import data.repository.ChatRepository
import data.repository.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import service.OllamaService

class ChatSendMessageUseCase(
    private val scope: CoroutineScope,
    private val chatRepository: ChatRepository,
    private val ollamaService: OllamaService,
    private val selectedModel: () -> String,
    private val systemContext: () -> String,
    private val onMessagesChanged: (List<ChatMessage>) -> Unit,
    private val onGeneratingChanged: (Boolean) -> Unit,
    private val onSessionsChanged: (List<data.repository.ChatSession>) -> Unit,
    private val onAutoSuggestionsChange: (List<String>) -> Unit,
    private val onCategorizeSession: (Long, List<ChatMessage>) -> Unit,
    private var currentJob: Job?
) {
    operator fun invoke(
        inputText: String,
        currentSessionId: Long,
        onInputCleared: () -> Unit
    ) {
        val userMessage = inputText.trim()
        val sessionId = currentSessionId

        scope.launch {
            if (!isActive) return@launch
            // Save user message
            chatRepository.addMessage(sessionId, userMessage, isUser = true)
            if (!isActive) return@launch
            onMessagesChanged(chatRepository.getMessagesForSession(sessionId))
            if (!isActive) return@launch
            onInputCleared()

            currentJob = scope.launch {
                if (!isActive) return@launch
                onGeneratingChanged(true)
                try {
                    // Get fresh messages from DB for context
                    val sessionMessages = chatRepository.getMessagesForSession(sessionId)
                    val response = ollamaService.chat(
                        messages = sessionMessages,
                        systemContext = systemContext(),
                        model = selectedModel()
                    )

                    // Save AI response
                    chatRepository.addMessage(sessionId, response, isUser = false)
                    val updatedMessages = chatRepository.getMessagesForSession(sessionId)
                    onMessagesChanged(updatedMessages)

                    // Trigger AI categorization after AI responds
                    onCategorizeSession(sessionId, updatedMessages)

                    // Generate follow-up response suggestions
                    generateFollowUpSuggestions(updatedMessages)

                    // Update sessions list to reflect new timestamp
                    onSessionsChanged(chatRepository.getAllSessions())
                } catch (e: Exception) {
                    val errorMsg = if (e is kotlinx.coroutines.CancellationException) {
                        "Generation stopped."
                    } else {
                        "Error: ${e.message}"
                    }
                    chatRepository.addMessage(sessionId, errorMsg, isUser = false)
                    onMessagesChanged(chatRepository.getMessagesForSession(sessionId))
                } finally {
                    onGeneratingChanged(false)
                    currentJob = null
                }
            }
        }
    }

    private fun generateFollowUpSuggestions(messages: List<ChatMessage>) {
        scope.launch {
            try {
                val followUpSuggestions = ollamaService.suggestUserResponses(
                    messages = messages,
                    model = selectedModel()
                )
                onAutoSuggestionsChange(followUpSuggestions)
            } catch (e: Exception) {
                // Silently fail - suggestions are not critical
                onAutoSuggestionsChange(emptyList())
            }
        }
    }
}
