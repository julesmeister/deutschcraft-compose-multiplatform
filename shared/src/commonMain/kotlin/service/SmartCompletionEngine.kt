package service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Smart phrase-level completion engine that predicts entire phrases
 * based on context, user patterns, and AI suggestions.
 */
class SmartCompletionEngine(
    private val ollamaService: OllamaService
) {
    private val json = Json { ignoreUnknownKeys = true }
    
    // User pattern learning
    private val userPatterns = mutableMapOf<String, MutableList<PhrasePattern>>()
    private val commonStarters = mutableMapOf<String, Int>()
    private var totalCompletions = 0
    
    /**
     * Get smart completion suggestions based on current text and context.
     * Returns ranked list of phrase completions with confidence scores.
     */
    suspend fun getCompletions(
        text: String,
        cursorPosition: Int,
        context: AnalysisContext
    ): List<PhraseCompletion> {
        val beforeCursor = text.substring(0, cursorPosition.coerceAtMost(text.length))
        val currentWord = beforeCursor.substringAfterLast(" ")
        
        // Build suggestions from multiple sources
        val suggestions = mutableListOf<PhraseCompletion>()
        
        // 1. User learned patterns (highest priority)
        suggestions.addAll(getUserPatterns(beforeCursor, context))
        
        // 2. Context-aware common phrases
        suggestions.addAll(getContextualPhrases(beforeCursor, context))
        
        // 3. AI-powered completions (if available)
        if (ollamaService.checkConnection()) {
            suggestions.addAll(getAiCompletions(beforeCursor, context))
        }
        
        // 4. Grammar-based completions (articles, prepositions, etc.)
        suggestions.addAll(getGrammarCompletions(currentWord))
        
        // Rank and deduplicate
        return suggestions
            .groupBy { it.phrase.lowercase() }
            .map { (_, variants) ->
                variants.maxByOrNull { it.confidence }!!
            }
            .sortedByDescending { it.confidence }
            .take(5)
    }
    
    /**
     * Stream completions as user types (real-time)."""
    fun streamCompletions(
        text: String,
        cursorPosition: Int,
        context: AnalysisContext
    ): Flow<List<PhraseCompletion>> = flow {
        val beforeCursor = text.substring(0, cursorPosition.coerceAtMost(text.length))
        
        // Quick local completions first
        val localCompletions = getContextualPhrases(beforeCursor, context) + 
                              getGrammarCompletions(beforeCursor.substringAfterLast(" "))
        emit(localCompletions.sortedByDescending { it.confidence }.take(3))
        
        // Then AI completions if connected
        if (ollamaService.checkConnection() && beforeCursor.length > 15) {
            try {
                val aiCompletions = getAiCompletions(beforeCursor, context)
                emit((localCompletions + aiCompletions).sortedByDescending { it.confidence }.take(5))
            } catch (_: Exception) { }
        }
    }
    
    /**
     * Learn from user's accepted completion to improve future suggestions.
     */
    fun learnFromAcceptance(
        prefix: String,
        acceptedPhrase: String,
        context: AnalysisContext
    ) {
        totalCompletions++
        
        // Extract pattern key (first 1-2 words of prefix)
        val patternKey = prefix.trim().split(" ").take(2).joinToString(" ").lowercase()
        
        // Store or update pattern
        val patterns = userPatterns.getOrPut(patternKey) { mutableListOf() }
        val existing = patterns.find { it.phrase.equals(acceptedPhrase, ignoreCase = true) }
        
        if (existing != null) {
            existing.frequency++
            existing.lastUsed = kotlinx.datetime.Clock.System.now()
        } else {
            patterns.add(PhrasePattern(
                phrase = acceptedPhrase,
                context = context.documentTopic,
                frequency = 1,
                firstUsed = kotlinx.datetime.Clock.System.now()
            ))
        }
        
        // Track common starters
        val starter = acceptedPhrase.split(" ").first().lowercase()
        commonStarters[starter] = commonStarters.getOrDefault(starter, 0) + 1
    }
    
    /**
     * Get phrase completions from learned user patterns.
     */
    private fun getUserPatterns(
        prefix: String,
        context: AnalysisContext
    ): List<PhraseCompletion> {
        val key = prefix.trim().split(" ").take(2).joinToString(" ").lowercase()
        val patterns = userPatterns[key] ?: return emptyList()
        
        return patterns
            .sortedByDescending { it.frequency }
            .take(3)
            .map { pattern ->
                PhraseCompletion(
                    phrase = pattern.phrase,
                    displayText = pattern.phrase,
                    confidence = calculateUserPatternConfidence(pattern),
                    source = CompletionSource.USER_PATTERN,
                    context = pattern.context,
                    insertType = InsertType.REPLACE_WORD
                )
            }
    }
    
    /**
     * Get context-aware common German phrases.
     */
    private fun getContextualPhrases(
        prefix: String,
        context: AnalysisContext
    ): List<PhraseCompletion> {
        val lastWord = prefix.substringAfterLast(" ").lowercase()
        val completions = mutableListOf<PhraseCompletion>()
        
        // Context-based connectors
        when {
            context.previousSentences.isNotEmpty() -> {
                val prevContext = context.previousSentences.last().lowercase()
                
                when {
                    prevContext.contains("weil") -> {
                        completions.add(PhraseCompletion("ist, dass", "ist, dass", 0.85, CompletionSource.GRAMMAR))
                        completions.add(PhraseCompletion("gilt auch für", "gilt auch für", 0.70, CompletionSource.CONTEXT))
                    }
                    prevContext.contains("erste") || prevContext.contains("zuerst") -> {
                        completions.add(PhraseCompletion("Dann", "Dann", 0.90, CompletionSource.CONTEXT))
                        completions.add(PhraseCompletion("Danach", "Danach", 0.85, CompletionSource.CONTEXT))
                    }
                    prevContext.contains("finde") || prevContext.contains("denke") -> {
                        completions.add(PhraseCompletion("weil es", "weil es", 0.75, CompletionSource.CONTEXT))
                        completions.add(PhraseCompletion("Außerdem", "Außerdem", 0.70, CompletionSource.CONTEXT))
                    }
                }
            }
        }
        
        // Common phrase starters
        completions.addAll(getCommonCompletions(lastWord, context))
        
        return completions
    }
    
    /**
     * Get AI-powered phrase completions.
     */
    private suspend fun getAiCompletions(
        prefix: String,
        context: AnalysisContext
    ): List<PhraseCompletion> {
        val prompt = buildString {
            appendLine("Continue this German text naturally. Provide 3 different continuations.")
            appendLine("Text so far: \"$prefix\"")
            
            if (context.previousSentences.isNotEmpty()) {
                appendLine("Context: ${context.previousSentences.last()}")
            }
            
            appendLine()
            appendLine("Return exactly 3 completions as JSON array:")
            appendLine("[{\"phrase\": \"continuation text\", \"confidence\": 0.9}, ...]")
            appendLine("Each should be 2-5 words that flow naturally.")
        }
        
        return try {
            val response = ollamaService.generateRawResponse(
                prompt = prompt,
                temperature = 0.6,
                maxTokens = 150
            )
            
            parseAiCompletions(response)
        } catch (_: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get grammar-based completions (articles, prepositions, common collocations).
     */
    private fun getGrammarCompletions(currentWord: String): List<PhraseCompletion> {
        val completions = mutableListOf<PhraseCompletion>()
        
        // Article completions
        when (currentWord.lowercase()) {
            "der", "die", "das", "den", "dem" -> {
                completions.add(PhraseCompletion("[noun/adj] ist", "... ist", 0.80, CompletionSource.GRAMMAR))
                completions.add(PhraseCompletion("[noun] und", "... und", 0.75, CompletionSource.GRAMMAR))
            }
            "ich" -> {
                completions.add(PhraseCompletion("habe", "habe", 0.85, CompletionSource.GRAMMAR))
                completions.add(PhraseCompletion("bin", "bin", 0.80, CompletionSource.GRAMMAR))
                completions.add(PhraseCompletion("möchte", "möchte", 0.75, CompletionSource.GRAMMAR))
                completions.add(PhraseCompletion("denke", "denke", 0.70, CompletionSource.GRAMMAR))
            }
            "es" -> {
                completions.add(PhraseCompletion("ist", "ist", 0.90, CompletionSource.GRAMMAR))
                completions.add(PhraseCompletion("gibt", "gibt", 0.80, CompletionSource.GRAMMAR))
                completions.add(PhraseCompletion("war", "war", 0.70, CompletionSource.GRAMMAR))
            }
            "zum" -> {
                completions.add(PhraseCompletion("Beispiel", "Beispiel", 0.95, CompletionSource.GRAMMAR))
            }
            "außerdem" -> {
                completions.add(PhraseCompletion("ist es", "ist es", 0.80, CompletionSource.GRAMMAR))
                completions.add(PhraseCompletion("gibt es", "gibt es", 0.75, CompletionSource.GRAMMAR))
            }
        }
        
        // Preposition + case hints
        if (currentWord in PREPOSITIONS_DATIVE) {
            completions.add(PhraseCompletion("[dative form]", "dem/der/den...", 0.70, CompletionSource.GRAMMAR_HINT))
        }
        if (currentWord in PREPOSITIONS_ACCUSATIVE) {
            completions.add(PhraseCompletion("[accusative form]", "den/die/das...", 0.70, CompletionSource.GRAMMAR_HINT))
        }
        
        return completions
    }
    
    private fun getCommonCompletions(lastWord: String, context: AnalysisContext): List<PhraseCompletion> {
        val completions = mutableListOf<PhraseCompletion>()
        
        // Context-independent common continuations
        val common: Map<String, List<Pair<String, Double>>> = mapOf(
            "und" to listOf("dann" to 0.70, "außerdem" to 0.65, "dadurch" to 0.60),
            "aber" to listOf("ich" to 0.75, "es" to 0.70, "dann" to 0.65),
            "weil" to listOf("ich" to 0.80, "es" to 0.75, "man" to 0.70),
            "deshalb" to listOf("habe ich" to 0.75, "ist es" to 0.70),
            "trotzdem" to listOf("ist es" to 0.70, "kann man" to 0.65),
            "obwohl" to listOf("es" to 0.80, "ich" to 0.75),
            "damit" to listOf("man" to 0.75, "ich" to 0.70)
        )
        
        common[lastWord]?.forEach { (phrase, conf) ->
            completions.add(PhraseCompletion(phrase, phrase, conf, CompletionSource.COMMON))
        }
        
        return completions
    }
    
    private fun parseAiCompletions(response: String): List<PhraseCompletion> {
        return try {
            val jsonArray = json.decodeFromString<List<AiCompletion>>(response.trim())
            jsonArray.map { 
                PhraseCompletion(
                    phrase = it.phrase,
                    displayText = it.phrase,
                    confidence = it.confidence.coerceIn(0.0, 1.0) * 0.85, // Slightly reduce AI confidence
                    source = CompletionSource.AI,
                    insertType = InsertType.APPEND
                )
            }
        } catch (_: Exception) {
            // Fallback: parse lines
            response.lines()
                .filter { it.isNotBlank() && !it.startsWith("[") && !it.startsWith("]") }
                .map { it.trim('"', ',', ' ') }
                .filter { it.isNotBlank() }
                .mapIndexed { index, phrase ->
                    PhraseCompletion(
                        phrase = phrase,
                        displayText = phrase,
                        confidence = 0.7 - (index * 0.1),
                        source = CompletionSource.AI
                    )
                }
                .take(3)
        }
    }
    
    private fun calculateUserPatternConfidence(pattern: PhrasePattern): Double {
        val baseConfidence = 0.6
        val frequencyBoost = (pattern.frequency.toDouble() / totalCompletions.coerceAtLeast(1)) * 0.3
        return (baseConfidence + frequencyBoost).coerceAtMost(0.95)
    }
    
    /**
     * Get statistics about learned patterns.
     */
    fun getLearningStats(): LearningStats {
        val totalPatterns = userPatterns.values.sumOf { it.size }
        val totalFrequency = userPatterns.values.sumOf { patterns -> patterns.sumOf { it.frequency } }
        val topPatterns = userPatterns.values
            .flatten()
            .sortedByDescending { it.frequency }
            .take(10)
            .map { it.phrase }
        
        return LearningStats(
            totalPatterns = totalPatterns,
            totalUsages = totalFrequency,
            topPatterns = topPatterns,
            commonStarters = commonStarters.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { it.key }
        )
    }
    
    companion object {
        private val PREPOSITIONS_DATIVE = setOf("mit", "bei", "nach", "seit", "von", "zu", "aus", "außer")
        private val PREPOSITIONS_ACCUSATIVE = setOf("für", "durch", "über", "um", "ohne", "gegen")
    }
}

// Data classes

@Serializable
data class PhraseCompletion(
    val phrase: String,
    val displayText: String,
    val confidence: Double,
    val source: CompletionSource,
    val context: String? = null,
    val insertType: InsertType = InsertType.APPEND,
    val grammarHint: String? = null
)

enum class CompletionSource {
    USER_PATTERN,    // Learned from user's writing
    AI,              // Generated by Ollama
    CONTEXT,         // Based on previous sentences
    GRAMMAR,         // Grammar rule-based
    GRAMMAR_HINT,    // Case hint only
    COMMON           // Common phrase
}

enum class InsertType {
    APPEND,          // Add to end
    REPLACE_WORD,    // Replace current word
    INSERT_CURSOR    // Insert at cursor
}

data class PhrasePattern(
    val phrase: String,
    val context: String?,
    var frequency: Int = 1,
    val firstUsed: kotlinx.datetime.Instant,
    var lastUsed: kotlinx.datetime.Instant = firstUsed
)

@Serializable
data class AiCompletion(
    val phrase: String,
    val confidence: Double
)

data class LearningStats(
    val totalPatterns: Int,
    val totalUsages: Int,
    val topPatterns: List<String>,
    val commonStarters: List<String>
)
