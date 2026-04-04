package data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import data.db.converter.InstantConverter
import data.model.EntryType
import kotlinx.datetime.Instant

@Entity(tableName = "study_entries")
@TypeConverters(InstantConverter::class)
data class StudyEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val type: String,
    val timestamp: Instant,
    val topic: String? = null,
    val correctedContent: String? = null,
    val analysisJson: String? = null
)

@Entity(tableName = "grammar_mistakes")
@TypeConverters(InstantConverter::class)
data class GrammarMistakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalText: String,
    val correction: String,
    val errorType: String,
    val explanation: String,
    val timestamp: Instant,
    val studyEntryId: Long,
    val recurrenceCount: Int = 1
)

@Entity(tableName = "study_topics")
@TypeConverters(InstantConverter::class)
data class StudyTopicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val firstEncountered: Instant,
    val lastEncountered: Instant,
    val frequency: Int = 1
)

@Entity(tableName = "strength_records")
@TypeConverters(InstantConverter::class)
data class StrengthRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val aspect: String,
    val description: String,
    val timestamp: Instant,
    val confidenceScore: Double = 1.0
)

@Entity(tableName = "vocabulary_items")
@TypeConverters(InstantConverter::class)
data class VocabularyItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val context: String,
    val translation: String? = null,
    val difficulty: String,
    val firstSeen: Instant,
    val encounterCount: Int = 1,
    val isLearned: Boolean = false
)

@Entity(tableName = "chat_sessions")
@TypeConverters(InstantConverter::class)
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String? = null,
    val category: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)

@Entity(tableName = "chat_messages")
@TypeConverters(InstantConverter::class)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val content: String,
    val isUser: Boolean,
    val timestamp: Instant
)
