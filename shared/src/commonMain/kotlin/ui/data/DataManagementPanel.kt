package ui.data

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.db.DatabaseDriverFactory
import kotlinx.coroutines.launch
import theme.*

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
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Section Title
        Text(
            text = "Data Management",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Indigo
        )
        
        // Main Content Surface
        Surface(
            color = Color.White,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Description
                Text(
                    text = "Manage your study data, clean up old entries, and view storage statistics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
                
                Divider(color = Gray200)
                
                // Storage Statistics
                stats?.let { dataStats ->
                    StorageStatsCard(stats = dataStats)
                }
            }
        }
        
        // Cleanup Section Title
        Text(
            text = "Data Cleanup",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Indigo
        )
        
        // Cleanup Cards Surface
        Surface(
            color = Color.White,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Study Entries Cleanup
                CleanupSection(
                    title = "Study Entries",
                    description = "Delete practice entries older than selected time period",
                    icon = androidx.compose.material.icons.Icons.Default.Edit,
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
                
                Divider(color = Gray200)
                
                // Chat History Cleanup
                CleanupSection(
                    title = "Chat History",
                    description = "Delete old chat sessions and messages",
                    icon = androidx.compose.material.icons.Icons.Default.Chat,
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
                
                Divider(color = Gray200)
                
                // Vocabulary Cleanup
                CleanupSection(
                    title = "Vocabulary",
                    description = "Manage learned words and clear vocabulary data",
                    icon = androidx.compose.material.icons.Icons.Default.Book,
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
                
                Divider(color = Gray200)
                
                // Grammar Mistakes Cleanup
                CleanupSection(
                    title = "Grammar Mistakes",
                    description = "Clear recorded grammar mistakes and corrections",
                    icon = androidx.compose.material.icons.Icons.Default.Warning,
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
            }
        }
        
        // Operation Result
        operationResult?.let { result ->
            Surface(
                color = Success.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
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
        CleanupDialog(
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
