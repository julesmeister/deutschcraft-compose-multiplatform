package ui.suggestions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import theme.Sage
import theme.SageDark

/**
 * A chip displaying a word with optional practice count indicator.
 *
 * @param word The word to display
 * @param practiceCount Number of times the word has been practiced
 * @param isSelected Whether this chip is currently selected
 * @param onClick Callback when the chip is clicked
 */
@Composable
fun WordChip(
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

/**
 * Card displaying a practice prompt for a word with progress tracking.
 *
 * @param prompt The practice prompt text
 * @param word The vocabulary item being practiced
 * @param fontSize The font size setting
 * @param onNewPrompt Callback to generate a new prompt
 * @param onPracticed Callback when user indicates they practiced the word
 * @param onMarkAsLearned Callback when user marks the word as learned
 * @param autoLearnThreshold Number of practices before word is ready to graduate
 */
@Composable
fun PracticePromptCard(
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
