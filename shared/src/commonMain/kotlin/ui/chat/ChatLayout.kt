package ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.ConnectionStatusBanner
import ui.SessionSidebar
import ui.ChatHeader
import ui.ChatInputArea
import ui.components.m3.DCConfirmDialog
import data.repository.ChatMessage
import data.repository.ChatSession
import data.settings.FontSize

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
    // DEBUG: Log when ChatLayout starts composing
    println("[DEBUG] ChatLayout START composition - messages.count=${messages.size}")
    // ═════════════════════════════════════════════════════════════════════════════
    // SECTION: Root Layout - Row with Sidebar + Main Chat Area
    // DEBUG: Log layout constraints for debugging infinite height issues
    // ═════════════════════════════════════════════════════════════════════════════
    val debugLayout = remember { false } // DEBUG DISABLED
    if (debugLayout) println("[ChatLayout] Rendering with modifier: $modifier")
    
    Row(modifier = modifier.fillMaxSize().debugConstraints("ChatLayout Row")) {
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
    // ═════════════════════════════════════════════════════════════════════════════
    // SECTION: ChatMainArea - Header + Messages List + Input Area
    // CRITICAL: Must use fillMaxSize() not fillMaxHeight() to avoid infinite constraints
    // DEBUG: Log when constraints might be unbounded
    // ═════════════════════════════════════════════════════════════════════════════
    val debugLayout = remember { false } // DEBUG DISABLED
    if (debugLayout) println("[ChatMainArea] Creating Column with fillMaxSize")
    
    Column(
        modifier = Modifier.fillMaxSize().debugConstraints("ChatMainArea Column")
    ) {
        // ─────────────────────────────────────────────────────────────────────────────
        // SUBSECTION: Header with session title and toggle controls
        // ─────────────────────────────────────────────────────────────────────────────
        ChatHeader(
            sessionTitle = sessions.find { it.id == currentSessionId }?.title ?: "Chat",
            showSessionList = showSessionList,
            onToggleSidebar = onToggleSidebar,
            onNewChat = onNewChat,
            connectionStatus = if (isGenerating) "AI typing..." else "Connected",
            isGenerating = isGenerating,
            modifier = Modifier.fillMaxWidth()
        )

        // ─────────────────────────────────────────────────────────────────────────────
        // SUBSECTION: Connection status indicator banner
        // ─────────────────────────────────────────────────────────────────────────────
        ConnectionStatusBanner(
            connectionStatus = "Connected",
            isGenerating = isGenerating
        )

        // ─────────────────────────────────────────────────────────────────────────────
        // SUBSECTION: Chat Messages List (LazyColumn with weight)
        // CRITICAL: Uses weight(1f) to take remaining space, not fillMaxSize()
        // ─────────────────────────────────────────────────────────────────────────────
        ChatMessagesList(
            messages = messages,
            isGenerating = isGenerating,
            onDeleteMessage = onDeleteMessage,
            onRegenerateMessage = onRegenerateMessage,
            onTextSelected = onTextSelected,
            onShowDeleteConfirmation = onShowDeleteConfirmation,
            fontSize = fontSize,
            modifier = Modifier.fillMaxWidth().weight(1f)
        )

        // ─────────────────────────────────────────────────────────────────────────────
        // SUBSECTION: Message Input Area at bottom
        // ─────────────────────────────────────────────────────────────────────────────
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
    // DEBUG: Log when ChatLayout finishes composing
    println("[DEBUG] ChatLayout END composition")
}
