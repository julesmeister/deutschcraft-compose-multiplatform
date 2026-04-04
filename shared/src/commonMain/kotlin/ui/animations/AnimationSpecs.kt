package ui.animations

import androidx.compose.animation.core.*
import androidx.compose.ui.unit.dp

/**
 * Shared animation constants and utilities.
 * Using Material Design motion guidelines with spring physics.
 */
object AnimationSpecs {
    // Spring specs for responsive, natural motion
    val DefaultSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val GentleSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    val BouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioHighBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val QuickSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )

    // Durations for tween-based animations
    const val QUICK_DURATION = 150
    const val DEFAULT_DURATION = 300
    const val SLOW_DURATION = 500

    // Stagger delays for lists
    const val STAGGER_DELAY = 50

    // Icon sizes for animations
    val ICON_SMALL = 16.dp
    val ICON_MEDIUM = 24.dp
    val ICON_LARGE = 32.dp
}

/**
 * Returns a tween spec with easing for fade animations.
 */
fun <T> fadeTween(durationMillis: Int = AnimationSpecs.DEFAULT_DURATION) = tween<T>(
    durationMillis = durationMillis,
    easing = FastOutSlowInEasing
)

/**
 * Returns a tween spec with easing for slide animations.
 */
fun <T> slideTween(durationMillis: Int = AnimationSpecs.DEFAULT_DURATION) = tween<T>(
    durationMillis = durationMillis,
    easing = EaseOutQuart
)
