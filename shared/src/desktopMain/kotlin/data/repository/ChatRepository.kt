package data.repository

import app.cash.sqldelight.db.SqlDriver
import data.db.StudyDatabaseSqlDelight
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class SqlDelightChatRepository(driver: SqlDriver) : ChatRepository {
    private val database = StudyDatabaseSqlDelight(driver)
    private val sessionQueries = database.chatSessionQueries
    private val messageQueries = database.chatMessageQueries

    override suspend fun createSession(title: String?): Long {
        val now = Clock.System.now().toEpochMilliseconds()
        sessionQueries.insertSession(title, now, now)
        return sessionQueries.lastInsertRowId().executeAsOne()
    }

    override suspend fun getAllSessions(): List<ChatSession> {
        return sessionQueries.selectAllSessions()
            .executeAsList()
            .map { ChatSession(it.id, it.title, Instant.fromEpochMilliseconds(it.created_at), Instant.fromEpochMilliseconds(it.updated_at)) }
    }

    override suspend fun getSession(sessionId: Long): ChatSession? {
        val row = sessionQueries.selectSessionById(sessionId).executeAsOneOrNull() ?: return null
        return ChatSession(row.id, row.title, Instant.fromEpochMilliseconds(row.created_at), Instant.fromEpochMilliseconds(row.updated_at))
    }

    override suspend fun addMessage(sessionId: Long, content: String, isUser: Boolean): Long {
        val now = Clock.System.now().toEpochMilliseconds()
        val session = sessionQueries.selectSessionById(sessionId).executeAsOneOrNull()
        if (session != null) {
            val newTitle = session.title ?: if (!isUser) content.take(50) else session.title
            sessionQueries.updateSession(now, newTitle, sessionId)
        }
        messageQueries.insertMessage(sessionId, content, if (isUser) 1 else 0, now)
        return messageQueries.lastInsertRowId().executeAsOne()
    }

    override suspend fun getMessagesForSession(sessionId: Long): List<ChatMessage> {
        return messageQueries.selectMessagesForSession(sessionId)
            .executeAsList()
            .map { ChatMessage(it.id, it.session_id, it.content, it.is_user == 1L, Instant.fromEpochMilliseconds(it.timestamp)) }
    }

    override suspend fun getRecentMessages(limit: Int): List<ChatMessage> {
        return messageQueries.selectRecentMessages(limit.toLong())
            .executeAsList()
            .map { ChatMessage(it.id, it.session_id, it.content, it.is_user == 1L, Instant.fromEpochMilliseconds(it.timestamp)) }
    }

    override suspend fun deleteSession(sessionId: Long) {
        messageQueries.deleteMessagesForSession(sessionId)
        sessionQueries.deleteSession(sessionId)
    }

    override suspend fun deleteMessage(messageId: Long) {
        messageQueries.deleteMessage(messageId)
    }

    override suspend fun updateMessage(messageId: Long, content: String) {
        messageQueries.updateMessage(content, messageId)
    }

    override suspend fun updateSessionTitle(sessionId: Long, title: String) {
        sessionQueries.updateSession(Clock.System.now().toEpochMilliseconds(), title, sessionId)
    }

    override suspend fun getOrCreateDefaultSession(): Long {
        val sessions = sessionQueries.selectAllSessions().executeAsList()
        return if (sessions.isNotEmpty()) sessions.first().id else createSession("Default Chat")
    }

    // Data cleanup methods
    suspend fun deleteSessionsBeforeDate(timestamp: Long): Long {
        val count = sessionQueries.countSessionsBeforeDate(timestamp).executeAsOne() ?: 0L
        sessionQueries.deleteSessionsBeforeDate(timestamp)
        messageQueries.deleteOrphanedMessages()
        return count
    }

    suspend fun deleteMessagesBeforeDate(timestamp: Long): Long {
        val count = messageQueries.countMessagesBeforeDate(timestamp).executeAsOne() ?: 0L
        messageQueries.deleteMessagesBeforeDate(timestamp)
        return count
    }

    suspend fun getStorageStats(): Triple<Long, Long, Long> {
        val sessions = sessionQueries.countSessions().executeAsOne() ?: 0L
        val messages = sessionQueries.countTotalMessages().executeAsOne() ?: 0L
        val orphaned = 0L // Would need a custom query for this
        return Triple(sessions, messages, orphaned)
    }
}
