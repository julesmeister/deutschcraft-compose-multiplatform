package ui.suggestions

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.scale
import theme.*

@Composable
internal fun EditorContextualActions(
    selectedText: String,
    isGenerating: Boolean,
    activeAction: String?,
    onCheckGrammar: () -> Unit,
    onImprove: () -> Unit,
    onRephrase: () -> Unit,
    onSuggestMore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "What would you like to do?",
            style = MaterialTheme.typography.labelMedium,
            color = Gray600,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ActionButton(
                icon = Icons.Default.CheckCircle,
                label = "Check",
                description = "Check grammar & spelling",
                onClick = onCheckGrammar,
                isActive = activeAction == "grammar",
                isLoading = isGenerating && activeAction == "grammar",
                modifier = Modifier.weight(1f)
            )
            
            ActionButton(
                icon = Icons.Default.Edit,
                label = "Improve",
                description = "Make it clearer",
                onClick = onImprove,
                isActive = activeAction == "improve",
                isLoading = isGenerating && activeAction == "improve",
                modifier = Modifier.weight(1f)
            )
            
            ActionButton(
                icon = Icons.Default.Refresh,
                label = "Rephrase",
                description = "Say it differently",
                onClick = onRephrase,
                isActive = activeAction == "rephrase",
                isLoading = isGenerating && activeAction == "rephrase",
                modifier = Modifier.weight(1f)
            )
        }
        
        OutlinedButton(
            onClick = onSuggestMore,
            enabled = !isGenerating,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Indigo
            ),
            border = androidx.compose.material3.ButtonDefaults.outlinedButtonBorder(enabled = !isGenerating)
        ) {
            if (isGenerating && activeAction == "continue") {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Indigo
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating ideas...")
            } else {
                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Suggest more to say based on this")
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit,
    isActive: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !isLoading) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "button_scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isActive -> Indigo
            else -> Gray100
        },
        animationSpec = tween(200),
        label = "button_bg"
    )
    
    val contentColor by animateColorAsState(
        targetValue = when {
            isActive -> androidx.compose.ui.graphics.Color.White
            else -> Gray700
        },
        animationSpec = tween(200),
        label = "button_content"
    )
    
    Surface(
        onClick = onClick,
        enabled = !isLoading,
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .height(56.dp)
            .scale(scale),
        interactionSource = interactionSource
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = if (isActive) androidx.compose.ui.graphics.Color.White else Indigo
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f) else Gray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
