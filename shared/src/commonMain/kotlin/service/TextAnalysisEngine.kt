package service

import data.model.*
import kotlinx.datetime.Clock

/**
 * Engine that performs local text analysis for German writing.
 * Contains all analysis logic: grammar detection, vocabulary analysis,
 * style suggestions, learning opportunities, and structure analysis.
 */
class TextAnalysisEngine {

    /**
     * Perform local analysis of the sentence.
     * This is a simplified version - in production, this would use
     * the OllamaService for AI-powered analysis.
     */
    fun performLocalAnalysis(sentence: String): WritingAnalysis {
        val now = Clock.System.now()

        // Basic pattern detection
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
            sentenceLevel = estimateLevel(sentence),
            score = calculateScore(sentence, grammarErrors)
        )
    }

    private fun detectBasicErrors(sentence: String): List<SentenceGrammarError> {
        val errors = mutableListOf<SentenceGrammarError>()

        // Check for common beginner mistakes

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

        // Check word order (verb should be second position in statements)
        if (sentence.split(" ").size > 3) {
            // Simple check: if sentence starts with time/place and verb isn't second
            val words = sentence.split(" ")
            val firstWord = words.firstOrNull()?.lowercase() ?: ""

            if (firstWord in setOf("gestern", "heute", "morgen", "jetzt", "hier", "dort", "im", "in")) {
                // If there's no verb in second position, flag it
                val secondWord = words.getOrNull(1)?.lowercase() ?: ""
                if (secondWord.isNotEmpty() && !secondWord.endsWith("e") && !secondWord.endsWith("t")) {
                    // This is a simplified check
                    // In real implementation, use proper POS tagging
                }
            }
        }

        // Check for repeated words
        val words = sentence.lowercase().split(Regex("\\s+")).filter { it.length > 3 }
        val wordCounts = words.groupingBy { it }.eachCount()
        wordCounts.filter { it.value > 1 }.forEach { (word, _) ->
            // Only flag if it's not a common word
            if (word !in setOf("sein", "haben", "werden", "konnen", "mussen")) {
                // Find positions and suggest alternatives
                // Implementation would be more sophisticated
            }
        }

        return errors
    }

    private fun analyzeVocabulary(sentence: String): VocabularyInsights {
        val words = sentence.split(Regex("\\s+|[^\\wÄäÖöÜüß]")).filter { it.isNotEmpty() }

        // Estimate difficulty based on word length and patterns
        val wordUsages = words.map { word ->
            val baseWord = word.lowercase().replace(Regex("[^\\wäöüß]"), "")
            WordUsage(
                word = word,
                baseForm = baseWord,
                partOfSpeech = estimatePartOfSpeech(word),
                difficulty = estimateWordLevel(word),
                isLearned = false, // Would check against user's learned words
                timesEncountered = 1
            )
        }

        // Find repeated words (simplified)
        val wordGroups = wordUsages.groupBy { it.baseForm }
        val repeated = wordGroups.filter { it.value.size > 1 }
            .map { (word, usages) ->
                RepeatedWord(
                    word = word,
                    count = usages.size,
                    suggestions = getSynonymSuggestions(word)
                )
            }

        // Find "advanced" words (longer or with specific patterns)
        val advanced = words.filter { it.length > 8 || it.contains(Regex("ung|heit|keit|schaft")) }

        // Suggest alternatives for common words
        val alternatives = mutableListOf<WordAlternative>()
        val commonWords = setOf("gut", "schon", "interessant", "wichtig", "groß")
        words.filter { it.lowercase() in commonWords }.forEach { word ->
            alternatives.add(
                WordAlternative(
                    original = word,
                    alternatives = getAdvancedAlternatives(word.lowercase())
                )
            )
        }

        return VocabularyInsights(
            wordsUsed = wordUsages,
            advancedVocabulary = advanced,
            repeatedWords = repeated,
            suggestedAlternatives = alternatives,
            diversityScore = calculateDiversity(words),
            complexityScore = advanced.size.toDouble() / words.size.coerceAtLeast(1)
        )
    }

    private fun detectStyleOpportunities(sentence: String): List<StyleSuggestion> {
        val suggestions = mutableListOf<StyleSuggestion>()

        // Check sentence length
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

        // Check for passive voice (simplified)
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

        // Check for weak openings
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

        // Check for patterns that indicate learning opportunities

        // Genitive case opportunity
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

        // Subjunctive opportunity
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

        // Preposition + case patterns
        val prepositions = mapOf(
            "mit" to "dative",
            "bei" to "dative",
            "nach" to "dative",
            "seit" to "dative",
            "von" to "dative",
            "zu" to "dative",
            "fur" to "accusative",
            "durch" to "accusative",
            "uber" to "accusative"
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

        // Determine context and suggest appropriate connectors
        when {
            lowerSentence.contains(Regex("\\b(weil|dass|ob)\\b")) -> {
                // After subordinate clause - main clause should follow
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
                suggestions.add(
                    NextWordSuggestion(
                        suggestedWord = "Danach",
                        wordType = WordSuggestionType.TIME,
                        context = "Continue a sequence (alternative)",
                        meaning = "After that",
                        grammaticalCase = null,
                        exampleSentence = "Danach esse ich zu Abend.",
                        difficulty = CefrLevel.A2,
                        confidence = 0.80
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
                suggestions.add(
                    NextWordSuggestion(
                        suggestedWord = "Außerdem",
                        wordType = WordSuggestionType.CONTINUATION,
                        context = "Add another point",
                        meaning = "Besides/Moreover",
                        grammaticalCase = null,
                        exampleSentence = "Außerdem ist es nicht teuer.",
                        difficulty = CefrLevel.B1,
                        confidence = 0.75
                    )
                )
            }

            else -> {
                // Generic suggestions based on sentence type
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
                    suggestions.add(
                        NextWordSuggestion(
                            suggestedWord = "Deswegen",
                            wordType = WordSuggestionType.CONTINUATION,
                            context = "Show consequence",
                            meaning = "Therefore",
                            grammaticalCase = null,
                            exampleSentence = "Deswegen habe ich gekauft.",
                            difficulty = CefrLevel.B1,
                            confidence = 0.65
                        )
                    )
                }
            }
        }

        return suggestions.sortedByDescending { it.confidence }
    }

    private fun analyzeStructure(sentence: String): StructureAnalysis {
        val words = sentence.split(" ")

        // Simple sentence type detection
        val sentenceType = when {
            sentence.endsWith("?") -> SentenceType.QUESTION
            sentence.endsWith("!") -> SentenceType.EXCLAMATION
            sentence.contains(",") && (sentence.contains("und") || sentence.contains("aber")) ->
                SentenceType.COMPOUND
            sentence.contains(Regex("\\b(weil|dass|obwohl|wenn)\\b")) -> SentenceType.COMPLEX
            else -> SentenceType.STATEMENT
        }

        // Simple clause detection
        val clauses = mutableListOf<ClauseInfo>()
        if (sentence.contains(",")) {
            // Likely has multiple clauses
            clauses.add(
                ClauseInfo(
                    clauseType = ClauseType.MAIN,
                    mainVerb = words.find { it.endsWith("e") || it.endsWith("t") } ?: "",
                    verbPosition = words.indexOfFirst { it.endsWith("e") || it.endsWith("t") } + 1,
                    subject = words.firstOrNull() ?: "",
                    isSubordinate = false
                )
            )
        } else {
            clauses.add(
                ClauseInfo(
                    clauseType = ClauseType.MAIN,
                    mainVerb = words.find { it.endsWith("e") || it.endsWith("t") } ?: "",
                    verbPosition = if (sentenceType == SentenceType.QUESTION) 1 else
                        words.indexOfFirst { it.endsWith("e") || it.endsWith("t") } + 1,
                    subject = words.firstOrNull() ?: "",
                    isSubordinate = false
                )
            )
        }

        // Check verb position (simplified)
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
            caseUsage = emptyList() // Would need detailed parsing
        )
    }

    private fun estimateLevel(sentence: String): CefrLevel {
        val factors = listOf(
            sentence.split(" ").size > 15,  // Longer sentences = higher level
            sentence.contains(Regex("\\b(weil|dass|obwohl|damit|sodass)\\b")), // Subordinate
            sentence.contains(Regex("\\b(wurde|hatte|ware)\\b")), // Past/subjunctive
            sentence.contains(Regex("\\b(deren|dessen|wo)\\b")), // Relative clauses
            sentence.contains(","), // Complex structure
        )

        val score = factors.count { it }
        return when (score) {
            0 -> CefrLevel.A1
            1 -> CefrLevel.A2
            2 -> CefrLevel.B1
            3, 4 -> CefrLevel.B2
            else -> CefrLevel.C1
        }
    }

    private fun calculateScore(sentence: String, errors: List<SentenceGrammarError>): Double {
        val baseScore = 100.0
        val errorPenalty = errors.sumOf {
            when (it.severity) {
                ErrorSeverity.LOW -> 2.0
                ErrorSeverity.MEDIUM -> 5.0
                ErrorSeverity.HIGH -> 10.0
                ErrorSeverity.CRITICAL -> 20.0
            }
        }
        return (baseScore - errorPenalty).coerceAtLeast(0.0)
    }

    private fun estimatePartOfSpeech(word: String): PartOfSpeech {
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

    private fun estimateWordLevel(word: String): CefrLevel {
        return when {
            word.length <= 4 -> CefrLevel.A1
            word.length <= 6 -> CefrLevel.A2
            word.length <= 10 -> CefrLevel.B1
            word.contains(Regex("ung|heit|keit|schaft")) -> CefrLevel.B2
            else -> CefrLevel.C1
        }
    }

    private fun getSynonymSuggestions(word: String): List<String> {
        return when (word.lowercase()) {
            "gut" -> listOf("exzellent", "hervorragend", "prima")
            "schon" -> listOf("wunderschön", "hübsch", "eindrucksvoll")
            "interessant" -> listOf("fesselnd", "spannend", "faszinierend")
            "wichtig" -> listOf("bedeutsam", "entscheidend", "wesentlich")
            "groß" -> listOf("enorm", "beträchtlich", "massiv")
            else -> emptyList()
        }
    }

    private fun getAdvancedAlternatives(word: String): List<AlternativeWithContext> {
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

    private fun calculateDiversity(words: List<String>): Double {
        val uniqueWords = words.map { it.lowercase() }.toSet()
        return uniqueWords.size.toDouble() / words.size.coerceAtLeast(1)
    }
}
