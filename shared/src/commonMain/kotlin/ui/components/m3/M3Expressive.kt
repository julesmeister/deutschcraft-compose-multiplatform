package ui.components.m3

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.DeutschCraftTheme

private const val CompactWidthBreakpoint = 380
private val ShimmerCurrencyColor = Color(0xFF334155)

/**
 * Section header — title is rendered as a hanging badge on the DCCard,
 * so this composable only shows the action buttons row.
 */
@Composable
fun M3SectionHeader(
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    actions: @Composable (() -> Unit)? = null,
) {
    if (actions != null) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = SectionHeaderTopSpacing, bottom = SectionHeaderTopSpacing),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions()
        }
    } else {
        Spacer(modifier = Modifier.height(SectionHeaderTopSpacing))
    }
}

@Composable
fun M3TonalActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = Dp.Unspecified,
    badge: @Composable (() -> Unit)? = null,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val isCompact = screenWidth < CompactWidthBreakpoint
    val buttonSize = if (size != Dp.Unspecified) size else if (isCompact) 32.dp else 40.dp
    val iconSize = if (isCompact && size == Dp.Unspecified) 18.dp else 22.dp
    val cornerRadius = if (isCompact && size == Dp.Unspecified) 10.dp else 12.dp

    Box(modifier = modifier) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(buttonSize),
            shape = RoundedCornerShape(cornerRadius),
            color = tint.copy(0.12f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(iconSize), tint = tint)
            }
        }
        if (badge != null) {
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                badge()
            }
        }
    }
}

@Composable
fun M3TonalChip(
    text: String,
    bg: Color,
    modifier: Modifier = Modifier,
    textColor: Color = bg,
    icon: ImageVector? = null,
    iconTint: Color = textColor,
    trailingChevron: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(50.dp)
    Row(
        modifier = modifier.clip(shape).background(bg.copy(0.12f))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(text = text, color = textColor, fontSize = DeutschCraftTheme.fontSize.base, fontWeight = FontWeight.SemiBold)
        if (trailingChevron) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun M3ProfileBanner(
    initial: String,
    title: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    subtitle: @Composable () -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxWidth().background(Brush.horizontalGradient(gradientColors), RoundedCornerShape(24.dp)).padding(24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(initial.take(1).uppercase(), fontSize = DeutschCraftTheme.fontSize.display, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = DeutschCraftTheme.fontSize.headingLg, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                subtitle()
            }
        }
    }
}

@Composable
fun M3AmountDisplay(amount: String, color: Color, modifier: Modifier = Modifier, currencySize: Int = 20, amountSize: Int = 36) {
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        Text(
            text = "\u20B1",
            fontSize = currencySize.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(top = (amountSize / 4).dp, end = 2.dp),
        )
        Text(
            text = amount,
            fontSize = amountSize.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            lineHeight = (amountSize + 4).sp,
        )
    }
}

@Composable
fun CurrencyShimmer(modifier: Modifier = Modifier, infoWidthFraction: Float = 0.7f) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Text("\u20B1", fontSize = DeutschCraftTheme.fontSize.heading, fontWeight = FontWeight.SemiBold, color = ShimmerCurrencyColor)
            Spacer(modifier = Modifier.width(2.dp))
            ShimmerEffect(height = 36.dp, widthFraction = 0.5f)
        }
        Spacer(modifier = Modifier.height(12.dp))
        ShimmerEffect(height = 14.dp, widthFraction = infoWidthFraction)
    }
}

@Composable
fun DashboardSectionBody(
    isLoading: Boolean,
    hasData: Boolean,
    subtitle: String? = null,
    loadingContent: @Composable () -> Unit = { CurrencyShimmer() },
    emptyContent: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    if (subtitle != null) {
        Text(
            text = subtitle,
            fontSize = DeutschCraftTheme.fontSize.base,
            color = M3OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
    }
    when {
        isLoading -> loadingContent()
        hasData -> Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            content = content,
        )
        else -> emptyContent()
    }
}

@Composable
fun DashboardEmptyBox(text: String, accentColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(accentColor.copy(0.08f))
            .padding(vertical = 16.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = M3OnSurfaceVariant, fontSize = DeutschCraftTheme.fontSize.base)
    }
}
