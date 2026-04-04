package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.text.selection.SelectionContainer
import data.repository.ChatMessage
import data.settings.FontSize

@Composable
internal fun PersistentChatBubble(
    message: ChatMessage,
    fontSize: FontSize = FontSize.MEDIUM,
    onDelete: (Long) -> Unit = {},
    onRegenerate: (Long) -> Unit = {},
    onTextSelected: (String) -> Unit = {},
    showDeleteConfirm: (Long, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val isUser = message.isUser
    val backgroundColor = if (isUser) colors.primary else colors.surfaceVariant
    val textColor = if (isUser) colors.onPrimary else colors.onSurfaceVariant
    val clipboardManager = LocalClipboardManager.current

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Scale text based on font size setting
    val textStyle = when (fontSize) {
        FontSize.SMALL -> typography.bodySmall
        FontSize.MEDIUM -> typography.bodyMedium
        FontSize.LARGE -> typography.bodyLarge
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Main bubble row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isUser) {
                // AI Avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI",
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Copy button (beside bubble, shown on hover)
            if (isHovered && isUser) {
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(message.content))
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Message bubble
            Box(
                modifier = Modifier.hoverable(interactionSource)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                ) {
                    SelectionContainer(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = message.content,
                            style = textStyle,
                            color = textColor
                        )
                    }
                }
            }

            // Copy button (beside bubble for AI, shown on hover)
            if (isHovered && !isUser) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(message.content))
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                // User Avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "You",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // AI message actions (delete & regenerate) - shown below the bubble
        if (!isUser) {
            Row(
                modifier = Modifier
                    .padding(start = 56.dp, top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Regenerate button
                TextButton(
                    onClick = { onRegenerate(message.id) },
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Regenerate",
                        modifier = Modifier.size(14.dp),
                        tint = colors.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Regenerate",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary
                    )
                }

                // Delete button
                TextButton(
                    onClick = { showDeleteConfirm(message.id, "Delete this AI response?") },
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(14.dp),
                        tint = colors.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Delete",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.error
                    )
                }
            }
        }
    }
}
