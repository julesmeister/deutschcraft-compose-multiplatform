package ui.grammar.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.model.UserProgressStats
import theme.Gray200
import theme.Gray50
import theme.Gray500
import theme.Gray800
import theme.Indigo

@Composable
fun ProgressOverviewCard(stats: UserProgressStats, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Indigo.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Indigo,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Your Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gray800
                    )
                    Text(
                        text = "Keep up the great work!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
            }

            Divider(color = Gray200)

            // Stats in a row with dividers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                M3StatItem(
                    icon = Icons.Default.Edit,
                    value = stats.totalWritingSessions.toString(),
                    label = "Sessions",
                    accentColor = Indigo
                )

                VerticalDivider(color = Gray200, modifier = Modifier.height(40.dp))

                M3StatItem(
                    icon = Icons.Default.MenuBook,
                    value = stats.totalWordsWritten.toString(),
                    label = "Words",
                    accentColor = Color(0xFF4CAF50)
                )

                VerticalDivider(color = Gray200, modifier = Modifier.height(40.dp))

                M3StatItem(
                    icon = Icons.Default.School,
                    value = stats.errorFrequencyByType.size.toString(),
                    label = "Focus Areas",
                    accentColor = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
fun M3StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    accentColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Gray800
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Gray500
        )
    }
}
