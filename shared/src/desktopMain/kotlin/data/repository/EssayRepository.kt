package data.repository

import app.cash.sqldelight.db.SqlDriver
import data.db.StudyDatabaseSqlDelight
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class SqlDelightEssayRepository(driver: SqlDriver) : EssayRepository {
    private val database = StudyDatabaseSqlDelight(driver)
    private val essayQueries = database.essayDraftQueries

    override suspend fun createEssay(title: String, content: String): Long {
        val now = Clock.System.now().toEpochMilliseconds()
        val displayTitle = title.ifBlank { content.take(50).ifBlank { "Untitled Essay" } }
        essayQueries.insertEssay(displayTitle, content, now, now)
        return essayQueries.lastInsertRowId().executeAsOne()
    }

    override suspend fun getAllEssays(): List<EssayDraft> {
        return essayQueries.selectAllEssays()
            .executeAsList()
            .map { 
                EssayDraft(
                    it.id, 
                    it.title, 
                    it.content, 
                    Instant.fromEpochMilliseconds(it.created_at), 
                    Instant.fromEpochMilliseconds(it.updated_at)
                ) 
            }
    }

    override suspend fun getEssay(essayId: Long): EssayDraft? {
        val row = essayQueries.selectEssayById(essayId).executeAsOneOrNull() ?: return null
        return EssayDraft(
            row.id, 
            row.title, 
            row.content, 
            Instant.fromEpochMilliseconds(row.created_at), 
            Instant.fromEpochMilliseconds(row.updated_at)
        )
    }

    override suspend fun updateEssay(essayId: Long, title: String, content: String) {
        val now = Clock.System.now().toEpochMilliseconds()
        val displayTitle = title.ifBlank { content.take(50).ifBlank { "Untitled Essay" } }
        essayQueries.updateEssay(displayTitle, content, now, essayId)
    }

    override suspend fun deleteEssay(essayId: Long) {
        essayQueries.deleteEssay(essayId)
    }

    override suspend fun searchEssays(query: String): List<EssayDraft> {
        return essayQueries.searchEssays(query)
            .executeAsList()
            .map { 
                EssayDraft(
                    it.id, 
                    it.title, 
                    it.content, 
                    Instant.fromEpochMilliseconds(it.created_at), 
                    Instant.fromEpochMilliseconds(it.updated_at)
                ) 
            }
    }

    override suspend fun deleteEssaysBeforeDate(timestamp: Long): Long {
        val count = essayQueries.countEssaysBeforeDate(timestamp).executeAsOne() ?: 0L
        essayQueries.deleteEssaysBeforeDate(timestamp)
        return count
    }

    override suspend fun getStorageStats(): Pair<Long, Long> {
        val essays = essayQueries.countEssays().executeAsOne() ?: 0L
        return Pair(essays, 0L)
    }
}
