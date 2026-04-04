package service

import data.db.DatabaseDriverFactory
import data.db.DatabaseManager
import data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * Service that combines AI grammar analysis with database persistence.
 * Tracks user progress and maintains study memory.
 */
class GrammarAnalysisService(
    driverFactory: DatabaseDriverFactory
) {
    private val ollamaService = OllamaService()
    private val parser = GrammarAnalysisParser()
    private val db = DatabaseManager(driverFactory)
    
    /**
     * Analyze text with AI and persist results to database.
     * Returns the analysis and stores all relevant data for progress tracking.
     */
    suspend fun analyzeAndRecord(
        text: String,
        entryType: EntryType = EntryType.FREE_WRITING,
        topic: String? = null
    ): AnalysisResult {
        return withContext(Dispatchers.Default) {
            // 1. Record the study entry first
            val entryId = db.entries.insert(
                content = text,
                type = entryType,
                topic = topic
            )
            
            // 2. Get AI analysis with structured JSON
            val analysis = getStructuredAnalysis(text)
            
            // 3. Update entry with analysis results
            db.entries.updateAnalysis(
                entryId = entryId,
                correctedContent = analysis.correctedText,
                analysisJson = parser.serializeAnalysis(analysis)
            )
            
            // 4. Record grammar mistakes
            analysis.grammarErrors.forEach { error ->
                db.mistakes.record(
                    originalText = error.originalText,
                    correction = error.correction,
                    errorType = error.errorType,
                    explanation = error.explanation,
                    studyEntryId = entryId
                )
            }
            
            // 5. Record strengths
            analysis.strengths.forEach { strength ->
                db.strengths.record(
                    aspect = strength.aspect,
                    description = strength.description,
                    confidenceScore = 1.0
                )
            }
            
            // 6. Record topics/learning areas
            analysis.learningTopics.forEach { topicName ->
                val category = categorizeTopic(topicName)
                db.topics.recordEncounter(topicName, category)
            }
            
            // 7. Extract and record vocabulary
            extractVocabulary(text, analysis).forEach { vocab ->
                db.vocabulary.record(
                    word = vocab.word,
                    context = vocab.context,
                    translation = vocab.translation,
                    difficulty = vocab.difficulty
                )
            }
            
            // 8. Get current progress stats
            val progressStats = getProgressStats()
            
            AnalysisResult(
                entryId = entryId,
                analysis = analysis,
                progressStats = progressStats,
                recurringMistakes = getRecurringMistakes(analysis.grammarErrors)
            )
        }
    }
    
    /**
     * Get structured grammar analysis from AI.
     */
    suspend fun getStructuredAnalysis(text: String): GrammarAnalysisResponse {
        val prompt = parser.generateStructuredPrompt(text)
        
        val response = ollamaService.generateRawResponse(
            prompt = prompt,
            systemPrompt = """You are a German language tutor. Always respond with valid JSON only. 
                |Follow the exact schema provided in the prompt. Never include markdown formatting.""".trimMargin(),
            temperature = 0.2 // Lower temperature for more consistent JSON
        )
        
        return parser.parse(response, text)
    }
    
    /**
     * Get quick grammar check without storing to database.
     */
    suspend fun quickCheck(text: String): QuickAnalysis {
        val analysis = getStructuredAnalysis(text)
        
        return QuickAnalysis(
            correctedText = analysis.correctedText,
            errorCount = analysis.grammarErrors.size,
            topErrors = analysis.grammarErrors
                .groupBy { it.errorType }
                .map { (type, errors) -> type to errors.size }
                .sortedByDescending { it.second }
                .take(3),
            cefrLevel = analysis.stats.estimatedCefrLevel
        )
    }
    
    /**
     * Get user's overall progress statistics.
     */
    fun getProgressStats(): UserProgressStats {
        val mistakeStats = db.mistakes.getStats()
        val topMistakeTypes = db.mistakes.getTopTypes(5)
        val topStrengths = db.strengths.getTop(5)
        
        return UserProgressStats(
            totalWritingSessions = db.entries.getCount().toInt(),
            totalWordsWritten = db.entries.getTotalWords().toInt(),
            errorFrequencyByType = topMistakeTypes.associate { it },
            averageCefrProgression = emptyMap(),
            topStrengths = topStrengths.map { it.first },
            improvementAreas = topMistakeTypes.map { it.first.name.lowercase().replace("_", " ") }
        )
    }
    
    /**
     * Get mistake patterns for focused practice.
     */
    fun getMistakePatterns(): MistakePatterns {
        val byType = db.mistakes.getTopTypes(10)
        val recent = db.mistakes.getRecent(20)
        val recurring = recent.filter { it.recurrenceCount > 1 }
        
        return MistakePatterns(
            mostFrequentTypes = byType,
            recurringMistakes = recurring,
            recentMistakes = recent.take(10),
            suggestion = generatePracticeSuggestion(byType, recurring)
        )
    }
    
    /**
     * Get recent study history.
     */
    fun getRecentHistory(limit: Int = 20): List<StudyHistoryEntry> {
        return db.entries.getRecent(limit.toLong()).map { entry ->
            val analysis = entry.analysisJson?.let { parser.deserializeAnalysis(it) }
            
            StudyHistoryEntry(
                id = entry.id,
                preview = entry.content.take(100),
                type = entry.type,
                timestamp = entry.timestamp,
                topic = entry.topic,
                errorCount = analysis?.grammarErrors?.size ?: 0,
                cefrLevel = analysis?.stats?.estimatedCefrLevel
            )
        }
    }
    
    /**
     * Get personalized learning recommendations.
     */
    suspend fun getRecommendations(): List<LearningRecommendation> {
        val patterns = getMistakePatterns()
        val stats = getProgressStats()
        
        val recommendations = mutableListOf<LearningRecommendation>()
        
        // Recommend based on frequent mistakes
        patterns.mostFrequentTypes.firstOrNull()?.let { (errorType, count) ->
            recommendations.add(
                LearningRecommendation(
                    type = RecommendationType.FOCUSED_PRACTICE,
                    title = "Focus on ${errorType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}",
                    description = "You've made $count mistakes of this type. Focused practice recommended.",
                    priority = Priority.HIGH,
                    estimatedTime = "15-20 min"
                )
            )
        }
        
        // Recommend based on recurring mistakes
        if (patterns.recurringMistakes.isNotEmpty()) {
            recommendations.add(
                LearningRecommendation(
                    type = RecommendationType.REVIEW,
                    title = "Review Recurring Mistakes",
                    description = "You have ${patterns.recurringMistakes.size} mistakes that keep appearing. Review them.",
                    priority = Priority.HIGH,
                    estimatedTime = "10 min"
                )
            )
        }
        
        // Recommend vocabulary expansion if complexity is low
        if (stats.totalWordsWritten > 1000 && patterns.recentMistakes.size < 3) {
            recommendations.add(
                LearningRecommendation(
                    type = RecommendationType.VOCABULARY,
                    title = "Expand Vocabulary",
                    description = "Try incorporating more complex vocabulary in your writing.",
                    priority = Priority.MEDIUM,
                    estimatedTime = "20 min"
                )
            )
        }
        
        return recommendations
    }
    
    // ==================== Private Helper Methods ====================
    
    private fun getRecurringMistakes(currentErrors: List<GrammarError>): List<GrammarError> {
        return currentErrors.filter { error ->
            db.mistakes.getByType(error.errorType, 100)
                .any { it.originalText.equals(error.originalText, ignoreCase = true) }
        }
    }
    
    private fun categorizeTopic(topicName: String): TopicCategory {
        val nameLower = topicName.lowercase()
        return when {
            nameLower.contains("verb") || nameLower.contains("tense") || 
            nameLower.contains("conjugation") || nameLower.contains("case") ||
            nameLower.contains("article") || nameLower.contains("preposition") -> TopicCategory.ACADEMIC
            
            nameLower.contains("work") || nameLower.contains("job") || 
            nameLower.contains("business") || nameLower.contains("professional") -> TopicCategory.WORK
            
            nameLower.contains("travel") || nameLower.contains("trip") || 
            nameLower.contains("journey") -> TopicCategory.TRAVEL
            
            nameLower.contains("story") || nameLower.contains("narrative") -> TopicCategory.STORYTELLING
            
            nameLower.contains("opinion") || nameLower.contains("argument") -> TopicCategory.OPINION
            
            nameLower.contains("culture") || nameLower.contains("history") -> TopicCategory.CULTURE
            
            else -> TopicCategory.DAILY_LIFE
        }
    }
    
    private fun extractVocabulary(text: String, analysis: GrammarAnalysisResponse): List<VocabularyItem> {
        val words = text.split(Regex("\\s+"))
            .map { it.trim(',', '.', '!', '?', ';', ':', '"', '\'', '(', ')') }
            .filter { it.length > 3 }
            .distinct()
        
        return words.take(10).map { word ->
            VocabularyItem(
                id = 0,
                word = word,
                context = text.take(200),
                translation = null,
                difficulty = analysis.stats.estimatedCefrLevel,
                firstSeen = Clock.System.now(),
                encounterCount = 1,
                isLearned = false
            )
        }
    }
    
    private fun generatePracticeSuggestion(
        byType: List<Pair<ErrorType, Int>>,
        recurring: List<GrammarMistake>
    ): String {
        return when {
            recurring.isNotEmpty() -> "Focus on avoiding recurring mistakes in ${recurring.first().errorType.name.lowercase().replace("_", " ")}"
            byType.isNotEmpty() -> "Practice ${byType.first().first.name.lowercase().replace("_", " ")} exercises"
            else -> "Keep practicing with varied writing prompts"
        }
    }
}

// ==================== Result Data Classes ====================

data class AnalysisResult(
    val entryId: Long,
    val analysis: GrammarAnalysisResponse,
    val progressStats: UserProgressStats,
    val recurringMistakes: List<GrammarError>
)

data class QuickAnalysis(
    val correctedText: String,
    val errorCount: Int,
    val topErrors: List<Pair<ErrorType, Int>>,
    val cefrLevel: CefrLevel
)

data class MistakePatterns(
    val mostFrequentTypes: List<Pair<ErrorType, Int>>,
    val recurringMistakes: List<GrammarMistake>,
    val recentMistakes: List<GrammarMistake>,
    val suggestion: String
)

data class StudyHistoryEntry(
    val id: Long,
    val preview: String,
    val type: EntryType,
    val timestamp: kotlinx.datetime.Instant,
    val topic: String?,
    val errorCount: Int,
    val cefrLevel: CefrLevel?
)

data class LearningRecommendation(
    val type: RecommendationType,
    val title: String,
    val description: String,
    val priority: Priority,
    val estimatedTime: String
)

enum class RecommendationType {
    FOCUSED_PRACTICE,
    REVIEW,
    VOCABULARY,
    GRAMMAR_LESSON,
    WRITING_PROMPT
}

enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}
