package ui

import data.repository.ChatRepository
import data.repository.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import service.OllamaService

/**
 * Handles chat session management operations.
 */
class ChatSessionManager(
    private val scope: CoroutineScope,
    private val chatRepository: ChatRepository,
    private var currentSessionId: Long?,
    private var sessions: List<data.repository.ChatSession>,
    private var messages: List<ChatMessage>,
    private val onSessionsChanged: (List<data.repository.ChatSession>) -> Unit,
    private val onCurrentSessionChanged: (Long?) -> Unit,
    private val onMessagesChanged: (List<ChatMessage>) -> Unit
) {
    fun createNewSession() {
        scope.launch {
            if (!isActive) return@launch
            val newId = chatRepository.createSession()
            if (!isActive) return@launch
            onCurrentSessionChanged(newId)
            val updatedSessions = chatRepository.getAllSessions()
            onSessionsChanged(updatedSessions)
            onMessagesChanged(emptyList())
        }
    }

    fun switchSession(sessionId: Long) {
        onCurrentSessionChanged(sessionId)
        scope.launch {
            if (!isActive) return@launch
            val sessionMessages = chatRepository.getMessagesForSession(sessionId)
            onMessagesChanged(sessionMessages)
        }
    }

    fun deleteSession(sessionId: Long) {
        scope.launch {
            if (!isActive) return@launch
            chatRepository.deleteSession(sessionId)
            if (!isActive) return@launch
            val updatedSessions = chatRepository.getAllSessions()
            onSessionsChanged(updatedSessions)
            if (!isActive) return@launch
            if (currentSessionId == sessionId) {
                val newCurrentId = updatedSessions.firstOrNull()?.id
                onCurrentSessionChanged(newCurrentId)
                val newMessages = newCurrentId?.let { chatRepository.getMessagesForSession(it) } ?: emptyList()
                onMessagesChanged(newMessages)
            }
        }
    }
}

/**
 * Handles chat message operations like edit, delete, and regenerate.
 */
class ChatMessageManager(
    private val scope: CoroutineScope,
    private val chatRepository: ChatRepository,
    private val ollamaService: OllamaService,
    private var messages: List<ChatMessage>,
    private val currentSessionId: () -> Long?,
    private val selectedModel: () -> String,
    private val systemContext: () -> String,
    private val onMessagesChanged: (List<ChatMessage>) -> Unit,
    private val onGeneratingChanged: (Boolean) -> Unit,
    private val onSessionsChanged: (List<data.repository.ChatSession>) -> Unit
) {
    fun editMessage(messageId: Long, newContent: String) {
        scope.launch {
            if (!isActive) return@launch
            chatRepository.updateMessage(messageId, newContent)
            if (!isActive) return@launch
            currentSessionId()?.let { sessionId ->
                onMessagesChanged(chatRepository.getMessagesForSession(sessionId))
            }
        }
    }

    fun deleteMessage(messageId: Long) {
        scope.launch {
            if (!isActive) return@launch
            chatRepository.deleteMessage(messageId)
            if (!isActive) return@launch
            currentSessionId()?.let { sessionId ->
                onMessagesChanged(chatRepository.getMessagesForSession(sessionId))
            }
        }
    }

    fun regenerateMessage(messageId: Long) {
        scope.launch {
            if (!isActive) return@launch

            // Find the message to regenerate
            val messageToRegenerate = messages.find { it.id == messageId }
            if (messageToRegenerate == null || messageToRegenerate.isUser) return@launch

            // Find the previous user message
            val messageIndex = messages.indexOfFirst { it.id == messageId }
            if (messageIndex <= 0) return@launch

            val sessionId = currentSessionId() ?: return@launch

            // Delete the AI message
            chatRepository.deleteMessage(messageId)
            if (!isActive) return@launch

            // Get updated messages for context
            val updatedMessages = chatRepository.getMessagesForSession(sessionId)
            onMessagesChanged(updatedMessages)
            if (!isActive) return@launch

            // Get context messages (up to the user message before this AI response)
            val contextMessages = updatedMessages.take(messageIndex)

            // Generate new response
            onGeneratingChanged(true)
            try {
                val response = ollamaService.chat(
                    messages = contextMessages,
                    systemContext = systemContext(),
                    model = selectedModel()
                )

                if (!isActive) return@launch

                // Save new AI response
                chatRepository.addMessage(sessionId, response, isUser = false)
                onMessagesChanged(chatRepository.getMessagesForSession(sessionId))
                onSessionsChanged(chatRepository.getAllSessions())
            } catch (e: Exception) {
                if (!isActive) return@launch
                val errorMsg = if (e is kotlinx.coroutines.CancellationException) {
                    "Generation stopped."
                } else {
                    "Error: ${e.message}"
                }
                chatRepository.addMessage(sessionId, errorMsg, isUser = false)
                onMessagesChanged(chatRepository.getMessagesForSession(sessionId))
            } finally {
                onGeneratingChanged(false)
            }
        }
    }
}
