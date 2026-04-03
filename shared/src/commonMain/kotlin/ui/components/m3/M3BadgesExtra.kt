package ui.components.m3

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.DeutschCraftTheme

@Composable
fun M3DateBadge(
    month: String,
    day: String,
    modifier: Modifier = Modifier,
    accentColor: Color = M3OnSurfaceVariant,
    bgColor: Color = Color(0xFFF0F1FA),
) {
    Column(
        modifier = modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(bgColor)
            .padding(top = 5.dp, bottom = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(month, fontSize = DeutschCraftTheme.fontSize.xs, fontWeight = FontWeight.Medium, color = accentColor, lineHeight = 12.sp)
        Text(day, fontSize = DeutschCraftTheme.fontSize.xlPlus, fontWeight = FontWeight.Bold, color = M3OnSurface, lineHeight = 20.sp)
    }
}

@Composable
fun M3StatusBadge(
    text: String,
    color: Color,
    containerColor: Color,
    icon: ImageVector? = null,
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        alpha.animateTo(1f, tween(200))
    }

    Box(
        modifier = Modifier.graphicsLayer(scaleX = scale.value, scaleY = scale.value, alpha = alpha.value)
            .clip(RoundedCornerShape(50)).background(containerColor).padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(text, fontSize = DeutschCraftTheme.fontSize.sm, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
fun M3ToggleBadgePill(
    enabled: Boolean,
    onLabel: String = "On",
    offLabel: String = "Off",
) {
    val color = if (enabled) M3GreenColor else M3RedColor
    val container = if (enabled) M3GreenContainer else M3RedContainer
    val icon = if (enabled) Icons.Default.Check else Icons.Default.Close

    Box(
        modifier = Modifier.clip(RoundedCornerShape(50)).background(container)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (enabled) onLabel else offLabel, fontSize = DeutschCraftTheme.fontSize.sm, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
fun TonalBadge(text: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(color.copy(0.10f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, modifier = Modifier.size(13.dp), tint = color)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = DeutschCraftTheme.fontSize.xs, fontWeight = FontWeight.SemiBold, color = color, maxLines = 1)
    }
}

@Composable
fun M3SummaryTab(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.width(140.dp).clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = if (isActive) 1f else 0.85f)).clickable(onClick = onClick)
            .then(if (isActive) Modifier.background(Color.White.copy(0.15f)) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(value, fontSize = DeutschCraftTheme.fontSize.xxl, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = Color.White.copy(0.8f))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = DeutschCraftTheme.fontSize.sm, color = Color.White.copy(0.85f))
        }
        if (isActive) {
            Box(
                modifier = Modifier.align(Alignment.TopEnd).size(8.dp).clip(CircleShape).background(Color.White),
            )
        }
    }
}
