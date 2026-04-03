import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.db.DatabaseDriverFactory
import data.settings.ThemeMode
import data.settings.FontSize
import androidx.compose.foundation.isSystemInDarkTheme
import theme.DeutschCraftTheme
import theme.Gray200
import theme.Gray50
import theme.Gray100
import theme.Gray700
import theme.Indigo
import ui.EditorPanel
import ui.ChatPanelWithPersistence
import ui.GrammarAnalysisPanel
import ui.SettingsPanel
import ui.suggestions.SuggestionsPanel
import kotlinx.coroutines.*

@Composable
fun App(driverFactory: DatabaseDriverFactory) {
    val scope = rememberCoroutineScope()
    val settingsRepo = remember(driverFactory) { driverFactory.settingsRepository }
    
    var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
    var fontSize by remember { mutableStateOf(FontSize.MEDIUM) }
    
    LaunchedEffect(Unit) {
        settingsRepo.themeMode.collect { themeMode = it }
    }
    LaunchedEffect(Unit) {
        settingsRepo.fontSize.collect { fontSize = it }
    }
    
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    DeutschCraftTheme(darkTheme = darkTheme) {
        var editorText by remember { mutableStateOf("") }
        var selectedText by remember { mutableStateOf("") }
        var activeTab by remember { mutableStateOf(0) } // 0 = Editor, 1 = Chat, 2 = Analysis, 3 = Settings
        
        Scaffold { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Left Panel (Editor/Chat) - Takes 2/3 of the space
                Column(
                    modifier = Modifier.weight(2f)
                ) {
                    // Tab Selector
                    ModeTabSelector(
                        activeTab = activeTab,
                        onTabSelected = { activeTab = it }
                    )
                    
                    Divider(color = Gray200)
                    
                    // Content based on selected tab
                    when (activeTab) {
                        0 -> EditorPanel(
                            text = editorText,
                            onTextChange = { editorText = it },
                            onSelectionChange = { selectedText = it },
                            onAnalyzeClick = { activeTab = 2 },
                            modifier = Modifier.fillMaxSize()
                        )
                        1 -> ChatPanelWithPersistence(
                            editorText = editorText,
                            driverFactory = driverFactory,
                            fontSize = fontSize,
                            modifier = Modifier.fillMaxSize()
                        )
                        2 -> GrammarAnalysisPanel(
                            text = editorText,
                            driverFactory = driverFactory,
                            modifier = Modifier.fillMaxSize()
                        )
                        3 -> SettingsPanel(
                            driverFactory = driverFactory,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                // Divider
                Divider(
                    modifier = Modifier.fillMaxHeight().width(1.dp),
                    color = Gray200
                )
                
                // Suggestions Panel - Takes 1/3 of the space
                SuggestionsPanel(
                    selectedText = selectedText,
                    fullText = editorText,
                    onApplySuggestion = { suggestion ->
                        editorText = suggestion
                    },
                    onAppendSuggestion = { suggestion ->
                        editorText = if (editorText.endsWith(" ") || editorText.isEmpty()) {
                            editorText + suggestion
                        } else {
                            editorText + " " + suggestion
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ModeTabSelector(
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        color = Gray50,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Editor Tab
            TabButton(
                icon = Icons.Default.Edit,
                label = "Editor",
                isSelected = activeTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            
            // Chat Tab
            TabButton(
                icon = Icons.Default.Chat,
                label = "Chat",
                isSelected = activeTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
            
            // Analysis Tab
            TabButton(
                icon = Icons.Default.Assessment,
                label = "Analysis",
                isSelected = activeTab == 2,
                onClick = { onTabSelected(2) },
                modifier = Modifier.weight(1f)
            )
            
            // Settings Tab
            TabButton(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = activeTab == 3,
                onClick = { onTabSelected(3) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Indigo else Gray100
    val contentColor = if (isSelected) androidx.compose.ui.graphics.Color.White else Gray700
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

expect fun getPlatformName(): String