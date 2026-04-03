package ui.components.m3

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import theme.DeutschCraftTheme

internal val FormFieldBg = Color(0xFFE8E9F2)

@Composable
fun DCTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    autoCapitalize: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor by animateColorAsState(targetValue = Color.Transparent, animationSpec = tween(200), label = "border")

    val mergedKeyboardOptions = if (autoCapitalize && keyboardOptions.capitalization == KeyboardCapitalization.None) {
        keyboardOptions.copy(capitalization = KeyboardCapitalization.Words)
    } else keyboardOptions

    val wrappedOnValueChange: (String) -> Unit = if (autoCapitalize) {
        { newValue ->
            onValueChange(newValue.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { c -> if (c.isLowerCase()) c.uppercase().first() else c }
            })
        }
    } else onValueChange

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, fontSize = DeutschCraftTheme.fontSize.md, fontWeight = FontWeight.Medium, color = M3OnSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = wrappedOnValueChange,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(FormFieldBg)
                .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                .then(if (minLines > 1) Modifier.heightIn(min = (minLines * 24 + 28).dp) else Modifier),
            textStyle = TextStyle(fontSize = DeutschCraftTheme.fontSize.xl, color = M3OnSurface),
            singleLine = singleLine,
            keyboardOptions = mergedKeyboardOptions,
            keyboardActions = keyboardActions,
            readOnly = readOnly,
            enabled = enabled,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(M3Primary),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(text = placeholder, fontSize = DeutschCraftTheme.fontSize.xl, color = M3OnSurfaceVariant.copy(0.5f))
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
fun DCCurrencyField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "0.00",
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val borderColor by animateColorAsState(targetValue = Color.Transparent, animationSpec = tween(200), label = "border")

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, fontSize = DeutschCraftTheme.fontSize.md, fontWeight = FontWeight.Medium, color = M3OnSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(FormFieldBg)
                .border(1.5.dp, borderColor, RoundedCornerShape(14.dp)),
            textStyle = TextStyle(fontSize = DeutschCraftTheme.fontSize.xl, color = M3OnSurface),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            enabled = enabled,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(M3Primary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(M3Primary.copy(0.10f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("\u20B1", fontSize = DeutschCraftTheme.fontSize.xl, fontWeight = FontWeight.Bold, color = M3Primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                        if (value.isEmpty()) {
                            Text(text = placeholder, fontSize = DeutschCraftTheme.fontSize.xl, color = M3OnSurfaceVariant.copy(0.5f))
                        }
                        innerTextField()
                    }
                    if (value.isNotEmpty() && enabled) {
                        Box(
                            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(50)).background(M3OnSurfaceVariant.copy(0.10f))
                                .clickable { onValueChange("") },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Clear, "Clear", tint = M3OnSurfaceVariant.copy(0.7f), modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            },
        )
    }
}

@Composable
fun DCSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String = "",
    enabled: Boolean = true,
    accentColor: Color = M3Primary,
) {
    Row(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(FormFieldBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = label,
                fontSize = DeutschCraftTheme.fontSize.lg,
                fontWeight = FontWeight.Medium,
                color = if (enabled) M3OnSurface else M3OnSurfaceVariant.copy(0.6f),
            )
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    fontSize = DeutschCraftTheme.fontSize.sm,
                    color = M3OnSurfaceVariant.copy(alpha = if (enabled) 1f else 0.5f),
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedTrackColor = accentColor),
        )
    }
}

@Composable
fun DCDropdownField(
    value: String,
    label: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select...",
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, fontSize = DeutschCraftTheme.fontSize.md, fontWeight = FontWeight.Medium, color = M3OnSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Box {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(FormFieldBg)
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value.ifEmpty { placeholder },
                    fontSize = DeutschCraftTheme.fontSize.xl,
                    color = if (value.isEmpty()) M3OnSurfaceVariant.copy(0.5f) else M3OnSurface,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Default.ArrowDropDown, null, tint = M3OnSurfaceVariant)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(14.dp),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontSize = DeutschCraftTheme.fontSize.lg, color = M3OnSurface) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun DCFieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        fontSize = DeutschCraftTheme.fontSize.md,
        fontWeight = FontWeight.SemiBold,
        color = M3OnSurfaceVariant,
        letterSpacing = 0.5.sp,
    )
}

@Composable
fun DCDateField(label: String, value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        DCFieldLabel(label)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(FormFieldBg)
                .clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(value, fontSize = DeutschCraftTheme.fontSize.lg, color = M3OnSurface)
                Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp), tint = M3OnSurfaceVariant)
            }
        }
    }
}
