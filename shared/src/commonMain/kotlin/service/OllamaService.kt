package service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

expect object OllamaProcessManager {
    fun startOllamaServer(): Boolean
    fun isOllamaRunning(): Boolean
    fun shutdown()
}

class OllamaService {
    private val baseUrl = "http://127.0.0.1:11434"
    private val defaultModel = "llama3.2"
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 5000
        }
    }
    
    suspend fun checkConnection(): Boolean {
        return try {
            val response = client.get("$baseUrl/api/tags")
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun ensureConnection(): Boolean {
        // First, try to check if already running
        if (checkConnection()) {
            return true
        }
        
        // Try to start the server
        if (OllamaProcessManager.startOllamaServer()) {
            // Wait for server to be ready (up to 10 seconds)
            repeat(20) {
                delay(500)
                if (checkConnection()) {
                    return true
                }
            }
        }
        
        return false
    }
    
    suspend fun getAvailableModels(): List<String> {
        return try {
            val response = client.get("$baseUrl/api/tags")
            if (response.status == HttpStatusCode.OK) {
                val data = response.body<TagsResponse>()
                data.models.map { it.name }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun generateSuggestion(
        text: String,
        context: String,
        model: String = defaultModel,
        suggestionType: String = "improve"
    ): Flow<String> = flow {
        val prompt = when (suggestionType) {
            "improve" -> """You are a German writing assistant. The user is learning German and writing in German. Improve the following German text to make it clearer, more professional, and more engaging. 
IMPORTANT: Your response MUST be in German only. Do not use English.

Context: $context

Text to improve:
"$text"

Provide only the improved German text without explanations or quotes:"""
            "grammar" -> """You are a German grammar checker. The user is learning German and writing in German. Fix any grammar, spelling, or punctuation errors in the following German text.
IMPORTANT: Your response MUST be in German only. Do not use English.

Context: $context

Text to check:
"$text"

Provide only the corrected German text without explanations or quotes:"""
            "rephrase" -> """You are a German writing assistant. The user is learning German and writing in German. Rephrase the following German text in a different way while maintaining the same meaning.
IMPORTANT: Your response MUST be in German only. Do not use English.

Context: $context

Text to rephrase:
"$text"

Provide only the rephrased German text without explanations or quotes:"""
            "expand" -> """You are a German writing assistant. The user is learning German and writing in German. Expand on the following German text with more detail and depth.
IMPORTANT: Your response MUST be in German only. Do not use English.

Context: $context

Text to expand:
"$text"

Provide only the expanded German text without explanations or quotes:"""
            else -> """You are a German writing assistant. The user is learning German. Help with the following German text.
IMPORTANT: Your response MUST be in German only. Do not use English.

Context: $context

Text:
"$text"

Provide only the result in German without explanations or quotes:"""
        }
        
        val requestBody = GenerateRequest(
            model = model,
            prompt = prompt,
            stream = false,
            system = null,
            options = null
        )
        
        try {
            val response = client.post("$baseUrl/api/generate") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            if (response.status == HttpStatusCode.OK) {
                val data = response.body<GenerateResponse>()
                data.response?.let { emit(it) }
            } else {
                emit("Error: Unable to generate suggestion")
            }
        } catch (e: Exception) {
            emit("Error: ${e.message}")
        }
    }
    
    suspend fun analyzeText(text: String): String {
        val prompt = """Analyze the following text and provide a brief summary of:
1. Tone (e.g., formal, casual, persuasive)
2. Readability (easy, moderate, complex)
3. Key points covered

Text:
"$text"

Keep your response concise (2-3 sentences max)."""
        
        val requestBody = GenerateRequest(
            model = defaultModel,
            prompt = prompt,
            stream = false,
            system = null,
            options = null
        )
        
        return try {
            val response = client.post("$baseUrl/api/generate") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            if (response.status == HttpStatusCode.OK) {
                val data = response.body<GenerateResponse>()
                data.response ?: "Unable to analyze text"
            } else {
                "Unable to analyze text"
            }
        } catch (e: Exception) {
            "Error analyzing text: ${e.message}"
        }
    }
    
    suspend fun suggestContinuation(
        fullText: String,
        selectedText: String,
        model: String = defaultModel
    ): List<String> {
        val prompt = """Based on the following German text context and the selected sentence, suggest 3 different ways to continue or expand on the text in German.
Each suggestion should be a complete sentence or short paragraph in German that flows naturally from the selected text.
IMPORTANT: Your response MUST be in German only. Do not use English.

Full context:
""" + fullText.take(800) + """

Selected text:
"$selectedText"

Provide exactly 3 suggestions in German, numbered 1., 2., 3. Keep each suggestion concise (max 2 sentences)."""
        
        val requestBody = GenerateRequest(
            model = model,
            prompt = prompt,
            stream = false,
            system = null,
            options = null
        )
        
        return try {
            val response = client.post("$baseUrl/api/generate") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            if (response.status == HttpStatusCode.OK) {
                val data = response.body<GenerateResponse>()
                val rawResponse = data.response ?: ""
                
                // Parse the numbered list from the response
                rawResponse.lines()
                    .filter { it.trim().matches(Regex("^\\d+[.\\)]\\s+.+")) }
                    .map { it.replace(Regex("^\\d+[.\\)]\\s*"), "").trim() }
                    .filter { it.isNotEmpty() }
                    .takeIf { it.isNotEmpty() }
                    ?: rawResponse.split("\n\n")
                        .filter { it.isNotBlank() }
                        .map { it.trim() }
                        .take(3)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
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
        
        val requestBody = GenerateRequest(
            model = model,
            prompt = prompt,
            stream = false,
            system = null,
            options = null
        )
        
        return try {
            val response = client.post("$baseUrl/api/generate") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            if (response.status == HttpStatusCode.OK) {
                val data = response.body<GenerateResponse>()
                data.response?.trim() ?: "No response from AI"
            } else {
                "Error: Unable to get response"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
    
    /**
     * Generate a raw response from the AI with optional system prompt and temperature control.
     * Used for structured JSON responses.
     */
    suspend fun generateRawResponse(
        prompt: String,
        model: String = defaultModel,
        systemPrompt: String? = null,
        temperature: Double? = null,
        maxTokens: Int? = null
    ): String {
        val requestBody = GenerateRequest(
            model = model,
            prompt = prompt,
            stream = false,
            system = systemPrompt,
            options = if (temperature != null || maxTokens != null) {
                ModelOptions(
                    temperature = temperature,
                    num_predict = maxTokens
                )
            } else null
        )
        
        return try {
            val response = client.post("$baseUrl/api/generate") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            if (response.status == HttpStatusCode.OK) {
                val data = response.body<GenerateResponse>()
                data.response?.trim() ?: ""
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Suggest a new title for a chat session based on the conversation content.
     */
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
        
        return generateRawResponse(prompt, model, maxTokens = 20)
    }
    
    /**
     * Suggest conversation directions or topics to explore based on current chat.
     */
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
        
        val response = generateRawResponse(prompt, model, maxTokens = 100)
        
        return response.lines()
            .filter { it.trim().matches(Regex("^\\d+[.\\)]\\s+.+")) }
            .map { it.replace(Regex("^\\d+[.\\)]\\s*"), "").trim() }
            .filter { it.isNotEmpty() }
            .takeIf { it.isNotEmpty() }
            ?: emptyList()
    }
    
    /**
     * Translate a German word or phrase to English with context.
     */
    suspend fun translateGermanWord(
        word: String,
        context: String = "",
        model: String = defaultModel
    ): String {
        val prompt = if (context.isNotBlank()) {
            """Translate the following German word/phrase to English. Consider the context provided.

German word/phrase: "$word"
Context: "$context"

Provide:
1. The English translation
2. A brief example sentence in German using this word
3. The English translation of the example sentence

Format your response as:
Translation: [English translation]
Example: [German example sentence]
Meaning: [English translation of example]"""
        } else {
            """Translate the following German word/phrase to English.

German word/phrase: "$word"

Provide:
1. The English translation
2. A brief example sentence in German using this word
3. The English translation of the example sentence

Format your response as:
Translation: [English translation]
Example: [German example sentence]
Meaning: [English translation of example]"""
        }
        
        return generateRawResponse(prompt, model, maxTokens = 150)
    }
    
    /**
     * Suggest possible user responses based on the AI's last message.
     * This helps users continue the conversation naturally.
     */
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
        
        val response = generateRawResponse(prompt, model, maxTokens = 120)
        
        println("[DEBUG] suggestUserResponses raw response: $response")
        
        val parsed = response.lines()
            .filter { it.trim().matches(Regex("^\\d+[.\\)]\\s+.+")) }
            .map { it.replace(Regex("^\\d+[.\\)]\\s*"), "").trim() }
            .filter { it.isNotEmpty() }
            .takeIf { it.isNotEmpty() }
            ?: emptyList()
        
        println("[DEBUG] suggestUserResponses parsed: $parsed")
        
        return parsed
    }

    /**
     * Analyze a German word for grammar, usage, and examples.
     */
    suspend fun analyzeGermanWord(
        word: String,
        model: String = defaultModel
    ): String {
        val prompt = """Analyze the following German word/phrase and provide helpful information for a German learner:

Word: "$word"

Provide:
1. Part of speech (e.g., noun, verb, adjective)
2. Article and plural form if it's a noun (e.g., "der Tisch, die Tische")
3. Brief usage explanation
4. One example sentence in German

Keep your response concise."""
        
        return generateRawResponse(prompt, model, maxTokens = 150)
    }
}

@Serializable
data class TagsResponse(
    val models: List<ModelInfo>
)

@Serializable
data class ModelInfo(
    val name: String
)

@Serializable
data class GenerateRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean,
    val system: String? = null,
    val options: ModelOptions? = null
)

@Serializable
data class ModelOptions(
    val temperature: Double? = null,
    val num_predict: Int? = null
)

@Serializable
data class GenerateResponse(
    val response: String? = null,
    val done: Boolean = false
)
