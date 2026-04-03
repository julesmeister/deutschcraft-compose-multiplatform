package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import data.db.DatabaseDriverFactory
import data.repository.ChatRepository
import data.repository.ChatSession
import data.repository.ChatMessage
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import service.OllamaService
import theme.Gray500
import theme.Indigo
import data.settings.FontSize

@Composable
fun ChatPanelWithPersistence(
    editorText: String,
    driverFactory: DatabaseDriverFactory,
    fontSize: FontSize = FontSize.MEDIUM,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val chatRepository = remember(driverFactory) { driverFactory.chatRepository }
    val ollamaService = remember { OllamaService() }

    var sessions by remember { mutableStateOf<List<ChatSession>>(emptyList()) }
    var currentSessionId by remember { mutableStateOf<Long?>(null) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf("Checking...") }
    var selectedModel by remember { mutableStateOf("llama3.2") }
    var showSessionList by remember { mutableStateOf(true) }

    // Load sessions on startup
    LaunchedEffect(Unit) {
        sessions = chatRepository.getAllSessions()
        if (sessions.isEmpty()) {
            // Create default session
            val newId = chatRepository.createSession("New Chat")
            currentSessionId = newId
            sessions = chatRepository.getAllSessions()
        } else {
            currentSessionId = sessions.first().id
            messages = chatRepository.getMessagesForSession(currentSessionId!!)
        }

        val isConnected = ollamaService.checkConnection()
        connectionStatus = if (isConnected) "Connected" else "Not connected"
        if (isConnected) {
            val models = ollamaService.getAvailableModels()
            if (models.isNotEmpty()) {
                selectedModel = models.first()
            }
        }
    }

    // Load messages when session changes
    LaunchedEffect(currentSessionId) {
        currentSessionId?.let { sessionId ->
            messages = chatRepository.getMessagesForSession(sessionId)
        }
    }

    val systemContext = "You are a helpful writing assistant. The user is currently working on a document. Current document content: ${editorText.take(1000)}"

    var currentJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    fun sendMessage() {
        if (inputText.isBlank() || isGenerating || currentSessionId == null) return

        val userMessage = inputText.trim()
        val sessionId = currentSessionId!!

        scope.launch {
            // Save user message
            chatRepository.addMessage(sessionId, userMessage, isUser = true)
            messages = chatRepository.getMessagesForSession(sessionId)
            inputText = ""

            currentJob = scope.launch {
                isGenerating = true
                try {
                    // Get fresh messages from DB for context
                    val sessionMessages = chatRepository.getMessagesForSession(sessionId)
                    val uiMessages = sessionMessages.map {
                        ui.ChatMessage(
                            text = it.content,
                            isUser = it.isUser,
                            timestamp = it.timestamp.toEpochMilliseconds()
                        )
                    }

                    val response = ollamaService.chat(
                        messages = uiMessages,
                        systemContext = systemContext,
                        model = selectedModel
                    )

                    // Save AI response
                    chatRepository.addMessage(sessionId, response, isUser = false)
                    messages = chatRepository.getMessagesForSession(sessionId)

                    // Update sessions list to reflect new timestamp
                    sessions = chatRepository.getAllSessions()
                } catch (e: Exception) {
                    val errorMsg = if (e is kotlinx.coroutines.CancellationException) {
                        "Generation stopped."
                    } else {
                        "Error: ${e.message}"
                    }
                    chatRepository.addMessage(sessionId, errorMsg, isUser = false)
                    messages = chatRepository.getMessagesForSession(sessionId)
                } finally {
                    isGenerating = false
                    currentJob = null
                }
            }
        }
    }

    fun stopGeneration() {
        currentJob?.cancel()
        currentJob = null
    }

    fun createNewSession() {
        scope.launch {
            val newId = chatRepository.createSession()
            currentSessionId = newId
            sessions = chatRepository.getAllSessions()
            messages = emptyList()
        }
    }

    fun switchSession(sessionId: Long) {
        currentSessionId = sessionId
        scope.launch {
            messages = chatRepository.getMessagesForSession(sessionId)
        }
    }

    fun deleteSession(sessionId: Long) {
        scope.launch {
            chatRepository.deleteSession(sessionId)
            sessions = chatRepository.getAllSessions()
            if (currentSessionId == sessionId) {
                currentSessionId = sessions.firstOrNull()?.id
                messages = currentSessionId?.let { chatRepository.getMessagesForSession(it) } ?: emptyList()
            }
        }
    }

    Row(modifier = modifier.fillMaxSize()) {
        // Session List Sidebar
        AnimatedVisibility(
            visible = showSessionList,
            enter = expandHorizontally(),
            exit = shrinkHorizontally()
        ) {
            SessionSidebar(
                sessions = sessions,
                currentSessionId = currentSessionId,
                onSessionClick = { switchSession(it) },
                onNewSessionClick = { createNewSession() },
                onDeleteSession = { deleteSession(it) },
                modifier = Modifier.width(240.dp)
            )
        }

        // Main Chat Area
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            // Chat header with toggle
            ChatHeader(
                sessionTitle = sessions.find { it.id == currentSessionId }?.title ?: "Chat",
                showSessionList = showSessionList,
                onToggleSidebar = { showSessionList = !showSessionList },
                onNewChat = { createNewSession() },
                connectionStatus = connectionStatus,
                modifier = Modifier.fillMaxWidth()
            )

            // Connection status banner
            ConnectionStatusBanner(
                connectionStatus = connectionStatus,
                isGenerating = isGenerating
            )

            // Chat messages
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

            // Input area
            ChatInputArea(
                value = inputText,
                onValueChange = { inputText = it },
                onSend = { sendMessage() },
                onStop = { stopGeneration() },
                isGenerating = isGenerating,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ConnectionStatusBanner(
    connectionStatus: String,
    isGenerating: Boolean = false,
    lastResponse: String? = null
) {
    val colors = MaterialTheme.colorScheme
    
    // Determine what to show
    val (containerColor, contentColor, icon, message) = when {
        // AI actively generating response
        isGenerating -> Quadruple(
            colors.tertiaryContainer,
            colors.onTertiaryContainer,
            Icons.Default.AutoMode,
            "AI is typing..."
        )
        // Connection states
        connectionStatus == "Connected" -> Quadruple(
            colors.primaryContainer,
            colors.onPrimaryContainer,
            Icons.Default.CheckCircle,
            "AI Ready"
        )
        connectionStatus == "Checking..." -> Quadruple(
            colors.secondaryContainer,
            colors.onSecondaryContainer,
            Icons.Default.Sync,
            "Checking AI connection..."
        )
        connectionStatus.contains("Error") || connectionStatus.contains("Unable") -> Quadruple(
            colors.errorContainer,
            colors.onErrorContainer,
            Icons.Default.Error,
            connectionStatus
        )
        // AI just responded (fleeting state)
        lastResponse != null -> Quadruple(
            colors.primaryContainer,
            colors.onPrimaryContainer,
            Icons.Default.Done,
            "AI responded"
        )
        else -> Quadruple(
            colors.surfaceVariant,
            colors.onSurfaceVariant,
            Icons.Default.SmartToy,
            connectionStatus
        )
    }
    
    Surface(
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )
        }
    }
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
