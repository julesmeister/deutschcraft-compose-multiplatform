package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.*

@Composable
internal fun ChatInputArea(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    isGenerating: Boolean,
    fontSize: data.settings.FontSize = data.settings.FontSize.MEDIUM,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Gray50,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Gray100)
                    .heightIn(min = 48.dp, max = 120.dp),
                textStyle = TextStyle(
                    fontSize = when (fontSize) {
                        data.settings.FontSize.SMALL -> 14.sp
                        data.settings.FontSize.MEDIUM -> 16.sp
                        data.settings.FontSize.LARGE -> 20.sp
                    },
                    color = Gray800
                ),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { if (value.isNotBlank() && !isGenerating) onSend() }
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = "Ask AI about your writing...",
                                fontSize = when (fontSize) {
                                    data.settings.FontSize.SMALL -> 14.sp
                                    data.settings.FontSize.MEDIUM -> 16.sp
                                    data.settings.FontSize.LARGE -> 20.sp
                                },
                                color = Gray400
                            )
                        }
                        innerTextField()
                    }
                }
            )

            IconButton(
                onClick = { if (isGenerating) onStop() else onSend() },
                enabled = if (isGenerating) true else value.isNotBlank(),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isGenerating) Icons.Default.Close else Icons.Default.Send,
                    contentDescription = if (isGenerating) "Stop" else "Send",
                    tint = when {
                        isGenerating -> Color(0xFFD32F2F)
                        value.isNotBlank() -> Indigo
                        else -> Gray400
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
