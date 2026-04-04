package ui.chat

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints

/**
 * Debug modifier that logs layout constraints at measurement time.
 * This helps identify where infinite constraints originate.
 */
fun Modifier.debugConstraints(tag: String): Modifier = this.then(
    object : LayoutModifier {
        override fun MeasureScope.measure(
            measurable: Measurable,
            constraints: Constraints
        ): MeasureResult {
            val hasInfiniteHeight = constraints.maxHeight == Constraints.Infinity
            val hasInfiniteWidth = constraints.maxWidth == Constraints.Infinity
            
            println("[DEBUG CONSTRAINTS] $tag")
            println("  minWidth=${constraints.minWidth}, maxWidth=${constraints.maxWidth}${if (hasInfiniteWidth) " (INFINITE!)" else ""}")
            println("  minHeight=${constraints.minHeight}, maxHeight=${constraints.maxHeight}${if (hasInfiniteHeight) " (INFINITE!)" else ""}")
            
            if (hasInfiniteHeight) {
                println("  ⚠️ INFINITE HEIGHT DETECTED - This will crash with LazyColumn!")
            }
            
            val placeable = measurable.measure(constraints)
            return layout(placeable.width, placeable.height) {
                placeable.placeRelative(0, 0)
            }
        }
    }
)
