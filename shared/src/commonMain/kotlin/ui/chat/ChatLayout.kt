package ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.chat.ConnectionStatusBanner
import ui.chat.ChatHeader
import ui.chat.ChatInputArea
import ui.chat.PersistentChatBubble
import ui.chat.EmptyChatState
import ui.components.m3.DCConfirmDialog
import data.repository.ChatMessage
import data.repository.ChatSession
import data.settings.FontSize
import theme.Gray500
import theme.Indigo
import ui.chat.SessionSidebar

@Composable
internal fun ChatLayout(
    sessions: List<ChatSession>,
    filteredSessions: List<ChatSession>,
    currentSessionId: Long?,
    categories: List<String>,
    selectedCategory: String?,
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    inputText: String,
    showSessionList: Boolean,
    showConfirmDialog: Boolean,
    confirmMessage: String,
    confirmAction: () -> Unit,
    onDismissDialog: () -> Unit,
    onSessionClick: (Long) -> Unit,
    onNewSessionClick: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onToggleSidebar: () -> Unit,
    onSendMessage: () -> Unit,
    onStopGeneration: () -> Unit,
    onInputChange: (String) -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onRegenerateMessage: (Long) -> Unit,
    onTextSelected: (String) -> Unit,
    onShowDeleteConfirmation: (Long, String) -> Unit,
    fontSize: FontSize,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxSize()) {
        // Session List Sidebar
        AnimatedVisibility(
            visible = showSessionList,
            enter = expandHorizontally(),
            exit = shrinkHorizontally()
        ) {
            SessionSidebar(
                sessions = filteredSessions,
                currentSessionId = currentSessionId,
                categories = categories,
                selectedCategory = selectedCategory,
                onSessionClick = onSessionClick,
                onNewSessionClick = onNewSessionClick,
                onDeleteSession = onDeleteSession,
                onCategorySelected = onCategorySelected,
                modifier = Modifier.width(240.dp)
            )
        }

        // Main Chat Area
        ChatMainArea(
            sessions = sessions,
            currentSessionId = currentSessionId,
            messages = messages,
            isGenerating = isGenerating,
            inputText = inputText,
            showSessionList = showSessionList,
            onToggleSidebar = onToggleSidebar,
            onNewChat = onNewSessionClick,
            onSendMessage = onSendMessage,
            onStopGeneration = onStopGeneration,
            onInputChange = onInputChange,
            onDeleteMessage = onDeleteMessage,
            onRegenerateMessage = onRegenerateMessage,
            onTextSelected = onTextSelected,
            onShowDeleteConfirmation = onShowDeleteConfirmation,
            fontSize = fontSize
        )
    }

    // Confirmation Dialog
    if (showConfirmDialog) {
        DCConfirmDialog(
            title = "Confirm Delete",
            message = confirmMessage,
            onConfirm = confirmAction,
            onDismiss = onDismissDialog
        )
    }
}

@Composable
private fun ChatMainArea(
    sessions: List<ChatSession>,
    currentSessionId: Long?,
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    inputText: String,
    showSessionList: Boolean,
    onToggleSidebar: () -> Unit,
    onNewChat: () -> Unit,
    onSendMessage: () -> Unit,
    onStopGeneration: () -> Unit,
    onInputChange: (String) -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onRegenerateMessage: (Long) -> Unit,
    onTextSelected: (String) -> Unit,
    onShowDeleteConfirmation: (Long, String) -> Unit,
    fontSize: FontSize
) {
    Column(
        modifier = Modifier.weight(1f).fillMaxHeight()
    ) {
        // Chat header with toggle
        ChatHeader(
            sessionTitle = sessions.find { it.id == currentSessionId }?.title ?: "Chat",
            showSessionList = showSessionList,
            onToggleSidebar = onToggleSidebar,
            onNewChat = onNewChat,
            connectionStatus = "Connected", // Simplified
            modifier = Modifier.fillMaxWidth()
        )

        // Connection status banner
        ConnectionStatusBanner(
            connectionStatus = "Connected",
            isGenerating = isGenerating
        )

        // Chat messages
        ChatMessagesList(
            messages = messages,
            isGenerating = isGenerating,
            onDeleteMessage = onDeleteMessage,
            onRegenerateMessage = onRegenerateMessage,
            onTextSelected = onTextSelected,
            onShowDeleteConfirmation = onShowDeleteConfirmation,
            fontSize = fontSize
        )

        // Input area
        ChatInputArea(
            value = inputText,
            onValueChange = onInputChange,
            onSend = onSendMessage,
            onStop = onStopGeneration,
            isGenerating = isGenerating,
            fontSize = fontSize,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ChatMessagesList(
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    onDeleteMessage: (Long) -> Unit,
    onRegenerateMessage: (Long) -> Unit,
    onTextSelected: (String) -> Unit,
    onShowDeleteConfirmation: (Long, String) -> Unit,
    fontSize: FontSize
) {
    Box(
        modifier = Modifier.weight(1f).fillMaxWidth()
    ) {
        if (messages.isEmpty()) {
            EmptyChatState(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    PersistentChatBubble(
                        message = message,
                        fontSize = fontSize,
                        onDelete = { id -> onDeleteMessage(id) },
                        onRegenerate = { id -> onRegenerateMessage(id) },
                        onTextSelected = onTextSelected,
                        showDeleteConfirm = { id, msg -> onShowDeleteConfirmation(id, msg) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (isGenerating) {
                    item {
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Indigo
                            )
                            Text(
                                text = "AI is typing...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500
                            )
                        }
                    }
                }
            }
        }
    }
}
