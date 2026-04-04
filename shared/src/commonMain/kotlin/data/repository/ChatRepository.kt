package data.repository

import kotlinx.datetime.Instant

interface ChatRepository {
    suspend fun createSession(title: String? = null): Long
    suspend fun getAllSessions(): List<ChatSession>
    suspend fun getSession(sessionId: Long): ChatSession?
    suspend fun addMessage(sessionId: Long, content: String, isUser: Boolean): Long
    suspend fun getMessagesForSession(sessionId: Long): List<ChatMessage>
    suspend fun getRecentMessages(limit: Int = 50): List<ChatMessage>
    suspend fun deleteMessage(messageId: Long)
    suspend fun updateMessage(messageId: Long, content: String)
    suspend fun deleteSession(sessionId: Long)
    suspend fun updateSessionTitle(sessionId: Long, title: String)
    suspend fun getOrCreateDefaultSession(): Long
}
