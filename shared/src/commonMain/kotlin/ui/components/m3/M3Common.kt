package ui.components.m3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.DeutschCraftTheme
import kotlin.math.roundToInt

private val AppBarBackButtonBg = Color.White.copy(alpha = 0.15f)

@Composable
fun DCBackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.padding(start = 4.dp)) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(AppBarBackButtonBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
        }
    }
}

@Composable
fun DCAppBarAction(icon: ImageVector, contentDescription: String? = null, onClick: () -> Unit, tint: Color = Color.White) {
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(AppBarBackButtonBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription, modifier = Modifier.size(18.dp), tint = tint)
        }
    }
}

@Stable
class CollapsibleAppBarState {
    var collapsibleHeightPx by mutableFloatStateOf(0f)
    var offsetPx by mutableFloatStateOf(0f)

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (collapsibleHeightPx <= 0f) return Offset.Zero
            val newOffset = (offsetPx + available.y).coerceIn(-collapsibleHeightPx, 0f)
            val consumed = newOffset - offsetPx
            return if (consumed != 0f) {
                offsetPx = newOffset
                Offset(0f, consumed)
            } else Offset.Zero
        }
    }
}

@Composable
fun rememberCollapsibleAppBarState(): CollapsibleAppBarState = remember { CollapsibleAppBarState() }

private val AppBarShape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)

@Composable
fun DCAppBarZone(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().shadow(4.dp, AppBarShape).clip(AppBarShape).background(M3AppBarBg),
        content = content,
    )
}

@Composable
fun DCAppBarZone(
    collapsibleState: CollapsibleAppBarState,
    modifier: Modifier = Modifier,
    pinnedContent: @Composable ColumnScope.() -> Unit,
    collapsibleContent: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().shadow(4.dp, AppBarShape).clip(AppBarShape).background(M3AppBarBg),
    ) {
        pinnedContent()
        Layout(
            content = {
                Column {
                    collapsibleContent()
                    Spacer(Modifier.height(AppBarZoneBottomPadding))
                }
            },
            modifier = Modifier.clipToBounds(),
        ) { measurables, constraints ->
            val placeable = measurables.first().measure(constraints)
            collapsibleState.collapsibleHeightPx = placeable.height.toFloat()
            val visibleHeight = (placeable.height + collapsibleState.offsetPx.roundToInt()).coerceAtLeast(0)
            layout(constraints.maxWidth, visibleHeight) {
                placeable.place(0, collapsibleState.offsetPx.roundToInt())
            }
        }
    }
}

@Composable
fun DCAddFab(
    onClick: () -> Unit,
    contentDescription: String = "Add",
    containerColor: Color = M3PrimaryContainer,
    contentColor: Color = M3Primary,
    shape: Shape = RoundedCornerShape(16.dp),
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.shadow(3.dp, shape, ambientColor = Color.Black.copy(0.15f), spotColor = Color.Black.copy(0.15f)),
        containerColor = containerColor,
        contentColor = contentColor,
        shape = shape,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
    ) {
        Icon(Icons.Default.Add, contentDescription)
    }
}

@Composable
fun DCLoadingBox(message: String = "Loading...") {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = M3Primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, color = M3OnSurfaceVariant, fontSize = DeutschCraftTheme.fontSize.xl)
        }
    }
}

@Composable
fun DCEmptyState(icon: ImageVector, message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(64.dp), tint = M3EmptyStateIcon)
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, color = M3OnSurfaceVariant, fontSize = DeutschCraftTheme.fontSize.xl)
        }
    }
}

@Composable
fun DCTopAppBarTitle(title: String, subtitle: String = "") {
    Column {
        Text(title, fontSize = DeutschCraftTheme.fontSize.heading, fontWeight = FontWeight.Bold, color = Color.White)
        if (subtitle.isNotBlank()) {
            Text(subtitle, fontSize = DeutschCraftTheme.fontSize.md, color = Color.White.copy(0.8f), lineHeight = 16.sp)
        }
    }
}

@Composable
fun rememberSnackbarState(message: String?, onClear: () -> Unit): SnackbarHostState {
    val hostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            hostState.showSnackbar(it)
            onClear()
        }
    }
    return hostState
}

@Composable
fun M3SnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState) { data ->
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth()
                .clip(RoundedCornerShape(14.dp)).background(Color(0xFF1E293B))
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Text(data.visuals.message, color = Color.White, fontSize = DeutschCraftTheme.fontSize.base, fontWeight = FontWeight.Medium)
        }
    }
}
