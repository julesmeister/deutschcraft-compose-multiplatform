package ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import theme.Gray200

@Composable
fun ErrorBanner(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    Surface(
        color = Color(0xFFFFE4E4),
        modifier = Modifier.fillMaxWidth()
    ) {
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD32F2F),
                modifier = Modifier.weight(1f),
                maxLines = 2
            )
            
            // Copy button
            val copyInteractionSource = remember { MutableInteractionSource() }
            val isCopyHovered by copyInteractionSource.collectIsHoveredAsState()
            
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isCopyHovered) Color(0xFFD32F2F).copy(alpha = 0.1f) else Color.Transparent)
                    .hoverable(copyInteractionSource)
                    .clickable { 
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(errorMessage))
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy error",
                    tint = if (isCopyHovered) Color(0xFFD32F2F) else Color(0xFFD32F2F).copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Dismiss button
            val dismissInteractionSource = remember { MutableInteractionSource() }
            val isDismissHovered by dismissInteractionSource.collectIsHoveredAsState()
            
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isDismissHovered) Color(0xFFD32F2F).copy(alpha = 0.1f) else Color.Transparent)
                    .hoverable(dismissInteractionSource)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = if (isDismissHovered) Color(0xFFD32F2F) else Color(0xFFD32F2F).copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
    HorizontalDivider(color = Gray200)
}
