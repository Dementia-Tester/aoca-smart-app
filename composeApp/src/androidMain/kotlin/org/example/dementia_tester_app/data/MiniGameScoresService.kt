package org.example.dementia_tester_app.data

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Android implementation of MiniGameScoresService
 */
actual class MiniGameScoresService {
    private val database = Firebase.database.reference
    private val miniGameScoresPath = "MiniGameScores"
    
    /**
     * Fetch user's mini-game scores from Firebase
     * @param userId The ID of the user to fetch scores for
     * @param callback Callback function that will be called with the result
     */
    actual fun getUserGameScores(userId: String, callback: (DatabaseResult<UserGameResults>) -> Unit) {
        // Initialize game results as null
        var focusFlickerResults: GameResult? = null
        var taskSwitcherResults: GameResult? = null
        var wordRecallResults: GameResult? = null
        
        // Counter to track when all games have been processed
        var gamesProcessed = 0
        val totalGames = GameType.entries.size

        // Process each game type
        for (gameType in GameType.entries) {
            val dbName = gameType.dbName
            val displayName = gameType.displayName
            
            database.child(miniGameScoresPath)
                .child(userId)
                .child(dbName)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        // Get all attempts
                        val scores = mutableListOf<Int>()
                        val timestamps = mutableListOf<Long>()

                        snapshot.children.forEach { attemptSnapshot ->
                            val score = attemptSnapshot.child("score").value?.toString()?.toIntOrNull() ?: 0
                            val timestamp = attemptSnapshot.child("timestamp").value?.toString()?.toLongOrNull() ?: 0L

                            scores.add(score)
                            timestamps.add(timestamp)
                        }

                        if (scores.isNotEmpty()) {
                            // Calculate statistics
                            val totalAttempts = scores.size
                            val lastTimestamp = timestamps.maxOrNull() ?: 0L
                            val lastPlayed = formatTimestamp(lastTimestamp)
                            val averageScore = scores.average().toInt()
                            val minScore = scores.minOrNull() ?: 0
                            val maxScore = scores.maxOrNull() ?: 0
                            
                            // Create GameResults object
                            val gameResults = GameResult(
                                gameName = displayName,
                                totalAttempts = totalAttempts,
                                lastPlayed = lastPlayed,
                                averageScore = averageScore,
                                minScore = minScore,
                                maxScore = maxScore
                            )
                            
                            // Assign to the appropriate game based on the game type
                            when (gameType) {
                                GameType.COMPLEX_ATTENTION -> focusFlickerResults = gameResults
                                GameType.EXECUTIVE_FUNCTION -> taskSwitcherResults = gameResults
                                GameType.LEARNING_AND_MEMORY -> wordRecallResults = gameResults
                            }
                        }
                    }
                    
                    // Increment counter and check if all games have been processed
                    gamesProcessed++
                    if (gamesProcessed == totalGames) {
                        // Create UserGameResults object
                        val userGameResults = UserGameResults(
                            focusFlicker = focusFlickerResults,
                            taskSwitcher = taskSwitcherResults,
                            wordRecall = wordRecallResults
                        )
                        
                        // Return the result
                        callback(DatabaseResult.Success(userGameResults))
                    }
                }
                .addOnFailureListener { e ->
                    // Increment counter and check if all games have been processed
                    gamesProcessed++
                    if (gamesProcessed == totalGames) {
                        // Create UserGameResults object with whatever data we have
                        val userGameResults = UserGameResults(
                            focusFlicker = focusFlickerResults,
                            taskSwitcher = taskSwitcherResults,
                            wordRecall = wordRecallResults
                        )
                        
                        // Return the result
                        callback(DatabaseResult.Success(userGameResults))
                    }
                }
        }
    }
    
    /**
     * Format a timestamp into a readable date string
     * @param timestamp The timestamp in milliseconds
     * @return A formatted date string
     */
    private fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(date)
    }
    
    /**
     * Add a user game attempt to the database
     * @param userId The ID of the user to add the attempt for
     * @param gameType The type of game (must be one of the GameType enum values)
     * @param score The score for this attempt
     * @param callback Callback to be invoked with the result of the operation
     */
    actual fun addUserGameAttempt(userId: String, gameType: GameType, score: Int, callback: (DatabaseResult<Boolean>) -> Unit) {
        // Get the database name directly from the GameType enum
        val dbName = gameType.dbName
        
        // Create a new attempt entry
        val timestamp = System.currentTimeMillis()
        val attemptData = mapOf(
            "score" to score,
            "timestamp" to timestamp
        )
        
        // Generate a unique key for this attempt
        val attemptKey = database.child(miniGameScoresPath).child(userId).child(dbName).push().key
        
        if (attemptKey == null) {
            callback(DatabaseResult.Error("Failed to generate key for attempt"))
            return
        }
        
        // Add the attempt to the database
        database.child(miniGameScoresPath)
            .child(userId)
            .child(dbName)
            .child(attemptKey)
            .setValue(attemptData)
            .addOnSuccessListener {
                callback(DatabaseResult.Success(true))
            }
            .addOnFailureListener { e ->
                callback(DatabaseResult.Error("Failed to add attempt: ${e.message}"))
            }
    }

    /**
     * Fetch all attempts of a particular minigame for a user
     * @param userId The ID of the user to fetch scores for
     * @param gameType The type of game (must be one of the GameType enum values)
     * @param callback Callback to be invoked with the result of the operation
     */
    actual fun getUserGameAttempts(userId: String, gameType: GameType, callback: (DatabaseResult<List<GameAttempts>>) -> Unit) {
        val dbName = gameType.dbName
        val displayName = gameType.displayName

        database.child(miniGameScoresPath)
            .child(userId)
            .child(dbName)
            .get()
            .addOnSuccessListener { snapshot ->
                val attemptsList = mutableListOf<GameAttempt>()
                val scores = mutableListOf<Int>()
                val timestamps = mutableListOf<Long>()

                if (snapshot.exists() && snapshot.hasChildren()) {
                    snapshot.children.forEach { attemptSnapshot ->
                        val score = attemptSnapshot.child("score").value?.toString()?.toIntOrNull() ?: 0
                        val timestamp = attemptSnapshot.child("timestamp").value?.toString()?.toLongOrNull() ?: 0L
                        scores.add(score)
                        timestamps.add(timestamp)
                        attemptsList.add(
                            GameAttempt(
                                gameName = displayName,
                                score = score,
                                timestamp = formatTimestamp(timestamp)
                            )
                        )
                    }
                        val totalAttempts = scores.size
                        val lastTimestamp = timestamps.maxOrNull() ?: 0L
                        val lastPlayed = formatTimestamp(lastTimestamp)
                        val averageScore = scores.average().toInt()
                        val minScore = scores.minOrNull() ?: 0
                        val maxScore = scores.maxOrNull() ?: 0

                        val gameResult = GameResult(
                            gameName = displayName,
                            totalAttempts = totalAttempts,
                            lastPlayed = lastPlayed,
                            averageScore = averageScore,
                            minScore = minScore,
                            maxScore = maxScore,
                        )
                    callback(DatabaseResult.Success(listOf(GameAttempts(attempts = attemptsList, gameResult = gameResult))))
                } else {
                    callback(DatabaseResult.Error("No data available"))
                }
            }
            .addOnFailureListener { e ->
                callback(DatabaseResult.Error("${e.message}"))
            }
    }
}

