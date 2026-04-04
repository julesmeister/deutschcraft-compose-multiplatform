package ui

import androidx.compose.runtime.*
import data.db.DatabaseDriverFactory
import data.repository.ChatMessage
import data.repository.ChatSession
import data.settings.FontSize
import kotlinx.coroutines.launch
import service.OllamaService
import ui.chat.*

@Composable
fun ChatPanelWithPersistence(
    editorText: String,
    driverFactory: DatabaseDriverFactory,
    fontSize: FontSize = FontSize.MEDIUM,
    onSelectionChange: (String) -> Unit = {},
    onMessagesChange: (List<ChatMessage>) -> Unit = {},
    onAutoSuggestionsChange: (List<String>) -> Unit = {},
    suggestedTitle: String? = null,
    onTitleApplied: () -> Unit = {},
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val scope = rememberCoroutineScope()
    val chatRepository = remember(driverFactory) { driverFactory.chatRepository }
    val ollamaService = remember { OllamaService() }

    var sessions by remember { mutableStateOf<List<ChatSession>>(emptyList()) }
    var currentSessionId by remember { mutableStateOf<Long?>(null) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf("llama3.2") }
    var showSessionList by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Filtered sessions based on category
    val filteredSessions = if (selectedCategory != null) sessions.filter { it.category == selectedCategory } else sessions
    val categories = sessions.mapNotNull { it.category }.distinct().sorted()

    // Dialog state
    var showConfirmDialog by remember { mutableStateOf(false) }
    var confirmMessageId by remember { mutableStateOf<Long?>(null) }
    var confirmMessage by remember { mutableStateOf("") }
    var confirmAction by remember { mutableStateOf<() -> Unit>({}) }

    // Initialize
    LaunchedEffect(Unit) {
        sessions = chatRepository.getAllSessions()
        if (sessions.isEmpty()) {
            val newId = chatRepository.createSession("New Chat")
            currentSessionId = newId
            sessions = chatRepository.getAllSessions()
        } else {
            currentSessionId = sessions.first().id
            messages = chatRepository.getMessagesForSession(currentSessionId!!)
        }
        val models = ollamaService.getAvailableModels()
        if (models.isNotEmpty()) selectedModel = models.first()
    }

    // Load messages on session change
    LaunchedEffect(currentSessionId) {
        currentSessionId?.let { messages = chatRepository.getMessagesForSession(it) }
    }

    // Notify messages change
    LaunchedEffect(messages) {
        onMessagesChange(messages.map { ui.ChatMessage(it.content, it.isUser, it.timestamp.toEpochMilliseconds()) })
    }

    // Handle title suggestion
    LaunchedEffect(suggestedTitle) {
        suggestedTitle?.let { title ->
            currentSessionId?.let { id ->
                chatRepository.updateSessionTitle(id, title)
                sessions = chatRepository.getAllSessions()
                onTitleApplied()
            }
        }
    }

    val systemContext = "You are a helpful writing assistant. Document: ${editorText.take(1000)}"

    // Managers
    val sessionManager = ChatSessionManager(
        scope, chatRepository, currentSessionId, sessions, messages,
        onSessionsChanged = { sessions = it },
        onCurrentSessionChanged = { currentSessionId = it },
        onMessagesChanged = { messages = it }
    )

    val messageManager = ChatMessageManager(
        scope, chatRepository, ollamaService, messages,
        currentSessionId = { currentSessionId },
        selectedModel = { selectedModel },
        systemContext = { systemContext },
        onMessagesChanged = { messages = it },
        onGeneratingChanged = { isGenerating = it },
        onSessionsChanged = { sessions = it }
    )

    val categorizer = SessionCategorizer(scope, chatRepository, ollamaService, { selectedModel }) { sessions = chatRepository.getAllSessions() }
    val suggestionGen = SuggestionGenerator(scope, ollamaService, { selectedModel }, onAutoSuggestionsChange)

    // Use cases
    val sendMessageUseCase = remember {
        ChatSendMessageUseCase(
            scope, chatRepository, ollamaService,
            selectedModel = { selectedModel },
            systemContext = { systemContext },
            onMessagesChanged = { messages = it },
            onGeneratingChanged = { isGenerating = it },
            onSessionsChanged = { sessions = it },
            onAutoSuggestionsChange = onAutoSuggestionsChange,
            onCategorizeSession = { id, msgs -> categorizer.categorizeSession(id, msgs) },
            currentJob = null
        )
    }

    // Action handlers
    fun createNewSession() = sessionManager.createNewSession()
    fun switchSession(id: Long) = sessionManager.switchSession(id)
    fun deleteSession(id: Long) = sessionManager.deleteSession(id)
    fun editMessage(id: Long, content: String) = messageManager.editMessage(id, content)
    fun deleteMessage(id: Long) = messageManager.deleteMessage(id)
    fun regenerateMessage(id: Long) = messageManager.regenerateMessage(id)

    fun sendMessage() {
        if (inputText.isBlank() || isGenerating || currentSessionId == null) return
        sendMessageUseCase(inputText, currentSessionId!!) { inputText = "" }
    }

    fun showDeleteConfirmation(id: Long, msg: String) {
        confirmMessageId = id
        confirmMessage = msg
        confirmAction = { deleteMessage(id); showConfirmDialog = false }
        showConfirmDialog = true
    }

    ChatLayout(
        sessions = sessions,
        filteredSessions = filteredSessions,
        currentSessionId = currentSessionId,
        categories = categories,
        selectedCategory = selectedCategory,
        messages = messages,
        isGenerating = isGenerating,
        inputText = inputText,
        showSessionList = showSessionList,
        showConfirmDialog = showConfirmDialog,
        confirmMessage = confirmMessage,
        confirmAction = confirmAction,
        onDismissDialog = { showConfirmDialog = false },
        onSessionClick = { switchSession(it) },
        onNewSessionClick = { createNewSession() },
        onDeleteSession = { deleteSession(it) },
        onCategorySelected = { selectedCategory = it },
        onToggleSidebar = { showSessionList = !showSessionList },
        onSendMessage = { sendMessage() },
        onStopGeneration = { /* cancel job */ },
        onInputChange = { inputText = it },
        onDeleteMessage = { deleteMessage(it) },
        onRegenerateMessage = { regenerateMessage(it) },
        onTextSelected = onSelectionChange,
        onShowDeleteConfirmation = { id, msg -> showDeleteConfirmation(id, msg) },
        fontSize = fontSize,
        modifier = modifier
    )
}
