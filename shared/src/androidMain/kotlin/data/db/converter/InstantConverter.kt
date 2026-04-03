package data.db.converter

import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant

class InstantConverter {
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }

    @TypeConverter
    fun toInstant(timestamp: Long?): Instant? {
        return timestamp?.let { Instant.fromEpochMilliseconds(it) }
    }
}
