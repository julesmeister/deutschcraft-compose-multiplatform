package service

import data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
