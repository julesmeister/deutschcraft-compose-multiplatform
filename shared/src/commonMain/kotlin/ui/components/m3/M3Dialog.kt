package ui.components.m3

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import theme.DeutschCraftTheme

@Composable
fun DCConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String = "Confirm",
    dismissLabel: String = "Cancel",
    icon: ImageVector? = null,
    accentColor: Color = M3Primary,
    isLoading: Boolean = false,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (icon != null) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(accentColor.copy(0.1f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, null, tint = accentColor, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text(title, fontSize = DeutschCraftTheme.fontSize.headingLg, fontWeight = FontWeight.Bold, color = M3OnSurface, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(message, fontSize = DeutschCraftTheme.fontSize.base, color = M3OnSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
                    TextButton(onClick = onDismiss, enabled = !isLoading) {
                        Text(dismissLabel, color = M3OnSurfaceVariant)
                    }
                    TextButton(onClick = onConfirm, enabled = !isLoading) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = accentColor, strokeWidth = 2.dp)
                        } else {
                            Text(confirmLabel, color = accentColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DCAlertDialog(title: String, message: String, onDismiss: () -> Unit, buttonLabel: String = "OK") {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(M3AmberColor.copy(0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Warning, null, tint = M3AmberColor, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(title, fontSize = DeutschCraftTheme.fontSize.headingLg, fontWeight = FontWeight.Bold, color = M3OnSurface, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(message, fontSize = DeutschCraftTheme.fontSize.base, color = M3OnSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(buttonLabel, color = M3Primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
