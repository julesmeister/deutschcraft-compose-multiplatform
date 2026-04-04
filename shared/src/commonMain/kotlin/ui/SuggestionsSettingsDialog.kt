package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import service.OllamaService
import theme.*
import ui.components.m3.*

/**
 * Settings dialog for AI suggestions panel using M3 components.
 * Uses DCSelectorField + DCChipPickerSheet pattern for model selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsSettingsDialog(
    availableModels: List<String>,
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var showModelPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    M3IconBox(
                        icon = Icons.Default.Settings,
                        tint = M3OnSurface,
                        bg = Gray200,
                        modifier = Modifier.size(44.dp)
                    )
                    Column {
                        Text(
                            "AI Settings",
                            fontSize = DeutschCraftTheme.fontSize.heading,
                            fontWeight = FontWeight.Bold,
                            color = M3OnSurface
                        )
                        Text(
                            "Configure your AI assistant",
                            fontSize = DeutschCraftTheme.fontSize.sm,
                            color = M3OnSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = Gray200)

                // Model Selector using DCSelectorField
                DCSelectorField(
                    label = "AI Model",
                    value = selectedModel,
                    onClick = { showModelPicker = true },
                    icon = Icons.Default.Build,
                    selectedColor = Indigo,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo)
                ) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // Model picker sheet using M3 DCChipPickerSheet
    if (showModelPicker) {
        DCChipPickerSheet(
            title = "Select AI Model",
            subtitle = "Choose the model for generating suggestions",
            options = availableModels,
            selected = selectedModel,
            accentColor = Indigo,
            onSelected = { model ->
                onModelSelected(model)
                showModelPicker = false
            },
            onDismiss = { showModelPicker = false },
            chipAvatar = { option, isSelected ->
                if (isSelected) {
                    Text(
                        text = "✓",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = DeutschCraftTheme.fontSize.base,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "🤖",
                        fontSize = DeutschCraftTheme.fontSize.base
                    )
                }
            }
        )
    }
}
