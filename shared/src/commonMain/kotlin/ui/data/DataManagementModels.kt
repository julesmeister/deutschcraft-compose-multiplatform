package ui.data

// Data Classes
internal data class DataStats(
    val entriesCount: Long,
    val chatSessionsCount: Long,
    val chatMessagesCount: Long,
    val vocabularyCount: Long,
    val mistakesCount: Long,
    val totalWords: Long
)

internal enum class ConfirmAction(
    val description: String
) {
    ENTRIES_LAST_WEEK("Delete all study entries from the last week? This action cannot be undone."),
    ENTRIES_LAST_MONTH("Delete all study entries from the last month? This action cannot be undone."),
    ENTRIES_ALL("Delete ALL study entries? This action cannot be undone."),
    CHAT_LAST_WEEK("Delete all chat sessions from the last week? This action cannot be undone."),
    CHAT_LAST_MONTH("Delete all chat sessions from the last month? This action cannot be undone."),
    CHAT_ALL("Delete ALL chat history? This action cannot be undone."),
    VOCAB_LEARNED("Delete all learned vocabulary words? This action cannot be undone."),
    VOCAB_OLD("Delete vocabulary words older than 3 months? This action cannot be undone."),
    VOCAB_ALL("Delete ALL vocabulary data? This action cannot be undone."),
    MISTAKES_LAST_WEEK("Delete grammar mistakes from the last week? This action cannot be undone."),
    MISTAKES_LAST_MONTH("Delete grammar mistakes from the last month? This action cannot be undone."),
    MISTAKES_ALL("Delete ALL grammar mistake records? This action cannot be undone.")
}
