package ui.chat

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import data.repository.ChatMessage
import data.settings.FontSize
import theme.Gray500
import theme.Indigo
import ui.EmptyChatState
import ui.PersistentChatBubble
import ui.animations.PulsingDots
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.material3.Icon

// DEBUG: Constraint tracing
import ui.chat.debugConstraints

/**
 * Displays a scrollable list of chat messages with entrance animations.
 *
 * CRITICAL: This component must be used with a parent that provides bounded constraints.
 * Use Modifier.weight(1f) from parent to allocate available space properly.
 * DO NOT use fillMaxSize() at the parent level or you'll get infinite constraints.
 *
 * @param messages List of chat messages to display
 * @param isGenerating Whether AI is currently generating a response
 * @param onDeleteMessage Callback when user deletes a message
 * @param onRegenerateMessage Callback when user regenerates an AI response
 * @param onTextSelected Callback when text is selected in a message
 * @param onShowDeleteConfirmation Callback to show delete confirmation dialog
 * @param fontSize Font size setting for message text
 * @param modifier Should include weight(1f) from parent for proper sizing
 */
@Composable
internal fun ChatMessagesList(
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    onDeleteMessage: (Long) -> Unit,
    onRegenerateMessage: (Long) -> Unit,
    onTextSelected: (String) -> Unit,
    onShowDeleteConfirmation: (Long, String) -> Unit,
    fontSize: FontSize,
    modifier: Modifier = Modifier
) {
    // ═════════════════════════════════════════════════════════════════════════════
    // SECTION: ChatMessagesList - LazyColumn with animated message bubbles
    // DEBUG: Verify modifier has bounded constraints (should include weight from parent)
    // ═════════════════════════════════════════════════════════════════════════════
    Box(
        modifier = modifier.debugConstraints("ChatMessagesList Box")
    ) {
        if (messages.isEmpty()) {
            // ─────────────────────────────────────────────────────────────────────────────
            // SUBSECTION: Empty state placeholder
            // ─────────────────────────────────────────────────────────────────────────────
            EmptyChatState(modifier = Modifier.align(Alignment.Center))
        } else {
            // ─────────────────────────────────────────────────────────────────────────────
            // SUBSECTION: Messages LazyColumn with entrance animations
            // NOTE: fillMaxSize here is safe because parent Box has bounded constraints from weight(1f)
            // ─────────────────────────────────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize().debugConstraints("ChatMessagesList LazyColumn"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(messages, key = { _, message -> message.id }) { index, message ->
                    var visible by remember(message.id) { mutableStateOf(false) }
                    LaunchedEffect(message.id) {
                        kotlinx.coroutines.delay(index * 50L)
                        visible = true
                    }
                    val scale by animateFloatAsState(
                        targetValue = if (visible) 1f else 0.8f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "message_scale_$index"
                    )
                    val alpha by animateFloatAsState(
                        targetValue = if (visible) 1f else 0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "message_alpha_$index"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            }
                    ) {
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
                }
                if (isGenerating) {
                    // ─────────────────────────────────────────────────────────────────────────────
                    // SUBSECTION: Typing indicator while AI generates response
                    // ─────────────────────────────────────────────────────────────────────────────
                    item {
                        Row(
                            // FIX: Changed start padding from 56.dp to 16.dp to match PersistentChatBubble Row padding
                            modifier = Modifier.padding(top = 8.dp, start = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // AI Avatar for typing indicator
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Indigo.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "AI",
                                    tint = Indigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            PulsingDots(
                                dotCount = 3,
                                dotSize = 8.dp,
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
