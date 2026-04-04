package ui.components.m3

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import theme.Gray100
import theme.Gray200
import theme.Gray400
import theme.Gray50
import theme.Gray500
import theme.Gray800
import theme.Indigo

@Composable
fun CategoryPickerDialog(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    DCPickerDialog(
        title = "Filter by Category",
        onDismiss = onDismiss,
        icon = Icons.Default.FilterList,
        accentColor = Indigo,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // "All" option
            CategoryOption(
                label = "All Conversations",
                isSelected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                showIcon = false,
            )

            if (categories.isNotEmpty()) {
                Spacer(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )

                categories.forEach { category ->
                    CategoryOption(
                        label = category,
                        isSelected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        showIcon = true,
                    )
                }
            }

            if (categories.isEmpty()) {
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gray50)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No categories yet. AI will categorize conversations automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    showIcon: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Indigo.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showIcon) {
            Box(
                modifier = Modifier.size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Gray100),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Label,
                    contentDescription = null,
                    tint = if (isSelected) Indigo else Gray400,
                    modifier = Modifier.size(14.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Indigo else Gray800,
            modifier = Modifier.weight(1f),
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Indigo,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
