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
        val now = Clock.System.now()
        sessionQueries.insertSession(
            title = title,
            created_at = now,
            updated_at = now
        )
        return sessionQueries.lastInsertRowId().executeAsOne()
    }

    override suspend fun getAllSessions(): List<ChatSession> {
        return sessionQueries.selectAllSessions()
            .executeAsList()
            .map { it.toChatSession() }
    }

    override suspend fun getSession(sessionId: Long): ChatSession? {
        return sessionQueries.selectSessionById(sessionId)
            .executeAsOneOrNull()
            ?.toChatSession()
    }

    override suspend fun addMessage(sessionId: Long, content: String, isUser: Boolean): Long {
        val now = Clock.System.now()
        
        // Update session timestamp
        val session = sessionQueries.selectSessionById(sessionId).executeAsOneOrNull()
        if (session != null) {
            val newTitle = session.title ?: if (!isUser) content.take(50) else session.title
            sessionQueries.updateSession(sessionId, now, newTitle)
        }
        
        // Insert message
        messageQueries.insertMessage(
            session_id = sessionId,
            content = content,
            is_user = isUser,
            timestamp = now
        )
        return messageQueries.lastInsertRowId().executeAsOne()
    }

    override suspend fun getMessagesForSession(sessionId: Long): List<ChatMessage> {
        return messageQueries.selectMessagesForSession(sessionId)
            .executeAsList()
            .map { it.toChatMessage() }
    }

    override suspend fun getRecentMessages(limit: Int): List<ChatMessage> {
        return messageQueries.selectRecentMessages(limit.toLong())
            .executeAsList()
            .map { it.toChatMessage() }
    }

    override suspend fun deleteSession(sessionId: Long) {
        messageQueries.deleteMessagesForSession(sessionId)
        sessionQueries.deleteSession(sessionId)
    }

    override suspend fun updateSessionTitle(sessionId: Long, title: String) {
        val now = Clock.System.now()
        sessionQueries.updateSession(sessionId, now, title)
    }

    override suspend fun getOrCreateDefaultSession(): Long {
        val sessions = sessionQueries.selectAllSessions().executeAsList()
        return if (sessions.isNotEmpty()) {
            sessions.first().id
        } else {
            createSession("Default Chat")
        }
    }
}

private fun SelectAllSessions.toChatSession(): ChatSession {
    return ChatSession(
        id = this.id,
        title = this.title,
        createdAt = this.created_at,
        updatedAt = this.updated_at
    )
}

private fun SelectMessagesForSession.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = this.id,
        sessionId = this.session_id,
        content = this.content,
        isUser = this.is_user,
        timestamp = this.timestamp
    )
}

private fun SelectRecentMessages.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = this.id,
        sessionId = this.session_id,
        content = this.content,
        isUser = this.is_user,
        timestamp = this.timestamp
    )
}
