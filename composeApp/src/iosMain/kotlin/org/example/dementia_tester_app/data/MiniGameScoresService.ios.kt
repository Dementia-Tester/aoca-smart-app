package org.example.dementia_tester_app.data

import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import platform.Foundation.NSDictionary
import platform.Foundation.NSError
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSEnumerator
import platform.Foundation.allKeys
import platform.posix.err


actual class MiniGameScoresService {
    private val miniGameScoresPath = "MiniGameScores"

    /**
     * Fetch user's mini-game scores from Firebase
     * @param userId The ID of the user to fetch scores for
     * @param callback Callback function that will be called with the result
     */
    actual fun getUserGameScores(userId: String, callback: (DatabaseResult<UserGameResults>) -> Unit) {
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) {
            callback(DatabaseResult.Error("Firebase not initialized"))
            return
        }

        var focusFlickerResults: GameResult? = null
        var taskSwitcherResults: GameResult? = null
        var wordRecallResults: GameResult? = null

        var gamesProcessed = 0
        val totalGames = GameType.entries.size

        for (gameType in GameType.entries) {
            val dbName = gameType.dbName
            val displayName = gameType.displayName

            ref.child(miniGameScoresPath)
                .child(userId)
                .child(dbName)
                .getDataWithCompletionBlock { error: NSError?, snapshot: FIRDataSnapshot? ->
                    if (error == null && snapshot != null && snapshot.exists()) {
                        val scores = mutableListOf<Int>()
                        val timestamps = mutableListOf<Long>()

                        val value = snapshot.value
                        when (value) {
                            is Map<*, *> -> {
                                for ((_, attemptAny) in value) {
                                    val attemptMap = when (attemptAny) {
                                        is Map<*, *> -> attemptAny
                                        is NSDictionary -> nsDictionaryToKotlinMap(attemptAny)
                                        else -> null
                                    }
                                    if (attemptMap != null) {
                                        val score = anyToInt(attemptMap["score"]) ?: 0
                                        val ts = anyToLong(attemptMap["timestamp"]) ?: 0L
                                        scores.add(score)
                                        timestamps.add(ts)
                                    }
                                }
                            }
                            is NSDictionary -> {
                                val dict = value
                                val keys = dict.allKeys
                                for (k in keys) {
                                    val v = dict.objectForKey(k)
                                    val attemptMap = when (v) {
                                        is Map<*, *> -> v
                                        is NSDictionary -> nsDictionaryToKotlinMap(v)
                                        else -> null
                                    }
                                    if (attemptMap != null) {
                                        val score = anyToInt(attemptMap["score"]) ?: 0
                                        val ts = anyToLong(attemptMap["timestamp"]) ?: 0L
                                        scores.add(score)
                                        timestamps.add(ts)
                                    }
                                }
                            }
                            else -> {
                                // No attempts
                            }
                        }

                        if (scores.isNotEmpty()) {
                            val totalAttempts = scores.size
                            val lastTimestamp = timestamps.maxOrNull() ?: 0L
                            val lastPlayed = formatTimestamp(lastTimestamp)
                            val averageScore = scores.sum().toDouble().div(scores.size).toInt()
                            val minScore = scores.minOrNull() ?: 0
                            val maxScore = scores.maxOrNull() ?: 0

                            val gameResults = GameResult(
                                gameName = displayName,
                                totalAttempts = totalAttempts,
                                lastPlayed = lastPlayed,
                                averageScore = averageScore,
                                minScore = minScore,
                                maxScore = maxScore
                            )

                            when (gameType) {
                                GameType.COMPLEX_ATTENTION -> focusFlickerResults = gameResults
                                GameType.EXECUTIVE_FUNCTION -> taskSwitcherResults = gameResults
                                GameType.LEARNING_AND_MEMORY -> wordRecallResults = gameResults
                            }
                        }
                    }

                    gamesProcessed++
                    if (gamesProcessed == totalGames) {
                        val userGameResults = UserGameResults(
                            focusFlicker = focusFlickerResults,
                            taskSwitcher = taskSwitcherResults,
                            wordRecall = wordRecallResults
                        )
                        callback(DatabaseResult.Success(userGameResults))
                    }
                }
        }
    }

    /**
     * Add a user game attempt to the database
     * @param userId The ID of the user to add the attempt for
     * @param gameType The type of game (must be one of the GameType enum values)
     * @param score The score for this attempt
     * @param callback Callback to be invoked with the result of the operation
     */
    actual fun addUserGameAttempt(userId: String, gameType: GameType, score: Int, callback: (DatabaseResult<Boolean>) -> Unit) {
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) {
            callback(DatabaseResult.Error("Firebase not initialized"))
            return
        }

        val dbName = gameType.dbName
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val attemptData: Map<String, Any> = mapOf(
            "score" to score,
            "timestamp" to timestamp
        )

        val attemptsRef = ref.child(miniGameScoresPath).child(userId).child(dbName).childByAutoId()
        if (attemptsRef == null) {
            callback(DatabaseResult.Error("Failed to generate key for attempt"))
            return
        }
        attemptsRef.setValue(attemptData) { error: NSError?, _ ->
            if (error == null) {
                callback(DatabaseResult.Success(true))
            } else {
                callback(DatabaseResult.Error("Failed to add attempt: ${'$'}{error.localizedDescription}"))
            }
        }
    }

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

    private fun anyToInt(v: Any?): Int? = when (v) {
        null -> null
        is Number -> v.toInt()
        is String -> v.toIntOrNull()
        else -> null
    }

    private fun anyToLong(v: Any?): Long? = when (v) {
        null -> null
        is Number -> v.toLong()
        is String -> v.toLongOrNull()
        else -> null
    }

    private fun formatTimestamp(ts: Long): String {
        if (ts <= 0L) return ""
        val ldt = Instant.fromEpochMilliseconds(ts).toLocalDateTime(TimeZone.currentSystemDefault())
        val y = ldt.year.toString()
        val m = ldt.monthNumber.toString().padStart(2, '0')
        val d = ldt.dayOfMonth.toString().padStart(2, '0')
        return "$y-$m-$d"
    }

    /**
     * Fetch all attempts of a particular minigame for a user
     * This is a stub implementation that returns empty game results
     * @param userId The ID of the user to fetch scores for
     * @param gameType The type of game (must be one of the GameType enum values)
     * @param callback Callback to be invoked with the result of the operation
     */
    actual fun getUserGameAttempts(userId: String, gameType: GameType, callback: (DatabaseResult<List<GameAttempts>>) -> Unit) {

        val ref = FIRDatabase.database()?.reference()
        if (ref == null) {
            callback(DatabaseResult.Error("Firebase not initialized"))
            return
        }

        val dbName = gameType.dbName
        val displayName = gameType.displayName

        ref.child(miniGameScoresPath)
            .child(userId)
            .child(dbName)
            .getDataWithCompletionBlock { error: NSError?, snapshot: FIRDataSnapshot? ->
                if (error != null) {
                    callback(DatabaseResult.Error(error.localizedDescription))
                    return@getDataWithCompletionBlock
                }
                if (error == null && snapshot != null && snapshot.exists()) {
                    val attemptsList = mutableListOf<GameAttempt>()
                    val scores = mutableListOf<Int>()
                    val timestamps = mutableListOf<Long>()

                    val enumerator = snapshot.children as? NSEnumerator
                    var obj = enumerator?.nextObject()
                    while (obj != null) {
                        val attemptSnapshot = obj as FIRDataSnapshot

                        val score = anyToInt(attemptSnapshot.childSnapshotForPath("score")?.value)
                        val timestampRaw = anyToLong(attemptSnapshot.childSnapshotForPath("timestamp")?.value)

                        if (score != null) {
                            scores.add(score)
                        }

                        if (timestampRaw != null) {
                            timestamps.add(timestampRaw)
                        }
                        if (score != null && timestampRaw != null) {
                            attemptsList.add(
                                GameAttempt(
                                    gameName = displayName,
                                    score = score,
                                    timestamp = formatTimestamp(timestampRaw)
                                )
                            )
                        }
                        obj = enumerator?.nextObject()
                    }
                    val totalAttempts = scores.size
                    val lastTimestamp = timestamps.maxOrNull() ?: 0L
                    val lastPlayed = formatTimestamp(lastTimestamp)
                    val averageScore = if (scores.isNotEmpty()) scores.average().toInt() else 0
                    val minScore = scores.minOrNull() ?: 0
                    val maxScore = scores.maxOrNull() ?: 0

                    val gameResult = GameResult(
                        gameName = displayName,
                        totalAttempts = totalAttempts,
                        lastPlayed = lastPlayed,
                        averageScore = averageScore,
                        minScore = minScore,
                        maxScore = maxScore
                    )
                    callback(DatabaseResult.Success(listOf(GameAttempts(attempts = attemptsList, gameResult = gameResult))))
                } else {
                    callback(DatabaseResult.Error("No data available"))
                }

            }
    }

}

