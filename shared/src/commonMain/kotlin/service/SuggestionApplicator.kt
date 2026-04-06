package service

import data.model.ActionType
import data.model.SuggestionAction

/**
 * Applies suggestion actions to text at a given cursor position.
 */
class SuggestionApplicator {

    /**
     * Apply a suggestion action to text.
     */
    fun applySuggestion(text: String, cursorPosition: Int, action: SuggestionAction): Pair<String, Int> {
        return when (action.type) {
            ActionType.REPLACE -> {
                if (action.position != null) {
                    // Replace at specific position
                    val before = text.substring(0, action.position.coerceAtLeast(0))
                    val after = text.substring(action.position.coerceAtMost(text.length))
                    val newText = before + action.value + after
                    val newCursor = action.position + action.value.length
                    newText to newCursor
                } else {
                    // Replace selected or use smart replacement
                    text to cursorPosition
                }
            }
            ActionType.INSERT -> {
                val before = text.substring(0, cursorPosition)
                val after = text.substring(cursorPosition)
                val newText = before + action.value + after
                newText to (cursorPosition + action.value.length)
            }
            ActionType.APPEND -> {
                val newText = text + action.value
                newText to newText.length
            }
            ActionType.DELETE -> {
                if (action.position != null) {
                    val before = text.substring(0, action.position.coerceAtLeast(0))
                    val after = text.substring((action.position + action.value.length).coerceAtMost(text.length))
                    before + after to action.position
                } else {
                    text to cursorPosition
                }
            }
            else -> text to cursorPosition
        }
    }
}
