package ui.components.m3

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import theme.DeutschCraftTheme

val M3SelectorBg = Color(0xFFE8EDF2)

@Composable
fun M3SegmentedSelector(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    onDarkBackground: Boolean = false,
) {
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)
    val spacing = 3.dp

    val trackBg = if (onDarkBackground) Color.White.copy(0.15f) else M3SelectorBg
    val indicatorBg = if (onDarkBackground) Color.White.copy(0.25f) else Color.White
    val selectedTextColor = if (onDarkBackground) Color.White else (accentColor ?: M3OnSurface)
    val unselectedTextColor = if (onDarkBackground) Color.White.copy(0.6f) else M3OnSurfaceVariant

    val density = LocalDensity.current

    Box(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(trackBg).padding(spacing),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
            options.forEach {
                Box(modifier = Modifier.weight(1f).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text(it, fontSize = DeutschCraftTheme.fontSize.md, color = Color.Transparent)
                }
            }
        }
        BoxWithConstraints(modifier = Modifier.matchParentSize()) {
            val containerPx = with(density) { maxWidth.toPx() }
            val totalSpacingPx = with(density) { (spacing * (options.size - 1)).toPx() }
            val itemPx = (containerPx - totalSpacingPx) / options.size
            val spacingPx = with(density) { spacing.toPx() }

            val animatedOffset by animateFloatAsState(
                targetValue = selectedIndex * (itemPx + spacingPx),
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                label = "selector_offset",
            )

            Box(
                modifier = Modifier
                    .offset(x = with(density) { animatedOffset.toDp() })
                    .width(with(density) { itemPx.toDp() })
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(9.dp))
                    .background(indicatorBg),
            )
        }
        Row(modifier = Modifier.matchParentSize(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
            options.forEach { option ->
                val isSelected = option == selected
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(9.dp)).clickable { onSelect(option) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = option,
                        fontSize = DeutschCraftTheme.fontSize.md,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) selectedTextColor else unselectedTextColor,
                    )
                }
            }
        }
    }
}
