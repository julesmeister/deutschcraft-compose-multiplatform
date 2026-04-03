package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    onAnalyzeClick: () -> Unit = {},
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side - Title only
                Text(
                    text = "Editor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                
                // Right side - Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Analyze button - NEW
                    val analyzeInteractionSource = remember { MutableInteractionSource() }
                    val isAnalyzeHovered by analyzeInteractionSource.collectIsHoveredAsState()
                    
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(CircleShape)
                            .background(if (isAnalyzeHovered) Indigo.copy(alpha = 0.1f) else Color.Transparent)
                            .hoverable(analyzeInteractionSource)
                            .clickable(enabled = text.isNotBlank()) { onAnalyzeClick() }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Spellcheck,
                                contentDescription = "Analyze grammar",
                                tint = if (text.isNotBlank()) (if (isAnalyzeHovered) Indigo else Gray600) else Gray300,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Analyze",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (text.isNotBlank()) (if (isAnalyzeHovered) Indigo else Gray600) else Gray300
                            )
                        }
                    }
                    
                    // Clear button with red hover
                    val clearInteractionSource = remember { MutableInteractionSource() }
                    val isClearHovered by clearInteractionSource.collectIsHoveredAsState()
                    
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isClearHovered) Color(0xFFFEE2E2) else Color.Transparent)
                            .hoverable(clearInteractionSource)
                            .clickable { 
                                textFieldValue = TextFieldValue("")
                                onTextChange("")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear all",
                            tint = if (isClearHovered) Color(0xFFDC2626) else Gray400,
                            modifier = Modifier
                                .size(18.dp)
                                .graphicsLayer {
                                    scaleX = if (isClearHovered) 1.15f else 1f
                                    scaleY = if (isClearHovered) 1.15f else 1f
                                }
                        )
                    }
                    
                    // Refresh button with green hover
                    val refreshInteractionSource = remember { MutableInteractionSource() }
                    val isRefreshHovered by refreshInteractionSource.collectIsHoveredAsState()
                    
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isRefreshHovered) Color(0xFFD1FAE5) else Color.Transparent)
                            .hoverable(refreshInteractionSource)
                            .clickable { /* Refresh action */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = if (isRefreshHovered) Color(0xFF059669) else Gray400,
                            modifier = Modifier
                                .size(18.dp)
                                .graphicsLayer {
                                    scaleX = if (isRefreshHovered) 1.15f else 1f
                                    scaleY = if (isRefreshHovered) 1.15f else 1f
                                }
                        )
                    }
                }
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
