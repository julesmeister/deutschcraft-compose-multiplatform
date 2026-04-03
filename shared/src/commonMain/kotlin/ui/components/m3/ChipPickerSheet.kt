package ui.components.m3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import theme.DeutschCraftTheme

/**
 * Generic chip picker bottom sheet for selecting from a list of options.
 * Uses DCSelectionChip for consistent styling.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DCChipPickerSheet(
    title: String,
    subtitle: String,
    options: List<String>,
    selected: String,
    accentColor: Color = M3Primary,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    chipAvatar: @Composable (option: String, isSelected: Boolean) -> Unit = { option, isSelected ->
        if (isSelected) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = option.take(1).uppercase(),
                fontSize = DeutschCraftTheme.fontSize.base,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    },
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 4.dp, bottom = 32.dp),
        ) {
            Text(
                title,
                fontSize = DeutschCraftTheme.fontSize.heading,
                fontWeight = FontWeight.Bold,
                color = M3OnSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                subtitle,
                fontSize = DeutschCraftTheme.fontSize.base,
                color = M3OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                options.forEach { option ->
                    val isSelected = option == selected
                    DCSelectionChip(
                        label = option,
                        selected = isSelected,
                        onClick = { onSelected(option) },
                        accentColor = accentColor,
                        avatar = { chipAvatar(option, isSelected) },
                    )
                }
            }
        }
    }
}
