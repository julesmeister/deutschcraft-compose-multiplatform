package ui.components.m3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import theme.DeutschCraftTheme
import theme.Danger

@Composable
fun NotificationBadge(count: Int, modifier: Modifier = Modifier, backgroundColor: Color = Danger) {
    if (count <= 0) return
    Box(
        modifier = modifier.size(18.dp).clip(CircleShape).background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = Color.White, fontSize = DeutschCraftTheme.fontSize.xxxs, fontWeight = FontWeight.Bold,
        )
    }
}
