package org.example.dementia_tester_app.data

/**
 * Interface for mini-game scores service
 */
expect class MiniGameScoresService() {
    /**
     * Fetch user's mini-game scores
     * @param userId The ID of the user to fetch scores for
     * @param callback Callback to be invoked with the result of the operation
     */
    fun getUserGameScores(userId: String, callback: (DatabaseResult<UserGameResults>) -> Unit)
    
    /**
     * Add a user game attempt to the database
     * @param userId The ID of the user to add the attempt for
     * @param gameType The type of game (must be one of the GameType enum values)
     * @param score The score for this attempt
     * @param callback Callback to be invoked with the result of the operation
     */
    fun addUserGameAttempt(userId: String, gameType: GameType, score: Int, callback: (DatabaseResult<Boolean>) -> Unit)

    /**
     * Fetch all attempts of a particular minigame for a user
     * @param userId The ID of the user to fetch scores for
     * @param gameType The type of game (must be one of the GameType enum values)
     * @param callback Callback to be invoked with the result of the operation
     */
    fun getUserGameAttempts(userId: String, gameType: GameType, callback: (DatabaseResult<List<GameAttempts>>) -> Unit)
}

