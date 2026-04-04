package ui.animations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Pre-built animated visibility wrappers for common UI transitions.
 */

@Composable
fun FadeInSlideUp(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = fadeTween()) +
                slideInVertically(animationSpec = slideTween()) { it / 4 },
        exit = fadeOut(animationSpec = fadeTween(AnimationSpecs.QUICK_DURATION)) +
               slideOutVertically(animationSpec = fadeTween(AnimationSpecs.QUICK_DURATION)) { it / 4 },
        modifier = modifier,
        content = content
    )
}

@Composable
fun ExpandFadeIn(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            expandFrom = Alignment.Top
        ) + fadeIn(animationSpec = fadeTween()),
        exit = shrinkVertically(
            animationSpec = fadeTween(AnimationSpecs.QUICK_DURATION),
            shrinkTowards = Alignment.Top
        ) + fadeOut(animationSpec = fadeTween(AnimationSpecs.QUICK_DURATION)),
        modifier = modifier,
        content = content
    )
}

@Composable
fun ScaleFadeIn(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = fadeTween()) +
                androidx.compose.animation.scaleIn(
                    animationSpec = AnimationSpecs.DefaultSpring,
                    initialScale = 0.9f
                ),
        exit = fadeOut(animationSpec = fadeTween(AnimationSpecs.QUICK_DURATION)) +
                androidx.compose.animation.scaleOut(
                    animationSpec = fadeTween(AnimationSpecs.QUICK_DURATION),
                    targetScale = 0.95f
                ),
        modifier = modifier,
        content = content
    )
}
