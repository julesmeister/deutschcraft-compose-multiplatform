package ui.data

import data.db.DatabaseDriverFactory
import data.repository.ChatRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal suspend fun refreshStats(
    databaseManager: data.db.DatabaseManager,
    chatRepository: ChatRepository,
    onStats: (DataStats) -> Unit
) {
    val entriesCount = databaseManager.entries.getCount()
    val totalWords = databaseManager.entries.getTotalWords()
    val vocabStats = databaseManager.vocabulary.getStats()
    val mistakesStats = databaseManager.mistakes.getStats()
    
    // Get chat stats from repository
    val chatStats = chatRepository.getStorageStats()
    val chatSessions = chatStats.first
    val chatMessages = chatStats.second
    
    onStats(
        DataStats(
            entriesCount = entriesCount,
            chatSessionsCount = chatSessions,
            chatMessagesCount = chatMessages,
            vocabularyCount = vocabStats.totalWords.toLong(),
            mistakesCount = mistakesStats.total,
            totalWords = totalWords
        )
    )
}

internal suspend fun executeCleanup(
    action: ConfirmAction,
    databaseManager: data.db.DatabaseManager,
    chatRepository: ChatRepository
): String {
    val now = Clock.System.now().toEpochMilliseconds()
    val oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000)
    val oneMonthAgo = now - (30 * 24 * 60 * 60 * 1000)
    
    return when (action) {
        ConfirmAction.ENTRIES_LAST_WEEK -> {
            val count = databaseManager.entries.deleteEntriesBeforeDate(oneWeekAgo)
            "Deleted $count study entries from last week"
        }
        ConfirmAction.ENTRIES_LAST_MONTH -> {
            val count = databaseManager.entries.deleteEntriesBeforeDate(oneMonthAgo)
            "Deleted $count study entries from last month"
        }
        ConfirmAction.ENTRIES_ALL -> {
            val count = databaseManager.entries.getCount()
            databaseManager.entries.deleteEntriesBeforeDate(now + 1)
            "Deleted all $count study entries"
        }
        ConfirmAction.CHAT_LAST_WEEK -> {
            val count = chatRepository.deleteSessionsBeforeDate(oneWeekAgo)
            "Deleted $count chat sessions from last week"
        }
        ConfirmAction.CHAT_LAST_MONTH -> {
            val count = chatRepository.deleteSessionsBeforeDate(oneMonthAgo)
            "Deleted $count chat sessions from last month"
        }
        ConfirmAction.CHAT_ALL -> {
            val count = chatRepository.deleteSessionsBeforeDate(now + 1)
            "Deleted all $count chat sessions"
        }
        ConfirmAction.VOCAB_LEARNED -> {
            val count = databaseManager.vocabulary.deleteLearnedVocabulary()
            "Deleted $count learned vocabulary words"
        }
        ConfirmAction.VOCAB_OLD -> {
            val threeMonthsAgo = now - (90 * 24 * 60 * 60 * 1000)
            val count = databaseManager.vocabulary.deleteVocabularyBeforeDate(threeMonthsAgo)
            "Deleted $count old vocabulary words"
        }
        ConfirmAction.VOCAB_ALL -> {
            val count = databaseManager.vocabulary.resetVocabulary()
            "Deleted all $count vocabulary words"
        }
        ConfirmAction.MISTAKES_LAST_WEEK -> {
            val count = databaseManager.mistakes.deleteMistakesBeforeDate(oneWeekAgo)
            "Deleted $count grammar mistakes from last week"
        }
        ConfirmAction.MISTAKES_LAST_MONTH -> {
            val count = databaseManager.mistakes.deleteMistakesBeforeDate(oneMonthAgo)
            "Deleted $count grammar mistakes from last month"
        }
        ConfirmAction.MISTAKES_ALL -> {
            val count = databaseManager.mistakes.deleteMistakesBeforeDate(now + 1)
            "Deleted all $count grammar mistake records"
        }
    }
}
