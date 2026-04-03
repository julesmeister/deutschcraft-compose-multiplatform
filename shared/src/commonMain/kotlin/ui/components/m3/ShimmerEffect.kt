package ui.components.m3

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import theme.Gray100
import theme.Gray200

@Composable
fun ShimmerEffect(modifier: Modifier = Modifier, height: Dp = 20.dp, widthFraction: Float = 1f) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -300f, targetValue = 300f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerTranslate",
    )
    val brush = Brush.linearGradient(
        colors = listOf(Gray200, Gray100, Gray200),
        start = Offset(translateX, 0f), end = Offset(translateX + 300f, 0f),
    )
    Box(modifier = modifier.fillMaxWidth(widthFraction).height(height).clip(RoundedCornerShape(4.dp)).background(brush))
}
