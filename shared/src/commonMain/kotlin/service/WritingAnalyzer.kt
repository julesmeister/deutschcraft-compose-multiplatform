package service

import data.model.*
import kotlinx.datetime.Clock

/**
 * Performs detailed analysis of German sentences to identify errors,
 * vocabulary insights, style opportunities, and learning moments.
 */
class WritingAnalyzer {
    
    /**
     * Perform comprehensive analysis of a sentence.
     */
    fun analyze(sentence: String): WritingAnalysis {
        val now = Clock.System.now()
        
        val grammarErrors = detectBasicErrors(sentence)
        val vocabularyInsights = analyzeVocabulary(sentence)
        val styleSuggestions = detectStyleOpportunities(sentence)
        val learningOpportunities = identifyLearningMoments(sentence)
        val nextWords = suggestNextWords(sentence)
        val structureAnalysis = analyzeStructure(sentence)
        
        return WritingAnalysis(
            sentence = sentence,
            timestamp = now,
            grammarErrors = grammarErrors,
            vocabularyInsights = vocabularyInsights,
            styleSuggestions = styleSuggestions,
            learningOpportunities = learningOpportunities,
            nextWordSuggestions = nextWords,
            structureAnalysis = structureAnalysis,
            sentenceLevel = TextAnalysisUtils.estimateLevel(sentence),
            score = TextAnalysisUtils.calculateScore(sentence, grammarErrors)
        )
    }
    
    private fun detectBasicErrors(sentence: String): List<SentenceGrammarError> {
        val errors = mutableListOf<SentenceGrammarError>()
        
        // "Ich bin gehen" instead of "Ich gehe"
        val gehenPattern = Regex("ich\\s+bin\\s+(\\w+en)", RegexOption.IGNORE_CASE)
        gehenPattern.find(sentence)?.let { match ->
            errors.add(
                SentenceGrammarError(
                    originalFragment = match.value,
                    correction = "ich ${match.groupValues[1].dropLast(2)}e",
                    errorType = ErrorType.VERB_CONJUGATION,
                    explanation = "German doesn't use 'bin' with infinitives. Use the correct verb form.",
                    positionStart = match.range.first,
                    positionEnd = match.range.last + 1,
                    severity = ErrorSeverity.HIGH,
                    canAutoFix = true
                )
            )
        }
        
        // Check for repeated words
        val words = sentence.lowercase().split(Regex("\\s+")).filter { it.length > 3 }
        val wordCounts = words.groupingBy { it }.eachCount()
        wordCounts.filter { it.value > 1 }.forEach { (word, _) ->
            if (word !in setOf("sein", "haben", "werden", "konnen", "mussen")) {
                // Find positions and suggest alternatives
            }
        }
        
        return errors
    }
    
    private fun analyzeVocabulary(sentence: String): VocabularyInsights {
        val words = sentence.split(Regex("\\s+|[^\\wÄäÖöÜüß]")).filter { it.isNotEmpty() }
        
        val wordUsages = words.map { word ->
            val baseWord = word.lowercase().replace(Regex("[^\\wäöüß]"), "")
            WordUsage(
                word = word,
                baseForm = baseWord,
                partOfSpeech = TextAnalysisUtils.estimatePartOfSpeech(word),
                difficulty = TextAnalysisUtils.estimateWordLevel(word),
                isLearned = false,
                timesEncountered = 1
            )
        }
        
        val wordGroups = wordUsages.groupBy { it.baseForm }
        val repeated = wordGroups.filter { it.value.size > 1 }
            .map { (word, usages) ->
                RepeatedWord(
                    word = word,
                    count = usages.size,
                    suggestions = TextAnalysisUtils.getSynonymSuggestions(word)
                )
            }
        
        val advanced = words.filter { it.length > 8 || it.contains(Regex("ung|heit|keit|schaft")) }
        
        val alternatives = mutableListOf<WordAlternative>()
        val commonWords = setOf("gut", "schon", "interessant", "wichtig", "groß")
        words.filter { it.lowercase() in commonWords }.forEach { word ->
            alternatives.add(
                WordAlternative(
                    original = word,
                    alternatives = TextAnalysisUtils.getAdvancedAlternatives(word.lowercase())
                )
            )
        }
        
        return VocabularyInsights(
            wordsUsed = wordUsages,
            advancedVocabulary = advanced,
            repeatedWords = repeated,
            suggestedAlternatives = alternatives,
            diversityScore = TextAnalysisUtils.calculateDiversity(words),
            complexityScore = advanced.size.toDouble() / words.size.coerceAtLeast(1)
        )
    }
    
    private fun detectStyleOpportunities(sentence: String): List<StyleSuggestion> {
        val suggestions = mutableListOf<StyleSuggestion>()
        
        val wordCount = sentence.split(" ").size
        if (wordCount > 25) {
            suggestions.add(
                StyleSuggestion(
                    type = StyleSuggestionType.CLARITY,
                    description = "This sentence is quite long. Consider breaking it into two.",
                    currentVersion = sentence.take(50) + "...",
                    suggestedVersion = "Split into: [First part]. [Second part].",
                    explanation = "Shorter sentences are clearer and easier to read."
                )
            )
        }
        
        if (sentence.contains(Regex("\\b(wurde|wurden|worden|sein|waren)\\b", RegexOption.IGNORE_CASE))) {
            suggestions.add(
                StyleSuggestion(
                    type = StyleSuggestionType.FLOW,
                    description = "Consider using active voice for more impact.",
                    currentVersion = sentence,
                    suggestedVersion = "[Active voice version]",
                    explanation = "Active voice makes your writing more direct and engaging."
                )
            )
        }
        
        val weakOpeners = setOf("ich denke", "ich glaube", "es ist", "da ist")
        weakOpeners.find { sentence.lowercase().startsWith(it) }?.let { opener ->
            suggestions.add(
                StyleSuggestion(
                    type = StyleSuggestionType.EMPHASIS,
                    description = "Consider a stronger opening than '$opener'",
                    currentVersion = sentence,
                    suggestedVersion = sentence.replace(opener, "", true).trim(),
                    explanation = "Stronger openings make your writing more confident."
                )
            )
        }
        
        return suggestions
    }
    
    private fun identifyLearningMoments(sentence: String): List<LearningOpportunity> {
        val opportunities = mutableListOf<LearningOpportunity>()
        
        if (sentence.contains(Regex("\\bvon\\s+dem\\b|\\bvon\\s+der\\b", RegexOption.IGNORE_CASE))) {
            opportunities.add(
                LearningOpportunity(
                    concept = "Genitive Case",
                    conceptType = ConceptType.GRAMMAR_PATTERN,
                    currentAttempt = sentence,
                    explanation = "'von dem/der' can often be replaced with the genitive case (des/der) for more formal German.",
                    examples = listOf(
                        "Das Buch von dem Mann → Das Buch des Mannes",
                        "Die Farbe von der Blume → Die Farbe der Blume"
                    ),
                    relatedExercises = emptyList(),
                    difficulty = CefrLevel.B1
                )
            )
        }
        
        if (sentence.contains(Regex("\\bwurde\\b|\\bware\\b", RegexOption.IGNORE_CASE))) {
            opportunities.add(
                LearningOpportunity(
                    concept = "Subjunctive II (Konjunktiv II)",
                    conceptType = ConceptType.GRAMMAR_PATTERN,
                    currentAttempt = sentence,
                    explanation = "You're using conditional forms. This is Konjunktiv II for hypothetical situations.",
                    examples = listOf(
                        "Ich würde gehen = I would go",
                        "Wenn ich Zeit hätte = If I had time"
                    ),
                    relatedExercises = emptyList(),
                    difficulty = CefrLevel.B2
                )
            )
        }
        
        val prepositions = mapOf(
            "mit" to "dative", "bei" to "dative", "nach" to "dative",
            "seit" to "dative", "von" to "dative", "zu" to "dative",
            "fur" to "accusative", "durch" to "accusative", "uber" to "accusative"
        )
        
        prepositions.forEach { (prep, case) ->
            if (sentence.contains(Regex("\\b$prep\\b", RegexOption.IGNORE_CASE))) {
                opportunities.add(
                    LearningOpportunity(
                        concept = "Preposition '$prep' + $case",
                        conceptType = ConceptType.COLLOCATION,
                        currentAttempt = sentence,
                        explanation = "'$prep' always takes the $case case.",
                        examples = listOf(
                            "mit $case: mit dem Hund, mit der Katze",
                            "Practice: Ich fahre $prep [case form]"
                        ),
                        relatedExercises = emptyList(),
                        difficulty = CefrLevel.A2
                    )
                )
            }
        }
        
        return opportunities
    }
    
    private fun suggestNextWords(sentence: String): List<NextWordSuggestion> {
        val suggestions = mutableListOf<NextWordSuggestion>()
        val lowerSentence = sentence.lowercase()
        
        when {
            lowerSentence.contains(Regex("\\b(weil|dass|ob)\\b")) -> {
                suggestions.add(
                    NextWordSuggestion(
                        suggestedWord = "Deswegen",
                        wordType = WordSuggestionType.CONTINUATION,
                        context = "Continue with a consequence",
                        meaning = "Therefore/That's why",
                        grammaticalCase = null,
                        exampleSentence = "Deswegen bin ich glücklich.",
                        difficulty = CefrLevel.B1,
                        confidence = 0.85
                    )
                )
            }
            
            lowerSentence.contains(Regex("\\b(erste|zuerst|anfangen)\\b")) -> {
                suggestions.add(
                    NextWordSuggestion(
                        suggestedWord = "Dann",
                        wordType = WordSuggestionType.TIME,
                        context = "Continue a sequence",
                        meaning = "Then",
                        grammaticalCase = null,
                        exampleSentence = "Dann gehe ich nach Hause.",
                        difficulty = CefrLevel.A1,
                        confidence = 0.90
                    )
                )
            }
            
            lowerSentence.contains(Regex("\\b(meinung|denke|glaube|finde)\\b")) -> {
                suggestions.add(
                    NextWordSuggestion(
                        suggestedWord = "Weil",
                        wordType = WordSuggestionType.CAUSE,
                        context = "Explain your reasoning",
                        meaning = "Because",
                        grammaticalCase = null,
                        exampleSentence = "Weil es mir gefällt.",
                        difficulty = CefrLevel.A2,
                        confidence = 0.85
                    )
                )
            }
            
            else -> {
                if (sentence.endsWith(".")) {
                    suggestions.add(
                        NextWordSuggestion(
                            suggestedWord = "Aber",
                            wordType = WordSuggestionType.CONTRAST,
                            context = "Introduce contrast",
                            meaning = "But",
                            grammaticalCase = null,
                            exampleSentence = "Aber ich bin nicht sicher.",
                            difficulty = CefrLevel.A1,
                            confidence = 0.70
                        )
                    )
                }
            }
        }
        
        return suggestions.sortedByDescending { it.confidence }
    }
    
    private fun analyzeStructure(sentence: String): StructureAnalysis {
        val words = sentence.split(" ")
        
        val sentenceType = when {
            sentence.endsWith("?") -> SentenceType.QUESTION
            sentence.endsWith("!") -> SentenceType.EXCLAMATION
            sentence.contains(",") && (sentence.contains("und") || sentence.contains("aber")) -> 
                SentenceType.COMPOUND
            sentence.contains(Regex("\\b(weil|dass|obwohl|wenn)\\b")) -> SentenceType.COMPLEX
            else -> SentenceType.STATEMENT
        }
        
        val clauses = mutableListOf<ClauseInfo>()
        val mainVerb = words.find { it.endsWith("e") || it.endsWith("t") } ?: ""
        val verbPosition = words.indexOfFirst { it.endsWith("e") || it.endsWith("t") } + 1
        
        clauses.add(
            ClauseInfo(
                clauseType = ClauseType.MAIN,
                mainVerb = mainVerb,
                verbPosition = if (sentenceType == SentenceType.QUESTION) 1 else verbPosition,
                subject = words.firstOrNull() ?: "",
                isSubordinate = false
            )
        )
        
        val verbPositionCorrect = when (sentenceType) {
            SentenceType.STATEMENT -> words.getOrNull(1)?.let { 
                it.endsWith("e") || it.endsWith("st") || it.endsWith("t") 
            } ?: false
            SentenceType.QUESTION -> words.firstOrNull()?.let {
                it.endsWith("e") || it.endsWith("st") || it.endsWith("t")
            } ?: false
            else -> true
        }
        
        return StructureAnalysis(
            sentenceType = sentenceType,
            clauses = clauses,
            wordOrderCorrect = verbPositionCorrect,
            verbPositionCorrect = verbPositionCorrect,
            caseUsage = emptyList()
        )
    }
}
