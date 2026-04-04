package ui.suggestions

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.scale
import theme.*
import ui.suggestions.animations.RotatingIcon
import ui.suggestions.animations.WiggleEffect

@Composable
internal fun ActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    isLoading: Boolean,
    isEnabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = when {
            !isEnabled -> 1f
            isPressed -> 0.95f
            else -> 1f
        },
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "button_scale"
    )
    
    val bgColor by animateColorAsState(
        targetValue = when {
            !isEnabled -> Gray200
            isActive -> Indigo.copy(alpha = 0.15f)
            else -> Gray100
        },
        animationSpec = tween(200),
        label = "button_bg"
    )
    
    val contentColor by animateColorAsState(
        targetValue = when {
            !isEnabled -> Gray400
            isActive -> Indigo
            else -> Gray700
        },
        animationSpec = tween(200),
        label = "button_content"
    )
    
    Surface(
        color = bgColor,
        shape = MaterialTheme.shapes.small,
        onClick = { if (isEnabled && !isLoading) onClick() },
        enabled = isEnabled && !isLoading,
        modifier = modifier
            .height(48.dp)
            .scale(scale),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                ui.animations.RotatingIcon(modifier = Modifier.size(16.dp)) { mod ->
                    CircularProgressIndicator(
                        modifier = mod,
                        strokeWidth = 2.dp,
                        color = Indigo
                    )
                }
            } else if (isActive) {
                ui.animations.WiggleEffect(targetValue = 3f) { mod ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = mod.then(Modifier.size(18.dp))
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}
