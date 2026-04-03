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

private val DarkColorScheme = darkColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Indigo.copy(alpha = 0.2f),
    onPrimaryContainer = IndigoLight,
    secondary = Success,
    onSecondary = Color.White,
    tertiary = Warning,
    error = Danger,
    onError = Color.White,
    surface = Gray800,
    onSurface = Gray100,
    surfaceVariant = Gray700,
    onSurfaceVariant = Gray400,
    outline = Gray600,
    outlineVariant = Gray700,
    surfaceTint = Color.Transparent,
    background = Color(0xFF121212),
    onBackground = Gray100
)

@Composable
fun DeutschCraftTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

object DeutschCraftTheme {
    val spacing: Spacing
        @Composable get() = LocalSpacing.current
    val fontSize: FontSize
        @Composable get() = LocalFontSize.current
}
