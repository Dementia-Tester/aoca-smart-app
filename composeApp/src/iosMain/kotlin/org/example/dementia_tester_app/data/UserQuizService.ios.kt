package org.example.dementia_tester_app.data

import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import platform.Foundation.NSDictionary
import platform.Foundation.NSError
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.allKeys
import platform.Foundation.NSEnumerator

actual class UserQuizService actual constructor(private val type: UserQuizType) {
    private val dbPath = type.dbPath
    private val questionTable = "Questions"
    private val userResponsesTable = "UserResponses"

    // -------------------- Health Survey Specific functions --------------------

    /**
     * Fetches all Health Survey questions from Firebase.
     * @param callback Callback with Success (list of SurveyQuestion) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun fetchSurveyQuestions(callback: (DatabaseResult<List<Question>>) -> Unit) {
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) {
            callback(DatabaseResult.Error("Firebase not initialized")); return
        }
        ref.child(dbPath).child(questionTable).getDataWithCompletionBlock { error: NSError?, snapshot: FIRDataSnapshot? ->
            if (error != null) { callback(DatabaseResult.Error("Failed to fetch questions: ${error.localizedDescription}")); return@getDataWithCompletionBlock }
            if (snapshot == null || !snapshot.exists()) { callback(DatabaseResult.Error("Health survey questions not found")); return@getDataWithCompletionBlock }
            val result = parseSurveyQuestions(snapshot)
            if (result.isEmpty()) callback(DatabaseResult.Error("No questions found in the database")) else callback(DatabaseResult.Success(result))
        }
    }

    // -------------------- Cognitive assessment specific functions --------------------

    /**
     * Fetches all Cognitive Assessment questions from Firebase.
     * @param callback Callback with Success (list of CognitiveQuestion) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun fetchCognitiveQuestions(callback: (DatabaseResult<List<Question>>) -> Unit) {
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }
        ref.child(dbPath).child(questionTable).getDataWithCompletionBlock { error: NSError?, snapshot: FIRDataSnapshot? ->
            if (error != null) { callback(DatabaseResult.Error("Failed to get questions: ${error.localizedDescription}")); return@getDataWithCompletionBlock }
            if (snapshot == null || !snapshot.exists()) { callback(DatabaseResult.Success(emptyList())); return@getDataWithCompletionBlock }
            val list = parseCognitiveQuestions(snapshot).sortedBy { it.id }
            callback(DatabaseResult.Success(list))
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
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }
        ref.child(dbPath).child(userResponsesTable).child(userId).getDataWithCompletionBlock { error: NSError?, snapshot: FIRDataSnapshot? ->
            if (error != null) { callback(DatabaseResult.Error("Failed to get scores: ${error.localizedDescription}")); return@getDataWithCompletionBlock }
            if (snapshot == null || !snapshot.exists() || !snapshot.hasChildren()) { callback(DatabaseResult.Success(emptyList())); return@getDataWithCompletionBlock }

            val value = snapshot.value
            val attemptsRoot = when (value) {
                is Map<*, *> -> value
                is NSDictionary -> nsDictionaryToKotlinMap(value)
                else -> emptyMap()
            }

            if (type == UserQuizType.CognitiveAssessment) {
                val userResultsList = mutableListOf<UserResults>()
                for ((k, v) in attemptsRoot) {
                    val key = k?.toString() ?: continue
                    if (!key.startsWith("Attempt_")) continue
                    val attemptMap = when (v) {
                        is Map<*, *> -> v
                        is NSDictionary -> nsDictionaryToKotlinMap(v)
                        else -> null
                    } ?: continue

                    val attempts = mutableListOf<Attempt>()
                    for ((ck, cv) in attemptMap) {
                        val childKey = ck?.toString() ?: continue
                        val asInt = childKey.toIntOrNull()
                        if (asInt != null) {
                            val qNode = when (cv) {
                                is Map<*, *> -> cv
                                is NSDictionary -> nsDictionaryToKotlinMap(cv)
                                else -> null
                            } ?: continue
                            val answer = anyToString(qNode["Answer"]) ?: ""
                            val cogSit = anyToString(qNode["CognitiveSituation"]) ?: ""
                            val domain = anyToString(qNode["Domain"]) ?: ""
                            val score = anyToInt(qNode["Score"]) ?: 0
                            val timestamp = anyToString(qNode["Timestamp"]) ?: ""
                            attempts.add(Attempt(answer = answer, cognitiveSituation = cogSit, domain = domain, score = score, timestamp = timestamp))
                        }
                    }

                    val cogScoresMap = when (attemptMap["CognitiveSituationScores"]) {
                        is Map<*, *> -> attemptMap["CognitiveSituationScores"] as Map<*, *>
                        is NSDictionary -> nsDictionaryToKotlinMap(attemptMap["CognitiveSituationScores"] as NSDictionary)
                        else -> emptyMap()
                    }
                    val domainScoresMap = when (attemptMap["DomainScores"]) {
                        is Map<*, *> -> attemptMap["DomainScores"] as Map<*, *>
                        is NSDictionary -> nsDictionaryToKotlinMap(attemptMap["DomainScores"] as NSDictionary)
                        else -> emptyMap()
                    }

                    val cognitiveSituationScores = CognitiveSituation(
                        correctiveMeasures = anyToInt(cogScoresMap["Corrective Measures"]) ?: 0,
                        decisionMaking = anyToInt(cogScoresMap["Decision Making"]) ?: 0,
                        emotionalIntelligence = anyToInt(cogScoresMap["Emotional Intelligence"]) ?: 0,
                        memoryTasks = anyToInt(cogScoresMap["Memory Tasks"]) ?: 0,
                        languageIssues = anyToInt(cogScoresMap["Observing Language Issues"]) ?: 0,
                        processingTime = anyToInt(cogScoresMap["Processing Time"]) ?: 0,
                        simpleInstructions = anyToInt(cogScoresMap["Simple Instructions"]) ?: 0,
                        visualTasks = anyToInt(cogScoresMap["Visual Tasks"]) ?: 0,
                    )

                    val domainScores = Domain(
                        complexAttentions = anyToInt(domainScoresMap["Complex Attentions"]) ?: 0,
                        executiveFunction = anyToInt(domainScoresMap["Executive Function"]) ?: 0,
                        language = anyToInt(domainScoresMap["Language"]) ?: 0,
                        learningAndMemory = anyToInt(domainScoresMap["Learning and Memory"]) ?: 0,
                        perceptualMotor = anyToInt(domainScoresMap["Perceptual-motor"]) ?: 0,
                        socialCognition = anyToInt(domainScoresMap["Social Cognition"]) ?: 0,
                    )

                    val lastUpdated = anyToString(attemptMap["Last Updated"]) ?: ""
                    val ncdCategory = anyToString(attemptMap["NCD Categorisation"]) ?: ""
                    val totalScore = anyToInt(attemptMap["Total Score"]) ?: 0

                    val userAttempts = UserAttempts(
                        attempts = attempts,
                        cognitiveSituationScores = cognitiveSituationScores,
                        domainScores = domainScores,
                        lastUpdated = lastUpdated,
                        ncdCategory = ncdCategory,
                        totalScore = totalScore,
                        totalQuestions = attempts.size,
                        type = type
                    )
                    userResultsList.add(UserResults(attempts = listOf(userAttempts)))
                }
                callback(DatabaseResult.Success(userResultsList))
            } else {
                fetchSurveyQuestions { qRes ->
                    val totalQuestionsCount = if (qRes is DatabaseResult.Success) qRes.data.size else 0
                    val userResultsList = mutableListOf<UserResults>()
                    for ((k, v) in attemptsRoot) {
                        val key = k?.toString() ?: continue
                        if (!key.startsWith("Attempt_")) continue
                        val attemptMap = when (v) {
                            is Map<*, *> -> v
                            is NSDictionary -> nsDictionaryToKotlinMap(v)
                            else -> null
                        } ?: continue
                        val isComplete = anyToBoolean(attemptMap["surveyComplete"]) ?: false
                        if (!isComplete) continue
                        val totalScore = anyToInt(attemptMap["Total Score"]) ?: 0
                        val lastUpdated = anyToString(attemptMap["Last Updated"]) ?: anyToString(attemptMap["timestamp"]) ?: ""
                        val ua = UserAttempts(
                            attempts = emptyList(),
                            lastUpdated = lastUpdated,
                            totalScore = totalScore,
                            totalQuestions = totalQuestionsCount,
                            type = type
                        )
                        userResultsList.add(UserResults(attempts = listOf(ua)))
                    }
                    callback(DatabaseResult.Success(userResultsList))
                }
            }
        }
    }

    /**
     * Saves or clears a user's answer for a question in a specific attempt.
     * Also maintains attempt-level metadata (Last Updated) and Total Score incrementally.
     * For CognitiveAssessment, includes Domain and CognitiveSituation; for HealthSurvey mirrors the same structure.
     * @param userId Unique identifier for the user.
     * @param attemptNumber Attempt number (1-based).
     * @param question The SurveyQuestion containing the selected answer(s).
     * @param callback Callback with Success (Unit) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun saveUserAnswer(
        userId: String,
        attemptNumber: Int,
        question: Question,
        callback: (DatabaseResult<Unit>) -> Unit
    ) {
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }
        val attemptRef = ref.child(dbPath).child(userResponsesTable).child(userId).child("Attempt_$attemptNumber")
        val nowStr = nowStringIos()

        // Update last updated
        attemptRef.child("Last Updated").setValue(nowStr)

        // Ensure Total Score exists
        attemptRef.child("Total Score").getDataWithCompletionBlock { _, totalSnap ->
            if (totalSnap == null || !totalSnap.exists()) {
                attemptRef.child("Total Score").setValue(0) { _, _ -> }
            }
        }

        val selected = when {
            !question.selectedAnswer.isNullOrEmpty() -> question.selectedAnswer!!
            question.selectedAnswers.isNotEmpty() -> question.selectedAnswers.joinToString(", ")
            else -> ""
        }
        val questionNodeRef = attemptRef.child(question.id.toString())

        if (selected.isEmpty()) {
            // Read the previous score then remove
            questionNodeRef.child("Score").getDataWithCompletionBlock { _: NSError?, scSnap: FIRDataSnapshot? ->
                val prevScore = if (scSnap != null && scSnap.exists()) anyToInt(scSnap.value) ?: 0 else 0
                questionNodeRef.removeValueWithCompletionBlock { e1: NSError?, _ ->
                    if (e1 != null) { callback(DatabaseResult.Error("Failed to remove answer: ${e1.localizedDescription}")); return@removeValueWithCompletionBlock }
                    // Adjust total down
                    attemptRef.child("Total Score").getDataWithCompletionBlock { _: NSError?, tSnap: FIRDataSnapshot? ->
                        val currentTotal = if (tSnap != null && tSnap.exists()) anyToInt(tSnap.value) ?: 0 else 0
                        val updated = (currentTotal - prevScore).coerceAtLeast(0)
                        attemptRef.child("Total Score").setValue(updated) { e2: NSError?, _ ->
                            if (e2 == null) callback(DatabaseResult.Success(Unit)) else callback(DatabaseResult.Error("Failed to update total score: ${e2.localizedDescription}"))
                        }
                    }
                }
            }
            return
        }

        val newScore = if (type == UserQuizType.CognitiveAssessment) {
            getCognitiveScore(selected)
        } else {
            getSurveyScore(selected, question.allowMultipleAnswers)
        }

        // Read previous score before overwrite
        questionNodeRef.child("Score").getDataWithCompletionBlock { _: NSError?, prevSnap: FIRDataSnapshot? ->
            val prevScore = if (prevSnap != null && prevSnap.exists()) anyToInt(prevSnap.value) ?: 0 else 0
            if (type == UserQuizType.CognitiveAssessment) {
                // Load meta from the question table
                ref.child(dbPath).child(questionTable).child(question.id.toString()).getDataWithCompletionBlock { eQ: NSError?, qSnap: FIRDataSnapshot? ->
                    if (eQ != null) { callback(DatabaseResult.Error("Failed to load question meta: ${eQ.localizedDescription}")); return@getDataWithCompletionBlock}
                    val qMap = if (qSnap?.value is NSDictionary) nsDictionaryToKotlinMap(qSnap.value as NSDictionary) else (qSnap?.value as? Map<*, *>)
                    val domain = anyToString(qMap?.get("domain")) ?: question.domain
                    val cognitiveSituation = anyToString(qMap?.get("cognitiveSituation")) ?: ""
                    val data: Map<String, Any> = mapOf(
                        "Answer" to selected,
                        "Domain" to domain,
                        "CognitiveSituation" to cognitiveSituation,
                        "Score" to newScore,
                        "Timestamp" to nowStr
                    )
                    questionNodeRef.setValue(data) { eSet: NSError?, _ ->
                        if (eSet != null) { callback(DatabaseResult.Error("Failed to save answer: ${eSet.localizedDescription}")); return@setValue }
                        adjustTotalByDelta(attemptRef, newScore - prevScore, callback)
                    }
                }
            } else {
                val data: Map<String, Any> = mapOf(
                    "Answer" to selected,
                    "Score" to newScore,
                    "Timestamp" to nowStr
                )
                questionNodeRef.setValue(data) { eSet: NSError?, _ ->
                    if (eSet != null) { callback(DatabaseResult.Error("Failed to save answer: ${eSet.localizedDescription}")); return@setValue }
                    adjustTotalByDelta(attemptRef, newScore - prevScore, callback)
                }
            }
        }
    }

    /**
     * Gets the current latest attempt number for a given user.
     * If no attempts exist, returns 1.
     * @param userId Unique identifier for the user.
     * @param callback Callback with Success(current attempt number) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun getLatestAttemptNumber(userId: String, callback: (DatabaseResult<Int>) -> Unit) {
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }
        ref.child(dbPath).child(userResponsesTable).child(userId).getDataWithCompletionBlock { error: NSError?, snapshot: FIRDataSnapshot? ->
            if (error != null) { callback(DatabaseResult.Error("Failed to get latest attempt number: ${error.localizedDescription}")); return@getDataWithCompletionBlock }
            if (snapshot == null || !snapshot.exists()) { callback(DatabaseResult.Success(1)); return@getDataWithCompletionBlock }
            val value = snapshot.value
            val map = when (value) {
                is Map<*, *> -> value
                is NSDictionary -> nsDictionaryToKotlinMap(value)
                else -> emptyMap()
            }
            var maxAttempt = 0
            for (k in map.keys) {
                val key = k?.toString() ?: continue
                if (key.startsWith("Attempt_")) {
                    val n = key.removePrefix("Attempt_").toIntOrNull()
                    if (n != null) maxAttempt = kotlin.math.max(maxAttempt, n)
                }
            }
            callback(DatabaseResult.Success(if (maxAttempt == 0) 1 else maxAttempt))
        }
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
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }
        val attemptRef = ref.child(dbPath).child(userResponsesTable).child(userId).child("Attempt_$currentAttemptNumber")
        val now = nowStringIos()
        attemptRef.child("Last Updated").setValue(now) { _, _ -> }
        val flagKey = if (type == UserQuizType.CognitiveAssessment) "quizComplete" else "surveyComplete"
        attemptRef.child(flagKey).setValue(true) { e: NSError?, _ ->
            if (e == null) callback(DatabaseResult.Success(currentAttemptNumber + 1)) else callback(DatabaseResult.Error(
                "Failed to finalize attempt: ${e.localizedDescription}"
            ))
        }
    }

    /**
     * Checks whether a user has any attempts stored for the current quiz type.
     * @param userId Unique identifier for the user.
     * @param callback Callback with Success (true/false) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun hasUserAttempts(userId: String, callback: (DatabaseResult<Boolean>) -> Unit) {
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }
        ref.child(dbPath).child(userResponsesTable).child(userId).getDataWithCompletionBlock { e: NSError?, s: FIRDataSnapshot? ->
            if (e != null) { callback(DatabaseResult.Error("Failed to check user attempts: ${e.localizedDescription}")); return@getDataWithCompletionBlock }
            callback(DatabaseResult.Success(s != null && s.exists() && s.hasChildren()))
        }
    }

    /**
     * Retrieves a summary of all attempts for the current quiz type for a user.
     * The summary includes attempt number, timestamp, completion status, and questions completed.
     * @param userId Unique identifier for the user.
     * @param callback Callback with Success (list of AttemptSummary) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun getUserAttempts(userId: String, callback: (DatabaseResult<List<AttemptSummary>>) -> Unit) {
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }
        ref.child(dbPath).child(userResponsesTable).child(userId).getDataWithCompletionBlock { e: NSError?, s: FIRDataSnapshot? ->
            if (e != null) { callback(DatabaseResult.Error("Failed to get user attempts: ${e.localizedDescription}")); return@getDataWithCompletionBlock }
            if (s == null || !s.exists()) { callback(DatabaseResult.Success(emptyList())); return@getDataWithCompletionBlock }
            val questionsLoader: (onDone: (Int) -> Unit) -> Unit = { onDone ->
                if (type == UserQuizType.CognitiveAssessment) {
                    fetchCognitiveQuestions { res ->
                        when (res) {
                            is DatabaseResult.Success -> onDone(res.data.size)
                            is DatabaseResult.Error -> onDone(0)
                        }
                    }
                } else {
                    fetchSurveyQuestions { res ->
                        when (res) {
                            is DatabaseResult.Success -> onDone(res.data.size)
                            is DatabaseResult.Error -> onDone(0)
                        }
                    }
                }
            }
            val value = s.value
            val map = when (value) {
                is Map<*, *> -> value
                is NSDictionary -> nsDictionaryToKotlinMap(value)
                else -> emptyMap()
            }
            questionsLoader { totalQuestions ->
                val attempts = mutableListOf<AttemptSummary>()
                for ((k, v) in map) {
                    val key = k?.toString() ?: continue
                    if (!key.startsWith("Attempt_")) continue
                    val attemptNum = key.removePrefix("Attempt_").toIntOrNull() ?: 0
                    val attemptMap = when (v) {
                        is Map<*, *> -> v
                        is NSDictionary -> nsDictionaryToKotlinMap(v)
                        else -> null
                    } ?: continue
                    val timestamp = anyToString(attemptMap["Last Updated"]) ?: anyToString(attemptMap["timestamp"]) ?: ""
                    val completeFlag = if (type == UserQuizType.CognitiveAssessment) (anyToBoolean(attemptMap["quizComplete"]) ?: anyToBoolean(attemptMap["Completed"]) ?: false) else (anyToBoolean(attemptMap["surveyComplete"]) ?: false)
                    var questionsCompleted = 0
                    for (ck in attemptMap.keys) {
                        val childKey = ck?.toString() ?: continue
                        if (childKey.all { it.isDigit() }) questionsCompleted++
                    }
                    val totalScore = anyToInt(attemptMap["Total Score"]) ?: 0
                    attempts.add(
                        AttemptSummary(
                            attemptNumber = attemptNum,
                            timestamp = timestamp,
                            surveyComplete = completeFlag,
                            questionsCompleted = questionsCompleted,
                            totalQuestions = totalQuestions,
                            totalScore = totalScore
                        )
                    )
                }
                attempts.sortWith(compareBy<AttemptSummary> { it.surveyComplete }.thenByDescending { it.attemptNumber })
                callback(DatabaseResult.Success(attempts))
            }
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
    val ref = FIRDatabase.database()?.reference()
    if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }

    // Safe converters to avoid K/V inference issues everywhere
    fun mapAnyToStringAny(src: Map<*, *>): Map<String, Any?> =
        src.mapNotNull { (k, v) ->
            val key = (k as? String) ?: k?.toString()
            key?.let { it to v }
        }.toMap()

    fun nsDictToStringAny(dict: NSDictionary): Map<String, Any?> {
        val raw = nsDictionaryToKotlinMap(dict) as? Map<*, *>
        return raw?.let { mapAnyToStringAny(it) } ?: emptyMap()
    }

    fun readAttemptNode(done: (Map<String, Any?>) -> Unit) {
        ref.child(dbPath)
            .child(userResponsesTable)
            .child(userId)
            .child("Attempt_$attemptNumber")
            .getDataWithCompletionBlock { e: NSError?, snap: FIRDataSnapshot? ->
                if (e != null) {
                    callback(DatabaseResult.Error("Failed to fetch attempt details: ${e.localizedDescription}"))
                    return@getDataWithCompletionBlock
                }
                val map: Map<String, Any?> = when (val v = snap?.value) {
                    is Map<*, *>   -> mapAnyToStringAny(v)
                    is NSDictionary -> nsDictToStringAny(v)
                    else           -> emptyMap<String, Any?>()
                }
                done(map)
            }
    }

    if (type == UserQuizType.CognitiveAssessment) {
        fetchCognitiveQuestions { result ->
            when (result) {
                is DatabaseResult.Error -> callback(DatabaseResult.Error(result.message))
                is DatabaseResult.Success -> {
                    val questions = result.data.sortedBy { it.id }

                    // Merge saved answers for this attempt
                    readAttemptNode { aMap ->
                        val updated = questions.map { q ->
                            val node = aMap[q.id.toString()]
                            val qMap: Map<String, Any?>? = when (node) {
                                is Map<*, *>    -> mapAnyToStringAny(node)
                                is NSDictionary -> nsDictToStringAny(node)
                                else            -> null
                            }
                            val ans = anyToString(qMap?.get("Answer"))
                            val sc = anyToInt(qMap?.get("Score")) ?: 0
                            q.copy(selectedAnswer = ans, score = sc)
                        }
                        callback(DatabaseResult.Success(updated))
                    }
                }
            }
        }
    } else {
        // Health Survey
        ref.child(dbPath).child(questionTable)
            .getDataWithCompletionBlock { qe: NSError?, qSnap: FIRDataSnapshot? ->
                if (qe != null) {
                    callback(DatabaseResult.Error("Failed to fetch questions: ${qe.localizedDescription}"))
                    return@getDataWithCompletionBlock
                }
                val questions = parseSurveyQuestions(qSnap)

                readAttemptNode { aMap ->
                    val updated = questions.map { q ->
                        val node = aMap[q.id.toString()]
                        val qMap: Map<String, Any?>? = when (node) {
                            is Map<*, *>    -> mapAnyToStringAny(node)
                            is NSDictionary -> nsDictToStringAny(node)
                            else            -> null
                        }
                        val sc = anyToInt(qMap?.get("Score")) ?: 0
                        if (q.allowMultipleAnswers) {
                            val csv = anyToString(qMap?.get("Answer"))
                            val answers = csv?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
                            q.copy(selectedAnswers = answers.toMutableList(), score = sc)
                        } else {
                            val ans = anyToString(qMap?.get("Answer"))
                            q.copy(selectedAnswer = ans, score = sc)
                        }
                    }
                    callback(DatabaseResult.Success(updated))
                }
            }
    }
}

    /**
     * Marks a given attempt as complete for the current quiz type and updates Last Updated.
     * @param userId Unique identifier for the user.
     * @param attemptNumber Attempt number to mark complete.
     * @param callback Callback with Success (Unit) or Error(message).
     * @return Unit (result delivered via callback)
     */
    actual fun markAttemptComplete(userId: String, attemptNumber: Int, callback: (DatabaseResult<Unit>) -> Unit) {
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }
        val attemptRef = ref.child(dbPath).child(userResponsesTable).child(userId).child("Attempt_$attemptNumber")
        val now = nowStringIos()
        attemptRef.child("Last Updated").setValue(now) { _, _ -> }
        val flagKey = if (type == UserQuizType.CognitiveAssessment) "quizComplete" else "surveyComplete"
        attemptRef.child(flagKey).setValue(true) { e: NSError?, _ ->
            if (e == null) callback(DatabaseResult.Success(Unit)) else callback(DatabaseResult.Error("Failed to mark attempt complete: ${e.localizedDescription}"))
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
    actual fun markAttemptIncomplete(userId: String, attemptNumber: Int, callback: (DatabaseResult<Unit>) -> Unit) {
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }
        val attemptRef = ref.child(dbPath).child(userResponsesTable).child(userId).child("Attempt_$attemptNumber")
        val now = nowStringIos()
        attemptRef.child("Last Updated").setValue(now) { _, _ -> }
        val flagKey = if (type == UserQuizType.CognitiveAssessment) "quizComplete" else "surveyComplete"
        // Only set to false if not complete already; otherwise no-op
        attemptRef.child(flagKey).getDataWithCompletionBlock { _: NSError?, s: FIRDataSnapshot? ->
            val isComplete = if (s != null && s.exists()) anyToBoolean(s.value) ?: false else false
            if (isComplete) {
                attemptRef.child(flagKey).setValue(false) { e: NSError?, _ ->
                    if (e == null) callback(DatabaseResult.Success(Unit)) else callback(DatabaseResult.Error("Failed to mark attempt incomplete: ${e.localizedDescription}"))
                }
            } else {
                callback(DatabaseResult.Success(Unit))
            }
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

        val ref = FIRDatabase.database()?.reference()
        if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }
        ref.child(dbPath).child(userResponsesTable).child(userId).child("Attempt_$attemptNumber").getDataWithCompletionBlock { e: NSError?, s: FIRDataSnapshot? ->
            if (e != null) { callback(DatabaseResult.Error("Failed to get next unanswered question: ${e.localizedDescription}")); return@getDataWithCompletionBlock }
            val map = when (s?.value) {
                is Map<*, *> -> s.value as Map<*, *>
                is NSDictionary -> nsDictionaryToKotlinMap(s.value as NSDictionary)
                else -> emptyMap()
            }
            val answeredIds = mutableSetOf<Int>()
            for (k in map.keys) {
                val key = k?.toString() ?: continue
                if (key.all { it.isDigit() }) key.toIntOrNull()?.let { answeredIds.add(it) }
            }
            for (i in 1..totalQuestions) {
                if (!answeredIds.contains(i)) { callback(DatabaseResult.Success(i - 1)); return@getDataWithCompletionBlock }
            }
            callback(DatabaseResult.Success(totalQuestions - 1))
        }
    }

    // -------------------- Helpers --------------------
    /**
     * Adjusts the Total Score field on an attempt node by a delta value.
     * Reads the current total, applies the delta (not below 0), and writes back.
     * @param attemptRef Firebase Database reference for the attempt root.
     * @param delta The amount to add to the current total score (can be negative).
     * @param callback Callback with Success (Unit) or Error(message).
     */
    private fun adjustTotalByDelta(attemptRef: cocoapods.FirebaseDatabase.FIRDatabaseReference, delta: Int, callback: (DatabaseResult<Unit>) -> Unit) {
        attemptRef.child("Total Score").getDataWithCompletionBlock { _: NSError?, tSnap: FIRDataSnapshot? ->
            val current = if (tSnap != null && tSnap.exists()) anyToInt(tSnap.value) ?: 0 else 0
            val updated = (current + delta).coerceAtLeast(0)
            attemptRef.child("Total Score").setValue(updated) { e: NSError?, _ ->
                if (e == null) callback(DatabaseResult.Success(Unit)) else callback(DatabaseResult.Error("Failed to update total score: ${e.localizedDescription}"))
            }
        }
    }

    /**
     * Parses a Firebase snapshot into a list of SurveyQuestion models.
     * @param snapshot Firebase snapshot at a Questions path.
     * @return List of SurveyQuestion (empty if snapshot missing/invalid).
     */
    private fun parseSurveyQuestions(snapshot: FIRDataSnapshot?): List<Question> {
        if (snapshot == null || !snapshot.exists()) return emptyList()

        val en = snapshot.children ?: return emptyList()
       val questions = mutableListOf<Question>()

        while (true) {
            val anyChild = en.nextObject() ?: break
            val child = anyChild as? FIRDataSnapshot ?: continue

            val id = child.key?.toIntOrNull() ?: continue

            val dict = child.value as? NSDictionary ?: continue
            val qMap = nsDictionaryToKotlinMap(dict)

            val questionText = anyToString(qMap["questionText"]) ?: ""
            val domain       = anyToString(qMap["domain"]) ?: ""

            val options = mutableListOf<String>()
            var i = 1
            while (true) {
                val v = anyToString(qMap["option$i"]) ?: ""
                if (v.isEmpty()) break
                options.add(v); i++
            }

            val allowMultiple = anyToBoolean(qMap["allowMultipleAnswers"]) ?: false

            if (questionText.isNotEmpty() && options.isNotEmpty()) {
                questions.add(
                    Question(
                        id = id,
                        domain = domain,
                        questionText = questionText,
                        options = options,
                        allowMultipleAnswers = allowMultiple,
                        cognitiveSituation = ""
                    )
                )
            }
        }
        return questions
    }

    /**
     * Parses a Firebase snapshot into a list of CognitiveQuestion models.
     * @param snapshot Firebase snapshot at a Questions path for cognitive assessment.
     * @return List of CognitiveQuestion (empty if snapshot missing/invalid).
     */
    private fun parseCognitiveQuestions(snapshot: FIRDataSnapshot?): List<Question> {
        if (snapshot == null || !snapshot.exists()) return emptyList()
        val en = snapshot.children ?: return emptyList()
        val out = mutableListOf<Question>()
        while (true) {
            val anyChild = en.nextObject() ?: break
            val child = anyChild as? FIRDataSnapshot ?: continue

            val id = child.key?.toIntOrNull() ?: continue
            val dict = child.value as? NSDictionary ?: continue
            val qMap = nsDictionaryToKotlinMap(dict)

            val questionText = anyToString(qMap["questionText"]) ?: ""
            val domain = anyToString(qMap["domain"]) ?: ""
            val cognitiveSituation = anyToString(qMap["cognitiveSituation"]) ?: ""

            // options map or option1..N
            val optNode = qMap["options"]
            val optsMap = when (optNode) {
                is Map<*, *>     -> optNode
                is NSDictionary  -> nsDictionaryToKotlinMap(optNode)
                else -> emptyMap()
            }

            val options =
                if (optsMap.isNotEmpty()) {
                    optsMap.entries
                        .mapNotNull { (k, v) -> k?.toString()?.let { it to anyToString(v) } }
                        .filter { it.second != null }
                        .sortedBy { it.first.filter(Char::isDigit).toIntOrNull() ?: Int.MAX_VALUE }
                        .map { it.second!! }
                } else {
                    buildList {
                        for (i in 1..10) {
                            anyToString(qMap["option$i"])?.let { add(it) }
                        }
                    }
                }

            out.add(
                Question(
                    id = id,
                    domain = domain,
                    questionText = questionText,
                    options = options,
                    allowMultipleAnswers = false,
                    cognitiveSituation = cognitiveSituation
                )
            )
        }
        return out
    }


    /**
     * Converts an iOS NSDictionary to a Kotlin Map<String, Any?>.
     * Recursively converts nested NSDictionary values to Maps.
     * @param dict NSDictionary to convert.
     * @return Kotlin Map representation of the NSDictionary.
     */
    private fun nsDictionaryToKotlinMap(dict: NSDictionary): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        val keys = dict.allKeys
        for (k in keys) {
            val keyStr = k?.toString() ?: continue
            val v = dict.objectForKey(k)
            result[keyStr] = when (v) {
                is NSDictionary -> nsDictionaryToKotlinMap(v)
                else -> v
            }
        }
        return result
    }

    /**
     * Safely converts an arbitrary value to Int.
     * Accepts Number or String (numeric); returns null otherwise.
     * @param v Any value from a Firebase snapshot.
     * @return Int? parsed integer or null if not convertible.
     */
    private fun anyToInt(v: Any?): Int? = when (v) {
        null -> null
        is Number -> v.toInt()
        is String -> v.toIntOrNull()
        else -> null
    }

    /**
     * Safely converts an arbitrary value to Boolean.
     * Accepts Boolean, Number (non-zero = true), or String ("true"/"1").
     * @param v Any value from a Firebase snapshot.
     * @return Boolean? parsed boolean or null if not convertible.
     */
    private fun anyToBoolean(v: Any?): Boolean? = when (v) {
        null -> null
        is Boolean -> v
        is Number -> v.toInt() != 0
        is String -> v.equals("true", ignoreCase = true) || v == "1"
        else -> null
    }

    /**
     * Safely converts an arbitrary value to String.
     * Accepts String or Number; falls back to toString() for other non-null values; returns null for null.
     * @param v Any value from a Firebase snapshot.
     * @return String? normalized string or null if v is null.
     */
    private fun anyToString(v: Any?): String? = when (v) {
        null -> null
        is String -> v
        is Number -> v.toString()
        else -> v.toString()
    }

    /**
     * Returns the current date-time string formatted as "yyyy-MM-dd HH:mm:ss" (iOS formatter).
     * Used for Last Updated and per-answer timestamps.
     * @return String formatted current timestamp.
     */
    private fun nowStringIos(): String {
        val formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        return formatter.stringFromDate(NSDate())
    }
}
