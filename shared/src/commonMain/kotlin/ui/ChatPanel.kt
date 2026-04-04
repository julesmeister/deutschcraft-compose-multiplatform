package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.hoverable
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import kotlinx.coroutines.launch
import service.OllamaService
import theme.*

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun ChatPanel(
    editorText: String,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val ollamaService = remember { OllamaService() }
    
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf("Checking...") }
    var selectedModel by remember { mutableStateOf("llama3.2") }
    
    // Check Ollama connection on startup
    LaunchedEffect(Unit) {
        val isConnected = ollamaService.checkConnection()
        connectionStatus = if (isConnected) "Connected" else "Not connected - Is Ollama running on localhost:11434?"
        if (isConnected) {
            val models = ollamaService.getAvailableModels()
            if (models.isNotEmpty()) {
                selectedModel = models.first()
            }
        }
    }
    
    // System prompt that includes current editor context
    val systemContext = "You are a helpful writing assistant. The user is currently working on a document. Current document content: ${editorText.take(1000)}"
    
    var currentJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    
    // Extract send logic for reuse
    fun sendMessage() {
        if (inputText.isBlank() || isGenerating) return
        
        val userMessage = inputText.trim()
        messages = messages + ChatMessage(userMessage, isUser = true)
        inputText = ""
        
        currentJob = scope.launch {
            isGenerating = true
            try {
                val response = ollamaService.chat(
                    messages = messages,
                    systemContext = systemContext,
                    model = selectedModel
                )
                messages = messages + ChatMessage(response, isUser = false)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    messages = messages + ChatMessage("Generation stopped by user.", isUser = false)
                } else {
                    messages = messages + ChatMessage(
                        "Sorry, I encountered an error: ${e.message}",
                        isUser = false
                    )
                }
            } finally {
                isGenerating = false
                currentJob = null
            }
        }
    }
    
    fun stopGeneration() {
        currentJob?.cancel()
        currentJob = null
    }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Connection status banner
        if (!connectionStatus.startsWith("Connected")) {
            Surface(
                color = Color(0xFFFFE4E4),
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
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = connectionStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
        
        // Chat messages area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                EmptyChatState(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    messages.forEach { message ->
                        ChatBubble(
                            message = message,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (isGenerating) {
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
        
        Divider(color = Gray200)
        
        // Input area
        Surface(
            color = Gray50,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Lotel-style text field - rounded fill, no border
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Gray100)
                        .heightIn(min = 48.dp, max = 120.dp),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = Gray800
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotBlank() && !isGenerating) {
                            sendMessage()
                        }
                    }),
                    cursorBrush = SolidColor(Indigo),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "Ask AI about your writing...",
                                    fontSize = 16.sp,
                                    color = Gray400
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                
                val sendButtonInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                val isSendButtonHovered by sendButtonInteractionSource.collectIsHoveredAsState()
                
                val buttonSize = if (isGenerating) 48.dp else 40.dp
                val buttonBgColor = when {
                    isGenerating -> Color(0xFFDC2626)
                    isSendButtonHovered && inputText.isNotBlank() -> IndigoDark
                    inputText.isNotBlank() -> Indigo
                    else -> Gray300
                }
                val iconTint = when {
                    isGenerating -> Color.White
                    inputText.isNotBlank() -> Color.White
                    else -> Gray500
                }
                val iconScale = if (isSendButtonHovered && !isGenerating) 1.1f else 1f
                
                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .clip(CircleShape)
                        .background(buttonBgColor)
                        .hoverable(sendButtonInteractionSource)
                        .then(
                            if (isGenerating || inputText.isNotBlank()) {
                                Modifier.clickable { if (isGenerating) stopGeneration() else sendMessage() }
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGenerating) Icons.Default.Close else Icons.Default.Send,
                        contentDescription = if (isGenerating) "Stop" else "Send",
                        tint = iconTint,
                        modifier = Modifier
                            .size(if (isGenerating) 24.dp else 20.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.isUser
    val backgroundColor = if (isUser) Indigo else Gray100
    val textColor = if (isUser) Color.White else Gray800
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    Row(
        modifier = modifier,
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // AI avatar (left side) - circular, aligned to bottom
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Indigo.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI",
                    tint = Indigo,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
        
        // Message bubble with hover effect and copy button
        Box(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .hoverable(interactionSource)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor
                ),
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                )
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    modifier = Modifier.padding(14.dp)
                )
            }
            
            // Copy button - appears on hover
            if (isHovered) {
                IconButton(
                    onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(message.text))
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 4.dp)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = if (isUser) Color.White.copy(alpha = 0.8f) else Gray600,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        // User avatar (right side) - circular, aligned to bottom
        if (isUser) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Indigo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "You",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

