package data.db

/**
 * Standardized column names for SQLDelight tables.
 * Use these to avoid typos and ensure consistency.
 */
object DbConstants {
    // Common columns
    const val COL_ID = "id"
    const val COL_TIMESTAMP = "timestamp"
    const val COL_NAME = "name"
    
    // StudyEntry columns
    const val COL_CONTENT = "content"
    const val COL_TYPE = "type"
    const val COL_TOPIC = "topic"
    const val COL_CORRECTED_CONTENT = "corrected_content"
    const val COL_ANALYSIS_JSON = "analysis_json"
    
    // StudyTopic columns
    const val COL_CATEGORY = "category"
    const val COL_FIRST_ENCOUNTERED = "first_encountered"
    const val COL_LAST_ENCOUNTERED = "last_encountered"
    const val COL_FREQUENCY = "frequency"
    
    // GrammarMistake columns
    const val COL_ORIGINAL_TEXT = "original_text"
    const val COL_CORRECTION = "correction"
    const val COL_ERROR_TYPE = "error_type"
    const val COL_EXPLANATION = "explanation"
    const val COL_STUDY_ENTRY_ID = "study_entry_id"
    const val COL_RECURRENCE_COUNT = "recurrence_count"
    
    // StrengthRecord columns
    const val COL_ASPECT = "aspect"
    const val COL_DESCRIPTION = "description"
    const val COL_CONFIDENCE_SCORE = "confidence_score"
    
    // Vocabulary columns
    const val COL_WORD = "word"
    const val COL_CONTEXT = "context"
    const val COL_TRANSLATION = "translation"
    const val COL_DIFFICULTY = "difficulty"
    const val COL_FIRST_SEEN = "first_seen"
    const val COL_ENCOUNTER_COUNT = "encounter_count"
    const val COL_IS_LEARNED = "is_learned"
    
    // ChatSession columns
    const val COL_TITLE = "title"
    const val COL_CREATED_AT = "created_at"
    const val COL_UPDATED_AT = "updated_at"
    
    // ChatMessage columns
    const val COL_SESSION_ID = "session_id"
    const val COL_IS_USER = "is_user"
    const val COL_MESSAGE_CONTENT = "content"
}

/**
 * Table names for reference.
 */
object TableNames {
    const val STUDY_ENTRY = "studyEntry"
    const val STUDY_TOPIC = "studyTopic"
    const val GRAMMAR_MISTAKE = "grammarMistake"
    const val STRENGTH_RECORD = "strengthRecord"
    const val VOCABULARY = "vocabulary"
    const val CHAT_SESSION = "chatSession"
    const val CHAT_MESSAGE = "chatMessage"
}

/**
 * Standard error types stored as strings in database.
 * These match the ErrorType enum names.
 */
enum class DbErrorType(val dbValue: String) {
    GRAMMAR("GRAMMAR"),
    SPELLING("SPELLING"),
    PUNCTUATION("PUNCTUATION"),
    WORD_ORDER("WORD_ORDER"),
    VERB_CONJUGATION("VERB_CONJUGATION"),
    CASE("CASE"),
    GENDER("GENDER"),
    TENSE("TENSE"),
    ARTICLE("ARTICLE"),
    PREPOSITION("PREPOSITION"),
    STYLE("STYLE"),
    OTHER("OTHER");
    
    companion object {
        fun fromDbValue(value: String): DbErrorType = 
            entries.find { it.dbValue == value } ?: OTHER
    }
}

/**
 * Entry types stored as strings in database.
 */
enum class DbEntryType(val dbValue: String) {
    FREE_WRITING("FREE_WRITING"),
    GRAMMAR_EXERCISE("GRAMMAR_EXERCISE"),
    CONVERSATION("CONVERSATION"),
    TRANSLATION("TRANSLATION");
    
    companion object {
        fun fromDbValue(value: String): DbEntryType =
            entries.find { it.dbValue == value } ?: FREE_WRITING
    }
}

/**
 * Topic categories stored as strings in database.
 */
enum class DbTopicCategory(val dbValue: String) {
    DAILY_LIFE("DAILY_LIFE"),
    WORK("WORK"),
    TRAVEL("TRAVEL"),
    CULTURE("CULTURE"),
    OPINION("OPINION"),
    STORYTELLING("STORYTELLING"),
    ACADEMIC("ACADEMIC"),
    OTHER("OTHER");
    
    companion object {
        fun fromDbValue(value: String): DbTopicCategory =
            entries.find { it.dbValue == value } ?: OTHER
    }
}

/**
 * CEFR levels stored as strings in database.
 */
enum class DbCefrLevel(val dbValue: String) {
    A1("A1"),
    A2("A2"),
    B1("B1"),
    B2("B2"),
    C1("C1"),
    C2("C2");
    
    companion object {
        fun fromDbValue(value: String): DbCefrLevel =
            entries.find { it.dbValue == value } ?: A1
    }
}
