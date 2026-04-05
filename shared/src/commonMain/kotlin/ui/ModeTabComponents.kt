package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import theme.Gray50
import theme.Gray100
import theme.Gray600
import theme.Gray700
import theme.Indigo

/**
 * Tab selector component for switching between Editor, Chat, Analysis, and Settings tabs.
 */
@Composable
fun ModeTabSelector(
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    showRightPanel: Boolean = true,
    onToggleRightPanel: () -> Unit = {}
) {
    Surface(
        color = Gray50,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Editor Tab
            TabButton(
                icon = Icons.Default.Edit,
                label = "Editor",
                isSelected = activeTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            
            // Chat Tab
            TabButton(
                icon = Icons.AutoMirrored.Filled.Chat,
                label = "Chat",
                isSelected = activeTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
            
            // Analysis Tab
            TabButton(
                icon = Icons.Default.Assessment,
                label = "Analysis",
                isSelected = activeTab == 2,
                onClick = { onTabSelected(2) },
                modifier = Modifier.weight(1f)
            )
            
            // Settings Tab
            TabButton(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = activeTab == 3,
                onClick = { onTabSelected(3) },
                modifier = Modifier.weight(1f)
            )
            
            // Right panel toggle button
            IconButton(
                onClick = onToggleRightPanel,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (showRightPanel) Icons.Default.Menu else Icons.AutoMirrored.Filled.MenuOpen,
                    contentDescription = if (showRightPanel) "Hide suggestions" else "Show suggestions",
                    tint = Gray600,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Individual tab button with selection state.
 */
@Composable
fun TabButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Indigo else Gray100
    val contentColor = if (isSelected) androidx.compose.ui.graphics.Color.White else Gray700
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}
