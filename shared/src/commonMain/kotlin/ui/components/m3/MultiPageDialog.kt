package ui.components.m3

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import theme.DeutschCraftTheme

private val MultiPageDialogBg = Color(0xFFFCFCFF)
private val MultiPageButtonBarBg = Color(0xFFF6F6FE)

@Composable
fun DCMultiPageDialog(
    title: (page: Int) -> String,
    icon: (page: Int) -> ImageVector,
    accentColor: Color = M3Primary,
    pageCount: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    isEdit: Boolean = false,
    confirmEnabled: Boolean = true,
    confirmLabel: (page: Int) -> String = { page ->
        if (page < pageCount - 1) "Next" else if (isEdit) "Update" else "Save"
    },
    isSaving: Boolean = false,
    onConfirm: () -> Unit,
    headerExtra: @Composable (() -> Unit)? = null,
    showPageIndicators: Boolean = !isEdit,
    pageContent: @Composable (page: Int) -> Unit,
) {
    val headerBg = if (accentColor == M3Primary) M3PrimaryContainer.copy(0.35f) else accentColor.copy(0.08f)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(24.dp), color = MultiPageDialogBg, tonalElevation = 6.dp) {
            Box(modifier = Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.85f)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().background(headerBg).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val pageIcon = icon(currentPage)
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(0.8f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(pageIcon, null, tint = accentColor, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            title(currentPage),
                            fontSize = DeutschCraftTheme.fontSize.heading,
                            fontWeight = FontWeight.Bold,
                            color = M3OnSurface,
                        )
                        if (headerExtra != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            headerExtra()
                        }
                        if (showPageIndicators && pageCount > 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                            PageIndicators(pageCount = pageCount, currentPage = currentPage, accentColor = accentColor)
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        AnimatedContent(
                            targetState = currentPage,
                            transitionSpec = {
                                val direction = if (targetState > initialState) 1 else -1
                                (slideInHorizontally { it * direction } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it * -direction } + fadeOut())
                            },
                            label = "page_content",
                        ) { page ->
                            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                                pageContent(page)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().background(MultiPageButtonBarBg).padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (currentPage > 0) {
                            TextButton(onClick = { onPageChange(currentPage - 1) }) {
                                Text("Back", color = M3OnSurfaceVariant, fontWeight = FontWeight.Medium)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(64.dp))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (onDelete != null && currentPage == 0) {
                                TextButton(onClick = onDelete) {
                                    Text("Delete", color = M3RedColor, fontWeight = FontWeight.Medium)
                                }
                            }
                            if (currentPage < pageCount - 1) {
                                Button(
                                    onClick = { onPageChange(currentPage + 1) },
                                    enabled = confirmEnabled,
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.White),
                                ) {
                                    Text("Next", fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
                                }
                            } else {
                                Button(
                                    onClick = onConfirm,
                                    enabled = confirmEnabled && !isSaving,
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.White),
                                ) {
                                    if (isSaving) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                                    } else {
                                        Text(confirmLabel(currentPage), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                if (isSaving) {
                    Box(modifier = Modifier.matchParentSize().background(MultiPageDialogBg.copy(0.6f)))
                }
            }
        }
    }
}

@Composable
private fun PageIndicators(pageCount: Int, currentPage: Int, accentColor: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            Box(
                modifier = Modifier
                    .width(if (isActive) 24.dp else 8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isActive) accentColor else accentColor.copy(0.3f)),
            )
        }
    }
}
