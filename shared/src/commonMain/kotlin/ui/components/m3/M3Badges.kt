package ui.components.m3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import theme.Danger
import theme.Gray400
import theme.LocalFontSize
import theme.Success

@Composable
fun M3ToggleBadge(enabled: Boolean, modifier: Modifier = Modifier) {
    val bgColor = if (enabled) Success.copy(alpha = 0.15f) else Color(0xFFF1F5F9)
    val iconColor = if (enabled) Success else Gray400

    Box(
        modifier = modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (enabled) Icons.Default.Check else Icons.Default.Close,
            contentDescription = if (enabled) "Enabled" else "Disabled",
            tint = iconColor,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
fun M3ValueBadge(
    text: String,
    modifier: Modifier = Modifier,
    bgColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(8.dp)).background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = LocalFontSize.current.sm,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            color = textColor,
        )
    }
}
