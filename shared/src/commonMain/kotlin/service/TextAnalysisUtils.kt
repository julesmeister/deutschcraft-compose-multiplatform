package service

import data.model.AlternativeWithContext
import data.model.CefrLevel
import data.model.PartOfSpeech

/**
 * Utility functions for text analysis.
 */
object TextAnalysisUtils {
    
    fun estimatePartOfSpeech(word: String): PartOfSpeech {
        return when {
            word.endsWith("ung") || word.endsWith("heit") || word.endsWith("keit") || 
            word.endsWith("schaft") -> PartOfSpeech.NOUN
            word.endsWith("en") && word.length > 5 -> PartOfSpeech.VERB
            word.endsWith("e") && word.length < 6 -> PartOfSpeech.VERB
            word.endsWith("lich") || word.endsWith("ig") || word.endsWith("isch") -> PartOfSpeech.ADJECTIVE
            word.lowercase() in setOf("der", "die", "das", "den", "dem", "des") -> PartOfSpeech.ARTICLE
            word.lowercase() in setOf("ich", "du", "er", "sie", "es", "wir", "ihr") -> PartOfSpeech.PRONOUN
            word.lowercase() in setOf("in", "auf", "an", "mit", "zu", "fur", "bei") -> PartOfSpeech.PREPOSITION
            else -> PartOfSpeech.NOUN
        }
    }
    
    fun estimateWordLevel(word: String): CefrLevel {
        return when {
            word.length <= 4 -> CefrLevel.A1
            word.length <= 6 -> CefrLevel.A2
            word.length <= 10 -> CefrLevel.B1
            word.contains(Regex("ung|heit|keit|schaft")) -> CefrLevel.B2
            else -> CefrLevel.C1
        }
    }
    
    fun getSynonymSuggestions(word: String): List<String> {
        return when (word.lowercase()) {
            "gut" -> listOf("exzellent", "hervorragend", "prima")
            "schon" -> listOf("wunderschön", "hübsch", "eindrucksvoll")
            "interessant" -> listOf("fesselnd", "spannend", "faszinierend")
            "wichtig" -> listOf("bedeutsam", "entscheidend", "wesentlich")
            "groß" -> listOf("enorm", "beträchtlich", "massiv")
            else -> emptyList()
        }
    }
    
    fun getAdvancedAlternatives(word: String): List<AlternativeWithContext> {
        return when (word) {
            "gut" -> listOf(
                AlternativeWithContext("hervorragend", "outstanding/excellent", "positive quality", CefrLevel.B1),
                AlternativeWithContext("prima", "great/super", "casual positive", CefrLevel.A2),
                AlternativeWithContext("ausgezeichnet", "excellent", "formal praise", CefrLevel.B2)
            )
            "schon" -> listOf(
                AlternativeWithContext("wunderschön", "beautiful", "aesthetic quality", CefrLevel.A2),
                AlternativeWithContext("eindrucksvoll", "impressive", "striking quality", CefrLevel.B1)
            )
            "interessant" -> listOf(
                AlternativeWithContext("fesselnd", "captivating", "holds attention", CefrLevel.B2),
                AlternativeWithContext("spannend", "exciting/thrilling", "creates suspense", CefrLevel.B1)
            )
            "wichtig" -> listOf(
                AlternativeWithContext("entscheidend", "crucial/decisive", "very important", CefrLevel.B1),
                AlternativeWithContext("bedeutsam", "significant", "has meaning", CefrLevel.B2)
            )
            else -> emptyList()
        }
    }
    
    fun calculateDiversity(words: List<String>): Double {
        val uniqueWords = words.map { it.lowercase() }.toSet()
        return uniqueWords.size.toDouble() / words.size.coerceAtLeast(1)
    }
}
