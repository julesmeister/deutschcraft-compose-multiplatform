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
    suspend fun updateSessionCategory(sessionId: Long, category: String?)
    suspend fun getSessionsByCategory(category: String): List<ChatSession>
    suspend fun getAllCategories(): List<String>
    suspend fun getOrCreateDefaultSession(): Long
    
    // Data cleanup methods
    suspend fun deleteSessionsBeforeDate(timestamp: Long): Long
    suspend fun deleteMessagesBeforeDate(timestamp: Long): Long
    suspend fun getStorageStats(): Triple<Long, Long, Long>
}
