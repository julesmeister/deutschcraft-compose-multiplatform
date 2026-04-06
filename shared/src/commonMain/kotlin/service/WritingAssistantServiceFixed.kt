package service

import data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Service that analyzes writing in real-time and triggers suggestions
 * when sentences are completed (punctuation detected).
 */
class WritingAssistantService {

    private val _currentAnalysis = MutableStateFlow<WritingAnalysis?>(null)
    val currentAnalysis: Flow<WritingAnalysis?> = _currentAnalysis.asStateFlow()

    private val _quickSuggestions = MutableStateFlow<List<QuickSuggestion>>(emptyList())
    val quickSuggestions: Flow<List<QuickSuggestion>> = _quickSuggestions.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: Flow<Boolean> = _isAnalyzing.asStateFlow()

    private val sentenceEnders = setOf('.', '!', '?', '。', '！', '？')

    private var lastAnalyzedText = ""
    private var analysisCooldown = false

    private val analysisEngine = TextAnalysisEngine()
    private val suggestionGenerator = SuggestionGenerator()
    private val suggestionApplicator = SuggestionApplicator()

    fun checkAndAnalyze(text: String, cursorPosition: Int): Boolean {
        if (text.isBlank() || analysisCooldown) return false

        val beforeCursor = text.substring(0, cursorPosition.coerceAtMost(text.length))

        if (beforeCursor.isNotEmpty() && sentenceEnders.contains(beforeCursor.last())) {
            val lastSentence = extractLastSentence(beforeCursor)
            if (lastSentence.isNotBlank() && lastSentence != lastAnalyzedText) {
                analyzeSentence(lastSentence)
                return true
            }
        }
        return false
    }

    fun analyzeCurrentText(text: String) {
        if (text.isBlank()) {
            clear()
            return
        }

        val sentences = text.split(Regex("(?<=[.!?])\\s+"))
        val lastCompleteSentence = sentences.lastOrNull { it.trim().isNotEmpty() } ?: text.trim()
        analyzeSentence(lastCompleteSentence)
    }

    private fun extractLastSentence(text: String): String {
        val reversed = text.reversed()
        val lastPunctIndex = reversed.indexOfFirst { sentenceEnders.contains(it) }
        if (lastPunctIndex == -1) return text.trim()

        val prevPunctIndex = reversed.drop(lastPunctIndex + 1).indexOfFirst { sentenceEnders.contains(it) }
        val startIndex = if (prevPunctIndex == -1) 0 else text.length - (lastPunctIndex + 1) - prevPunctIndex - 1
        return text.substring(startIndex, text.length - lastPunctIndex).trim()
    }

    private fun analyzeSentence(sentence: String) {
        _isAnalyzing.value = true
        lastAnalyzedText = sentence
        analysisCooldown = true

        val analysis = analysisEngine.performLocalAnalysis(sentence)
        _currentAnalysis.value = analysis
        _quickSuggestions.value = suggestionGenerator.generate(analysis)
        _isAnalyzing.value = false

        MainScope().launch {
            delay(2000)
            analysisCooldown = false
        }
    }

    fun applySuggestion(text: String, cursorPosition: Int, action: SuggestionAction): Pair<String, Int> {
        return suggestionApplicator.applySuggestion(text, cursorPosition, action)
    }

    fun clear() {
        _currentAnalysis.value = null
        _quickSuggestions.value = emptyList()
        _isAnalyzing.value = false
        lastAnalyzedText = ""
    }
}
