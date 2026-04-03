package theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Immutable
data class FontSize(
    val xxxs: TextUnit = 9.sp,
    val xxs: TextUnit = 10.sp,
    val xs: TextUnit = 11.sp,
    val sm: TextUnit = 12.sp,
    val md: TextUnit = 13.sp,
    val base: TextUnit = 14.sp,
    val lg: TextUnit = 15.sp,
    val xl: TextUnit = 16.sp,
    val xlPlus: TextUnit = 17.sp,
    val xxl: TextUnit = 18.sp,
    val heading: TextUnit = 20.sp,
    val headingLg: TextUnit = 22.sp,
    val displaySm: TextUnit = 24.sp,
    val displaySmPlus: TextUnit = 26.sp,
    val display: TextUnit = 28.sp,
    val displayMd: TextUnit = 30.sp,
    val displayLg: TextUnit = 32.sp,
    val displayLgPlus: TextUnit = 34.sp,
    val displayXl: TextUnit = 36.sp,
    val displayHuge: TextUnit = 40.sp,
    val displayMega: TextUnit = 50.sp,
    val displayUltra: TextUnit = 56.sp,
)

val LocalFontSize = staticCompositionLocalOf { FontSize() }
