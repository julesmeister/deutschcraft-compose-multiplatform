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
import kotlinx.serialization.json.Json

expect object OllamaProcessManager {
    fun startOllamaServer(): Boolean
    fun isOllamaRunning(): Boolean
    fun shutdown()
}

class OllamaService {
    private val baseUrl = "http://127.0.0.1:11434"
    private val defaultModel = "llama3.2"
    
    val client = HttpClient {
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
    
    fun getClient(): HttpClient = client
    
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
