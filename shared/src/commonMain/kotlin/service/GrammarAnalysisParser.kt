package service

import data.model.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

/**
 * Robust parser for structured AI responses with multiple fallback strategies.
 * Ensures we always get a valid GrammarAnalysisResponse even from malformed AI output.
 */
class GrammarAnalysisParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    /**
     * Main parsing method with multiple fallback strategies.
     */
    fun parse(aiResponse: String, originalText: String): GrammarAnalysisResponse {
        // Strategy 1: Try direct JSON parsing
        val directResult = tryParseDirectJson(aiResponse, originalText)
        if (directResult != null) return directResult
        
        // Strategy 2: Try to extract JSON from markdown code blocks
        val markdownResult = tryExtractFromMarkdown(aiResponse, originalText)
        if (markdownResult != null) return markdownResult
        
        // Strategy 3: Try to extract JSON from the response (find first { and last })
        val extractedResult = tryExtractJsonBlock(aiResponse, originalText)
        if (extractedResult != null) return extractedResult
        
        // Strategy 4: Use AI's text response to construct a minimal valid response
        return createFallbackResponse(aiResponse, originalText)
    }
    
    /**
     * Generate the JSON schema prompt to send to AI for structured responses.
     */
    fun generateStructuredPrompt(originalText: String): String {
        return """Analyze the following German text and provide a detailed analysis in JSON format.

Text to analyze:
"$originalText"

You MUST respond with ONLY valid JSON matching this exact structure:

{
  "corrected_text": "The grammatically correct version of the text",
  "grammar_errors": [
    {
      "original_text": "the incorrect text segment",
      "correction": "the corrected version",
      "error_type": "GRAMMAR or SPELLING or PUNCTUATION or WORD_ORDER or VERB_CONJUGATION or CASE or GENDER or TENSE or ARTICLE or PREPOSITION or STYLE or OTHER",
      "explanation": "Brief explanation of the rule",
      "position_start": -1,
      "position_end": -1,
      "severity": "LOW or MEDIUM or HIGH or CRITICAL"
    }
  ],
  "strengths": [
    {
      "aspect": "What the user did well",
      "description": "Detailed description",
      "examples": ["specific examples from text"]
    }
  ],
  "stats": {
    "word_count": number,
    "sentence_count": number,
    "vocabulary_diversity_score": 0.0-1.0,
    "complexity_score": 0.0-1.0,
    "estimated_cefr_level": "A1 or A2 or B1 or B2 or C1 or C2",
    "error_count": number
  },
  "suggestions": [
    {
      "type": "VOCABULARY or STRUCTURE or STYLE or CLARITY or FORMALITY",
      "description": "What to improve",
      "example_implementation": "optional example"
    }
  ],
  "learning_topics": ["detected topics like 'past tense', 'subjunctive', 'word order'"]
}

Rules:
1. Respond ONLY with the JSON object, no markdown formatting, no extra text
2. Use double quotes for all strings
3. Ensure the JSON is valid and complete
4. If there are no errors, return an empty grammar_errors array
5. Always include at least one strength
6. Position values can be -1 if unknown
""".trimIndent()
    }
    
    private fun tryParseDirectJson(response: String, originalText: String): GrammarAnalysisResponse? {
        return try {
            json.decodeFromString<GrammarAnalysisResponse>(response.trim())
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }
    
    private fun tryExtractFromMarkdown(response: String, originalText: String): GrammarAnalysisResponse? {
        val jsonRegex = Regex("""```(?:json)?\s*([\s\S]*?)```""")
        val match = jsonRegex.find(response)
        
        return match?.groupValues?.get(1)?.let { jsonContent ->
            tryParseDirectJson(jsonContent, originalText)
        }
    }
    
    private fun tryExtractJsonBlock(response: String, originalText: String): GrammarAnalysisResponse? {
        // Find the first { and last } to extract potential JSON
        val startIndex = response.indexOf('{')
        val endIndex = response.lastIndexOf('}')
        
        if (startIndex >= 0 && endIndex > startIndex) {
            val potentialJson = response.substring(startIndex, endIndex + 1)
            return tryParseDirectJson(potentialJson, originalText)
        }
        
        return null
    }
    
    private fun createFallbackResponse(aiResponse: String, originalText: String): GrammarAnalysisResponse {
        // Create a minimal valid response from unstructured AI output
        val estimatedLevel = estimateCefrLevel(originalText)
        val wordCount = originalText.split(Regex("\\s+")).size
        val sentenceCount = originalText.split(Regex("[.!?]+")).filter { it.isNotBlank() }.size
        
        // Try to extract corrections from plain text
        val extractedErrors = extractErrorsFromText(aiResponse)
        val extractedStrengths = extractStrengthsFromText(aiResponse)
        
        return GrammarAnalysisResponse(
            correctedText = if (aiResponse.length > originalText.length * 0.8) aiResponse else originalText,
            grammarErrors = extractedErrors,
            strengths = extractedStrengths.ifEmpty { 
                listOf(
                    WritingStrength(
                        aspect = "Writing Practice",
                        description = "Keep practicing to improve your German writing skills",
                        examples = listOf(originalText.take(50))
                    )
                )
            },
            stats = WritingStats(
                wordCount = wordCount,
                sentenceCount = sentenceCount,
                vocabularyDiversityScore = 0.5,
                complexityScore = 0.5,
                estimatedCefrLevel = estimatedLevel,
                errorCount = extractedErrors.size
            ),
            suggestions = listOf(
                WritingSuggestion(
                    type = SuggestionType.CLARITY,
                    description = "Continue practicing with structured feedback",
                    exampleImplementation = null
                )
            ),
            learningTopics = emptyList()
        )
    }
    
    private fun extractErrorsFromText(text: String): List<GrammarError> {
        val errors = mutableListOf<GrammarError>()
        
        // Look for common error patterns in AI responses
        val errorPatterns = listOf(
            Regex("""["']([^"']+)["']\s*(?:should be|->|→)\s*["']([^"']+)["']"""),
            Regex("""Error:\s*["']?([^"']+)["']?\s*(?:correction|fix):\s*["']?([^"']+)["']?""", RegexOption.IGNORE_CASE),
            Regex("""([^:]+):\s*["']([^"']+)["']\s*(?:->|→|to)\s*["']([^"']+)["']""")
        )
        
        errorPatterns.forEach { pattern ->
            pattern.findAll(text).forEach { match ->
                val original = match.groupValues.getOrNull(1) ?: ""
                val correction = match.groupValues.getOrNull(2) ?: ""
                
                if (original.isNotBlank() && correction.isNotBlank()) {
                    errors.add(
                        GrammarError(
                            originalText = original,
                            correction = correction,
                            errorType = ErrorType.OTHER,
                            explanation = "Identified from AI feedback",
                            positionStart = -1,
                            positionEnd = -1,
                            severity = ErrorSeverity.MEDIUM
                        )
                    )
                }
            }
        }
        
        return errors
    }
    
    private fun extractStrengthsFromText(text: String): List<WritingStrength> {
        val strengths = mutableListOf<WritingStrength>()
        
        // Look for positive feedback patterns
        val positivePatterns = listOf(
            Regex("""(?:good|great|excellent|well done|strength|positive)[,:]\s*([^.,]+)""", RegexOption.IGNORE_CASE),
            Regex("""you\s+(?:did well|excelled|succeeded)\s*(?:in|at)?\s*([^.,]+)""", RegexOption.IGNORE_CASE)
        )
        
        positivePatterns.forEach { pattern ->
            pattern.findAll(text).forEach { match ->
                val aspect = match.groupValues.getOrNull(1)?.trim() ?: return@forEach
                if (aspect.isNotBlank() && aspect.length > 3) {
                    strengths.add(
                        WritingStrength(
                            aspect = aspect.replaceFirstChar { it.uppercase() },
                            description = "Identified strength from analysis",
                            examples = emptyList()
                        )
                    )
                }
            }
        }
        
        return strengths
    }
    
    private fun estimateCefrLevel(text: String): CefrLevel {
        val wordCount = text.split(Regex("\\s+")).size
        val complexWords = text.split(Regex("\\s+")).count { it.length > 6 }
        val subordinateClauses = text.split(",").size
        
        // Simple heuristic for CEFR estimation
        return when {
            wordCount < 20 && complexWords < 3 -> CefrLevel.A1
            wordCount < 50 && complexWords < 5 && subordinateClauses < 2 -> CefrLevel.A2
            wordCount < 100 && complexWords < 10 && subordinateClauses < 4 -> CefrLevel.B1
            wordCount < 200 && complexWords < 20 -> CefrLevel.B2
            wordCount < 300 -> CefrLevel.C1
            else -> CefrLevel.C2
        }
    }
    
    /**
     * Serialize analysis to JSON for storage.
     */
    fun serializeAnalysis(analysis: GrammarAnalysisResponse): String {
        return json.encodeToString(analysis)
    }
    
    /**
     * Deserialize analysis from stored JSON.
     */
    fun deserializeAnalysis(jsonString: String): GrammarAnalysisResponse? {
        return try {
            json.decodeFromString<GrammarAnalysisResponse>(jsonString)
        } catch (e: Exception) {
            null
        }
    }
}
