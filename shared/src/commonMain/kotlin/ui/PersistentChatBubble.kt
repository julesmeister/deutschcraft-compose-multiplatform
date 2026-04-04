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
import data.repository.ChatMessage
import data.settings.FontSize

@Composable
internal fun PersistentChatBubble(
    message: ChatMessage,
    fontSize: FontSize = FontSize.MEDIUM,
    onEdit: (Long, String) -> Unit = { _, _ -> },
    onDelete: (Long) -> Unit = {},
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

    var isEditing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(message.content) }

    // Scale text based on font size setting
    val textStyle = when (fontSize) {
        FontSize.SMALL -> typography.bodySmall
        FontSize.MEDIUM -> typography.bodyMedium
        FontSize.LARGE -> typography.bodyLarge
    }

    Surface(
        color = Color.Transparent,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isUser) {
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

            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .hoverable(interactionSource)
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
                    if (isEditing) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BasicTextField(
                                value = editText,
                                onValueChange = { editText = it },
                                textStyle = textStyle.copy(color = textColor),
                                modifier = Modifier.widthIn(min = 100.dp, max = 300.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        onEdit(message.id, editText)
                                        isEditing = false
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = textColor
                                    )
                                ) {
                                    Text("Save")
                                }
                                TextButton(
                                    onClick = {
                                        isEditing = false
                                        editText = message.content
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = textColor
                                    )
                                ) {
                                    Text("Cancel")
                                }
                            }
                        }
                    } else {
                        Text(
                            text = message.content,
                            style = textStyle,
                            color = textColor,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Action buttons on hover (only when not editing)
                if (isHovered && !isEditing) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-8).dp)
                            .background(
                                color = colors.surface,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Edit button (only for user messages)
                        if (isUser) {
                            IconButton(
                                onClick = { isEditing = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = colors.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Copy button
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(message.content))
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = colors.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Delete button
                        IconButton(
                            onClick = { onDelete(message.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = colors.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (isUser) {
                Spacer(modifier = Modifier.width(8.dp))
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
    }
}
