package ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import data.repository.ChatSession
import theme.*
import ui.components.m3.DCChipPickerSheet
import ui.chat.debugConstraints

@Composable
internal fun SessionSidebar(
    sessions: List<ChatSession>,
    currentSessionId: Long?,
    categories: List<String>,
    selectedCategory: String?,
    onSessionClick: (Long) -> Unit,
    onNewSessionClick: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCategoryDialog by remember { mutableStateOf(false) }
    Surface(
        color = Gray50,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Gray200),
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chat History",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filter button
                    IconButton(
                        onClick = { showCategoryDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter by category",
                            tint = if (selectedCategory != null) Indigo else Gray500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onNewSessionClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New chat",
                            tint = Indigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Divider(color = Gray200)

            // Category Picker Sheet
            if (showCategoryDialog) {
                val allOptions = listOf("All") + categories
                DCChipPickerSheet(
                    title = "Filter by Category",
                    subtitle = "Select a category to filter conversations",
                    options = allOptions,
                    selected = selectedCategory ?: "All",
                    accentColor = Indigo,
                    onSelected = { option ->
                        onCategorySelected(if (option == "All") null else option)
                        showCategoryDialog = false
                    },
                    onDismiss = { showCategoryDialog = false },
                    chipAvatar = { option, isSelected ->
                        if (isSelected) {
                            Text(
                                text = "✓",
                                color = Color.White,
                                fontSize = DeutschCraftTheme.fontSize.base,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = when (option) {
                                    "All" -> "◦"
                                    else -> option.take(1).uppercase()
                                },
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Indigo
                            )
                        }
                    }
                )
            }

            // Session list
            LazyColumn(
                modifier = Modifier.weight(1f).debugConstraints("SessionSidebar LazyColumn"),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(sessions, key = { it.id }) { session ->
                    val isSelected = session.id == currentSessionId
                    val interactionSource = remember { MutableInteractionSource() }
                    val isHovered by interactionSource.collectIsHoveredAsState()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isSelected -> Indigo.copy(alpha = 0.1f)
                                    isHovered -> Gray100
                                    else -> Color.Transparent
                                }
                            )
                            .hoverable(interactionSource)
                            .clickable { onSessionClick(session.id) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = null,
                            tint = if (isSelected) Indigo else Gray500,
                            modifier = Modifier.size(16.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = session.title ?: "Chat ${session.id}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal,
                                color = if (isSelected) Indigo else Gray800,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatTimestamp(session.updatedAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray500
                            )
                        }

                        if (isSelected || isHovered) {
                            IconButton(
                                onClick = { onDeleteSession(session.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Gray400,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
