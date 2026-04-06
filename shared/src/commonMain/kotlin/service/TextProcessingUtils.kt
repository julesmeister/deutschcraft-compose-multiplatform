package service

/**
 * Utility functions for text processing and sentence extraction.
 */
object TextProcessingUtils {
    
    private val sentenceEnders = setOf('.', '!', '?', '。', '！', '？')
    
    /**
     * Extract the last sentence from text ending with punctuation.
     */
    fun extractLastSentence(text: String): String {
        val reversed = text.reversed()
        val lastPunctIndex = reversed.indexOfFirst { sentenceEnders.contains(it) }
        
        if (lastPunctIndex == -1) return text.trim()
        
        val prevPunctIndex = reversed.drop(lastPunctIndex + 1).indexOfFirst { sentenceEnders.contains(it) }
        
        val startIndex = if (prevPunctIndex == -1) {
            0
        } else {
            text.length - (lastPunctIndex + 1) - prevPunctIndex - 1
        }
        
        return text.substring(startIndex, text.length - lastPunctIndex).trim()
    }
    
    /**
     * Check if text ends with a sentence-ending punctuation.
     */
    fun endsWithSentencePunctuation(text: String): Boolean {
        return text.isNotEmpty() && sentenceEnders.contains(text.last())
    }
    
    /**
     * Split text into sentences.
     */
    fun splitIntoSentences(text: String): List<String> {
        return text.split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
    
    /**
     * Get the last complete sentence from text.
     */
    fun getLastCompleteSentence(text: String): String? {
        val sentences = splitIntoSentences(text)
        return sentences.lastOrNull { it.isNotEmpty() }
    }
}
