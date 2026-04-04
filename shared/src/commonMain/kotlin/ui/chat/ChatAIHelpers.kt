package ui

import data.repository.ChatRepository
import data.repository.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import service.OllamaService

/**
 * Handles AI-powered session categorization based on conversation content.
 */
class SessionCategorizer(
    private val scope: CoroutineScope,
    private val chatRepository: ChatRepository,
    private val ollamaService: OllamaService,
    private val selectedModel: () -> String,
    private val onSessionsUpdated: () -> Unit
) {
    fun categorizeSession(sessionId: Long, messages: List<ChatMessage>) {
        if (messages.size < 2) return // Need at least a user message and AI response

        scope.launch {
            try {
                // Check if already categorized
                val session = chatRepository.getSession(sessionId) ?: return@launch
                if (session.category != null) return@launch

                // Build conversation context for categorization
                val conversationText = messages.takeLast(6).joinToString("\n") {
                    val prefix = if (it.isUser) "User: " else "AI: "
                    "$prefix${it.content.take(200)}"
                }

                val categorizePrompt = """Analyze this German learning conversation and categorize it into ONE of these categories:
- Grammar: Grammar questions, conjugation, sentence structure
- Vocabulary: Word meanings, translations, new words
- Writing: Essay help, text improvement, creative writing
- Conversation: Speaking practice, dialogues, everyday chat
- Culture: German culture, customs, regions
- Exam Prep: Test preparation, Goethe, TestDaF, DSH
- Other: General learning assistance

Conversation:
$conversationText

Respond with ONLY the category name (single word)."""

                val category = ollamaService.chat(
                    messages = listOf(
                        ui.ChatMessage(text = categorizePrompt, isUser = true, timestamp = System.currentTimeMillis())
                    ),
                    systemContext = "You are a categorization assistant. Respond with only the category name.",
                    model = selectedModel()
                ).trim().take(50)

                // Validate and clean up the category
                val validCategories = listOf("Grammar", "Vocabulary", "Writing", "Conversation", "Culture", "Exam Prep", "Other")
                val cleanCategory = validCategories.find { category.contains(it, ignoreCase = true) } ?: "Other"

                chatRepository.updateSessionCategory(sessionId, cleanCategory)
                onSessionsUpdated()
            } catch (e: Exception) {
                // Silently fail - categorization is not critical
            }
        }
    }
}

/**
 * Generates follow-up response suggestions after AI replies.
 */
class SuggestionGenerator(
    private val scope: CoroutineScope,
    private val ollamaService: OllamaService,
    private val selectedModel: () -> String,
    private val onSuggestionsGenerated: (List<String>) -> Unit
) {
    fun generateFollowUpSuggestions(messages: List<ChatMessage>) {
        scope.launch {
            try {
                val uiMessagesForSuggestions = messages.map {
                    ui.ChatMessage(
                        text = it.content,
                        isUser = it.isUser,
                        timestamp = it.timestamp.toEpochMilliseconds()
                    )
                }
                val followUpSuggestions = ollamaService.suggestUserResponses(
                    messages = uiMessagesForSuggestions,
                    model = selectedModel()
                )
                onSuggestionsGenerated(followUpSuggestions)
            } catch (e: Exception) {
                // Silently fail - suggestions are not critical
                onSuggestionsGenerated(emptyList())
            }
        }
    }
}
