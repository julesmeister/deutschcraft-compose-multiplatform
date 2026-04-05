package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import theme.Gray100
import theme.Gray200
import theme.Gray400
import theme.Gray500
import theme.Gray700
import theme.Gray800
import theme.Gray900
import theme.Indigo

/**
 * Data class representing a German character autocomplete suggestion.
 */
data class GermanSuggestion(
    val trigger: String,
    val replacement: String,
    val display: String
)

/**
 * Predefined German character suggestions for umlaut and special characters.
 */
val GERMAN_SUGGESTIONS = listOf(
    GermanSuggestion("ae", "ä", "ae → ä"),
    GermanSuggestion("oe", "ö", "oe → ö"),
    GermanSuggestion("ue", "ü", "ue → ü"),
    GermanSuggestion("Ae", "Ä", "Ae → Ä"),
    GermanSuggestion("Oe", "Ö", "Oe → Ö"),
    GermanSuggestion("Ue", "Ü", "Ue → Ü"),
    GermanSuggestion("ss", "ß", "ss → ß"),
    GermanSuggestion("Ss", "ß", "Ss → ß")
)

/**
 * State holder for German character autocomplete functionality.
 */
class GermanAutocompleteState {
    var showSuggestion by mutableStateOf(false)
    var currentSuggestion by mutableStateOf<GermanSuggestion?>(null)
    var triggerStartPos by mutableStateOf(0)
    var popupPosition by mutableStateOf(androidx.compose.ui.geometry.Offset.Zero)
    var fieldHeight by mutableStateOf(0f)
}

/**
 * Check for German character triggers and return a suggestion if found.
 *
 * @param text The current text content
 * @param cursorPos The cursor position
 * @return The suggestion and trigger start position if found, null otherwise
 */
fun checkForGermanSuggestion(
    text: String,
    cursorPos: Int
): Pair<GermanSuggestion, Int>? {
    if (cursorPos <= 0) return null

    val textBeforeCursor = text.substring(0, cursorPos)

    for (suggestion in GERMAN_SUGGESTIONS) {
        val triggerLength = suggestion.trigger.length
        if (textBeforeCursor.length >= triggerLength) {
            val potentialTrigger = textBeforeCursor.takeLast(triggerLength)

            if (potentialTrigger == suggestion.trigger) {
                // Check that we're at a word boundary
                val charAfterCursor = if (cursorPos < text.length) text[cursorPos] else '\u0000'
                val isAtWordEnd = charAfterCursor == '\u0000' ||
                    " \t,.-!?;:()[]{}" .contains(charAfterCursor)

                if (isAtWordEnd) {
                    val triggerStart = cursorPos - triggerLength
                    return Pair(suggestion, triggerStart)
                }
            }
        }
    }
    return null
}

/**
 * Accept a German character suggestion and return the modified text.
 *
 * @param text The current text
 * @param suggestion The suggestion to apply
 * @param triggerStartPos The position where the trigger starts
 * @param cursorPos The current cursor position
 * @return Pair of new text and new cursor position
 */
fun acceptGermanSuggestion(
    text: String,
    suggestion: GermanSuggestion,
    triggerStartPos: Int,
    cursorPos: Int
): Pair<String, Int> {
    val beforeTrigger = text.substring(0, triggerStartPos)
    val afterCursor = if (cursorPos <= text.length) text.substring(cursorPos) else ""
    val newText = beforeTrigger + suggestion.replacement + afterCursor
    val newCursorPos = triggerStartPos + suggestion.replacement.length
    return Pair(newText, newCursorPos)
}

/**
 * TextField with German character autocomplete support.
 *
 * This component wraps a BasicTextField and provides autocomplete suggestions
 * for German umlaut characters (ae→ä, oe→ö, ue→ü, ss→ß).
 *
 * @param value The current text value
 * @param onValueChange Callback when text changes
 * @param modifier Modifier for the text field
 * @param textStyle Style for the text
 * @param maxLines Maximum number of lines
 * @param singleLine Whether the field is single line
 * @param keyboardOptions Keyboard options
 * @param decorationBox Optional decoration box
 */
@Composable
fun GermanAutocompleteTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default,
    maxLines: Int = Int.MAX_VALUE,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit = { innerTextField -> innerTextField() }
) {
    val state = remember { GermanAutocompleteState() }
    val density = LocalDensity.current

    // Check for suggestions when text changes
    LaunchedEffect(value.text, value.selection.start) {
        val cursorPos = value.selection.start
        val result = checkForGermanSuggestion(value.text, cursorPos)

        if (result != null) {
            state.currentSuggestion = result.first
            state.triggerStartPos = result.second
            state.showSuggestion = true
        } else {
            state.showSuggestion = false
            state.currentSuggestion = null
        }
    }

    Box(modifier = modifier) {
        // The text field
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                // Handle Tab or Enter key to accept suggestion
                if (state.showSuggestion && state.currentSuggestion != null) {
                    // Check if this change is just a tab or newline
                    val isTabOrEnter = newValue.text == value.text + "\t" ||
                        newValue.text == value.text + "\n"

                    if (isTabOrEnter) {
                        // Accept suggestion
                        val (newText, newCursorPos) = acceptGermanSuggestion(
                            value.text,
                            state.currentSuggestion!!,
                            state.triggerStartPos,
                            value.selection.start
                        )
                        onValueChange(
                            TextFieldValue(
                                text = newText,
                                selection = androidx.compose.ui.text.TextRange(newCursorPos)
                            )
                        )
                        state.showSuggestion = false
                        state.currentSuggestion = null
                        return@BasicTextField
                    }
                }

                // Check if Escape was pressed (text unchanged, just hide suggestion)
                if (newValue.text == value.text && state.showSuggestion) {
                    state.showSuggestion = false
                    return@BasicTextField
                }

                onValueChange(newValue)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    // Calculate position for popup
                    val position = coordinates.positionInWindow()
                    val size = coordinates.size
                    with(density) {
                        state.popupPosition = androidx.compose.ui.geometry.Offset(
                            position.x,
                            position.y + size.height.toFloat()
                        )
                        state.fieldHeight = size.height.toFloat()
                    }
                },
            textStyle = textStyle,
            maxLines = maxLines,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            decorationBox = decorationBox
        )

        // Suggestion popup
        if (state.showSuggestion && state.currentSuggestion != null) {
            Popup(
                alignment = Alignment.TopStart,
                offset = with(density) {
                    androidx.compose.ui.unit.IntOffset(
                        0,
                        state.fieldHeight.toInt() + 4.dp.roundToPx()
                    )
                },
                properties = PopupProperties(
                    focusable = false,
                    dismissOnClickOutside = false,
                    dismissOnBackPress = false
                )
            ) {
                GermanSuggestionPopup(
                    suggestion = state.currentSuggestion!!,
                    onAccept = {
                        val (newText, newCursorPos) = acceptGermanSuggestion(
                            value.text,
                            state.currentSuggestion!!,
                            state.triggerStartPos,
                            value.selection.start
                        )
                        onValueChange(
                            TextFieldValue(
                                text = newText,
                                selection = androidx.compose.ui.text.TextRange(newCursorPos)
                            )
                        )
                        state.showSuggestion = false
                    },
                    onDismiss = {
                        state.showSuggestion = false
                    }
                )
            }
        }
    }
}

/**
 * Popup showing the German character suggestion.
 */
@Composable
fun GermanSuggestionPopup(
    suggestion: GermanSuggestion,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Indigo, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable { onAccept() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = suggestion.display,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray800,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Press",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )

            // Tab key badge
            KeyBadge(text = "Tab")

            Text(
                text = "or",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )

            // Enter key badge
            KeyBadge(text = "Enter")
        }
    }
}

/**
 * A small badge representing a keyboard key.
 */
@Composable
private fun KeyBadge(text: String) {
    Box(
        modifier = Modifier
            .background(Gray100, RoundedCornerShape(4.dp))
            .border(1.dp, Gray200, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Gray700,
            fontSize = 11.sp
        )
    }
}
