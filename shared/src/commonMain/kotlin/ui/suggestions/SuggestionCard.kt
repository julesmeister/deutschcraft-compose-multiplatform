package ui.suggestions

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.model.*
import data.settings.FontSize
import theme.*

@Composable
internal fun SuggestionCard(
    suggestion: QuickSuggestion,
    isExpanded: Boolean,
    fontSize: FontSize,
    onExpandToggle: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    onShowExercise: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "card_scale"
    )
    
    val (icon, colors) = getCategoryVisuals(suggestion.type)
    
    val textSize = when (fontSize) {
        FontSize.EXTRA_SMALL -> 11.sp
        FontSize.SMALL -> 12.sp
        FontSize.MEDIUM -> 13.sp
        FontSize.LARGE -> 14.sp
        FontSize.EXTRA_LARGE -> 15.sp
        FontSize.HUGE -> 16.sp
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onExpandToggle
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = colors.first.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(color = colors.first.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.first,
                        modifier = Modifier.padding(6.dp).size(18.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = suggestion.title,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = textSize),
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    Text(
                        text = suggestion.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = textSize - 1.sp),
                        color = Gray600,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (suggestion.preview != null) {
                        Surface(
                            color = Gray100,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = suggestion.preview,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = textSize - 1.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                ),
                                color = Gray700,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onApply,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.first),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = when (suggestion.action.type) {
                                    ActionType.REPLACE -> Icons.Default.Edit
                                    ActionType.INSERT -> Icons.Default.Add
                                    ActionType.APPEND -> Icons.Default.AddComment
                                    ActionType.DELETE -> Icons.Default.Delete
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (suggestion.action.type) {
                                    ActionType.REPLACE -> "Apply"
                                    ActionType.INSERT -> "Insert"
                                    ActionType.APPEND -> "Add"
                                    ActionType.DELETE -> "Remove"
                                    ActionType.OPEN_EXERCISE -> "Practice"
                                    ActionType.SHOW_INFO -> "Learn"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = textSize)
                            )
                        }
                        
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.heightIn(min = 36.dp),
                            border = ButtonDefaults.outlinedButtonBorder,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Gray600
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun getCategoryColors(category: SuggestionCategory): Pair<Color, Color> {
    return when (category) {
        SuggestionCategory.GRAMMAR_FIX -> Error to Color.White
        SuggestionCategory.VOCABULARY -> Indigo to Color.White
        SuggestionCategory.STYLE -> Violet to Color.White
        SuggestionCategory.STRUCTURE -> Blue600 to Color.White
        SuggestionCategory.NEXT_WORD -> Emerald to Color.White
        SuggestionCategory.LEARNING -> Amber to Color.Black
        SuggestionCategory.CONTINUATION -> Cyan to Color.Black
    }
}

internal fun getCategoryVisuals(category: SuggestionCategory): Pair<ImageVector, Pair<Color, Color>> {
    return when (category) {
        SuggestionCategory.GRAMMAR_FIX -> Icons.Default.Error to (Error to Color.White)
        SuggestionCategory.VOCABULARY -> Icons.Default.MenuBook to (Indigo to Color.White)
        SuggestionCategory.STYLE -> Icons.Default.Brush to (Violet to Color.White)
        SuggestionCategory.STRUCTURE -> Icons.Default.AccountTree to (Blue600 to Color.White)
        SuggestionCategory.NEXT_WORD -> Icons.Default.AddComment to (Emerald to Color.White)
        SuggestionCategory.LEARNING -> Icons.Default.School to (Amber to Color.Black)
        SuggestionCategory.CONTINUATION -> Icons.Default.DoubleArrow to (Cyan to Color.Black)
    }
}
