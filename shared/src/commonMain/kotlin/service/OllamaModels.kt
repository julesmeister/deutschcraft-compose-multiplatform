package service

import kotlinx.serialization.Serializable

/**
 * Data models for Ollama API requests and responses.
 */

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
