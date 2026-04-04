package ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import theme.Gray100
import theme.Gray200
import theme.Gray400
import theme.Gray500

@Composable
fun EditorStatusBar(
    wordCount: Int,
    charCount: Int
) {
    Divider(color = Gray200)
    
    Surface(
        tonalElevation = 0.dp,
        color = Gray100,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$wordCount words  |  $charCount characters",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Ctrl+A: Select All  •  Select text for AI help",
                style = MaterialTheme.typography.bodySmall,
                color = Gray400
            )
        }
    }
}
