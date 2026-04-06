package service

import data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock

/**
 * Enhanced writing assistant with AI integration, debounced analysis,
 * context memory, confidence scoring, and undo/redo support.
 */
class WritingAssistantService(
    private val ollamaService: OllamaService,
    private val preferencesStore: PreferencesStore = PreferencesStore()
) {

    // State flows
    private val _currentAnalysis = MutableStateFlow<WritingAnalysis?>(null)
    val currentAnalysis: Flow<WritingAnalysis?> = _currentAnalysis.asStateFlow()

    private val _quickSuggestions = MutableStateFlow<List<QuickSuggestion>>(emptyList())
    val quickSuggestions: Flow<List<QuickSuggestion>> = _quickSuggestions.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: Flow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisConfidence = MutableStateFlow(0.0)
    val analysisConfidence: Flow<Double> = _analysisConfidence.asStateFlow()

    private val _analysisSource = MutableStateFlow<AnalysisSource?>(null)
    val analysisSource: Flow<AnalysisSource?> = _analysisSource.asStateFlow()

    // Components
    private val aiEngine = AiAnalysisEngine(ollamaService)
    private val suggestionGenerator = SuggestionGenerator()
    private val suggestionApplicator = SuggestionApplicator()

    // Context and memory
    private val sentenceHistory = mutableListOf<String>()
    private var lastAnalyzedText = ""
    private var analysisCooldown = false

    // Undo/Redo stack
    private val undoStack = mutableListOf<TextRevision>()
    private val redoStack = mutableListOf<TextRevision>()
    private val maxStackSize = 50

    // Debounced analysis
    private val analysisScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var debounceJob: Job? = null

    private val sentenceEnders = setOf('.', '!', '?', '。', '！', '？')

    fun checkAndAnalyze(text: String, cursorPosition: Int): Boolean {
        if (text.isBlank() || analysisCooldown) return false

        val beforeCursor = text.substring(0, cursorPosition.coerceAtMost(text.length))

        if (beforeCursor.isNotEmpty() && sentenceEnders.contains(beforeCursor.last())) {
            val lastSentence = extractLastSentence(beforeCursor)
            if (lastSentence.isNotBlank() && lastSentence != lastAnalyzedText) {
                addToContext(lastSentence)
                analyzeSentence(lastSentence)
                return true
            }
        }
        return false
    }

    fun onTextChanged(text: String, cursorPosition: Int) {
        val prefs = preferencesStore.getPreferences()
        
        debounceJob?.cancel()
        debounceJob = analysisScope.launch {
            delay(prefs.debounceDelayMs)
            
            val beforeCursor = text.substring(0, cursorPosition.coerceAtMost(text.length))
            val currentSentence = extractCurrentSentence(beforeCursor)
            
            if (currentSentence.isNotBlank() && currentSentence.length > 10) {
                analyzeIncremental(currentSentence, text)
            }
        }
    }

    fun analyzeCurrentText(text: String) {
        if (text.isBlank()) {
            clear()
            return
        }

        val sentences = text.split(Regex("(?<=[.!?])\\s+"))
        val lastCompleteSentence = sentences.lastOrNull { it.trim().isNotEmpty() } ?: text.trim()
        
        addToContext(lastCompleteSentence)
        analyzeSentence(lastCompleteSentence)
    }

    suspend fun analyzeDocument(fullText: String): DocumentAnalysis {
        val sentences = fullText.split(Regex("(?<=[.!?])\\s+"))
        
        val sentenceAnalyses = sentences.map { sentence ->
            aiEngine.analyzeWithConfidence(sentence, buildContext())
        }

        val repeatedPhrases = findRepeatedPhrases(sentences)
        val consistencyIssues = checkConsistency(sentenceAnalyses)
        val flowScore = calculateFlowScore(sentenceAnalyses)

        return DocumentAnalysis(
            totalSentences = sentences.size,
            averageSentenceLength = sentences.map { it.split(" ").size }.average(),
            repeatedPhrases = repeatedPhrases,
            consistencyIssues = consistencyIssues,
            flowScore = flowScore,
            overallLevel = estimateDocumentLevel(sentenceAnalyses),
            suggestions = generateDocumentSuggestions(sentenceAnalyses, repeatedPhrases)
        )
    }

    fun applySuggestion(text: String, cursorPosition: Int, action: SuggestionAction, skipUndo: Boolean = false): Pair<String, Int> {
        val result = suggestionApplicator.applySuggestion(text, cursorPosition, action)
        
        if (!skipUndo) {
            recordRevision(TextRevision(originalText = text, newText = result.first, cursorPosition = cursorPosition, newCursorPosition = result.second, action = action, timestamp = Clock.System.now()))
        }
        
        return result
    }

    suspend fun autoApplyFixes(text: String): AutoApplyResult {
        val prefs = preferencesStore.getPreferences()
        if (!prefs.autoApplyHighConfidence) {
            return AutoApplyResult(text, emptyList())
        }

        val analysis = aiEngine.analyzeWithConfidence(text, buildContext())
        val highConfidenceErrors = analysis.analysis.grammarErrors.filter { it.canAutoFix && analysis.confidence >= prefs.highConfidenceThreshold }.sortedByDescending { it.severity.ordinal }

        var currentText = text
        var currentCursor = text.length
        val applied = mutableListOf<SentenceGrammarError>()

        for (error in highConfidenceErrors.take(3)) {
            val action = SuggestionAction(type = ActionType.REPLACE, value = error.correction, position = error.positionStart)
            val result = applySuggestion(currentText, currentCursor, action, skipUndo = true)
            currentText = result.first
            currentCursor = result.second
            applied.add(error)
        }

        if (applied.isNotEmpty()) {
            recordRevision(TextRevision(originalText = text, newText = currentText, cursorPosition = text.length, newCursorPosition = currentCursor, action = SuggestionAction(ActionType.REPLACE, "", null), timestamp = Clock.System.now(), isAutoFix = true))
        }

        return AutoApplyResult(currentText, applied)
    }

    fun undo(): TextRevision? {
        val revision = undoStack.removeLastOrNull() ?: return null
        redoStack.add(revision.copy(direction = RevisionDirection.REDO))
        if (redoStack.size > maxStackSize) redoStack.removeAt(0)
        return revision.copy(direction = RevisionDirection.UNDO)
    }

    fun redo(): TextRevision? {
        val revision = redoStack.removeLastOrNull() ?: return null
        undoStack.add(revision.copy(direction = RevisionDirection.UNDO))
        if (undoStack.size > maxStackSize) undoStack.removeAt(0)
        return revision.copy(direction = RevisionDirection.REDO)
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()
    fun getUndoHistory(): List<TextRevision> = undoStack.toList()

    fun clear() {
        _currentAnalysis.value = null
        _quickSuggestions.value = emptyList()
        _isAnalyzing.value = false
        _analysisConfidence.value = 0.0
        _analysisSource.value = null
        lastAnalyzedText = ""
        analysisCooldown = false
        sentenceHistory.clear()
        debounceJob?.cancel()
    }

    fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
    }

    private fun analyzeSentence(sentence: String) {
        _isAnalyzing.value = true
        lastAnalyzedText = sentence
        analysisCooldown = true

        analysisScope.launch {
            try {
                val result = aiEngine.analyzeWithConfidence(sentence, buildContext())
                _currentAnalysis.value = result.analysis
                _analysisConfidence.value = result.confidence
                _analysisSource.value = result.source
                _quickSuggestions.value = suggestionGenerator.generate(result.analysis).filter { it.type.toString() in preferencesStore.getPreferences().enabledSuggestionTypes.map { it.name } }
                _isAnalyzing.value = false
                delay(2000)
                analysisCooldown = false
            } catch (e: Exception) {
                _isAnalyzing.value = false
                analysisCooldown = false
            }
        }
    }

    private suspend fun analyzeIncremental(currentSentence: String, fullText: String) {
        _isAnalyzing.value = true
        try {
            val result = aiEngine.analyzeWithConfidence(currentSentence, buildContext().copy(documentTopic = extractTopic(fullText)))
            _currentAnalysis.value = result.analysis
            _analysisConfidence.value = result.confidence * 0.8
            _analysisSource.value = result.source
            _quickSuggestions.value = suggestionGenerator.generate(result.analysis).filter { it.priority > 70 }.take(3)
        } finally {
            _isAnalyzing.value = false
        }
    }

    private fun addToContext(sentence: String) {
        sentenceHistory.add(sentence)
        val maxSize = preferencesStore.getPreferences().contextMemorySize
        while (sentenceHistory.size > maxSize) sentenceHistory.removeAt(0)
    }

    private fun buildContext() = AnalysisContext(previousSentences = sentenceHistory.toList(), userPreferences = preferencesStore.getPreferences())

    private fun recordRevision(revision: TextRevision) {
        undoStack.add(revision)
        redoStack.clear()
        if (undoStack.size > maxStackSize) undoStack.removeAt(0)
    }

    private fun extractLastSentence(text: String): String {
        val reversed = text.reversed()
        val lastPunctIndex = reversed.indexOfFirst { sentenceEnders.contains(it) }
        if (lastPunctIndex == -1) return text.trim()
        val prevPunctIndex = reversed.drop(lastPunctIndex + 1).indexOfFirst { sentenceEnders.contains(it) }
        val startIndex = if (prevPunctIndex == -1) 0 else text.length - (lastPunctIndex + 1) - prevPunctIndex - 1
        return text.substring(startIndex, text.length - lastPunctIndex).trim()
    }

    private fun extractCurrentSentence(text: String): String {
        val lastPunct = text.lastIndexOfAny(sentenceEnders)
        return if (lastPunct == -1) text else text.substring(lastPunct + 1).trim()
    }

    private fun extractTopic(text: String): String? = text.split(" ").take(5).firstOrNull { it.length > 3 }

    private fun findRepeatedPhrases(sentences: List<String>): List<RepeatedPhrase> {
        val phrases = mutableMapOf<String, Int>()
        sentences.forEach { sentence ->
            val words = sentence.lowercase().split(" ")
            for (i in 0 until words.size - 1) {
                val bigram = "${words[i]} ${words[i + 1]}"
                phrases[bigram] = phrases.getOrDefault(bigram, 0) + 1
                if (i < words.size - 2) {
                    val trigram = "$bigram ${words[i + 2]}"
                    phrases[trigram] = phrases.getOrDefault(trigram, 0) + 1
                }
            }
        }
        return phrases.filter { it.value > 2 }.map { (phrase, count) -> RepeatedPhrase(phrase, count) }.sortedByDescending { it.count }.take(5)
    }

    private fun checkConsistency(analyses: List<AnalysisResult>): List<ConsistencyIssue> {
        val issues = mutableListOf<ConsistencyIssue>()
        val tenses = analyses.map { it.analysis.sentenceLevel }
        if (tenses.toSet().size > 2) {
            issues.add(ConsistencyIssue("TENSE_MIXING", "Multiple formality levels detected", ConsistencySeverity.WARNING))
        }
        return issues
    }

    private fun calculateFlowScore(analyses: List<AnalysisResult>): Double {
        if (analyses.size < 2) return 1.0
        val variety = analyses.map { it.analysis.sentenceLevel }.distinct().size
        val avgScore = analyses.map { it.analysis.score }.average()
        return (variety * 0.2 + avgScore * 0.8).coerceIn(0.0, 100.0) / 100.0
    }

    private fun estimateDocumentLevel(analyses: List<AnalysisResult>): CefrLevel {
        val avgOrdinal = analyses.map { it.analysis.sentenceLevel.ordinal }.average()
        return CefrLevel.entries.getOrNull(avgOrdinal.toInt()) ?: CefrLevel.B1
    }

    private fun generateDocumentSuggestions(analyses: List<AnalysisResult>, repeatedPhrases: List<RepeatedPhrase>): List<DocumentSuggestion> {
        val suggestions = mutableListOf<DocumentSuggestion>()
        if (repeatedPhrases.isNotEmpty()) {
            suggestions.add(DocumentSuggestion(DocumentSuggestionType.VARIETY, "Consider varying phrases: ${repeatedPhrases.first().phrase}"))
        }
        val lowConfidence = analyses.count { it.confidence < 0.5 }
        if (lowConfidence > analyses.size / 2) {
            suggestions.add(DocumentSuggestion(DocumentSuggestionType.CLARITY, "Several sentences may need clarification"))
        }
        return suggestions
    }
}

data class TextRevision(val originalText: String, val newText: String, val cursorPosition: Int, val newCursorPosition: Int, val action: SuggestionAction, val timestamp: kotlinx.datetime.Instant, val direction: RevisionDirection = RevisionDirection.UNDO, val isAutoFix: Boolean = false)
enum class RevisionDirection { UNDO, REDO }
data class AutoApplyResult(val text: String, val appliedFixes: List<SentenceGrammarError>)
data class DocumentAnalysis(val totalSentences: Int, val averageSentenceLength: Double, val repeatedPhrases: List<RepeatedPhrase>, val consistencyIssues: List<ConsistencyIssue>, val flowScore: Double, val overallLevel: CefrLevel, val suggestions: List<DocumentSuggestion>)
data class RepeatedPhrase(val phrase: String, val count: Int)
data class ConsistencyIssue(val type: String, val description: String, val severity: ConsistencySeverity)
enum class ConsistencySeverity { INFO, WARNING, ERROR }
data class DocumentSuggestion(val type: DocumentSuggestionType, val description: String)
enum class DocumentSuggestionType { VARIETY, CLARITY, STRUCTURE, TONE }
