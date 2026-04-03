package service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class OllamaService {
    private val baseUrl = "http://localhost:11434"
    private val defaultModel = "llama3.2"
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        timeout {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 5000
        }
    }
    
    suspend fun checkConnection(): Boolean {
        return try {
            val response = client.get("$baseUrl/api/tags") {
                timeout { requestTimeoutMillis = 3000 }
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
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
            "improve" -> """You are a writing assistant. Improve the following text to make it clearer, more professional, and more engaging. 
Context: $context

Text to improve:
"$text"

Provide only the improved text without explanations or quotes:"""
            "grammar" -> """You are a grammar checker. Fix any grammar, spelling, or punctuation errors in the following text.
Context: $context

Text to check:
"$text"

Provide only the corrected text without explanations or quotes:"""
            "rephrase" -> """You are a writing assistant. Rephrase the following text in a different way while maintaining the same meaning.
Context: $context

Text to rephrase:
"$text"

Provide only the rephrased text without explanations or quotes:"""
            "expand" -> """You are a writing assistant. Expand on the following text with more detail and depth.
Context: $context

Text to expand:
"$text"

Provide only the expanded text without explanations or quotes:"""
            else -> """You are a writing assistant. Help with the following text.
Context: $context

Text:
"$text"

Provide only the result without explanations or quotes:"""
        }
        
        val requestBody = GenerateRequest(
            model = model,
            prompt = prompt,
            stream = false
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
            stream = false
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
    val stream: Boolean
)

@Serializable
data class GenerateResponse(
    val response: String? = null,
    val done: Boolean = false
)
