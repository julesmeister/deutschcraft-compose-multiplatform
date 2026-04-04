package ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import theme.Gray200
import theme.Gray500
import theme.Gray800
import theme.Indigo

@Composable
fun SegmentedSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Indigo
) {
    val spacing = 2.dp
    val trackBg = Gray200
    val indicatorBg = Color.White

    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(trackBg)
            .padding(spacing),
    ) {
        // Labels row (invisible, just to set the height)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            options.forEach {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(it, style = MaterialTheme.typography.labelMedium, color = Color.Transparent)
                }
            }
        }

        // Animated sliding indicator behind text
        BoxWithConstraints(modifier = Modifier.matchParentSize()) {
            val containerPx = with(density) { maxWidth.toPx() }
            val totalSpacingPx = with(density) { (spacing * (options.size - 1)).toPx() }
            val itemPx = (containerPx - totalSpacingPx) / options.size
            val spacingPx = with(density) { spacing.toPx() }

            val animatedOffset by animateFloatAsState(
                targetValue = selectedIndex * (itemPx + spacingPx),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "selector_offset",
            )

            Box(
                modifier = Modifier
                    .offset(x = with(density) { animatedOffset.toDp() })
                    .width(with(density) { itemPx.toDp() })
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(indicatorBg),
            )
        }

        // Visible labels on top
        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onSelect(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) accentColor else Gray500,
                    )
                }
            }
        }
    }
}
