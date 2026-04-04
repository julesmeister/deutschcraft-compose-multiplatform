package data.repository

import data.db.AppDatabase
import data.db.entity.ChatMessageEntity
import data.db.entity.ChatSessionEntity
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Repository for chat persistence using Room on Android.
 */
class ChatRepository(private val database: AppDatabase) : data.repository.ChatRepository {
    private val sessionDao = database.chatSessionDao()
    private val messageDao = database.chatMessageDao()

    /**
     * Create a new chat session.
     */
    override suspend fun createSession(title: String?): Long {
        val now = Clock.System.now()
        val session = ChatSessionEntity(
            title = title,
            category = null,
            createdAt = now,
            updatedAt = now
        )
        return sessionDao.insert(session)
    }

    /**
     * Get all chat sessions ordered by most recent.
     */
    override suspend fun getAllSessions(): List<ChatSession> {
        return sessionDao.getAllSessions().map { it.toChatSession() }
    }

    /**
     * Get a specific session by ID.
     */
    override suspend fun getSession(sessionId: Long): ChatSession? {
        return sessionDao.getSessionById(sessionId)?.toChatSession()
    }

    /**
     * Add a message to a session.
     */
    override suspend fun addMessage(sessionId: Long, content: String, isUser: Boolean): Long {
        val now = Clock.System.now()

        // Update session timestamp
        val session = sessionDao.getSessionById(sessionId)
        if (session != null) {
            val title = session.title ?: if (!isUser) content.take(50) else session.title
            sessionDao.updateSession(sessionId, now, title, session.category)
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
    override suspend fun getMessagesForSession(sessionId: Long): List<ChatMessage> {
        return messageDao.getMessagesForSession(sessionId).map { it.toChatMessage() }
    }

    override suspend fun getRecentMessages(limit: Int): List<ChatMessage> {
        return messageDao.getRecentMessages(limit).map { it.toChatMessage() }
    }

    override suspend fun deleteSession(sessionId: Long) {
        messageDao.deleteMessagesForSession(sessionId)
        sessionDao.deleteById(sessionId)
    }

    /**
     * Update session title.
     */
    override suspend fun updateSessionTitle(sessionId: Long, title: String) {
        val now = Clock.System.now()
        val session = sessionDao.getSessionById(sessionId)
        sessionDao.updateSession(sessionId, now, title, session?.category)
    }

    override suspend fun updateSessionCategory(sessionId: Long, category: String?) {
        val now = Clock.System.now()
        val session = sessionDao.getSessionById(sessionId)
        sessionDao.updateSession(sessionId, now, session?.title, category)
    }

    override suspend fun getSessionsByCategory(category: String): List<ChatSession> {
        return sessionDao.getSessionsByCategory(category).map { it.toChatSession() }
    }

    override suspend fun getAllCategories(): List<String> {
        return sessionDao.getAllCategories().mapNotNull { it }
    }

    override suspend fun updateMessage(messageId: Long, content: String) {
        // Room doesn't have a direct update method for messages in the current DAO
        // This would need to be added to ChatMessageDao if needed
    }

    override suspend fun deleteMessage(messageId: Long) {
        // Room doesn't have a direct delete by ID method in the current DAO
        // This would need to be added to ChatMessageDao
    }

    // Data cleanup methods
    override suspend fun deleteSessionsBeforeDate(timestamp: Long): Long {
        // Would need implementation in DAO
        return 0L
    }

    override suspend fun deleteMessagesBeforeDate(timestamp: Long): Long {
        // Would need implementation in DAO
        return 0L
    }

    override suspend fun getStorageStats(): Triple<Long, Long, Long> {
        // Would need implementation in DAO
        return Triple(0L, 0L, 0L)
    }

    /**
     * Get or create the default session (for single-session mode).
     */
    override suspend fun getOrCreateDefaultSession(): Long {
        val sessions = sessionDao.getAllSessions()
        return if (sessions.isNotEmpty()) {
            sessions.first().id
        } else {
            createSession("Default Chat")
        }
    }
}

// ==================== Domain Models ====================

// ChatSession and ChatMessage are defined in commonMain

// ==================== Mappers ====================

private fun ChatSessionEntity.toChatSession(): ChatSession {
    return ChatSession(
        id = this.id,
        title = this.title,
        category = this.category,
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
