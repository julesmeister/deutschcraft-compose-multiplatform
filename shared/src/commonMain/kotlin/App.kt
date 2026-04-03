import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import theme.DeutschCraftTheme
import theme.Gray200
import ui.EditorPanel
import ui.SuggestionsPanel

@Composable
fun App() {
    DeutschCraftTheme {
        var editorText by remember { mutableStateOf("") }
        var selectedText by remember { mutableStateOf("") }
        
        Scaffold { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Editor Panel - Takes 2/3 of the space
                EditorPanel(
                    text = editorText,
                    onTextChange = { editorText = it },
                    onSelectionChange = { selectedText = it },
                    modifier = Modifier.weight(2f)
                )
                
                // Divider
                Divider(
                    modifier = Modifier.fillMaxHeight().width(1.dp),
                    color = Gray200
                )
                
                // Suggestions Panel - Takes 1/3 of the space
                SuggestionsPanel(
                    selectedText = selectedText,
                    onApplySuggestion = { suggestion ->
                        editorText = suggestion
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

expect fun getPlatformName(): String