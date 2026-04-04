package ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import theme.Gray300
import theme.Gray400
import theme.Gray50
import theme.Gray600
import theme.Indigo

@Composable
fun EditorToolbar(
    text: String,
    currentEssayTitle: String? = null,
    onAnalyzeClick: () -> Unit,
    onClearClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onOpenClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Surface(
        tonalElevation = 0.dp,
        color = Gray50,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side - Title with essay name if saved
            Column {
                Text(
                    text = "Editor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                if (!currentEssayTitle.isNullOrBlank()) {
                    Text(
                        text = currentEssayTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Indigo
                    )
                }
            }
            
            // Right side - Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Open button
                val openInteractionSource = remember { MutableInteractionSource() }
                val isOpenHovered by openInteractionSource.collectIsHoveredAsState()

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isOpenHovered) Indigo.copy(alpha = 0.1f) else Color.Transparent)
                        .hoverable(openInteractionSource)
                        .clickable { onOpenClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = "Open essay",
                        tint = if (isOpenHovered) Indigo else Gray400,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer {
                                scaleX = if (isOpenHovered) 1.15f else 1f
                                scaleY = if (isOpenHovered) 1.15f else 1f
                            }
                    )
                }

                // Save button
                val saveInteractionSource = remember { MutableInteractionSource() }
                val isSaveHovered by saveInteractionSource.collectIsHoveredAsState()

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isSaveHovered) Color(0xFFDBEAFE) else Color.Transparent)
                        .hoverable(saveInteractionSource)
                        .clickable(enabled = text.isNotBlank()) { onSaveClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = "Save essay",
                        tint = if (text.isNotBlank()) (if (isSaveHovered) Color(0xFF2563EB) else Gray600) else Gray300,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer {
                                scaleX = if (isSaveHovered) 1.15f else 1f
                                scaleY = if (isSaveHovered) 1.15f else 1f
                            }
                    )
                }

                // Analyze button
                val analyzeInteractionSource = remember { MutableInteractionSource() }
                val isAnalyzeHovered by analyzeInteractionSource.collectIsHoveredAsState()
                
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(CircleShape)
                        .background(if (isAnalyzeHovered) Indigo.copy(alpha = 0.1f) else Color.Transparent)
                        .hoverable(analyzeInteractionSource)
                        .clickable(enabled = text.isNotBlank()) { onAnalyzeClick() }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Spellcheck,
                            contentDescription = "Analyze grammar",
                            tint = if (text.isNotBlank()) (if (isAnalyzeHovered) Indigo else Gray600) else Gray300,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Analyze",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (text.isNotBlank()) (if (isAnalyzeHovered) Indigo else Gray600) else Gray300
                        )
                    }
                }
                
                // Clear button with red hover
                val clearInteractionSource = remember { MutableInteractionSource() }
                val isClearHovered by clearInteractionSource.collectIsHoveredAsState()
                
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isClearHovered) Color(0xFFFEE2E2) else Color.Transparent)
                        .hoverable(clearInteractionSource)
                        .clickable { onClearClick() },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear all",
                        tint = if (isClearHovered) Color(0xFFDC2626) else Gray400,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer {
                                scaleX = if (isClearHovered) 1.15f else 1f
                                scaleY = if (isClearHovered) 1.15f else 1f
                            }
                    )
                }
                
                // Refresh button with green hover
                val refreshInteractionSource = remember { MutableInteractionSource() }
                val isRefreshHovered by refreshInteractionSource.collectIsHoveredAsState()
                
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isRefreshHovered) Color(0xFFD1FAE5) else Color.Transparent)
                        .hoverable(refreshInteractionSource)
                        .clickable { onRefreshClick() },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = if (isRefreshHovered) Color(0xFF059669) else Gray400,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer {
                                scaleX = if (isRefreshHovered) 1.15f else 1f
                                scaleY = if (isRefreshHovered) 1.15f else 1f
                            }
                    )
                }
            }
        }
    }
}
