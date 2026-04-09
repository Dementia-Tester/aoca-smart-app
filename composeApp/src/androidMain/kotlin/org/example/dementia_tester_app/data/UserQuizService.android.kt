package org.example.dementia_tester_app.data

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual class UserQuizService actual constructor(private val type: UserQuizType) {
    private val database = Firebase.database.reference
    private val dbPath = type.dbPath
    private val questionTable = "Questions"
    private val userResponsesTable = "UserResponses"

    // For cognitive questions parsing
    private val MAX_OPTIONS = 10

    // Date formatter for readable timestamps
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // Helper to parse 'surveyComplete' stored as 1/0 or true/false or string variants
    private fun parseSurveyComplete(value: Any?): Boolean {
        return when (value) {
            is Number -> value.toInt() == 1
            is Boolean -> value
            is String -> {
                val v = value.trim().lowercase(Locale.getDefault())
                when (v) {
                    "1", "true" -> true
                    "0", "false" -> false
                    else -> v.toIntOrNull()?.let { it == 1 } ?: false
                }
            }
            else -> false
        }
    }

    // -------------------- Health Survey Specific functions --------------------

    /**
     * Fetches all Health Survey questions from Firebase.
     * @param callback Callback with Success (list of Question) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun fetchSurveyQuestions(callback: (DatabaseResult<List<Question>>) -> Unit) {
        database.child(dbPath).child(questionTable)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    callback(DatabaseResult.Error("Health survey questions not found"))
                    return@addOnSuccessListener
                }
                val questions = mutableListOf<Question>()
                snapshot.children.forEach { questionSnapshot ->
                    if (questionSnapshot.exists()) {
                        val questionId = questionSnapshot.key?.toIntOrNull() ?: return@forEach
                        val questionText = questionSnapshot.child("questionText").value?.toString() ?: ""
                        val domain = questionSnapshot.child("domain").value?.toString() ?: ""

                        // Get options
                        val options = mutableListOf<String>()
                        var optionIndex = 1
                        while (true) {
                            val optionKey = "option$optionIndex"
                            val optionChild = questionSnapshot.child(optionKey)
                            val optionValue = optionChild.value?.toString() ?: ""
                            if (optionValue.isNotEmpty()) {
                                options.add(optionValue)
                                optionIndex++
                            } else {
                                break
                            }
                        }
                        // Check if this question allows multiple answers
                        val allowMultipleAnswers = questionSnapshot.child("allowMultipleAnswers").value?.toString()?.toBoolean() ?: false

                        // Create a Question object
                        questions.add(
                            Question(
                                id = questionId,
                                domain = domain,
                                questionText = questionText,
                                options = options,
                                allowMultipleAnswers = allowMultipleAnswers,
                                cognitiveSituation = ""
                            )
                        )
                    }
                }

                if (questions.isEmpty()) {
                    callback(DatabaseResult.Error("No questions found in the database"))
                } else {
                    callback(DatabaseResult.Success(questions))
                }
            }
            .addOnFailureListener { e ->
                callback(DatabaseResult.Error("Failed to fetch questions: ${e.message}"))
            }
    }

    // -------------------- Cognitive assessment specific functions --------------------

    /**
     * Fetches all Cognitive Assessment questions from Firebase.
     * @param callback Callback with Success (list of CognitiveQuestion) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun fetchCognitiveQuestions(callback: (DatabaseResult<List<Question>>) -> Unit) {
        if (type != UserQuizType.CognitiveAssessment) {
            callback(DatabaseResult.Error("fetchCognitiveQuestions is only supported for CognitiveAssessment."))
            return
        }

        database.child(dbPath)
            .child(questionTable)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    callback(DatabaseResult.Success(emptyList()))
                    return@addOnSuccessListener
                }

                val questions = mutableListOf<Question>()

                // Process each question node
                snapshot.children.forEach { questionSnapshot ->
                    try {
                        val questionNumber = questionSnapshot.key?.toIntOrNull() ?: 0
                        if (questionNumber > 0) {
                            val questionText = questionSnapshot.child("questionText").value?.toString() ?: ""
                            val domain = questionSnapshot.child("domain").value?.toString() ?: ""
                            val cognitiveSituation = questionSnapshot.child("cognitiveSituation").value?.toString() ?: ""

                            // Parse options
                            val options = mutableMapOf<String, String>()
                            val optionsSnapshot = questionSnapshot.child("options")
                            if (optionsSnapshot.exists() && optionsSnapshot.hasChildren()) {
                                optionsSnapshot.children.forEach { optionSnapshot ->
                                    val optionKey = optionSnapshot.key ?: ""
                                    val optionValue = optionSnapshot.value?.toString() ?: ""
                                    options[optionKey] = optionValue
                                }
                            } else {
                                for (i in 1..MAX_OPTIONS) {
                                    val optionKey = "option$i"
                                    val optionValue = questionSnapshot.child(optionKey).value?.toString()
                                    if (optionValue != null) {
                                        options[optionKey] = optionValue
                                    }
                                }
                            }

                            // Create Question object
                            val sortedOptions = options.toSortedMap(compareBy { key ->
                                key.filter { it.isDigit() }.toIntOrNull() ?: Int.MAX_VALUE
                            }).values.toList()
                            val question = Question(
                                id = questionNumber,
                                questionText = questionText,
                                options = sortedOptions,
                                domain = domain,
                                cognitiveSituation = cognitiveSituation,
                                allowMultipleAnswers = false
                            )

                            questions.add(question)
                        }
                    } catch (e: Exception) {
                        println("Error parsing question: ${e.message}")
                    }
                }
                
                // Sort questions by id
                val sortedQuestions = questions.sortedBy { it.id }

                callback(DatabaseResult.Success(sortedQuestions))
            }
            .addOnFailureListener { e ->
                callback(DatabaseResult.Error("Failed to get questions: ${e.message}"))
            }
    }

    /**
     * Retrieves Cognitive Assessment questions for a specific attempt as CognitiveQuestion models.
     * Currently, returns the full list of questions (attemptNumber reserved for future filtering).
     * @param userId Unique identifier for the user.
     * @param attemptNumber Attempt number to load (currently unused for filtering).
     * @param callback Callback with Success (list of CognitiveQuestion) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun getCognitiveAttemptDetails(
        userId: String,
        attemptNumber: Int,
        callback: (DatabaseResult<List<Question>>) -> Unit
    ) {
        if (type != UserQuizType.CognitiveAssessment) {
            callback(DatabaseResult.Error("getCognitiveAttemptDetails is only for CognitiveAssessment"))
            return
        }
        fetchCognitiveQuestions { result ->
            when (result) {
                is DatabaseResult.Success -> callback(DatabaseResult.Success(result.data.sortedBy { it.id }))
                is DatabaseResult.Error -> callback(DatabaseResult.Error(result.message))
            }
        }
    }

    // -------------------- Shared functions --------------------

    /**
     * Retrieves all users scores results
     * @param userId Unique identifier for the user.
     * @param callback Callback with Success (list of UserResults) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun getUserScores(userId: String, callback: (DatabaseResult<List<UserResults>>) -> Unit) {
        database.child(dbPath)
            .child(userResponsesTable)
            .child(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    callback(DatabaseResult.Success(emptyList()))
                    return@addOnSuccessListener
                }

                if (type == UserQuizType.CognitiveAssessment) {
                    val userResultsList = mutableListOf<UserResults>()
                    snapshot.children.forEach { testSnapshot ->
                        try {
                            val attemptsList = mutableListOf<Attempt>()
                            testSnapshot.children.forEach { childSnapshot ->
                                val key = childSnapshot.key ?: ""
                                val keyAsInt = key.toIntOrNull()
                                if (keyAsInt != null) {
                                    val answer = childSnapshot.child("Answer").value?.toString() ?: ""
                                    val cognitiveSituation = childSnapshot.child("CognitiveSituation").value?.toString() ?: ""
                                    val domain = childSnapshot.child("Domain").value?.toString() ?: ""
                                    val score = childSnapshot.child("Score").value?.toString()?.toIntOrNull() ?: 0
                                    val timestamp = childSnapshot.child("Timestamp").value?.toString() ?: ""

                                    attemptsList.add(
                                        Attempt(
                                            answer = answer,
                                            cognitiveSituation = cognitiveSituation,
                                            domain = domain,
                                            score = score,
                                            timestamp = timestamp
                                        )
                                    )
                                }
                            }

                            // Parse cognitive situation scores
                            val cogSituationSnapshot = testSnapshot.child("CognitiveSituationScores")
                            val cognitiveSituationScores = CognitiveSituation(
                                correctiveMeasures = cogSituationSnapshot.child("Corrective Measures").value?.toString()?.toIntOrNull() ?: 0,
                                decisionMaking = cogSituationSnapshot.child("Decision Making").value?.toString()?.toIntOrNull() ?: 0,
                                emotionalIntelligence = cogSituationSnapshot.child("Emotional Intelligence").value?.toString()?.toIntOrNull() ?: 0,
                                memoryTasks = cogSituationSnapshot.child("Memory Tasks").value?.toString()?.toIntOrNull() ?: 0,
                                languageIssues = cogSituationSnapshot.child("Observing Language Issues").value?.toString()?.toIntOrNull() ?: 0,
                                processingTime = cogSituationSnapshot.child("Processing Time").value?.toString()?.toIntOrNull() ?: 0,
                                simpleInstructions = cogSituationSnapshot.child("Simple Instructions").value?.toString()?.toIntOrNull() ?: 0,
                                visualTasks = cogSituationSnapshot.child("Visual Tasks").value?.toString()?.toIntOrNull() ?: 0
                            )

                            // Parse domain scores
                            val domainSnapshot = testSnapshot.child("DomainScores")
                            val domainScores = Domain(
                                complexAttentions = domainSnapshot.child("Complex Attentions").value?.toString()?.toIntOrNull() ?: 0,
                                executiveFunction = domainSnapshot.child("Executive Function").value?.toString()?.toIntOrNull() ?: 0,
                                language = domainSnapshot.child("Language").value?.toString()?.toIntOrNull() ?: 0,
                                learningAndMemory = domainSnapshot.child("Learning and Memory").value?.toString()?.toIntOrNull() ?: 0,
                                perceptualMotor = domainSnapshot.child("Perceptual-motor").value?.toString()?.toIntOrNull() ?: 0,
                                socialCognition = domainSnapshot.child("Social Cognition").value?.toString()?.toIntOrNull() ?: 0
                            )

                            // Parse other fields
                            val lastUpdated = testSnapshot.child("Last Updated").value?.toString() ?: ""
                            val ncdCategory = testSnapshot.child("NCD Categorisation").value?.toString() ?: ""
                            val totalScore = testSnapshot.child("Total Score").value?.toString()?.toIntOrNull() ?: 0

                            // Create UserAttempts object
                            val userAttempts = UserAttempts(
                                attempts = attemptsList,
                                cognitiveSituationScores = cognitiveSituationScores,
                                domainScores = domainScores,
                                lastUpdated = lastUpdated,
                                ncdCategory = ncdCategory,
                                totalScore = totalScore,
                                type = type

                            )

                            // Add to the list of UserResults
                            userResultsList.add(UserResults(attempts = listOf(userAttempts)))

                        } catch (e: Exception) {
                            println("Error parsing test result: ${e.message}")
                        }
                    }

                    callback(DatabaseResult.Success(userResultsList))
                } else {
                    // Health Survey: return only completed attempts with their total scores
                    val userResultsList = mutableListOf<UserResults>()
                    snapshot.children.forEach { attemptSnapshot ->
                        val attemptKey = attemptSnapshot.key ?: return@forEach
                        if (!attemptKey.startsWith("Attempt_")) return@forEach
                        val isComplete = parseSurveyComplete(attemptSnapshot.child("surveyComplete").value)
                        if (!isComplete) return@forEach

                        val totalScore = attemptSnapshot.child("Total Score").value?.toString()?.toIntOrNull() ?: 0
                        val lastUpdated = attemptSnapshot.child("Last Updated").value?.toString()
                            ?: attemptSnapshot.child("timestamp").value?.toString() ?: ""

                        val userAttempts = UserAttempts(
                            attempts = emptyList(),
                            lastUpdated = lastUpdated,
                            totalScore = totalScore,
                            type = type
                        )
                        userResultsList.add(UserResults(attempts = listOf(userAttempts)))
                    }
                    callback(DatabaseResult.Success(userResultsList))
                }
            }
            .addOnFailureListener { e ->
                callback(DatabaseResult.Error("Failed to get scores: ${e.message}"))
            }
    }

    /**
     * Saves or clears a user's answer for a question in a specific attempt.
     * Also maintains attempt-level metadata (Last Updated) and Total Score incrementally.
     * For CognitiveAssessment, includes Domain and CognitiveSituation; for HealthSurvey mirrors the same structure.
     * @param userId Unique identifier for the user.
     * @param attemptNumber Attempt number (1-based).
     * @param question The Question containing the selected answer(s).
     * @param callback Callback with Success (Unit) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun saveUserAnswer(
    userId: String,
    attemptNumber: Int,
    question: Question,
    callback: (DatabaseResult<Unit>) -> Unit
    ) {
        val attemptRef = database.child(dbPath)
            .child(userResponsesTable)
            .child(userId)
            .child("Attempt_$attemptNumber")

        val nowStr = dateFormatter.format(Date())
        
        // Ensure attempt root meta is updated/initialized
        attemptRef.child("Last Updated").setValue(nowStr)
        // Initialize Total Score if missing (do not compute here)
        attemptRef.child("Total Score").get().addOnSuccessListener { snap ->
            if (!snap.exists()) attemptRef.child("Total Score").setValue(0)
        }
        
        val selected = when {
            !question.selectedAnswer.isNullOrEmpty() -> question.selectedAnswer!!
            question.selectedAnswers.isNotEmpty() -> question.selectedAnswers.joinToString(", ")
            else -> ""
        }
        val questionNodeRef = attemptRef.child(question.id.toString())
        
        if (selected.isEmpty()) {
            // If answer cleared, remove the node and decrement total score by previous score
            questionNodeRef.child("Score").get().addOnSuccessListener { scSnap ->
                val prevScore = scSnap.value?.toString()?.toIntOrNull() ?: 0
                questionNodeRef.removeValue()
                    .addOnSuccessListener {
                        attemptRef.child("Total Score").get().addOnSuccessListener { totalSnap ->
                            val currentTotal = totalSnap.value?.toString()?.toIntOrNull() ?: 0
                            val updatedTotal = (currentTotal - prevScore).coerceAtLeast(0)
                            attemptRef.child("Total Score").setValue(updatedTotal)
                                .addOnSuccessListener { callback(DatabaseResult.Success(Unit)) }
                                .addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to update total score: ${e.message}")) }
                        }.addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to read total score: ${e.message}")) }
                    }
                    .addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to remove answer: ${e.message}")) }
            }.addOnFailureListener { _ ->
                // Fallback: remove without total adjustment
                questionNodeRef.removeValue()
                    .addOnSuccessListener { callback(DatabaseResult.Success(Unit)) }
                    .addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to remove answer: ${e.message}")) }
            }
            return
        }
        
        // Compute new score depending on quiz type
        val newScore = if (type == UserQuizType.CognitiveAssessment) {
            getCognitiveScore(selected)
        } else {
            getSurveyScore(selected, question.allowMultipleAnswers)
        }
        
        // Read previous score before overwriting
        questionNodeRef.child("Score").get().addOnSuccessListener { prevSnap ->
            val prevScore = prevSnap.value?.toString()?.toIntOrNull() ?: 0
            if (type == UserQuizType.CognitiveAssessment) {
                // Load extra meta from the question table
                database.child(dbPath).child(questionTable).child(question.id.toString())
                    .get()
                    .addOnSuccessListener { qSnap ->
                        val domain = qSnap.child("domain").value?.toString() ?: question.domain
                        val cognitiveSituation = qSnap.child("cognitiveSituation").value?.toString() ?: ""
                        val data = hashMapOf<String, Any>(
                            "Answer" to selected,
                            "Domain" to domain,
                            "CognitiveSituation" to cognitiveSituation,
                            "Score" to newScore,
                            "Timestamp" to nowStr
                        )
                        questionNodeRef.setValue(data)
                            .addOnSuccessListener {
                                val delta = newScore - prevScore
                                attemptRef.child("Total Score").get().addOnSuccessListener { totalSnap ->
                                    val currentTotal = totalSnap.value?.toString()?.toIntOrNull() ?: 0
                                    val updatedTotal = (currentTotal + delta).coerceAtLeast(0)
                                    attemptRef.child("Total Score").setValue(updatedTotal)
                                        .addOnSuccessListener { callback(DatabaseResult.Success(Unit)) }
                                        .addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to update total score: ${e.message}")) }
                                }.addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to read total score: ${e.message}")) }
                            }
                            .addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to save answer: ${e.message}")) }
                    }
                    .addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to load question meta: ${e.message}")) }
            } else {
                // Health Survey: mirror cognitive structure (Answer, Score, Timestamp)
                val data = hashMapOf<String, Any>(
                    "Answer" to selected,
                    "Score" to newScore,
                    "Timestamp" to nowStr
                )
                questionNodeRef.setValue(data)
                    .addOnSuccessListener {
                        val delta = newScore - prevScore
                        attemptRef.child("Total Score").get().addOnSuccessListener { totalSnap ->
                            val currentTotal = totalSnap.value?.toString()?.toIntOrNull() ?: 0
                            val updatedTotal = (currentTotal + delta).coerceAtLeast(0)
                            attemptRef.child("Total Score").setValue(updatedTotal)
                                .addOnSuccessListener { callback(DatabaseResult.Success(Unit)) }
                                .addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to update total score: ${e.message}")) }
                        }.addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to read total score: ${e.message}")) }
                    }
                    .addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to save answer: ${e.message}")) }
            }
        }.addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to read previous score: ${e.message}")) }
    }

    /**
     * Gets the current latest attempt number for a given user.
     * If no attempts exist, returns 1.
     * @param userId Unique identifier for the user.
     * @param callback Callback with Success(current attempt number) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun getLatestAttemptNumber(
        userId: String,
        callback: (DatabaseResult<Int>) -> Unit
    ) {
        val userRef = database.child(dbPath)
            .child(userResponsesTable)
            .child(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                callback(DatabaseResult.Success(1))
                return@addOnSuccessListener
            }
            var maxAttempt = 0
            snapshot.children.forEach { attemptSnapshot ->
                val key = attemptSnapshot.key
                if (key != null && key.startsWith("Attempt_")) {
                    val attemptNum = key.substringAfter("Attempt_").toIntOrNull()
                    if (attemptNum != null) maxAttempt = maxOf(maxAttempt, attemptNum)
                }
            }
            val currentAttempt = if (maxAttempt == 0) 1 else maxAttempt
            callback(DatabaseResult.Success(currentAttempt))
        }.addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to get latest attempt number: ${e.message}")) }
    }

    /**
     * Finalizes an attempt by marking it complete for the current quiz type and returns the next attempt number.
     * Also updates the Last-Updated timestamp.
     * @param userId Unique identifier for the user.
     * @param currentAttemptNumber Attempt number to finalize.
     * @param callback Callback with Success(next attempt number) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun finalizeAttempt(
        userId: String,
        currentAttemptNumber: Int,
        callback: (DatabaseResult<Int>) -> Unit
    ) {
        val attemptRef = database.child(dbPath)
            .child(userResponsesTable)
            .child(userId)
            .child("Attempt_$currentAttemptNumber")
        val nowStr = dateFormatter.format(Date())

        attemptRef.child("Last Updated").setValue(nowStr)
        if (type == UserQuizType.CognitiveAssessment) {
            attemptRef.child("quizComplete").setValue(1)
                .addOnSuccessListener {
                    callback(DatabaseResult.Success(currentAttemptNumber + 1))
                }
                .addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to finalize attempt: ${e.message}")) }
        } else {
            attemptRef.child("surveyComplete").setValue(1)
                .addOnSuccessListener {
                    callback(DatabaseResult.Success(currentAttemptNumber + 1))
                }
                .addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to finalize attempt: ${e.message}")) }
        }
    }

    /**
     * Checks whether a user has any attempts stored for the current quiz type.
     * @param userId Unique identifier for the user.
     * @param callback Callback with Success (true/false) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun hasUserAttempts(
        userId: String,
        callback: (DatabaseResult<Boolean>) -> Unit
    ) {
        val userRef = database.child(dbPath)
            .child(userResponsesTable)
            .child(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            callback(DatabaseResult.Success(snapshot.exists() && snapshot.hasChildren()))
        }.addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to check user attempts: ${e.message}")) }
    }

    /**
     * Retrieves a summary of all attempts for the current quiz type for a user.
     * The summary includes attempt number, timestamp, completion status, and questions completed.
     * @param userId Unique identifier for the user.
     * @param callback Callback with Success (list of AttemptSummary) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun getUserAttempts(
        userId: String,
        callback: (DatabaseResult<List<AttemptSummary>>) -> Unit
    ) {
        when (type) {
            UserQuizType.HealthSurvey -> getUserAttemptsHealth(userId, callback)
            UserQuizType.CognitiveAssessment -> getUserAttemptsCognitive(userId, callback)
        }
    }

    /**
     * Retrieves detailed questions for a specific attempt, including any saved answers.
     * Dispatches to the appropriate quiz type (Health Survey or Cognitive Assessment).
     * @param userId Unique identifier for the user.
     * @param attemptNumber Attempt number to load.
     * @param callback Callback with Success (list of SurveyQuestion with selections) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun getAttemptDetails(
        userId: String,
        attemptNumber: Int,
        callback: (DatabaseResult<List<Question>>) -> Unit
    ) {
        when (type) {
            UserQuizType.HealthSurvey -> getAttemptDetailsHealth(userId, attemptNumber, callback)
            UserQuizType.CognitiveAssessment -> getAttemptDetailsCognitive(userId, attemptNumber, callback)
        }
    }

    /**
     * Marks a given attempt as complete for the current quiz type and updates Last Updated.
     * @param userId Unique identifier for the user.
     * @param attemptNumber Attempt number to mark complete.
     * @param callback Callback with Success (Unit) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun markAttemptComplete(
    userId: String,
    attemptNumber: Int,
    callback: (DatabaseResult<Unit>) -> Unit
    ) {
        val attemptRef = database.child(dbPath)
            .child(userResponsesTable)
            .child(userId)
            .child("Attempt_$attemptNumber")
        val nowStr = dateFormatter.format(Date())
        attemptRef.child("Last Updated").setValue(nowStr)
        if (type == UserQuizType.CognitiveAssessment) {
            attemptRef.child("quizComplete").setValue(1)
                .addOnSuccessListener { callback(DatabaseResult.Success(Unit)) }
                .addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to mark attempt complete: ${e.message}")) }
        } else {
            attemptRef.child("surveyComplete").setValue(1)
                .addOnSuccessListener { callback(DatabaseResult.Success(Unit)) }
                .addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to mark attempt complete: ${e.message}")) }
        }
    }

    /**
     * Marks a given attempt as incomplete for the current quiz type and updates Last Updated.
     * If already complete, this may no-op depending on the quiz type.
     * @param userId Unique identifier for the user.
     * @param attemptNumber Attempt number to mark incomplete.
     * @param callback Callback with Success (Unit) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun markAttemptIncomplete(
        userId: String,
        attemptNumber: Int,
        callback: (DatabaseResult<Unit>) -> Unit
    ) {
        val attemptRef = database.child(dbPath)
            .child(userResponsesTable)
            .child(userId)
            .child("Attempt_$attemptNumber")
        val nowStr = dateFormatter.format(Date())
        attemptRef.child("Last Updated").setValue(nowStr)
        if (type == UserQuizType.CognitiveAssessment) {
            attemptRef.child("quizComplete").get().addOnSuccessListener { snap ->
                val isComplete = parseSurveyComplete(snap.value)
                if (isComplete) {
                    attemptRef.child("quizComplete").setValue(0)
                        .addOnSuccessListener { callback(DatabaseResult.Success(Unit)) }
                        .addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to mark attempt incomplete: ${e.message}")) }
                } else {
                    callback(DatabaseResult.Success(Unit))
                }
            }.addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to mark attempt incomplete: ${e.message}")) }
        } else {
            attemptRef.child("surveyComplete").get().addOnSuccessListener { snap ->
                val isComplete = parseSurveyComplete(snap.value)
                if (isComplete) {
                    attemptRef.child("surveyComplete").setValue(0)
                        .addOnSuccessListener { callback(DatabaseResult.Success(Unit)) }
                        .addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to mark attempt incomplete: ${e.message}")) }
                } else {
                    callback(DatabaseResult.Success(Unit))
                }
            }.addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to mark attempt incomplete: ${e.message}")) }
        }
    }

    /**
     * Determines the next unanswered question index (0-based) for a user's attempt.
     * If all questions are answered, returns the last index (totalQuestions - 1).
     * @param userId Unique identifier for the user.
     * @param attemptNumber Attempt number to inspect.
     * @param totalQuestions Total number of questions available.
     * @param callback Callback with Success(next unanswered index) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun getNextUnansweredQuestionIndex(
        userId: String,
        attemptNumber: Int,
        totalQuestions: Int,
        callback: (DatabaseResult<Int>) -> Unit
    ) {
        val attemptRef = database.child(dbPath)
            .child(userResponsesTable)
            .child(userId)
            .child("Attempt_$attemptNumber")
        attemptRef.get().addOnSuccessListener { snapshot ->
            val answeredIds = mutableSetOf<Int>()
            snapshot.children.forEach { child ->
                val key = child.key ?: return@forEach
                if (key.all { it.isDigit() }) key.toIntOrNull()?.let { answeredIds.add(it) }
            }
            for (i in 1..totalQuestions) {
                if (!answeredIds.contains(i)) {
                    callback(DatabaseResult.Success(i - 1)); return@addOnSuccessListener
                }
            }
            callback(DatabaseResult.Success(totalQuestions - 1))
        }.addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to get next unanswered question: ${e.message}")) }
    }

    // -------------------- Private helpers --------------------

    /**
     * Builds AttemptSummary list for Health Survey attempts for a user.
     * @param userId Unique identifier for the user.
     * @param callback Callback with Success (list of AttemptSummary) or Error(message).
     */
    private fun getUserAttemptsHealth(userId: String, callback: (DatabaseResult<List<AttemptSummary>>) -> Unit) {
        val userRef = database.child(dbPath)
            .child(userResponsesTable)
            .child(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists() || !snapshot.hasChildren()) {
                callback(DatabaseResult.Success(emptyList()))
                return@addOnSuccessListener
            }
            val attempts = mutableListOf<AttemptSummary>()
            fetchSurveyQuestions { questionsResult ->
                when (questionsResult) {
                    is DatabaseResult.Success -> {
                        val totalQuestions = questionsResult.data.size
                        snapshot.children.forEach { attemptSnapshot ->
                            val attemptKey = attemptSnapshot.key ?: ""
                            if (attemptKey.startsWith("Attempt_")) {
                                val attemptNumber = attemptKey.removePrefix("Attempt_").toIntOrNull() ?: 0
                                val timestamp = attemptSnapshot.child("Last Updated").value?.toString()
                                    ?: attemptSnapshot.child("timestamp").value?.toString() ?: ""
                                val surveyComplete = parseSurveyComplete(attemptSnapshot.child("surveyComplete").value)
                                var questionsCompleted = 0
                                attemptSnapshot.children.forEach { child ->
                                    val key = child.key ?: ""
                                    if (key.all { it.isDigit() }) {
                                        questionsCompleted++
                                    }
                                }
                                val totalScore = attemptSnapshot.child("Total Score").value?.toString()?.toIntOrNull() ?: 0
                                attempts.add(
                                    AttemptSummary(
                                        attemptNumber = attemptNumber,
                                        timestamp = timestamp,
                                        surveyComplete = surveyComplete,
                                        questionsCompleted = questionsCompleted,
                                        totalQuestions = totalQuestions,
                                        totalScore = totalScore
                                    )
                                )
                            }
                        }
                        attempts.sortWith(compareBy<AttemptSummary> { it.surveyComplete }.thenByDescending { it.attemptNumber })
                        callback(DatabaseResult.Success(attempts))
                    }
                    is DatabaseResult.Error -> callback(DatabaseResult.Error("Failed to fetch questions: ${questionsResult.message}"))
                }
            }
        }.addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to get user attempts: ${e.message}")) }
    }

    /**
     * Builds AttemptSummary list for Cognitive Assessment attempts for a user.
     * @param userId Unique identifier for the user.
     * @param callback Callback with Success (list of AttemptSummary) or Error(message).
     */
    private fun getUserAttemptsCognitive(userId: String, callback: (DatabaseResult<List<AttemptSummary>>) -> Unit) {
    val userRef = database.child(dbPath)
        .child(userResponsesTable)
        .child(userId)
    userRef.get().addOnSuccessListener { snapshot ->
        if (!snapshot.exists() || !snapshot.hasChildren()) {
            callback(DatabaseResult.Success(emptyList()))
            return@addOnSuccessListener
        }
        fetchCognitiveQuestions { qRes ->
            when (qRes) {
                is DatabaseResult.Success -> {
                    val totalQuestions = qRes.data.size
                    val attempts = mutableListOf<AttemptSummary>()
                    snapshot.children.forEach { attemptSnapshot ->
                        val attemptKey = attemptSnapshot.key ?: ""
                        if (attemptKey.startsWith("Attempt_")) {
                            val attemptNumber = attemptKey.removePrefix("Attempt_").toIntOrNull() ?: 0
                            val timestamp = attemptSnapshot.child("Last Updated").value?.toString() ?: ""
                            val surveyComplete = parseSurveyComplete(
                                attemptSnapshot.child("quizComplete").value ?: attemptSnapshot.child("Completed").value
                            )
                            var questionsCompleted = 0
                            attemptSnapshot.children.forEach { child ->
                                val key = child.key ?: ""
                                if (key.all { it.isDigit() }) {
                                    questionsCompleted++
                                }
                            }
                            val totalScore = attemptSnapshot.child("Total Score").value?.toString()?.toIntOrNull() ?: 0
                            attempts.add(
                                AttemptSummary(
                                    attemptNumber = attemptNumber,
                                    timestamp = timestamp,
                                    surveyComplete = surveyComplete,
                                    questionsCompleted = questionsCompleted,
                                    totalQuestions = totalQuestions,
                                    totalScore = totalScore
                                )
                            )
                        }
                    }
                    attempts.sortWith(compareBy<AttemptSummary> { it.surveyComplete }.thenByDescending { it.attemptNumber })
                    callback(DatabaseResult.Success(attempts))
                }
                is DatabaseResult.Error -> callback(DatabaseResult.Error("Failed to fetch questions: ${qRes.message}"))
            }
        }
    }.addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to get user tests: ${e.message}")) }
}

    private fun getAttemptDetailsHealth(userId: String, attemptNumber: Int, callback: (DatabaseResult<List<Question>>) -> Unit) {
        val attemptRef = database.child(dbPath)
            .child(userResponsesTable)
            .child(userId)
            .child("Attempt_$attemptNumber")
        database.child(dbPath).child(questionTable)
            .get()
            .addOnSuccessListener { questionsSnapshot ->
                val questions = mutableListOf<Question>()
                questionsSnapshot.children.forEach { questionSnapshot ->
                    val questionId = questionSnapshot.key?.toIntOrNull() ?: return@forEach
                    val questionText = questionSnapshot.child("questionText").value?.toString() ?: ""
                    val domain = questionSnapshot.child("domain").value?.toString() ?: ""
                    val options = mutableListOf<String>()
                    var optionIndex = 1
                    while (true) {
                        val optionKey = "option$optionIndex"
                        val optionValue = questionSnapshot.child(optionKey).value?.toString() ?: ""
                        if (optionValue.isNotEmpty()) {
                            options.add(optionValue)
                            optionIndex++
                        } else break
                    }
                    val allowMultipleAnswers = questionSnapshot.child("allowMultipleAnswers").value?.toString()?.toBoolean() ?: false
                    questions.add(
                        Question(
                            id = questionId,
                            domain = domain,
                            questionText = questionText,
                            options = options,
                            allowMultipleAnswers = allowMultipleAnswers,
                            cognitiveSituation = ""
                        )
                    )
                }
                attemptRef.get().addOnSuccessListener { attemptSnapshot ->
                    val updatedQuestions = questions.map { q ->
                        val qNode = attemptSnapshot.child(q.id.toString())
                        val answerFromField = qNode.child("Answer").value?.toString()
                        val legacyValue = qNode.value?.toString()
                        val legacyChildren = mutableListOf<String>()
                        qNode.children.forEach { child ->
                            val v = child.value?.toString(); if (!v.isNullOrEmpty()) legacyChildren.add(v)
                        }
                        val score = qNode.child("Score").value?.toString()?.toIntOrNull() ?: 0
                        if (q.allowMultipleAnswers) {
                            val answers = when {
                                !answerFromField.isNullOrEmpty() -> answerFromField.split(", ").map { it.trim() }.filter { it.isNotEmpty() }
                                legacyChildren.isNotEmpty() -> legacyChildren
                                !legacyValue.isNullOrEmpty() -> legacyValue.split(", ").map { it.trim() }.filter { it.isNotEmpty() }
                                else -> emptyList()
                            }
                            q.copy(selectedAnswers = answers.toMutableList(), score = score)
                        } else {
                            val single = when {
                                !answerFromField.isNullOrEmpty() -> answerFromField
                                !legacyValue.isNullOrEmpty() -> legacyValue
                                else -> null
                            }
                            q.copy(selectedAnswer = single, score = score)
                        }
                    }
                    callback(DatabaseResult.Success(updatedQuestions))
                }.addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to fetch attempt details: ${e.message}")) }
            }
            .addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to fetch questions: ${e.message}")) }
    }

    private fun getAttemptDetailsCognitive(userId: String, attemptNumber: Int, callback: (DatabaseResult<List<Question>>) -> Unit) {
        val attemptRef = database.child(dbPath)
            .child(userResponsesTable)
            .child(userId)
            .child("Attempt_$attemptNumber")
        fetchCognitiveQuestions { result ->
            when (result) {
                is DatabaseResult.Success -> {
                    attemptRef.get().addOnSuccessListener { attemptSnap ->
                        val updated = result.data.map { q ->
                            val node = attemptSnap.child(q.id.toString())
                            val answer = node.child("Answer").value?.toString()
                            val score = node.child("Score").value?.toString()?.toIntOrNull() ?: 0
                            q.copy(selectedAnswer = answer, score = score)
                        }
                        callback(DatabaseResult.Success(updated))
                    }.addOnFailureListener { e -> callback(DatabaseResult.Error("Failed to fetch attempt details: ${e.message}")) }
                }
                is DatabaseResult.Error -> callback(DatabaseResult.Error(result.message))
            }
        }
    }
}
