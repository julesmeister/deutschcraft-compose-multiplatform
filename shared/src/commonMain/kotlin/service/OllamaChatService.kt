package service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Chat-specific Ollama service methods.
 */
class OllamaChatService(private val baseService: OllamaService) {
    
    private val baseUrl = "http://127.0.0.1:11434"
    private val defaultModel = "llama3.2"
    private val client = baseService.getClient()
    
    suspend fun chat(
        messages: List<data.repository.ChatMessage>,
        systemContext: String,
        model: String = defaultModel
    ): String {
        val conversationHistory = messages.joinToString("\n") { msg ->
            val role = if (msg.isUser) "User" else "Assistant"
            "$role: ${msg.content}"
        }
        
        val prompt = """$systemContext
The user is learning German. You are a German language tutor. Respond in German to help the user practice.
IMPORTANT: Your response MUST be in German only. Do not use English unless the user explicitly asks for translation or explanation in English.

Conversation history:
$conversationHistory

As the Assistant, provide a helpful response in German:"""
        
        return baseService.generateRawResponse(prompt, model)
    }
    
    suspend fun suggestChatTitle(
        messages: List<data.repository.ChatMessage>,
        model: String = defaultModel
    ): String {
        val conversationPreview = messages.takeLast(6).joinToString("\n") { msg ->
            val role = if (msg.isUser) "User" else "Assistant"
            "$role: ${msg.content.take(100)}"
        }
        
        val prompt = """Based on the following German conversation, suggest a short, descriptive title (2-4 words max) that captures the main topic or theme.
The title should be in German.

Conversation:
$conversationPreview

Provide only the title, nothing else:"""
        
        return baseService.generateRawResponse(prompt, model, maxTokens = 20)
    }
    
    suspend fun suggestConversationDirections(
        messages: List<data.repository.ChatMessage>,
        model: String = defaultModel
    ): List<String> {
        val conversationHistory = messages.takeLast(10).joinToString("\n") { msg ->
            val role = if (msg.isUser) "User" else "Assistant"
            "$role: ${msg.content.take(80)}"
        }
        
        val prompt = """Based on this German conversation, suggest 3 different directions or topics the user could explore next to continue practicing German.
Each suggestion should be a brief question or topic idea in German that naturally follows from the conversation.
IMPORTANT: Your response MUST be in German only.

Conversation:
$conversationHistory

Provide exactly 3 suggestions in German, numbered 1., 2., 3. Each should be concise (max 10 words):"""
        
        val response = baseService.generateRawResponse(prompt, model, maxTokens = 100)
        
        return response.lines()
            .filter { it.trim().matches(Regex("^\\d+[.\\)]\\s+.+")) }
            .map { it.replace(Regex("^\\d+[.\\)]\\s*"), "").trim() }
            .filter { it.isNotEmpty() }
            .takeIf { it.isNotEmpty() }
            ?: emptyList()
    }
    
    suspend fun suggestUserResponses(
        messages: List<data.repository.ChatMessage>,
        model: String = defaultModel
    ): List<String> {
        if (messages.isEmpty()) return emptyList()
        
        val conversationHistory = messages.takeLast(8).joinToString("\n") { msg ->
            val role = if (msg.isUser) "User" else "Assistant"
            "$role: ${msg.content.take(100)}"
        }
        
        val lastAiMessage = messages.lastOrNull { !it.isUser }?.content?.take(150) ?: ""
        
        val prompt = """Based on this German conversation, suggest 3 different ways the user could respond to continue the conversation naturally.
Each suggestion should be a brief response in German (5-15 words) that makes sense given what the AI just said.
The suggestions should vary in style: one could be a follow-up question, one an agreement/acknowledgment, and one a related comment.
IMPORTANT: Your response MUST be in German only.

Conversation:
$conversationHistory

Provide exactly 3 brief response suggestions in German, numbered 1., 2., 3. Each should be something the USER could say next:"""
        
        val response = baseService.generateRawResponse(prompt, model, maxTokens = 120)
        
        return response.lines()
            .filter { it.trim().matches(Regex("^\\d+[.\\)]\\s+.+")) }
            .map { it.replace(Regex("^\\d+[.\\)]\\s*"), "").trim() }
            .filter { it.isNotEmpty() }
            .takeIf { it.isNotEmpty() }
            ?: emptyList()
    }
}
