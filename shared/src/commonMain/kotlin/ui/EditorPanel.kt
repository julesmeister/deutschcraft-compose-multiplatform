package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.*

@Composable
fun EditorPanel(
    text: String,
    onTextChange: (String) -> Unit,
    onSelectionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text)) }
    var wordCount by remember { mutableIntStateOf(0) }
    var charCount by remember { mutableIntStateOf(0) }
    
    // Update local state when external text changes
    LaunchedEffect(text) {
        if (text != textFieldValue.text) {
            textFieldValue = TextFieldValue(text)
        }
    }
    
    LaunchedEffect(textFieldValue.text) {
        wordCount = textFieldValue.text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.size
        charCount = textFieldValue.text.length
    }
    
    // Report selection changes
    LaunchedEffect(textFieldValue.selection) {
        val selection = textFieldValue.selection
        if (selection.start != selection.end) {
            val selectedText = textFieldValue.text.substring(selection.start, selection.end)
            onSelectionChange(selectedText)
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Toolbar
        Surface(
            tonalElevation = 0.dp,
            color = Gray50,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Editor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Close, contentDescription = "Undo")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Redo")
                }
                Divider(
                    modifier = Modifier
                        .height(24.dp)
                        .padding(horizontal = 8.dp),
                    color = Gray200
                )
                // Format buttons removed - icons not available in commonMain
            }
        }
        
        Divider(color = Gray200)
        
        // Text Editor
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { 
                    textFieldValue = it
                    onTextChange(it.text)
                },
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 25.6.sp,
                    fontFamily = FontFamily.Default,
                    color = Gray700
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                text = "Start writing here...\n\nSelect any text to get AI suggestions",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    lineHeight = 25.6.sp,
                                    color = Gray400
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        
        Divider(color = Gray200)
        
        // Status Bar
        Surface(
            tonalElevation = 0.dp,
            color = Gray100,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$wordCount words  |  $charCount characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Ctrl+A: Select All  •  Select text for AI help",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray400
                )
            }
        }
    }
}
