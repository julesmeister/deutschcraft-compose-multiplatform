package ui.data

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.db.DatabaseDriverFactory
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import theme.*
import ui.components.m3.*

@Composable
fun DataManagementPanel(
    driverFactory: DatabaseDriverFactory,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val databaseManager = remember(driverFactory) { driverFactory.databaseManager }
    val chatRepository = remember(driverFactory) { driverFactory.chatRepository }
    
    var stats by remember { mutableStateOf<DataStats?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var operationResult by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf<ConfirmAction?>(null) }
    
    // Load stats on startup
    LaunchedEffect(Unit) {
        refreshStats(databaseManager, chatRepository) { stats = it }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Data Management",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Gray800
        )
        
        Text(
            text = "Manage your study data, clean up old entries, and view storage statistics",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray600
        )
        
        // Storage Statistics Card
        stats?.let { dataStats ->
            StorageStatsCard(stats = dataStats)
        }
        
        // Data Cleanup Section
        Text(
            text = "Data Cleanup",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Gray800,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        // Study Entries Cleanup
        CleanupSection(
            title = "Study Entries",
            description = "Delete practice entries older than selected time period",
            icon = Icons.Default.Edit,
            onClearLastWeek = {
                showConfirmDialog = ConfirmAction.ENTRIES_LAST_WEEK
            },
            onClearLastMonth = {
                showConfirmDialog = ConfirmAction.ENTRIES_LAST_MONTH
            },
            onClearAll = {
                showConfirmDialog = ConfirmAction.ENTRIES_ALL
            }
        )
        
        // Chat History Cleanup
        CleanupSection(
            title = "Chat History",
            description = "Delete old chat sessions and messages",
            icon = Icons.Default.Chat,
            onClearLastWeek = {
                showConfirmDialog = ConfirmAction.CHAT_LAST_WEEK
            },
            onClearLastMonth = {
                showConfirmDialog = ConfirmAction.CHAT_LAST_MONTH
            },
            onClearAll = {
                showConfirmDialog = ConfirmAction.CHAT_ALL
            }
        )
        
        // Vocabulary Cleanup
        CleanupSection(
            title = "Vocabulary",
            description = "Manage learned words and clear vocabulary data",
            icon = Icons.Default.Book,
            onClearLearned = {
                showConfirmDialog = ConfirmAction.VOCAB_LEARNED
            },
            onClearOld = {
                showConfirmDialog = ConfirmAction.VOCAB_OLD
            },
            onClearAll = {
                showConfirmDialog = ConfirmAction.VOCAB_ALL
            }
        )
        
        // Grammar Mistakes Cleanup
        CleanupSection(
            title = "Grammar Mistakes",
            description = "Clear recorded grammar mistakes and corrections",
            icon = Icons.Default.Warning,
            onClearLastWeek = {
                showConfirmDialog = ConfirmAction.MISTAKES_LAST_WEEK
            },
            onClearLastMonth = {
                showConfirmDialog = ConfirmAction.MISTAKES_LAST_MONTH
            },
            onClearAll = {
                showConfirmDialog = ConfirmAction.MISTAKES_ALL
            }
        )
        
        // Operation Result
        operationResult?.let { result ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Success.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = result,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700
                    )
                }
            }
        }
    }
    
    // Confirmation Dialog
    showConfirmDialog?.let { action ->
        ConfirmCleanupDialog(
            action = action,
            onConfirm = {
                scope.launch {
                    isLoading = true
                    val result = executeCleanup(action, databaseManager, chatRepository)
                    operationResult = result
                    refreshStats(databaseManager, chatRepository) { stats = it }
                    isLoading = false
                    showConfirmDialog = null
                }
            },
            onDismiss = { showConfirmDialog = null }
        )
    }
}

@Composable
private fun StorageStatsCard(stats: DataStats) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Indigo.copy(alpha = 0.05f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Indigo.copy(alpha = 0.3f))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = Indigo,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Storage Statistics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Indigo
                )
            }
            
            Divider(color = Gray200)
            
            StatRow(label = "Study Entries", value = "${stats.entriesCount}")
            StatRow(label = "Chat Sessions", value = "${stats.chatSessionsCount}")
            StatRow(label = "Chat Messages", value = "${stats.chatMessagesCount}")
            StatRow(label = "Vocabulary Items", value = "${stats.vocabularyCount}")
            StatRow(label = "Grammar Mistakes", value = "${stats.mistakesCount}")
            StatRow(label = "Total Words Written", value = "${stats.totalWords}")
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray600
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Gray800
        )
    }
}

@Composable
private fun CleanupSection(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClearLastWeek: () -> Unit = {},
    onClearLastMonth: () -> Unit = {},
    onClearLearned: (() -> Unit)? = null,
    onClearOld: (() -> Unit)? = null,
    onClearAll: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Gray50
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Gray600,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (onClearLearned != null && onClearOld != null) {
                    // Vocabulary-specific buttons
                    OutlinedButton(
                        onClick = onClearLearned,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear Learned")
                    }
                    OutlinedButton(
                        onClick = onClearOld,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear Old")
                    }
                } else {
                    // Standard buttons
                    OutlinedButton(
                        onClick = onClearLastWeek,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Last Week")
                    }
                    OutlinedButton(
                        onClick = onClearLastMonth,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Last Month")
                    }
                }
                
                Button(
                    onClick = onClearAll,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Danger
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All")
                }
            }
        }
    }
}

@Composable
private fun ConfirmCleanupDialog(
    action: ConfirmAction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Warning
            )
        },
        title = {
            Text(text = "Confirm Data Cleanup")
        },
        text = {
            Text(
                text = action.description
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Danger
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Data Classes
private data class DataStats(
    val entriesCount: Long,
    val chatSessionsCount: Long,
    val chatMessagesCount: Long,
    val vocabularyCount: Long,
    val mistakesCount: Long,
    val totalWords: Long
)

private enum class ConfirmAction(
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

// Helper Functions
private suspend fun refreshStats(
    databaseManager: data.db.DatabaseManager,
    chatRepository: data.repository.ChatRepository,
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

private suspend fun executeCleanup(
    action: ConfirmAction,
    databaseManager: data.db.DatabaseManager,
    chatRepository: data.repository.ChatRepository
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
