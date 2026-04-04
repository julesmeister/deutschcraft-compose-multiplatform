package ui.animations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Container for staggered list item animations.
 * Children animate in sequentially with a stagger delay.
 */
@Composable
fun StaggeredAnimatedContainer(
    itemCount: Int,
    modifier: Modifier = Modifier,
    content: @Composable (index: Int, itemModifier: Modifier) -> Unit
) {
    val animatedItems = remember(itemCount) { List(itemCount) { mutableStateOf(false) } }

    LaunchedEffect(itemCount) {
        animatedItems.forEachIndexed { index, state ->
            delay(index * AnimationSpecs.STAGGER_DELAY.toLong())
            state.value = true
        }
    }

    Column(modifier = modifier) {
        animatedItems.forEachIndexed { index, visibilityState ->
            AnimatedVisibility(
                visible = visibilityState.value,
                enter = fadeIn(animationSpec = fadeTween()) +
                        slideInVertically(animationSpec = slideTween()) { it / 3 }
            ) {
                content(index, Modifier)
            }
        }
    }
}

/**
 * Pulsing dot animation for loading states.
 */
@Composable
fun PulsingDots(
    modifier: Modifier = Modifier,
    dotCount: Int = 3,
    dotSize: androidx.compose.ui.unit.Dp = 8.dp,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    val animations = List(dotCount) { index ->
        rememberInfiniteTransition(label = "pulse_$index")
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        animations.forEachIndexed { index, transition ->
            val scale by transition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 150, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale_$index"
            )

            val alpha by transition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 150, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha_$index"
            )

            androidx.compose.foundation.Canvas(
                modifier = Modifier.size(dotSize).scale(scale).alpha(alpha)
            ) {
                drawCircle(color = color)
            }
        }
    }
}

/**
 * Animated counter that counts up to a value.
 */
@Composable
fun AnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    content: @Composable (String) -> Unit
) {
    var currentValue by remember { mutableStateOf(0) }

    LaunchedEffect(targetValue) {
        animate(
            initialValue = currentValue.toFloat(),
            targetValue = targetValue.toFloat(),
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        ) { value, _ ->
            currentValue = value.toInt()
        }
    }

    content(currentValue.toString())
}
