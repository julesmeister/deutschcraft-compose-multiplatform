package ui.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import theme.Danger
import theme.Gray500
import theme.Gray800
import theme.Indigo

@Composable
internal fun CleanupSection(
    title: String,
    description: String,
    icon: ImageVector,
    onClearLastWeek: () -> Unit = {},
    onClearLastMonth: () -> Unit = {},
    onClearLearned: (() -> Unit)? = null,
    onClearOld: (() -> Unit)? = null,
    onClearAll: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // M3 Circular icon background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Indigo.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Indigo,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Gray800
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (onClearLearned != null && onClearOld != null) {
                // Vocabulary-specific buttons
                OutlinedButton(
                    onClick = onClearLearned,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear Learned")
                }
                OutlinedButton(
                    onClick = onClearOld,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear Old")
                }
            } else {
                // Standard buttons
                OutlinedButton(
                    onClick = onClearLastWeek,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Last Week")
                }
                OutlinedButton(
                    onClick = onClearLastMonth,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Last Month")
                }
            }
            
            Button(
                onClick = onClearAll,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Danger
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear All")
            }
        }
    }
}
