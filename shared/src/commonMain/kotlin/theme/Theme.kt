package theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Indigo.copy(alpha = 0.12f),
    onPrimaryContainer = Indigo,
    secondary = Success,
    onSecondary = Color.White,
    tertiary = Warning,
    error = Danger,
    onError = Color.White,
    surface = Color.White,
    onSurface = Gray800,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    outline = Gray300,
    outlineVariant = Gray200,
    surfaceTint = Color.Transparent,
    background = Gray50,
    onBackground = Gray800
)

@Composable
fun DeutschCraftTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalFontSize provides FontSize(),
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

object DeutschCraftTheme {
    val spacing: Spacing
        @Composable get() = LocalSpacing.current
    val fontSize: FontSize
        @Composable get() = LocalFontSize.current
}
