package ui.suggestions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.model.VocabularyItem
import data.settings.FontSize
import theme.Gray100
import theme.Gray600
import theme.Gray800
import theme.Indigo
import theme.IndigoLight
import theme.Sage
import theme.SageDark
import kotlin.random.Random

/**
 * Data class representing a word practice suggestion.
 */
data class WordPracticeSuggestion(
    val word: String,
    val context: String,
    val translation: String?,
    val prompt: String
)

/**
 * Component that suggests difficult words for practice at appropriate moments.
 * Shows in the editor or chat panel when the user might benefit from revisiting
 * words they've previously marked as difficult.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DifficultWordsSuggestion(
    difficultWords: List<VocabularyItem>,
    onWordSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onWordPracticed: (String) -> Unit = {},
    onMarkAsLearned: (String) -> Unit = {},
    fontSize: FontSize = FontSize.MEDIUM,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    autoLearnThreshold: Int = 3
) {
    var selectedWord by remember { mutableStateOf<VocabularyItem?>(null) }
    var currentPrompt by remember { mutableStateOf<String>("") }
    
    // Generate a practice prompt when a word is selected
    LaunchedEffect(selectedWord) {
        selectedWord?.let { word ->
            currentPrompt = generatePracticePrompt(word)
        }
    }
    
    AnimatedVisibility(
        visible = isVisible && difficultWords.isNotEmpty(),
        enter = slideInVertically { it / 2 } + fadeIn(),
        exit = slideOutVertically { it / 2 } + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Sage.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = SageDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Practice Difficult Words",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SageDark
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Text(
                            text = "Dismiss",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Try using these challenging words in your writing:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600,
                    fontSize = when (fontSize) {
                        FontSize.EXTRA_SMALL -> 11.sp
                        FontSize.SMALL -> 12.sp
                        FontSize.MEDIUM -> 13.sp
                        FontSize.LARGE -> 15.sp
                        FontSize.EXTRA_LARGE -> 17.sp
                        FontSize.HUGE -> 19.sp
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Word chips with practice indicators
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    difficultWords
                        .filter { it.practiceCount < autoLearnThreshold }
                        .take(5)
                        .forEach { word ->
                            WordChip(
                                word = word.word,
                                practiceCount = word.practiceCount,
                                isSelected = selectedWord?.word == word.word,
                                onClick = { 
                                    selectedWord = if (selectedWord?.word == word.word) null else word
                                    onWordSelected(word.word)
                                }
                            )
                        }
                }
                
                // Show practice prompt and grading UI for selected word
                if (selectedWord != null && currentPrompt.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    PracticePromptCard(
                        prompt = currentPrompt,
                        word = selectedWord!!,
                        fontSize = fontSize,
                        onNewPrompt = {
                            currentPrompt = generatePracticePrompt(selectedWord!!, Random.nextInt())
                        },
                        onPracticed = {
                            onWordPracticed(selectedWord!!.word)
                        },
                        onMarkAsLearned = {
                            onMarkAsLearned(selectedWord!!.word)
                            selectedWord = null
                        },
                        autoLearnThreshold = autoLearnThreshold
                    )
                }
            }
        }
    }
}

@Composable
private fun WordChip(
    word: String,
    practiceCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Sage else Gray100
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else Gray800
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = word,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
        // Show practice count indicator
        if (practiceCount > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "($practiceCount)",
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else Gray600,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun PracticePromptCard(
    prompt: String,
    word: VocabularyItem,
    fontSize: FontSize,
    onNewPrompt: () -> Unit,
    onPracticed: () -> Unit,
    onMarkAsLearned: () -> Unit,
    autoLearnThreshold: Int
) {
    val isReadyToGraduate = word.practiceCount >= autoLearnThreshold - 1
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "New prompt",
                    tint = Indigo,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onNewPrompt)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray800,
                        fontSize = when (fontSize) {
                            FontSize.EXTRA_SMALL -> 12.sp
                            FontSize.SMALL -> 13.sp
                            FontSize.MEDIUM -> 14.sp
                            FontSize.LARGE -> 16.sp
                            FontSize.EXTRA_LARGE -> 18.sp
                            FontSize.HUGE -> 20.sp
                        }
                    )
                    word.translation?.let { translation ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Translation: $translation",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600,
                            fontSize = when (fontSize) {
                                FontSize.EXTRA_SMALL -> 11.sp
                                FontSize.SMALL -> 12.sp
                                FontSize.MEDIUM -> 13.sp
                                FontSize.LARGE -> 15.sp
                                FontSize.EXTRA_LARGE -> 17.sp
                                FontSize.HUGE -> 19.sp
                            }
                        )
                    }
                }
            }
            
            // Practice progress indicator
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Practice: ${word.practiceCount}/$autoLearnThreshold",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Progress dots
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(autoLearnThreshold) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (index < word.practiceCount) Sage else Gray100
                                )
                        )
                    }
                }
            }
            
            // Action buttons
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onPracticed,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("I Used It!")
                }
                
                if (isReadyToGraduate) {
                    TextButton(
                        onClick = onMarkAsLearned,
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = SageDark
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Mark as Learned")
                    }
                }
            }
        }
    }
}

/**
 * Generates a practice prompt for a given word.
 */
private fun generatePracticePrompt(word: VocabularyItem, seed: Int = 0): String {
    val prompts = listOf(
        "Try to use \"${word.word}\" in a sentence about your day.",
        "Write a sentence using \"${word.word}\" that shows its meaning.",
        "Can you think of a situation where you'd use \"${word.word}\"?",
        "Use \"${word.word}\" to describe something you see around you.",
        "Create a question that includes the word \"${word.word}\".",
        "Write about a time when you encountered \"${word.word}\".",
        "Describe your favorite activity using \"${word.word}\".",
        "How would you explain \"${word.word}\" to a friend?"
    )
    
    return prompts[(seed + word.word.hashCode()).absoluteValue % prompts.size]
}

private val Int.absoluteValue: Int get() = if (this < 0) -this else this

/**
 * Determines if it is an appropriate moment to suggest difficult words.
 * This is based on heuristics like:
 * - User has written enough text (at least a few sentences)
 * - Some time has passed since last suggestion
 * - User has difficult words to practice
 */
fun shouldSuggestDifficultWords(
    textLength: Int,
    wordCount: Int,
    difficultWordCount: Int,
    lastSuggestionTime: Long? = null,
    minWordsSinceLastSuggestion: Int = 50
): Boolean {
    // Need at least some content to suggest words
    if (textLength < 20 || wordCount < 5) return false
    
    // Need difficult words to suggest
    if (difficultWordCount == 0) return false
    
    // Check time/words since last suggestion
    lastSuggestionTime?.let { lastTime ->
        val timeSinceLastSuggestion = System.currentTimeMillis() - lastTime
        // Don't show more than once per 5 minutes
        if (timeSinceLastSuggestion < 5 * 60 * 1000) return false
    }
    
    // Random chance to avoid being too intrusive (30% chance when conditions are met)
    return kotlin.random.Random.Default.nextFloat() < 0.3f
}
