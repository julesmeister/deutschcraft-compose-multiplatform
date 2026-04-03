package data.repository

import data.db.AppDatabase
import data.db.entity.ChatMessageEntity
import data.db.entity.ChatSessionEntity
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Repository for chat persistence using Room on Android.
 */
class ChatRepository(private val database: AppDatabase) {
    private val sessionDao = database.chatSessionDao()
    private val messageDao = database.chatMessageDao()

    /**
     * Create a new chat session.
     */
    suspend fun createSession(title: String? = null): Long {
        val now = Clock.System.now()
        val session = ChatSessionEntity(
            title = title,
            createdAt = now,
            updatedAt = now
        )
        return sessionDao.insert(session)
    }

    /**
     * Get all chat sessions ordered by most recent.
     */
    suspend fun getAllSessions(): List<ChatSession> {
        return sessionDao.getAllSessions().map { it.toChatSession() }
    }

    /**
     * Get a specific session by ID.
     */
    suspend fun getSession(sessionId: Long): ChatSession? {
        return sessionDao.getSessionById(sessionId)?.toChatSession()
    }

    /**
     * Add a message to a session.
     */
    suspend fun addMessage(sessionId: Long, content: String, isUser: Boolean): Long {
        val now = Clock.System.now()

        // Update session timestamp
        val session = sessionDao.getSessionById(sessionId)
        if (session != null) {
            val title = session.title ?: if (!isUser) content.take(50) else session.title
            sessionDao.updateSession(sessionId, now, title)
        }

        // Insert message
        val message = ChatMessageEntity(
            sessionId = sessionId,
            content = content,
            isUser = isUser,
            timestamp = now
        )
        return messageDao.insert(message)
    }

    /**
     * Get all messages for a session.
     */
    suspend fun getMessagesForSession(sessionId: Long): List<ChatMessage> {
        return messageDao.getMessagesForSession(sessionId).map { it.toChatMessage() }
    }

    /**
     * Get recent messages across all sessions.
     */
    suspend fun getRecentMessages(limit: Int = 50): List<ChatMessage> {
        return messageDao.getRecentMessages(limit).map { it.toChatMessage() }
    }

    /**
     * Delete a session and all its messages.
     */
    suspend fun deleteSession(sessionId: Long) {
        messageDao.deleteMessagesForSession(sessionId)
        sessionDao.deleteById(sessionId)
    }

    /**
     * Update session title.
     */
    suspend fun updateSessionTitle(sessionId: Long, title: String) {
        val now = Clock.System.now()
        sessionDao.updateSession(sessionId, now, title)
    }

    /**
     * Get or create the default session (for single-session mode).
     */
    suspend fun getOrCreateDefaultSession(): Long {
        val sessions = sessionDao.getAllSessions()
        return if (sessions.isNotEmpty()) {
            sessions.first().id
        } else {
            createSession("Default Chat")
        }
    }
}

// ==================== Domain Models ====================

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

// ==================== Mappers ====================

private fun ChatSessionEntity.toChatSession(): ChatSession {
    return ChatSession(
        id = this.id,
        title = this.title,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

private fun ChatMessageEntity.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = this.id,
        sessionId = this.sessionId,
        content = this.content,
        isUser = this.isUser,
        timestamp = this.timestamp
    )
}
