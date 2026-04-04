package ui.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import theme.Gray600
import theme.Gray800
import theme.Indigo

@Composable
internal fun StorageStatsCard(stats: DataStats) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = Indigo,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "Storage Statistics",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
        }
        
        StatRow(label = "Study Entries", value = "${stats.entriesCount}")
        StatRow(label = "Chat Sessions", value = "${stats.chatSessionsCount}")
        StatRow(label = "Chat Messages", value = "${stats.chatMessagesCount}")
        StatRow(label = "Vocabulary Items", value = "${stats.vocabularyCount}")
        StatRow(label = "Grammar Mistakes", value = "${stats.mistakesCount}")
        StatRow(label = "Total Words Written", value = "${stats.totalWords}")
    }
}

@Composable
internal fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray600
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Gray800
        )
    }
}
