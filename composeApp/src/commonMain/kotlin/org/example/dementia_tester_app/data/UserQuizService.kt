package org.example.dementia_tester_app.data

/**
 * Enum representing the type of quiz and its root database path
 */
enum class UserQuizType(val dbPath: String) {
    HealthSurvey("HealthSurvey"),
    CognitiveAssessment("CognitiveAssessment")
}
/**
 * Data class to represent a question object for both the health survey and cognitive assessment
 * */
data class Question(
    val id: Int,
    val questionText: String,
    val options: List<String>,
    val domain: String,
    val cognitiveSituation: String,
    val allowMultipleAnswers: Boolean = false,
    var selectedAnswer: String? = null,
    val selectedAnswers: MutableList<String> = mutableListOf(),
    val score: Int = 0,
)
/**
 * Data classes for Cognitive Assessment results and questions
 */
data class Attempt(
    val answer: String = "",
    val cognitiveSituation: String = "",
    val domain: String = "",
    val score: Int = 0,
    val timestamp: String = "",
)

data class CognitiveSituation(
    val correctiveMeasures: Int = 0,
    val decisionMaking: Int = 0,
    val emotionalIntelligence: Int = 0,
    val memoryTasks: Int = 0,
    val languageIssues: Int = 0,
    val processingTime: Int = 0,
    val simpleInstructions: Int = 0,
    val visualTasks: Int = 0
)

data class Domain(
    val complexAttentions: Int = 0,
    val executiveFunction: Int = 0,
    val language: Int = 0,
    val learningAndMemory: Int = 0,
    val perceptualMotor: Int = 0,
    val socialCognition: Int = 0
)

data class UserAttempts(
    val attempts: List<Attempt> = emptyList(),
    val cognitiveSituationScores: CognitiveSituation = CognitiveSituation(),
    val domainScores: Domain = Domain(),
    val lastUpdated: String = "",
    val ncdCategory: String = "",
    val totalScore: Int = 0,
    val type: UserQuizType

)

data class UserResults(
    val attempts: List<UserAttempts> = emptyList()
)
/**
 * Unified Quiz Service (Health Survey and Cognitive Assessment)
 * Constructor takes a UserQuizType to determine which quiz database path to operate on.
 */
expect class UserQuizService(type: UserQuizType) {
    //Survey Specific functions
    /**
     * Fetches Health Survey questions from the data source.
     * @param callback Invoked with Success containing a list of Question or Error with a message.
     * @return Unit (result delivered via callback)
     */
    fun fetchSurveyQuestions(callback: (DatabaseResult<List<Question>>) -> Unit)

    //Cognitive Assessment specific functions
    /**
     * Fetches Cognitive Assessment questions from the data source.
     * @param callback Invoked with Success containing a list of Question or Error with a message.
     * @return Unit (result delivered via callback)
     */
    fun fetchCognitiveQuestions(callback: (DatabaseResult<List<Question>>) -> Unit)
    /**
     * Retrieves the user's past Cognitive Assessment results and scores.
     * @param userId The unique user identifier.
     * @param callback Invoked with Success containing a list of UserResults or Error with a message.
     * @return Unit (result delivered via callback)
     */
    fun getUserScores(userId: String, callback: (DatabaseResult<List<UserResults>>) -> Unit)
    /**
     * Retrieves Cognitive Assessment questions for a specific attempt, as Question models.
     * @param userId The unique user identifier.
     * @param attemptNumber The attempt number to load.
     * @param callback Invoked with Success(list of Question) or Error with a message.
     * @return Unit (result delivered via callback)
     */
    fun getCognitiveAttemptDetails(
    userId: String,
    attemptNumber: Int,
    callback: (DatabaseResult<List<Question>>) -> Unit
    )

    //Shared functions
    /**
     * Saves or clears the user's answer for a given question within an attempt.
     * Also updates attempt-level metadata such as timestamps and (on platforms that support it) total score.
     * @param userId The unique user identifier.
     * @param attemptNumber The attempt index (1-based) to save the answer into.
     * @param question The Question containing selectedAnswer/selectedAnswers to be stored.
     * @param callback Invoked with Success(Unit) if saved, or Error with a message.
     * @return Unit (result delivered via callback)
     */
    fun saveUserAnswer(
    userId: String,
    attemptNumber: Int,
    question: Question,
    callback: (DatabaseResult<Unit>) -> Unit
    )
    /**
     * Gets the latest (current) attempt number for a user.
     * If no attempts exist, returns 1 to indicate the first attempt.
     * @param userId The unique user identifier.
     * @param callback Invoked with Success(current attempt number) or Error with a message.
     * @return Unit (result delivered via callback)
     */
    fun getLatestAttemptNumber(userId: String, callback: (DatabaseResult<Int>) -> Unit)
    /**
     * Marks the current attempt as completed and advances to the next attempt number.
     * @param userId The unique user identifier.
     * @param currentAttemptNumber The attempt number being finalized.
     * @param callback Invoked with Success(next attempt number) or Error with a message.
     * @return Unit (result delivered via callback)
     */
    fun finalizeAttempt(
    userId: String,
    currentAttemptNumber: Int,
    callback: (DatabaseResult<Int>) -> Unit
    )
    /**
     * Checks whether the user has any attempts stored.
     * @param userId The unique user identifier.
     * @param callback Invoked with Success(true/false) or Error with a message.
     * @return Unit (result delivered via callback)
     */
    fun hasUserAttempts(userId: String, callback: (DatabaseResult<Boolean>) -> Unit)
    /**
     * Retrieves a summary list of the user's attempts for the current quiz type.
     * @param userId The unique user identifier.
     * @param callback Invoked with Success(list of AttemptSummary) or Error with a message.
     * @return Unit (result delivered via callback)
     */
    fun getUserAttempts(userId: String, callback: (DatabaseResult<List<AttemptSummary>>) -> Unit)
    /**
     * Retrieves detailed questions for a specific attempt, including any previously selected answers.
     * @param userId The unique user identifier.
     * @param attemptNumber The attempt number to load.
     * @param callback Invoked with Success(list of Question with selections) or Error with a message.
     * @return Unit (result delivered via callback)
     */
    fun getAttemptDetails(
    userId: String,
    attemptNumber: Int,
    callback: (DatabaseResult<List<Question>>) -> Unit
    )
    /**
     * Marks an attempt as complete for the current quiz type.
     * @param userId The unique user identifier.
     * @param attemptNumber The attempt number to mark complete.
     * @param callback Invoked with Success(Unit) or Error with a message.
     * @return Unit (result delivered via callback)
     */
    fun markAttemptComplete(userId: String, attemptNumber: Int, callback: (DatabaseResult<Unit>) -> Unit)
    /**
     * Marks an attempt as incomplete for the current quiz type.
     * If already complete, the implementation may no-op.
     * @param userId The unique user identifier.
     * @param attemptNumber The attempt number to mark incomplete.
     * @param callback Invoked with Success(Unit) or Error with a message.
     * @return Unit (result delivered via callback)
     */
    fun markAttemptIncomplete(userId: String, attemptNumber: Int, callback: (DatabaseResult<Unit>) -> Unit)
    /**
     * Computes the index of the next unanswered question for a given attempt.
     * If all questions are answered, returns the last index (totalQuestions - 1).
     * @param userId The unique user identifier.
     * @param attemptNumber The attempt number to check within.
     * @param totalQuestions Total number of questions in the quiz.
     * @param callback Invoked with Success(next unanswered index, 0-based) or Error with a message.
     * @return Unit (result delivered via callback)
     */
    fun getNextUnansweredQuestionIndex(
    userId: String,
    attemptNumber: Int,
    totalQuestions: Int,
    callback: (DatabaseResult<Int>) -> Unit
    )
}

// Shared scoring helpers (common for Android and iOS)
/**
 * Calculates the per-question score for Cognitive Assessment based on the selected answer label.
 * @param selectedAnswer The selected option text (nullable).
 * @return Int score from 0 to 4.
 */
fun getCognitiveScore(selectedAnswer: String?): Int {
    return when (selectedAnswer) {
        null -> 0
        "None or not at all" -> 0
        "Rare, less than a day or two" -> 1
        "Mild, several days" -> 2
        "Moderate, more than half the days" -> 3
        "Severe, nearly every day" -> 4
        else -> 0
    }
}

/**
 * Calculates the per-question score for the Health Survey.
 * Handles both single-select and multi-select (special-case None) questions.
 * @param selectedAnswer The selected option(s) as a single string. For multi-select, values are comma-separated.
 * @param allowMultipleAnswers True if the question allows multiple selections.
 * @return Int score from 0 to 4.
 */
fun getSurveyScore(selectedAnswer: String, allowMultipleAnswers: Boolean): Int {
    val answer = selectedAnswer.trim()
    if (answer.isEmpty()) return 0

    if (allowMultipleAnswers) {
        val tokens = answer.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return 0
        return if (tokens.size == 1 && tokens[0] == "None") 1 else 4
    }

    val scoreMap = mapOf(
        // Diet & Alcohol Intake
        "1–2 servings" to 2,
        "3–4 servings" to 3,
        "5 or more servings" to 4,

        "Daily or almost daily" to 4,
        "3–4 times a week" to 3,
        "Once or twice a week" to 2,
        "Once a week or less" to 2,
        "Rarely or never" to 1,
        "Never" to 1,

        "4 or more" to 4,
        "2–3" to 3,
        "1" to 2,
        "I don’t drink alcohol" to 1,

        // Physical Activity
        "0 days" to 1,
        "1–2 days" to 2,
        "3–4 days" to 3,
        "5 or more days" to 4,

        // Q7 multiselect individual items if treated as single-select
        "Walking" to 4,
        "Swimming" to 4,
        "Gardening" to 4,
        "House cleaning" to 4,
        "Gym or strength training" to 4,
        "None" to 1,

        "Very inactive" to 1,
        "Somewhat inactive" to 2,
        "Moderately active" to 3,
        "Very active" to 4,

        // Social Engagement
        "Not at all" to 1,
        "1–2 times" to 2,
        "3–4 times" to 3,
        "Daily" to 4,

        "Yes – more than once" to 4,
        "Yes – once" to 3,
        "No – but I had planned to" to 2,
        "No – I stayed home" to 1,

        "Very isolated" to 1,
        "Somewhat isolated" to 2,
        "Somewhat connected" to 3,
        "Very connected" to 4
    )

    return scoreMap[answer] ?: 0
}