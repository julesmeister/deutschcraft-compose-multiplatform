package ui.suggestions.animations

// Re-export all animation components from the shared ui.animations package
// This maintains backwards compatibility while allowing both suggestions and chat to use the same animations

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// AnimationSpecs re-export
@Deprecated("Use ui.animations.AnimationSpecs directly", ReplaceWith("ui.animations.AnimationSpecs"))
typealias AnimationSpecs = ui.animations.AnimationSpecs

@Deprecated("Use ui.animations.fadeTween directly", ReplaceWith("ui.animations.fadeTween"))
fun <T> fadeTween(durationMillis: Int = ui.animations.AnimationSpecs.DEFAULT_DURATION) = ui.animations.fadeTween<T>(durationMillis)

@Deprecated("Use ui.animations.slideTween directly", ReplaceWith("ui.animations.slideTween"))
fun <T> slideTween(durationMillis: Int = ui.animations.AnimationSpecs.DEFAULT_DURATION) = ui.animations.slideTween<T>(durationMillis)

// AnimatedVisibility re-exports
@Deprecated("Use ui.animations.FadeInSlideUp directly", ReplaceWith("ui.animations.FadeInSlideUp"))
@Composable
fun FadeInSlideUp(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) = ui.animations.FadeInSlideUp(visible, modifier, content)

@Deprecated("Use ui.animations.ExpandFadeIn directly", ReplaceWith("ui.animations.ExpandFadeIn"))
@Composable
fun ExpandFadeIn(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) = ui.animations.ExpandFadeIn(visible, modifier, content)

@Deprecated("Use ui.animations.ScaleFadeIn directly", ReplaceWith("ui.animations.ScaleFadeIn"))
@Composable
fun ScaleFadeIn(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) = ui.animations.ScaleFadeIn(visible, modifier, content)

// AnimatedContainer re-exports
@Deprecated("Use ui.animations.StaggeredAnimatedContainer directly", ReplaceWith("ui.animations.StaggeredAnimatedContainer"))
@Composable
fun StaggeredAnimatedContainer(
    itemCount: Int,
    modifier: Modifier = Modifier,
    content: @Composable (index: Int, itemModifier: Modifier) -> Unit
) = ui.animations.StaggeredAnimatedContainer(itemCount, modifier, content)

@Deprecated("Use ui.animations.PulsingDots directly", ReplaceWith("ui.animations.PulsingDots"))
@Composable
fun PulsingDots(
    modifier: Modifier = Modifier,
    dotCount: Int = 3,
    dotSize: Dp = 8.dp,
    color: Color = androidx.compose.material3.MaterialTheme.colorScheme.primary
) = ui.animations.PulsingDots(modifier, dotCount, dotSize, color)

@Deprecated("Use ui.animations.AnimatedCounter directly", ReplaceWith("ui.animations.AnimatedCounter"))
@Composable
fun AnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    content: @Composable (String) -> Unit
) = ui.animations.AnimatedCounter(targetValue, modifier, content)

// Effects re-exports
@Deprecated("Use ui.animations.TypewriterText directly", ReplaceWith("ui.animations.TypewriterText"))
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
    typingDelayMs: Long = 15,
    onComplete: () -> Unit = {}
) = ui.animations.TypewriterText(text, modifier, style, typingDelayMs, onComplete)

@Deprecated("Use ui.animations.PulsingIcon directly", ReplaceWith("ui.animations.PulsingIcon"))
@Composable
fun PulsingIcon(
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) = ui.animations.PulsingIcon(modifier, content)

@Deprecated("Use ui.animations.BounceIn directly", ReplaceWith("ui.animations.BounceIn"))
@Composable
fun BounceIn(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) = ui.animations.BounceIn(visible, modifier, content)

@Deprecated("Use ui.animations.ShimmerLoading directly", ReplaceWith("ui.animations.ShimmerLoading"))
@Composable
fun ShimmerLoading(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    height: Dp = Dp.Unspecified
) = ui.animations.ShimmerLoading(modifier, widthFraction, height)

@Deprecated("Use ui.animations.RotatingIcon directly", ReplaceWith("ui.animations.RotatingIcon"))
@Composable
fun RotatingIcon(
    modifier: Modifier = Modifier,
    rotationDuration: Int = 2000,
    content: @Composable (Modifier) -> Unit
) = ui.animations.RotatingIcon(modifier, rotationDuration, content)

@Deprecated("Use ui.animations.WiggleEffect directly", ReplaceWith("ui.animations.WiggleEffect"))
@Composable
fun WiggleEffect(
    targetValue: Float = 5f,
    content: @Composable (Modifier) -> Unit
) = ui.animations.WiggleEffect(targetValue, content)

@Deprecated("Use ui.animations.AnimatedNumber directly", ReplaceWith("ui.animations.AnimatedNumber"))
@Composable
fun AnimatedNumber(
    target: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = androidx.compose.material3.MaterialTheme.typography.bodyMedium
) = ui.animations.AnimatedNumber(target, modifier, style)

@Deprecated("Use ui.animations.StaggeredFadeIn directly", ReplaceWith("ui.animations.StaggeredFadeIn"))
@Composable
fun StaggeredFadeIn(
    itemCount: Int,
    staggerDelayMs: Long = 50,
    modifier: Modifier = Modifier,
    content: @Composable (Int, Modifier) -> Unit
) = ui.animations.StaggeredFadeIn(itemCount, staggerDelayMs, modifier, content)

@Deprecated("Use ui.animations.RippleExpand directly", ReplaceWith("ui.animations.RippleExpand"))
@Composable
fun RippleExpand(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) = ui.animations.RippleExpand(expanded, modifier, content)
