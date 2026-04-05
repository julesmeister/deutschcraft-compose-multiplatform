package ui.suggestions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.model.VocabularyItem
import data.settings.FontSize
import theme.Gray600
import theme.Sage
import theme.SageDark
import kotlin.random.Random

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
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
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
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material3.IconDefaults.Psychology,
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
                            ui.suggestions.WordChip(
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
                    ui.suggestions.PracticePromptCard(
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
