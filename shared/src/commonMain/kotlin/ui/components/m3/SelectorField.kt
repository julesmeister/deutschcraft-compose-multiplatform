package ui.components.m3

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import theme.DeutschCraftTheme

@Composable
fun DCSelectorField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    placeholder: String = "Tap to select",
    selectedColor: Color = M3Primary,
) {
    Column(modifier = modifier) {
        Text(
            label,
            fontSize = DeutschCraftTheme.fontSize.md,
            fontWeight = FontWeight.Medium,
            color = M3OnSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF1F5F9),
            tonalElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Icon(
                        icon, null,
                        tint = if (value.isNotBlank()) selectedColor else M3OnSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = value.ifBlank { placeholder },
                    fontSize = DeutschCraftTheme.fontSize.lg,
                    fontWeight = if (value.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (value.isNotBlank()) M3OnSurface else M3OnSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Default.ChevronRight, null,
                    tint = M3OnSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
