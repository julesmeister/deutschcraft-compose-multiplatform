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
import data.model.VocabularyItem
import theme.*
import ui.components.GermanAutocompleteState
import ui.components.GermanSuggestionPopup
import ui.components.acceptGermanSuggestion
import ui.components.checkForGermanSuggestion
import ui.editor.EditorStatusBar
import ui.editor.EditorToolbar
import ui.editor.ErrorBanner
import ui.suggestions.DifficultWordsSuggestion
import ui.suggestions.shouldSuggestDifficultWords

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
    difficultWords: List<VocabularyItem> = emptyList(),
    onWordSelected: (String) -> Unit = {},
    onDismissDifficultWords: () -> Unit = {},
    onWordPracticed: (String) -> Unit = {},
    onMarkWordAsLearned: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text)) }
    var wordCount by remember { mutableIntStateOf(0) }
    var charCount by remember { mutableIntStateOf(0) }
    var showDifficultWords by remember { mutableStateOf(false) }
    var lastSuggestionTime by remember { mutableStateOf<Long?>(null) }
    
    // German autocomplete state
    val germanAutocompleteState = remember { GermanAutocompleteState() }
    
    // Check for German character suggestions when text changes
    LaunchedEffect(textFieldValue.text, textFieldValue.selection.start) {
        val cursorPos = textFieldValue.selection.start
        val result = checkForGermanSuggestion(textFieldValue.text, cursorPos)
        
        if (result != null) {
            germanAutocompleteState.currentSuggestion = result.first
            germanAutocompleteState.triggerStartPos = result.second
            germanAutocompleteState.showSuggestion = true
        } else {
            germanAutocompleteState.showSuggestion = false
            germanAutocompleteState.currentSuggestion = null
        }
    }
    
    // Update local state when external text changes
    LaunchedEffect(text) {
        if (text != textFieldValue.text) {
            textFieldValue = TextFieldValue(text)
        }
    }
    
    LaunchedEffect(textFieldValue.text) {
        wordCount = textFieldValue.text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.size
        charCount = textFieldValue.text.length
        
        // Check if we should suggest difficult words
        if (shouldSuggestDifficultWords(
            textLength = charCount,
            wordCount = wordCount,
            difficultWordCount = difficultWords.size,
            lastSuggestionTime = lastSuggestionTime
        )) {
            showDifficultWords = true
            lastSuggestionTime = System.currentTimeMillis()
        }
    }
    
    // Report selection changes
    LaunchedEffect(textFieldValue.selection) {
        val selection = textFieldValue.selection
        if (selection.start != selection.end) {
            val selectedText = textFieldValue.text.substring(selection.start, selection.end)
            onSelectionChange(selectedText)
        }
    }
    
    // Handle German autocomplete accept
    fun acceptGermanSuggestionAndUpdate() {
        val suggestion = germanAutocompleteState.currentSuggestion ?: return
        val (newText, newCursorPos) = acceptGermanSuggestion(
            textFieldValue.text,
            suggestion,
            germanAutocompleteState.triggerStartPos,
            textFieldValue.selection.start
        )
        textFieldValue = TextFieldValue(
            text = newText,
            selection = androidx.compose.ui.text.TextRange(newCursorPos)
        )
        onTextChange(newText)
        germanAutocompleteState.showSuggestion = false
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
                showDifficultWords = false
            },
            onRefreshClick = { /* Refresh action */ },
            onOpenClick = onOpenEssay,
            onSaveClick = onSaveEssay
        )
        
        HorizontalDivider(color = Gray200)
        
        // Text Editor with German autocomplete
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    // Check for Tab key (indent) or specific key combinations to accept German suggestion
                    // In Compose, we handle this via onKeyEvent on the parent or by detecting specific text changes
                    // For now, we'll just update the value normally
                    textFieldValue = newValue
                    onTextChange(newValue.text)
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
            
            // German autocomplete suggestion popup
            if (germanAutocompleteState.showSuggestion && germanAutocompleteState.currentSuggestion != null) {
                GermanSuggestionPopup(
                    suggestion = germanAutocompleteState.currentSuggestion!!,
                    onAccept = { acceptGermanSuggestionAndUpdate() },
                    onDismiss = { germanAutocompleteState.showSuggestion = false },
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        }
        
        // Difficult words suggestion (shown above error banner when appropriate)
        if (showDifficultWords && difficultWords.isNotEmpty()) {
            DifficultWordsSuggestion(
                difficultWords = difficultWords,
                onWordSelected = { word ->
                    onWordSelected(word)
                    // Insert the word at cursor position
                    val currentText = textFieldValue.text
                    val selection = textFieldValue.selection
                    val before = currentText.substring(0, selection.start)
                    val after = currentText.substring(selection.end)
                    val newText = "$before$word$after"
                    textFieldValue = TextFieldValue(
                        text = newText,
                        selection = androidx.compose.ui.text.TextRange(selection.start + word.length)
                    )
                    onTextChange(newText)
                },
                onDismiss = {
                    showDifficultWords = false
                    onDismissDifficultWords()
                },
                onWordPracticed = onWordPracticed,
                onMarkAsLearned = onMarkWordAsLearned,
                fontSize = fontSize,
                isVisible = showDifficultWords
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
