package ui.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Typewriter text effect that reveals text character by character.
 */
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    typingDelayMs: Long = 15,
    onComplete: () -> Unit = {}
) {
    var visibleChars by remember { mutableStateOf(0) }

    LaunchedEffect(text) {
        visibleChars = 0
        text.forEachIndexed { index, _ ->
            delay(typingDelayMs)
            visibleChars = index + 1
        }
        onComplete()
    }

    Text(
        text = text.take(visibleChars),
        style = style,
        modifier = modifier
    )
}

/**
 * Breathing/pulsing scale animation for icons.
 */
@Composable
fun PulsingIcon(
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    content(modifier.graphicsLayer { scaleX = scale; scaleY = scale })
}

/**
 * Bouncing entry animation for elements.
 */
@Composable
fun BounceIn(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounce"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(200),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha)
    ) {
        content()
    }
}

/**
 * Shimmer loading effect with gradient sweep.
 */
@Composable
fun ShimmerLoading(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    height: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified
) {
    val shimmerAnimation = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation = shimmerAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "translate"
    )

    val alpha by shimmerAnimation.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .background(
                color = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = alpha),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            )
    ) {
        // Empty box that shows shimmer background
    }
}

/**
 * Rotating animation for loading spinners or icons.
 */
@Composable
fun RotatingIcon(
    modifier: Modifier = Modifier,
    rotationDuration: Int = 2000,
    content: @Composable (Modifier) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(rotationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    content(modifier.graphicsLayer { rotationZ = rotation })
}

/**
 * Wiggle animation for drawing attention to elements.
 */
@Composable
fun WiggleEffect(
    targetValue: Float = 5f,
    content: @Composable (Modifier) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wiggle")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -targetValue,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wiggle_rotation"
    )

    content(Modifier.graphicsLayer { rotationZ = rotation })
}

/**
 * Count up animation for numbers.
 */
@Composable
fun AnimatedNumber(
    target: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    var current by remember { mutableStateOf(0) }

    LaunchedEffect(target) {
        animate(
            initialValue = current.toFloat(),
            targetValue = target.toFloat(),
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        ) { value, _ ->
            current = value.toInt()
        }
    }

    Text(
        text = current.toString(),
        style = style,
        modifier = modifier
    )
}

/**
 * Staggered fade-in animation for multiple items.
 */
@Composable
fun StaggeredFadeIn(
    itemCount: Int,
    staggerDelayMs: Long = 50,
    modifier: Modifier = Modifier,
    content: @Composable (Int, Modifier) -> Unit
) {
    val visibilityStates = remember(itemCount) {
        List(itemCount) { mutableStateOf(false) }
    }

    LaunchedEffect(itemCount) {
        visibilityStates.forEachIndexed { index, state ->
            delay(index * staggerDelayMs)
            state.value = true
        }
    }

    Column(modifier = modifier) {
        visibilityStates.forEachIndexed { index, visibleState ->
            val alpha by animateFloatAsState(
                targetValue = if (visibleState.value) 1f else 0f,
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "fade_$index"
            )

            val offsetY by animateFloatAsState(
                targetValue = if (visibleState.value) 0f else 20f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "offset_$index"
            )

            Box(
                modifier = Modifier
                    .alpha(alpha)
                    .graphicsLayer { translationY = offsetY }
            ) {
                content(index, Modifier)
            }
        }
    }
}

/**
 * Ripple/expand effect for buttons or cards.
 */
@Composable
fun RippleExpand(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "ripple_scale"
    )

    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        content()
    }
}
