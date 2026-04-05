package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.*
import ui.editor.EditorStatusBar
import ui.editor.EditorToolbar
import ui.editor.ErrorBanner

@Composable
fun EditorPanel(
    text: String,
    onTextChange: (String) -> Unit,
    onSelectionChange: (String) -> Unit,
    onAnalyzeClick: () -> Unit = {},
    errorMessage: String? = null,
    onErrorDismiss: () -> Unit = {},
    fontSize: data.settings.FontSize = data.settings.FontSize.MEDIUM,
    currentEssayTitle: String? = null,
    onOpenEssay: () -> Unit = {},
    onSaveEssay: () -> Unit = {},
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
        EditorToolbar(
            text = text,
            currentEssayTitle = currentEssayTitle,
            onAnalyzeClick = onAnalyzeClick,
            onClearClick = { 
                textFieldValue = TextFieldValue("")
                onTextChange("")
            },
            onRefreshClick = { /* Refresh action */ },
            onOpenClick = onOpenEssay,
            onSaveClick = onSaveEssay
        )
        
        HorizontalDivider(color = Gray200)
        
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
                    fontSize = when (fontSize) {
                        data.settings.FontSize.EXTRA_SMALL -> 12.sp
                        data.settings.FontSize.SMALL -> 14.sp
                        data.settings.FontSize.MEDIUM -> 16.sp
                        data.settings.FontSize.LARGE -> 20.sp
                        data.settings.FontSize.EXTRA_LARGE -> 24.sp
                        data.settings.FontSize.HUGE -> 28.sp
                    },
                    lineHeight = when (fontSize) {
                        data.settings.FontSize.EXTRA_SMALL -> 18.sp
                        data.settings.FontSize.SMALL -> 22.sp
                        data.settings.FontSize.MEDIUM -> 25.6.sp
                        data.settings.FontSize.LARGE -> 32.sp
                        data.settings.FontSize.EXTRA_LARGE -> 38.sp
                        data.settings.FontSize.HUGE -> 44.sp
                    },
                    fontFamily = FontFamily.Default,
                    color = Gray700
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                text = "Start writing here...\n\nSelect any text to get AI suggestions",
                                style = TextStyle(
                                    fontSize = when (fontSize) {
                                        data.settings.FontSize.EXTRA_SMALL -> 12.sp
                                        data.settings.FontSize.SMALL -> 14.sp
                                        data.settings.FontSize.MEDIUM -> 16.sp
                                        data.settings.FontSize.LARGE -> 20.sp
                                        data.settings.FontSize.EXTRA_LARGE -> 24.sp
                                        data.settings.FontSize.HUGE -> 28.sp
                                    },
                                    lineHeight = when (fontSize) {
                                        data.settings.FontSize.EXTRA_SMALL -> 18.sp
                                        data.settings.FontSize.SMALL -> 22.sp
                                        data.settings.FontSize.MEDIUM -> 25.6.sp
                                        data.settings.FontSize.LARGE -> 32.sp
                                        data.settings.FontSize.EXTRA_LARGE -> 38.sp
                                        data.settings.FontSize.HUGE -> 44.sp
                                    },
                                    color = Gray400
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        
        // Error notification banner
        if (!errorMessage.isNullOrBlank()) {
            ErrorBanner(
                errorMessage = errorMessage,
                onDismiss = onErrorDismiss
            )
        }
        
        HorizontalDivider(color = Gray200)
        
        // Status Bar
        EditorStatusBar(
            wordCount = wordCount,
            charCount = charCount
        )
    }
}
