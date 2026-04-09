package org.example.dementia_tester_app.data



enum class AttemptType {
    COGNITIVE_ASSESSMENT,
    HEALTH_SURVEY,
    MINIGAME

}
/**
 * A wrapper class for graphing user data.
 *
 * Constructor takes a list of either UserResults or GameAttempts
 * and sets its fields accordingly.
 *
 * @property attempts The flattened list of attempts (either user results or game attempts)
 * @property type The type of attempt. Must be a member of the AttemptType enum class
 * @property scores The list of integer scores extracted from the attempts
 * @property gameResult Optional game result details (only set for minigames)
 */
class GraphableAttempts private constructor(
    val attempts: List<Any>,
    val type: AttemptType,
    val scores: List<Int>,
    val gameResult: GameResult? = null

) {
    constructor(testResults: List<*>) : this(
        attempts = when {
            testResults.first() is UserResults ->
                (testResults as List<UserResults>).flatMap { it.attempts }
            testResults.first() is GameAttempts ->
                (testResults as List<GameAttempts>).flatMap { it.attempts }
            else -> throw IllegalArgumentException("Unsupported attempt type. GraphableAttempts currently supports UserResults and GameAttempts objects")
        },
        type = when {
            testResults.first() is UserResults -> {
                val quizType = (testResults as List<UserResults>)
                    .first()
                    .attempts
                    .first()
                    .type
                when (quizType) {
                    UserQuizType.CognitiveAssessment -> AttemptType.COGNITIVE_ASSESSMENT
                    UserQuizType.HealthSurvey -> AttemptType.HEALTH_SURVEY
                }
            }
            testResults.first() is GameAttempts -> AttemptType.MINIGAME
            else -> throw IllegalArgumentException("Unsupported attempt type. GraphableAttempts currently supports UserResults and GameAttempts objects")
        },
        scores = when {
            testResults.first() is UserResults -> {
                (testResults as List<UserResults>)
                    .flatMap { it.attempts }
                    .map { it.totalScore }
            }
            testResults.first() is GameAttempts -> {
                (testResults as List<GameAttempts>)
                    .flatMap { it.attempts }
                    .map { it.score }
            }
            else -> throw IllegalArgumentException("Unsupported attempt type. GraphableAttempts currently supports UserResults and GameAttempts objects")
        },
        gameResult = when (testResults.firstOrNull()) {
            null -> null
            is UserResults -> null
            is GameAttempts ->
                (testResults as List<GameAttempts>).singleOrNull()?.gameResult
            else -> throw IllegalArgumentException("Unsupported attempt type. GraphableAttempts currently supports UserResults and GameAttempts objects")
        }
    )

    /**
     * Gets the scores for a subset of UserAttempts or GameAttempt objects
     * @param filteredAttempts the list of filtered attempts
     * @return a list of Int representing scores for a subset of UserAttempts or GameAttempt objects
     */
    fun getFilteredScores(filteredAttempts : List<Any>): List<Int> {
       return when {
           filteredAttempts.all { it is UserAttempts } ->
               filteredAttempts
                   .filterIsInstance<UserAttempts>()
                   .map { it.totalScore }

            filteredAttempts.all { it is GameAttempt } ->
                filteredAttempts
                    .filterIsInstance<GameAttempt>()
                    .map { it.score }

           else -> throw IllegalArgumentException("Unsupported attempt type. GraphableAttempts.getFilteredScores currently supports List<UserAttempts> and List<GameAttempt>")
        }
    }
}





