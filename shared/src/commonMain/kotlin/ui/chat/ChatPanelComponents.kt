package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import theme.Indigo

@Composable
internal fun ConnectionStatusBanner(
    connectionStatus: String,
    isGenerating: Boolean = false,
    lastResponse: String? = null
) {
    val colors = MaterialTheme.colorScheme

    // Determine what to show
    val (containerColor, contentColor, icon, message) = when {
        // AI actively generating response
        isGenerating -> Quadruple(
            colors.tertiaryContainer,
            colors.onTertiaryContainer,
            Icons.Default.AutoMode,
            "AI is typing..."
        )
        // Connection states
        connectionStatus == "Connected" -> Quadruple(
            colors.primaryContainer,
            colors.onPrimaryContainer,
            Icons.Default.CheckCircle,
            "AI Ready"
        )
        connectionStatus == "Checking..." -> Quadruple(
            colors.secondaryContainer,
            colors.onSecondaryContainer,
            Icons.Default.Sync,
            "Checking AI connection..."
        )
        connectionStatus.contains("Error") || connectionStatus.contains("Unable") -> Quadruple(
            colors.errorContainer,
            colors.onErrorContainer,
            Icons.Default.Error,
            connectionStatus
        )
        // AI just responded (fleeting state)
        lastResponse != null -> Quadruple(
            colors.primaryContainer,
            colors.onPrimaryContainer,
            Icons.Default.Done,
            "AI responded"
        )
        else -> Quadruple(
            colors.surfaceVariant,
            colors.onSurfaceVariant,
            Icons.Default.SmartToy,
            connectionStatus
        )
    }

    Surface(
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )
        }
    }
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
