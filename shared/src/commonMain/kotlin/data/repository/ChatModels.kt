package data.repository

import kotlinx.datetime.Instant

data class ChatSession(
    val id: Long,
    val title: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class ChatMessage(
    val id: Long,
    val sessionId: Long,
    val content: String,
    val isUser: Boolean,
    val timestamp: Instant
)
