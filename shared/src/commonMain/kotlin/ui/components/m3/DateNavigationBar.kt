package ui.components.m3

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import theme.DeutschCraftTheme

@Composable
fun DateNavigationBar(
    title: String,
    subtitle: String?,
    accentColor: Color,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.DateRange,
    onCenterClick: (() -> Unit)? = null,
) {
    val centerInteractionSource = remember { MutableInteractionSource() }
    val centerClickModifier = if (onCenterClick != null) {
        Modifier.clickable(indication = null, interactionSource = centerInteractionSource) { onCenterClick() }
    } else Modifier

    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = AppBarZoneRowPaddingV)) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.15f)).padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(0.15f))
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onPrevious() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f).then(centerClickModifier),
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(title, fontSize = DeutschCraftTheme.fontSize.xl, fontWeight = FontWeight.SemiBold, color = Color.White)
                    if (subtitle != null) {
                        Text(subtitle, fontSize = DeutschCraftTheme.fontSize.sm, color = Color.White.copy(0.8f))
                    }
                }
            }
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(0.15f))
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onNext() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    }
}
