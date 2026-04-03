package ui.components.m3

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import theme.DeutschCraftTheme

@Composable
fun DCSelectionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color = M3Primary,
    avatar: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) accentColor else accentColor.copy(alpha = 0.08f),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape)
                    .background(if (selected) Color.White.copy(0.25f) else accentColor.copy(0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                avatar()
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                label,
                fontSize = DeutschCraftTheme.fontSize.base,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else M3OnSurface,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun DCQuantityStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minValue: Int = 0,
    maxValue: Int = Int.MAX_VALUE,
    accentColor: Color = M3Primary,
    valueDisplay: @Composable ((Int) -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            onClick = { if (value > minValue) onValueChange(value - 1) },
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.10f),
            modifier = Modifier.size(36.dp),
            enabled = value > minValue,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Remove, null, modifier = Modifier.size(18.dp), tint = accentColor)
            }
        }
        if (valueDisplay != null) {
            valueDisplay(value)
        } else {
            Text(
                "$value",
                fontSize = DeutschCraftTheme.fontSize.displaySm,
                fontWeight = FontWeight.Bold,
                color = M3OnSurface,
            )
        }
        Surface(
            onClick = { if (value < maxValue) onValueChange(value + 1) },
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.10f),
            modifier = Modifier.size(36.dp),
            enabled = value < maxValue,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = accentColor)
            }
        }
    }
}

@Composable
fun DCTextChip(label: String, selected: Boolean, onClick: () -> Unit, accentColor: Color = M3Primary) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) accentColor else accentColor.copy(alpha = 0.08f),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = DeutschCraftTheme.fontSize.md,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else accentColor,
            maxLines = 1,
        )
    }
}

@Composable
fun DCActionChip(icon: ImageVector, label: String, color: Color, containerColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(containerColor).clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = color)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = DeutschCraftTheme.fontSize.sm, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

data class DCTab(val label: String, val icon: ImageVector? = null)

@Composable
fun DCTabSelector(
    tabs: List<DCTab>,
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    accentColor: Color = M3Primary,
    onDarkBackground: Boolean = false,
) {
    val outerBg = if (onDarkBackground) Color.Transparent else Color.White
    val trackBg = if (onDarkBackground) Color.White.copy(0.15f) else M3FieldBg
    val selectedBg = if (onDarkBackground) Color.White.copy(0.25f) else Color.White
    val selectedTextColor = if (onDarkBackground) Color.White else accentColor
    val unselectedTextColor = if (onDarkBackground) Color.White.copy(0.6f) else M3OnSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth().background(outerBg).padding(horizontal = 16.dp, vertical = AppBarZoneChipPaddingV),
    ) {
        Box(
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(50)).background(trackBg).padding(4.dp),
        ) {
            Row {
                tabs.forEachIndexed { index, tab ->
                    val selected = activeTab == index
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(50))
                            .background(if (selected) selectedBg else Color.Transparent)
                            .clickable { onTabSelected(index) }.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (tab.icon != null) {
                                Icon(tab.icon, null, modifier = Modifier.size(16.dp), tint = if (selected) selectedTextColor else unselectedTextColor)
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                tab.label,
                                fontSize = DeutschCraftTheme.fontSize.lg,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (selected) selectedTextColor else unselectedTextColor,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DCAnimatedActionRow(startPadding: Dp = 70.dp, content: @Composable RowScope.() -> Unit) {
    val buttonsVisible = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = buttonsVisible,
        enter = fadeIn(tween(250, delayMillis = 100)) + expandVertically(tween(250, delayMillis = 100)),
    ) {
        Row(
            modifier = Modifier.padding(start = startPadding, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}
