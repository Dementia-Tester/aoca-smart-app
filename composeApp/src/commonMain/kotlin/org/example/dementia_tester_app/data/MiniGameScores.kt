package org.example.dementia_tester_app.data

/**
 * Enum for game types
 * Represents the three types of cognitive games in the app
 */
enum class GameType(val dbName: String, val displayName: String) {
    COMPLEX_ATTENTION("ComplexAttention", "Focus Flicker - Complex Attention"),
    EXECUTIVE_FUNCTION("ExecutiveFunction", "Task Switcher - Executive Function"),
    LEARNING_AND_MEMORY("LearningAndMemory", "Word Recall - Learning and Memory");
}

/**
 * Data class for game results
 * Stores statistics for a single game
 */
data class GameResult(
    val gameName: String,
    val totalAttempts: Int,
    val lastPlayed: String,
    val averageScore: Int,
    val minScore: Int,
    val maxScore: Int
)

/**
 * Data class for user game results
 * Stores results for all three games
 */
data class UserGameResults(
    val focusFlicker: GameResult?,
    val taskSwitcher: GameResult?,
    val wordRecall: GameResult?
)

/**
 * Data class for a user game attempt
 * Stores results for a single attempt of a particular game
 */
data class GameAttempt(
    val gameName: String,
    val score: Int,
    val timestamp: String
)

data class GameAttempts(
    val attempts: List<GameAttempt> = emptyList(),
    val gameResult: GameResult
)
