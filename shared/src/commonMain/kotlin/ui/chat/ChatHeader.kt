package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import theme.*

@Composable
internal fun ChatHeader(
    sessionTitle: String,
    showSessionList: Boolean,
    onToggleSidebar: () -> Unit,
    onNewChat: () -> Unit,
    connectionStatus: String,
    isGenerating: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        modifier = modifier
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleSidebar,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (showSessionList) Icons.AutoMirrored.Filled.MenuOpen else Icons.Default.Menu,
                            contentDescription = "Toggle sidebar",
                            tint = Gray600,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = sessionTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Connection/Generating indicator
                    if (isGenerating) {
                        // Animated loading indicator
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp,
                            color = Indigo
                        )
                    } else {
                        val connectionColor = if (connectionStatus == "Connected") Color(0xFF4CAF50) else Color(0xFFFF9800)
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(connectionColor)
                        )
                    }
                    Text(
                        text = connectionStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray500
                    )

                    IconButton(
                        onClick = onNewChat,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "New chat",
                            tint = Indigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = Gray200)
        }
    }
}
