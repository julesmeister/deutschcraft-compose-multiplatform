package service

import data.model.WritingAnalysis
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Manages analysis flow with cooldown and state tracking.
 */
class AnalysisOrchestrator(
    private val analyzer: WritingAnalyzer,
    private val suggestionGenerator: SuggestionGenerator,
    private val onAnalysisComplete: (WritingAnalysis, List<QuickSuggestion>) -> Unit
) {
    private var lastAnalyzedText = ""
    private var analysisCooldown = false
    private val cooldownMs = 2000L
    
    /**
     * Check if analysis can proceed (not in cooldown and text is new).
     */
    fun canAnalyze(text: String): Boolean {
        return !analysisCooldown && text.isNotBlank() && text != lastAnalyzedText
    }
    
    /**
     * Perform analysis with cooldown management.
     */
    fun analyze(sentence: String) {
        if (!canAnalyze(sentence)) return
        
        lastAnalyzedText = sentence
        analysisCooldown = true
        
        val analysis = analyzer.analyze(sentence)
        val suggestions = suggestionGenerator.generate(analysis)
        
        onAnalysisComplete(analysis, suggestions)
        
        MainScope().launch {
            delay(cooldownMs)
            analysisCooldown = false
        }
    }
    
    /**
     * Force analysis bypassing cooldown.
     */
    fun forceAnalyze(sentence: String) {
        lastAnalyzedText = sentence
        
        val analysis = analyzer.analyze(sentence)
        val suggestions = suggestionGenerator.generate(analysis)
        
        onAnalysisComplete(analysis, suggestions)
    }
    
    fun reset() {
        lastAnalyzedText = ""
        analysisCooldown = false
    }
}
