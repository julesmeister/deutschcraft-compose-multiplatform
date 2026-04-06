package service

import kotlinx.serialization.Serializable

/**
 * User preferences for the writing assistant.
 * Controls formality, analysis depth, auto-fix behavior, and dialect preferences.
 */
@Serializable
data class UserPreferences(
    val formalityPreference: FormalityLevel? = null,
    val analysisDepth: AnalysisDepth = AnalysisDepth.BALANCED,
    val autoApplyHighConfidence: Boolean = false,
    val highConfidenceThreshold: Double = 0.85,
    val dialectPreference: GermanDialect = GermanDialect.STANDARD,
    val ignoredWords: Set<String> = emptySet(),
    val preferredCorrections: Map<String, String> = emptyMap(),
    val enabledSuggestionTypes: Set<SuggestionType> = SuggestionType.ALL,
    val debounceDelayMs: Long = 500,
    val contextMemorySize: Int = 5
) {
    companion object {
        val DEFAULT = UserPreferences()
    }
}

enum class FormalityLevel {
    INFORMAL,    // Casual German (du, slang OK)
    NEUTRAL,     // Mixed
    FORMAL       // Formal German (Sie, professional)
}

enum class AnalysisDepth {
    FAST,        // Local rules only, no AI
    BALANCED,    // Local + AI for complex issues
    THOROUGH     // Full AI analysis always
}

enum class GermanDialect {
    STANDARD,    // Hochdeutsch
    AUSTRIAN,    // Österreichisches Deutsch
    SWISS        // Schweizerdeutsch
}

enum class SuggestionType {
    GRAMMAR_FIX,
    VOCABULARY,
    STYLE,
    NEXT_WORD,
    LEARNING,
    CONTINUATION;
    
    companion object {
        val ALL = entries.toSet()
    }
}

/**
 * Stores user preferences and provides persistence interface.
 */
class PreferencesStore {
    private var preferences: UserPreferences = UserPreferences.DEFAULT
    private val listeners = mutableListOf<(UserPreferences) -> Unit>()
    
    fun getPreferences(): UserPreferences = preferences
    
    fun updatePreferences(update: (UserPreferences) -> UserPreferences) {
        preferences = update(preferences)
        listeners.forEach { it(preferences) }
    }
    
    fun addListener(listener: (UserPreferences) -> Unit) {
        listeners.add(listener)
    }
    
    fun removeListener(listener: (UserPreferences) -> Unit) {
        listeners.remove(listener)
    }
    
    // TODO: Add actual persistence (DataStore, SQLDelight, etc.)
    fun save() {
        // Persist to storage
    }
    
    fun load() {
        // Load from storage
    }
}
