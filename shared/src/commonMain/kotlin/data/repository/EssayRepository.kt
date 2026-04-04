package data.repository

import kotlinx.datetime.Instant

interface EssayRepository {
    suspend fun createEssay(title: String, content: String): Long
    suspend fun getAllEssays(): List<EssayDraft>
    suspend fun getEssay(essayId: Long): EssayDraft?
    suspend fun updateEssay(essayId: Long, title: String, content: String)
    suspend fun deleteEssay(essayId: Long)
    suspend fun searchEssays(query: String): List<EssayDraft>
    
    // Data cleanup methods
    suspend fun deleteEssaysBeforeDate(timestamp: Long): Long
    suspend fun getStorageStats(): Pair<Long, Long>
}

data class EssayDraft(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
