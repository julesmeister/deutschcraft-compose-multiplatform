package ui.components.m3

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.DeutschCraftTheme

@Composable
fun DCCard(
    modifier: Modifier = Modifier,
    horizontalMargin: Dp = 12.dp,
    label: String? = null,
    labelColor: Color = Color.Transparent,
    animationDelay: Int = 0,
    content: @Composable () -> Unit,
) {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        alpha.animateTo(1f, tween(400))
        offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalMargin)
            .graphicsLayer {
                this.alpha = alpha.value
                translationY = offsetY.value
            },
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
    ) {
        Box {
            content()
            if (label != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 20.dp)
                        .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                        .background(labelColor.copy(0.12f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label.uppercase(),
                        fontSize = DeutschCraftTheme.fontSize.xs,
                        fontWeight = FontWeight.Bold,
                        color = labelColor,
                        letterSpacing = 0.8.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun DCCardRow(
    columns: Int,
    baseDelay: Int = 80,
    content: @Composable RowScope.(modifier: Modifier, delay: (Int) -> Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val weightMod = Modifier.weight(1f)
        val delayFn = { index: Int -> baseDelay + index * 40 }
        content(weightMod, delayFn)
    }
}
