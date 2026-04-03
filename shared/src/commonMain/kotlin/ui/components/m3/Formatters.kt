package ui.components.m3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char

// Currency formatter (simple version for multiplatform)
fun formatCurrency(amount: Number, currency: String = ""): String {
    val formatted = amount.toDouble().toString()
        .replace("\\.0+$".toRegex(), "")
        .replace("(\\d)(?=(\\d{3})+$)".toRegex(), "$1,")
    return if (currency.isNotEmpty()) "$currency$formatted" else formatted
}

@Composable
fun rememberCurrencyFormatter(): (Number) -> String = remember { { amount -> formatCurrency(amount) } }

// Date formatters using kotlinx-datetime for multiplatform
class DateFormatters {
    companion object {
        /** "h:mm a" – e.g. "2:30 PM" */
        fun timeFormat(): String = "h:mm a"
        
        /** "d" – day of month, e.g. "9" */
        fun dayOfMonth(date: LocalDate): String = date.dayOfMonth.toString()
        
        /** "MMM" – short month, e.g. "Mar" */
        fun shortMonth(date: LocalDate): String = date.month.name.take(3)
        
        /** "MMM d, yyyy" – e.g. "Mar 9, 2026" */
        fun mediumDate(date: LocalDate): String = 
            "${shortMonth(date)} ${date.dayOfMonth}, ${date.year}"
        
        /** "MMMM d, yyyy" – e.g. "March 9, 2026" */
        fun fullDate(date: LocalDate): String = 
            "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}, ${date.year}"
        
        /** "EEEE" – full day name, e.g. "Monday" */
        fun dayName(date: LocalDate): String = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        
        /** "MMMM yyyy" – e.g. "March 2026" */
        fun monthYear(date: LocalDate): String = 
            "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}"
        
        /** "MMM d" – e.g. "Mar 9" */
        fun monthDay(date: LocalDate): String = "${shortMonth(date)} ${date.dayOfMonth}"
    }
}

// Duration formatters
fun formatDurationDaysHours(days: Int, hours: Int): String {
    return when {
        days > 0 -> "$days day${if (days != 1) "s" else ""}"
        hours > 0 -> "$hours hour${if (hours != 1) "s" else ""}"
        else -> "Just now"
    }
}

fun formatDurationMinutes(minutes: Int): String {
    return when {
        minutes >= 60 -> {
            val hours = minutes / 60
            val mins = minutes % 60
            "$hours hr${if (hours != 1) "s" else ""}${if (mins > 0) " $mins min" else ""}"
        }
        minutes > 0 -> "$minutes min${if (minutes != 1) "s" else ""}"
        else -> "Just now"
    }
}
