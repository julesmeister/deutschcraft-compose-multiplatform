package ui

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal fun formatTimestamp(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    return if (localDateTime.date == now.date) {
        "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
    } else {
        "${localDateTime.monthNumber}/${localDateTime.dayOfMonth}"
    }
}
