package service

import data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.datetime.Clock

/**
 * AI-powered analysis engine that uses Ollama with fallback to local rules.
 * Returns structured WritingAnalysis with confidence scores.
 */
class AiAnalysisEngine(private val ollamaService: OllamaService) {

    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }

    private val localEngine = TextAnalysisEngine()

    /**
     * Analyze text with AI, fallback to local rules if Ollama unavailable.
     * Returns analysis with confidence score (0.0-1.0).
     */
    suspend fun analyzeWithConfidence(
        sentence: String,
        context: AnalysisContext = AnalysisContext()
    ): AnalysisResult {
        // Try AI first if available
        if (ollamaService.checkConnection()) {
            try {
                val aiResult = analyzeWithAi(sentence, context)
                if (aiResult.confidence > 0.6) {
                    return aiResult
                }
                // Low confidence - merge with local analysis
                return mergeWithLocal(aiResult, sentence, context)
            } catch (e: Exception) {
                // AI failed, use local
            }
        }
        
        // Fallback to local rules
        val localAnalysis = localEngine.performLocalAnalysis(sentence)
        return AnalysisResult(
            analysis = localAnalysis,
            confidence = 0.5, // Local rules are moderate confidence
            source = AnalysisSource.LOCAL_RULES
        )
    }

    private suspend fun analyzeWithAi(
        sentence: String,
        context: AnalysisContext
    ): AnalysisResult {
        val prompt = buildString {
            appendLine("Analyze this German text and return JSON. Be thorough but concise.")
            appendLine()
            appendLine("Text to analyze: \"$sentence\"")
            
            if (context.previousSentences.isNotEmpty()) {
                appendLine()
                appendLine("Context (previous sentences):")
                context.previousSentences.takeLast(3).forEach { 
                    appendLine("- $it") 
                }
            }
            
            if (context.userPreferences.formalityPreference != null) {
                appendLine()
                appendLine("User prefers: ${context.userPreferences.formalityPreference} German")
            }
            
            appendLine()
            appendLine("Return JSON with this structure:")
            appendLine("""
            {
              "grammarErrors": [
                {
                  "originalFragment": "text with error",
                  "correction": "corrected text",
                  "errorType": "VERB_CONJUGATION|WORD_ORDER|CASE|GENDER|SPELLING|OTHER",
                  "explanation": "brief explanation in English",
                  "severity": "LOW|MEDIUM|HIGH|CRITICAL",
                  "canAutoFix": true|false
                }
              ],
              "vocabularyInsights": {
                "advancedWords": ["word1", "word2"],
                "repeatedWords": ["word"],
                "suggestedAlternatives": [
                  {
                    "original": "common word",
                    "alternatives": ["better word 1", "better word 2"],
                    "context": "when to use this"
                  }
                ]
              },
              "styleSuggestions": [
                {
                  "type": "CLARITY|FLOW|EMPHASIS|CONCISION",
                  "currentVersion": "current text",
                  "suggestedVersion": "improved version",
                  "explanation": "why this improves the text"
                }
              ],
              "learningOpportunities": [
                {
                  "concept": "Grammar concept name",
                  "explanation": "Brief explanation",
                  "examples": ["example 1", "example 2"],
                  "difficulty": "A1|A2|B1|B2|C1"
                }
              ],
              "nextWordSuggestions": [
                {
                  "suggestedWord": "word",
                  "context": "why this fits",
                  "confidence": 0.85
                }
              ],
              "sentenceLevel": "A1|A2|B1|B2|C1",
              "score": 85,
              "confidence": 0.9
            }
            """.trimIndent())
        }

        val response = ollamaService.generateRawResponse(
            prompt = prompt,
            systemPrompt = "You are a German language expert. Analyze text and return valid JSON only. No markdown, no explanations outside JSON.",
            temperature = 0.3,
            maxTokens = 800
        )

        return try {
            val aiResponse = json.decodeFromString<AiAnalysisResponse>(response)
            AnalysisResult(
                analysis = aiResponse.toWritingAnalysis(sentence),
                confidence = aiResponse.confidence,
                source = AnalysisSource.AI
            )
        } catch (e: Exception) {
            // JSON parsing failed, fall back
            AnalysisResult(
                analysis = localEngine.performLocalAnalysis(sentence),
                confidence = 0.3,
                source = AnalysisSource.LOCAL_FALLBACK
            )
        }
    }

    private fun mergeWithLocal(
        aiResult: AnalysisResult,
        sentence: String,
        context: AnalysisContext
    ): AnalysisResult {
        val localAnalysis = localEngine.performLocalAnalysis(sentence)
        
        // Merge: use AI for high-confidence items, supplement with local rules
        val mergedGrammar = (aiResult.analysis.grammarErrors + localAnalysis.grammarErrors)
            .distinctBy { it.originalFragment }
            .take(5)
        
        val mergedVocab = aiResult.analysis.vocabularyInsights ?: localAnalysis.vocabularyInsights
        val mergedStyle = (aiResult.analysis.styleSuggestions + localAnalysis.styleSuggestions)
            .distinctBy { it.currentVersion }
            .take(3)
        
        return AnalysisResult(
            analysis = WritingAnalysis(
                sentence = sentence,
                timestamp = Clock.System.now(),
                grammarErrors = mergedGrammar,
                vocabularyInsights = mergedVocab,
                styleSuggestions = mergedStyle,
                learningOpportunities = aiResult.analysis.learningOpportunities.take(3),
                nextWordSuggestions = aiResult.analysis.nextWordSuggestions.take(3),
                structureAnalysis = localAnalysis.structureAnalysis,
                sentenceLevel = aiResult.analysis.sentenceLevel,
                score = (aiResult.analysis.score + localAnalysis.score) / 2
            ),
            confidence = aiResult.confidence * 0.9, // Slightly reduce for merged result
            source = AnalysisSource.HYBRID
        )
    }

    fun generateStreamingSuggestions(
        text: String,
        analysis: WritingAnalysis
    ): Flow<SuggestionStreamEvent> = flow {
        if (!ollamaService.checkConnection()) {
            emit(SuggestionStreamEvent.Error("Ollama not available"))
            return@flow
        }

        val prompt = """
            Based on this German text and its analysis, provide 3 specific, actionable suggestions.
            Format each suggestion as a JSON object.
            
            Text: "$text"
            Analysis: ${analysis.grammarErrors.size} grammar errors, score: ${analysis.score}
            
            Stream 3 suggestions, one per line, as JSON:
            {"type": "grammar_fix", "title": "Fix verb conjugation", "description": "...", "priority": 90}
            {"type": "vocabulary", "title": "Use 'hervorragend' instead", "description": "...", "priority": 70}
            {"type": "style", "title": "Make sentence clearer", "description": "...", "priority": 60}
        """.trimIndent()

        try {
            val response = ollamaService.generateRawResponse(
                prompt = prompt,
                temperature = 0.4,
                maxTokens = 400
            )
            
            response.lines()
                .filter { it.trim().startsWith("{") }
                .forEach { line ->
                    try {
                        val suggestion = json.decodeFromString<StreamingSuggestion>(line)
                        emit(SuggestionStreamEvent.Suggestion(suggestion))
                    } catch (_: Exception) { }
                }
            
            emit(SuggestionStreamEvent.Complete)
        } catch (e: Exception) {
            emit(SuggestionStreamEvent.Error(e.message ?: "Unknown error"))
        }
    }
}

data class AnalysisResult(
    val analysis: WritingAnalysis,
    val confidence: Double,
    val source: AnalysisSource
)

enum class AnalysisSource {
    AI,
    LOCAL_RULES,
    LOCAL_FALLBACK,
    HYBRID
}

data class AnalysisContext(
    val previousSentences: List<String> = emptyList(),
    val userPreferences: UserPreferences = UserPreferences(),
    val documentTopic: String? = null
)

@Serializable
private data class AiAnalysisResponse(
    val grammarErrors: List<AiGrammarError> = emptyList(),
    val vocabularyInsights: AiVocabularyInsights? = null,
    val styleSuggestions: List<AiStyleSuggestion> = emptyList(),
    val learningOpportunities: List<AiLearningOpportunity> = emptyList(),
    val nextWordSuggestions: List<AiNextWordSuggestion> = emptyList(),
    val sentenceLevel: String = "A2",
    val score: Double = 70.0,
    val confidence: Double = 0.7
) {
    fun toWritingAnalysis(sentence: String): WritingAnalysis {
        return WritingAnalysis(
            sentence = sentence,
            timestamp = Clock.System.now(),
            grammarErrors = grammarErrors.map { it.toSentenceGrammarError() },
            vocabularyInsights = vocabularyInsights?.toVocabularyInsights(),
            styleSuggestions = styleSuggestions.map { it.toStyleSuggestion() },
            learningOpportunities = learningOpportunities.map { it.toLearningOpportunity() },
            nextWordSuggestions = nextWordSuggestions.map { it.toNextWordSuggestion() },
            structureAnalysis = null, // AI doesn't provide this yet
            sentenceLevel = parseLevel(sentenceLevel),
            score = score
        )
    }
    
    private fun parseLevel(level: String): CefrLevel {
        return try {
            CefrLevel.valueOf(level.uppercase())
        } catch (_: Exception) {
            CefrLevel.A2
        }
    }
}

@Serializable
data class AiGrammarError(
    @SerialName("originalFragment") val originalFragment: String,
    @SerialName("correction") val correction: String,
    @SerialName("errorType") val errorType: String,
    @SerialName("explanation") val explanation: String,
    @SerialName("severity") val severity: String,
    @SerialName("canAutoFix") val canAutoFix: Boolean
) {
    fun toSentenceGrammarError(): SentenceGrammarError {
        return SentenceGrammarError(
            originalFragment = originalFragment,
            correction = correction,
            errorType = try {
                ErrorType.valueOf(errorType.uppercase())
            } catch (_: Exception) { ErrorType.OTHER },
            explanation = explanation,
            positionStart = 0,
            positionEnd = originalFragment.length,
            severity = try {
                ErrorSeverity.valueOf(severity.uppercase())
            } catch (_: Exception) { ErrorSeverity.MEDIUM },
            canAutoFix = canAutoFix
        )
    }
}

@Serializable
data class AiVocabularyInsights(
    val advancedWords: List<String> = emptyList(),
    val repeatedWords: List<String> = emptyList(),
    val suggestedAlternatives: List<AiWordAlternative> = emptyList()
) {
    fun toVocabularyInsights(): VocabularyInsights {
        return VocabularyInsights(
            wordsUsed = emptyList(),
            advancedVocabulary = advancedWords,
            repeatedWords = repeatedWords.map { RepeatedWord(it, 2, emptyList()) },
            suggestedAlternatives = suggestedAlternatives.map { 
                WordAlternative(
                    original = it.original,
                    alternatives = it.alternatives.map { alt -> 
                        AlternativeWithContext(alt, "", "", CefrLevel.B1)
                    }
                )
            },
            diversityScore = 0.7,
            complexityScore = 0.5
        )
    }
}

@Serializable
data class AiWordAlternative(
    val original: String,
    val alternatives: List<String>,
    val context: String = ""
)

@Serializable
data class AiStyleSuggestion(
    val type: String,
    val currentVersion: String,
    val suggestedVersion: String,
    val explanation: String
) {
    fun toStyleSuggestion(): StyleSuggestion {
        return StyleSuggestion(
            type = try {
                StyleSuggestionType.valueOf(type.uppercase())
            } catch (_: Exception) { StyleSuggestionType.CLARITY },
            description = explanation,
            currentVersion = currentVersion,
            suggestedVersion = suggestedVersion,
            explanation = explanation
        )
    }
}

@Serializable
data class AiLearningOpportunity(
    val concept: String,
    val explanation: String,
    val examples: List<String>,
    val difficulty: String
) {
    fun toLearningOpportunity(): LearningOpportunity {
        return LearningOpportunity(
            concept = concept,
            conceptType = ConceptType.GRAMMAR_PATTERN,
            currentAttempt = "",
            explanation = explanation,
            examples = examples,
            relatedExercises = emptyList(),
            difficulty = try {
                CefrLevel.valueOf(difficulty.uppercase())
            } catch (_: Exception) { CefrLevel.A2 }
        )
    }
}

@Serializable
data class AiNextWordSuggestion(
    val suggestedWord: String,
    val context: String,
    val confidence: Double
) {
    fun toNextWordSuggestion(): NextWordSuggestion {
        return NextWordSuggestion(
            suggestedWord = suggestedWord,
            wordType = WordSuggestionType.CONTINUATION,
            context = context,
            meaning = "",
            grammaticalCase = null,
            exampleSentence = "",
            difficulty = CefrLevel.A2,
            confidence = confidence.toFloat()
        )
    }
}

@Serializable
data class StreamingSuggestion(
    val type: String,
    val title: String,
    val description: String,
    val priority: Int
)

sealed class SuggestionStreamEvent {
    data class Suggestion(val suggestion: StreamingSuggestion) : SuggestionStreamEvent()
    data class Error(val message: String) : SuggestionStreamEvent()
    object Complete : SuggestionStreamEvent()
}
