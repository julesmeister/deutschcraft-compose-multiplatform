package ui.components.m3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import theme.DeutschCraftTheme
import theme.Gray400

@Composable
fun NoDataPlaceholder(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(DeutschCraftTheme.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DeutschCraftTheme.spacing.sm),
    ) {
        Icon(imageVector = Icons.Outlined.Info, contentDescription = null, tint = Gray400, modifier = Modifier.size(32.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = Gray400, textAlign = TextAlign.Center)
    }
}
