package ui.components.m3

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import theme.DeutschCraftTheme
import theme.Gray800

@Composable
fun CurrencyDisplay(
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
    color: Color = Gray800,
    prefix: String = "\u20B1", // ₱
) {
    Text(
        text = "$prefix${formatCurrency(amount)}",
        style = style,
        color = color,
        modifier = modifier,
    )
}

fun formatCurrencyDouble(amount: Double): String {
    val formatted = amount.toString()
        .replace(Regex("\\.0+$"), "")
        .replace(Regex("(?<=\\d)(?=(\\d{3})+$)"), ",")
    return formatted
}
