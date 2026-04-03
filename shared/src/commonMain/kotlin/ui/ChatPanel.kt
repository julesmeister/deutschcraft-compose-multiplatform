package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    var selectedModel by remember { mutableStateOf("llama3.2") }
    
    // System prompt that includes current editor context
    val systemContext = "You are a helpful writing assistant. The user is currently working on a document. Current document content: ${editorText.take(1000)}"
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
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
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask AI about your writing...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo,
                        focusedContainerColor = androidx.compose.ui.graphics.Color.White
                    )
                )
                
                IconButton(
                    onClick = {
                        if (inputText.isBlank() || isGenerating) return@IconButton
                        
                        val userMessage = inputText.trim()
                        messages = messages + ChatMessage(userMessage, isUser = true)
                        inputText = ""
                        
                        scope.launch {
                            isGenerating = true
                            try {
                                val response = ollamaService.chat(
                                    messages = messages,
                                    systemContext = systemContext,
                                    model = selectedModel
                                )
                                messages = messages + ChatMessage(response, isUser = false)
                            } catch (e: Exception) {
                                messages = messages + ChatMessage(
                                    "Sorry, I encountered an error: ${e.message}",
                                    isUser = false
                                )
                            } finally {
                                isGenerating = false
                            }
                        }
                    },
                    enabled = inputText.isNotBlank() && !isGenerating,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank() && !isGenerating) Indigo else Gray400,
                        modifier = Modifier.size(24.dp)
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
    val backgroundColor = if (message.isUser) Indigo else Gray100
    val textColor = if (message.isUser) androidx.compose.ui.graphics.Color.White else Gray800
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    
    Box(
        modifier = modifier,
        contentAlignment = alignment
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun EmptyChatState(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.ChatBubble,
            contentDescription = null,
            tint = Gray300,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chat with AI",
            style = MaterialTheme.typography.titleMedium,
            color = Gray600
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ask questions about your writing, get feedback, or brainstorm ideas. The AI knows about your current document.",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray400,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
