package data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

/**
 * Represents a study session or writing entry in the database.
 */
@Serializable
data class StudyEntry(
    val id: Long = 0,
    val content: String,
    val type: EntryType,
    val timestamp: Instant,
    val topic: String? = null,
    val correctedContent: String? = null,
    val analysisJson: String? = null
)

@Serializable
enum class EntryType {
    @SerialName("free_writing") FREE_WRITING,
    @SerialName("grammar_exercise") GRAMMAR_EXERCISE,
    @SerialName("conversation") CONVERSATION,
    @SerialName("translation") TRANSLATION
}

/**
 * Topics and themes the user has written about.
 */
@Serializable
data class StudyTopic(
    val id: Long = 0,
    val name: String,
    val category: TopicCategory,
    val firstEncountered: Instant,
    val lastEncountered: Instant,
    val frequency: Int = 1
)

@Serializable
enum class TopicCategory {
    @SerialName("daily_life") DAILY_LIFE,
    @SerialName("work") WORK,
    @SerialName("travel") TRAVEL,
    @SerialName("culture") CULTURE,
    @SerialName("opinion") OPINION,
    @SerialName("storytelling") STORYTELLING,
    @SerialName("academic") ACADEMIC,
    @SerialName("other") OTHER
}

/**
 * Grammar errors tracked over time for pattern detection.
 */
@Serializable
data class GrammarMistake(
    val id: Long = 0,
    val originalText: String,
    val correction: String,
    val errorType: ErrorType,
    val explanation: String,
    val timestamp: Instant,
    val studyEntryId: Long,
    val recurrenceCount: Int = 1
)

/**
 * User's strengths tracked over time.
 */
@Serializable
data class StrengthRecord(
    val id: Long = 0,
    val aspect: String,
    val description: String,
    val timestamp: Instant,
    val confidenceScore: Double = 1.0
)

/**
 * Learning goals and progress.
 */
@Serializable
data class LearningGoal(
    val id: Long = 0,
    val title: String,
    val description: String,
    val targetDate: Instant? = null,
    val isCompleted: Boolean = false,
    val createdAt: Instant
)

/**
 * Vocabulary encountered during writing.
 */
@Serializable
data class VocabularyItem(
    val id: Long = 0,
    val word: String,
    val context: String,
    val translation: String? = null,
    val difficulty: CefrLevel,
    val firstSeen: Instant,
    val encounterCount: Int = 1,
    val isLearned: Boolean = false,
    val isUserMarkedDifficult: Boolean = false,
    val practiceCount: Int = 0,
    val lastPracticed: Instant? = null
)
