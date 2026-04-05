package ui.components.m3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import theme.Gray500
import theme.Gray700
import theme.LocalFontSize
import theme.LocalSpacing

@Composable
fun M3ListRow(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconBg: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = LocalSpacing.current.md, vertical = LocalSpacing.current.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        M3IconBox(icon = icon, tint = iconTint, bg = iconBg)
        Spacer(modifier = Modifier.width(LocalSpacing.current.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = LocalFontSize.current.base, fontWeight = FontWeight.Medium, color = Gray700)
            if (description != null) {
                Text(text = description, fontSize = LocalFontSize.current.xs, color = Gray500)
            }
        }
        content()
    }
}

@Composable
fun M3ListDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = LocalSpacing.current.md),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

@Composable
fun M3IconBox(icon: ImageVector, modifier: Modifier = Modifier, tint: Color = Color.White, bg: Color = Color.Gray) {
    Box(
        modifier = modifier.size(36.dp).clip(CircleShape).background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}
