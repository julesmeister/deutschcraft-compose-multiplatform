package ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.settings.FontSize
import data.settings.ThemeMode
import theme.Gray100
import theme.Gray600
import theme.Gray700
import theme.Gray800
import theme.Indigo

@Composable
fun ThemeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeMode.entries.forEach { mode ->
            val isSelected = mode == selected
            Surface(
                color = if (isSelected) Indigo else Gray100,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { onSelect(mode) }
            ) {
                Text(
                    text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Color.White else Gray700,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun FontSizeSelector(
    selected: FontSize,
    onSelect: (FontSize) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FontSize.entries.forEach { size ->
            val isSelected = size == selected
            Surface(
                color = if (isSelected) Indigo else Gray100,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { onSelect(size) }
            ) {
                Text(
                    text = when (size) {
                        FontSize.EXTRA_SMALL -> "XS"
                        FontSize.SMALL -> "S"
                        FontSize.MEDIUM -> "M"
                        FontSize.LARGE -> "L"
                        FontSize.EXTRA_LARGE -> "XL"
                        FontSize.HUGE -> "XXL"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = when (size) {
                            FontSize.EXTRA_SMALL -> 10.sp
                            FontSize.SMALL -> 12.sp
                            FontSize.MEDIUM -> 14.sp
                            FontSize.LARGE -> 16.sp
                            FontSize.EXTRA_LARGE -> 18.sp
                            FontSize.HUGE -> 20.sp
                        }
                    ),
                    color = if (isSelected) Color.White else Gray700,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun ModelSelector(
    selected: String,
    availableModels: List<String>,
    isLoading: Boolean,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            color = Gray100,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.clickable(enabled = !isLoading) { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Gray600
                    )
                    Text(
                        text = "Loading...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600
                    )
                } else {
                    Text(
                        text = selected,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray800
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Gray600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableModels.forEach { model ->
                DropdownMenuItem(
                    text = { Text(model) },
                    onClick = {
                        onSelect(model)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DailyGoalSelector(
    minutes: Int,
    onSelect: (Int) -> Unit
) {
    val options = listOf(5, 10, 15, 20, 30, 45, 60)

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { mins ->
            val isSelected = mins == minutes
            Surface(
                color = if (isSelected) Indigo else Gray100,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { onSelect(mins) }
            ) {
                Text(
                    text = "${mins}m",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Color.White else Gray700,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
