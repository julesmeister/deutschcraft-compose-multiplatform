package data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

/**
 * Comprehensive writing analysis that covers all aspects of German learning.
 * Triggered automatically when a sentence ends (punctuation detected).
 */
@Serializable
data class WritingAnalysis(
    val id: Long = 0,
    val sentence: String,
    val timestamp: Instant,
    
    // Grammar analysis
    val grammarErrors: List<SentenceGrammarError> = emptyList(),
    
    // Vocabulary insights
    val vocabularyInsights: VocabularyInsights? = null,
    
    // Style and structure suggestions
    val styleSuggestions: List<StyleSuggestion> = emptyList(),
    
    // Learning opportunities (what user can learn from this sentence)
    val learningOpportunities: List<LearningOpportunity> = emptyList(),
    
    // Next word/concept suggestions
    val nextWordSuggestions: List<NextWordSuggestion> = emptyList(),
    
    // Sentence structure analysis
    val structureAnalysis: StructureAnalysis? = null,
    
    // CEFR level assessment for this sentence
    val sentenceLevel: CefrLevel = CefrLevel.A1,
    
    // Overall score
    val score: Double = 0.0
)

@Serializable
data class SentenceGrammarError(
    val id: Long = 0,
    val originalFragment: String,
    val correction: String,
    val errorType: ErrorType,
    val explanation: String,
    val positionStart: Int,
    val positionEnd: Int,
    val severity: ErrorSeverity,
    val canAutoFix: Boolean = false,
    val practiceExercise: PracticeExercise? = null
)

@Serializable
data class VocabularyInsights(
    val wordsUsed: List<WordUsage>,
    val advancedVocabulary: List<String>,
    val repeatedWords: List<RepeatedWord>,
    val suggestedAlternatives: List<WordAlternative>,
    val diversityScore: Double,
    val complexityScore: Double
)

@Serializable
data class WordUsage(
    val word: String,
    val baseForm: String,
    val partOfSpeech: PartOfSpeech,
    val difficulty: CefrLevel,
    val isLearned: Boolean,
    val timesEncountered: Int
)

@Serializable
data class RepeatedWord(
    val word: String,
    val count: Int,
    val suggestions: List<String>
)

@Serializable
data class WordAlternative(
    val original: String,
    val alternatives: List<AlternativeWithContext>
)

@Serializable
data class AlternativeWithContext(
    val word: String,
    val meaning: String,
    val context: String,
    val difficulty: CefrLevel
)

@Serializable
data class StyleSuggestion(
    val id: Long = 0,
    val type: StyleSuggestionType,
    val description: String,
    val currentVersion: String,
    val suggestedVersion: String,
    val explanation: String,
    val canApply: Boolean = true
)

@Serializable
enum class StyleSuggestionType {
    @SerialName("formality") FORMALITY,
    @SerialName("clarity") CLARITY,
    @SerialName("conciseness") CONCISENESS,
    @SerialName("variety") VARIETY,
    @SerialName("flow") FLOW,
    @SerialName("structure") STRUCTURE,
    @SerialName("transition") TRANSITION,
    @SerialName("emphasis") EMPHASIS
}

@Serializable
data class LearningOpportunity(
    val id: Long = 0,
    val concept: String,
    val conceptType: ConceptType,
    val currentAttempt: String,
    val explanation: String,
    val examples: List<String>,
    val relatedExercises: List<PracticeExercise>,
    val difficulty: CefrLevel
)

@Serializable
enum class ConceptType {
    @SerialName("grammar_pattern") GRAMMAR_PATTERN,
    @SerialName("vocabulary_theme") VOCABULARY_THEME,
    @SerialName("sentence_structure") SENTENCE_STRUCTURE,
    @SerialName("idiom") IDIOM,
    @SerialName("collocation") COLLOCATION,
    @SerialName("register") REGISTER,
    @SerialName("cultural_reference") CULTURAL_REFERENCE
}

@Serializable
data class NextWordSuggestion(
    val id: Long = 0,
    val suggestedWord: String,
    val wordType: WordSuggestionType,
    val context: String,
    val meaning: String,
    val grammaticalCase: String? = null,
    val exampleSentence: String,
    val difficulty: CefrLevel,
    val confidence: Double
)

@Serializable
enum class WordSuggestionType {
    @SerialName("connector") CONNECTOR,        // conjunctions, adverbs
    @SerialName("continuation") CONTINUATION,  // continue the thought
    @SerialName("contrast") CONTRAST,          // opposite/contrasting ideas
    @SerialName("example") EXAMPLE,            // for illustration
    @SerialName("conclusion") CONCLUSION,      // wrapping up
    @SerialName("emphasis") EMPHASIS,          // strengthening
    @SerialName("time") TIME,                  // temporal expressions
    @SerialName("cause") CAUSE                 // causation
}

@Serializable
data class StructureAnalysis(
    val sentenceType: SentenceType,
    val clauses: List<ClauseInfo>,
    val wordOrderCorrect: Boolean,
    val verbPositionCorrect: Boolean,
    val caseUsage: List<CaseUsageInfo>
)

@Serializable
enum class SentenceType {
    @SerialName("statement") STATEMENT,
    @SerialName("question") QUESTION,
    @SerialName("command") COMMAND,
    @SerialName("exclamation") EXCLAMATION,
    @SerialName("complex") COMPLEX,
    @SerialName("compound") COMPOUND
}

@Serializable
data class ClauseInfo(
    val clauseType: ClauseType,
    val mainVerb: String,
    val verbPosition: Int,
    val subject: String,
    val isSubordinate: Boolean
)

@Serializable
enum class ClauseType {
    @SerialName("main") MAIN,
    @SerialName("subordinate") SUBORDINATE,
    @SerialName("relative") RELATIVE,
    @SerialName("infinitive") INFINITIVE
}

@Serializable
data class CaseUsageInfo(
    val word: String,
    val detectedCase: GermanCase,
    val correctCase: GermanCase,
    val isCorrect: Boolean,
    val governingElement: String  // preposition, verb, etc.
)

@Serializable
enum class GermanCase {
    @SerialName("nominative") NOMINATIVE,
    @SerialName("accusative") ACCUSATIVE,
    @SerialName("dative") DATIVE,
    @SerialName("genitive") GENITIVE
}

@Serializable
enum class PartOfSpeech {
    @SerialName("noun") NOUN,
    @SerialName("verb") VERB,
    @SerialName("adjective") ADJECTIVE,
    @SerialName("adverb") ADVERB,
    @SerialName("preposition") PREPOSITION,
    @SerialName("conjunction") CONJUNCTION,
    @SerialName("article") ARTICLE,
    @SerialName("pronoun") PRONOUN,
    @SerialName("determiner") DETERMINER,
    @SerialName("particle") PARTICLE
}

@Serializable
data class PracticeExercise(
    val id: Long = 0,
    val type: ExerciseType,
    val question: String,
    val options: List<String>? = null,
    val correctAnswer: String,
    val explanation: String
)

@Serializable
enum class ExerciseType {
    @SerialName("multiple_choice") MULTIPLE_CHOICE,
    @SerialName("fill_blank") FILL_BLANK,
    @SerialName("translation") TRANSLATION,
    @SerialName("reorder") REORDER,
    @SerialName("match") MATCH
}

/**
 * User's writing patterns and habits tracked over time.
 */
@Serializable
data class WritingPattern(
    val id: Long = 0,
    val patternType: PatternType,
    val pattern: String,
    val frequency: Int,
    val firstSeen: Instant,
    val lastSeen: Instant,
    val examples: List<String>
)

@Serializable
enum class PatternType {
    @SerialName("favorite_phrase") FAVORITE_PHRASE,
    @SerialName("common_error") COMMON_ERROR,
    @SerialName("preferred_structure") PREFERRED_STRUCTURE,
    @SerialName("vocabulary_theme") VOCABULARY_THEME,
    @SerialName("transition_word") TRANSITION_WORD
}

/**
 * Quick suggestion that appears as a button in the writing assistant panel.
 */
@Serializable
data class QuickSuggestion(
    val id: String,
    val type: SuggestionCategory,
    val title: String,
    val description: String,
    val action: SuggestionAction,
    val preview: String? = null,
    val priority: Int = 0
)

@Serializable
enum class SuggestionCategory {
    @SerialName("grammar_fix") GRAMMAR_FIX,
    @SerialName("vocabulary") VOCABULARY,
    @SerialName("style") STYLE,
    @SerialName("structure") STRUCTURE,
    @SerialName("next_word") NEXT_WORD,
    @SerialName("learning") LEARNING,
    @SerialName("continuation") CONTINUATION
}

@Serializable
data class SuggestionAction(
    val type: ActionType,
    val value: String,
    val position: Int? = null
)

@Serializable
enum class ActionType {
    @SerialName("replace") REPLACE,
    @SerialName("insert") INSERT,
    @SerialName("append") APPEND,
    @SerialName("delete") DELETE,
    @SerialName("open_exercise") OPEN_EXERCISE,
    @SerialName("show_info") SHOW_INFO
}
