package org.example.dementia_tester_app.utils

import kotlin.math.round

/**
 * Data class for ML prediction entries
 */
data class Entry(val x: Double, val y: Double)

/**
 * Determines the NCD stage based on the total score
 */
fun getNCDStage(totalScore: Int): String {
    return when {
        totalScore >= 90 -> "Normal"
        totalScore >= 70 -> "Mild"
        totalScore >= 50 -> "Moderate"
        else -> "Severe"
    }
}

/**
 * Determines the next NCD stage based on the current stage
 */
fun getNextNCDStage(currentStage: String): String {
    return when (currentStage) {
        "Normal" -> "Mild"
        "Mild" -> "Moderate"
        "Moderate" -> "Severe"
        else -> "Maximum"
    }
}

/**
 * Determines the threshold score for a given NCD stage
 */
fun getThresholdScore(stage: String): Int {
    return when (stage) {
        "Mild" -> 70
        "Moderate" -> 50
        "Severe" -> 30
        else -> 0
    }
}

/**
 * Computes the ML prediction for cognitive decline
 */
fun computeMLPrediction(entries: List<Entry>): String {
    val n = entries.size
    if (n < 6) {
        return "ML Prediction: Not enough data for prediction. At least 6 attempts are needed for ML prediction."
    }
    
    var sumX = 0.0
    var sumY = 0.0
    var sumXY = 0.0
    var sumX2 = 0.0

    for (entry in entries) {
        val x = entry.x
        val y = entry.y
        sumX += x
        sumY += y
        sumXY += x * y
        sumX2 += x * x
    }

    // Calculate slope (m) and intercept (b)
    val denominator = n * sumX2 - sumX * sumX
    if (denominator == 0.0) {
        return "ML Prediction: Unable to calculate prediction."
    }
    
    val m = (n * sumXY - sumX * sumY) / denominator
    val b = (sumY - m * sumX) / n

    // Get the last total score and attempt number
    val lastAttemptNumber = entries[n - 1].x
    val lastTotalScore = entries[n - 1].y

    // Determine current NCD stage
    val currentStage = getNCDStage(lastTotalScore.toInt())

    // Determine next NCD stage
    val nextStage = getNextNCDStage(currentStage)

    if (nextStage == "Maximum") {
        return "ML Prediction: Already at the highest NCD stage."
    }

    // Threshold scores for NCD stages
    val thresholdScore = getThresholdScore(nextStage)

    // Predict when the total score will reach the threshold
    if (m <= 0) {
        return "ML Prediction: Not progressing to the next NCD stage."
    }

    val predictedAttemptNumber = (thresholdScore - b) / m
    val deltaAttempts = predictedAttemptNumber - lastAttemptNumber

    if (deltaAttempts <= 0) {
        return "ML Prediction: Not progressing to the next NCD stage."
    }

    // Each attempt is 2 weeks apart
    val deltaWeeks = round(deltaAttempts * 2).toInt()

    return "ML Prediction: Next stage ($nextStage) in next $deltaWeeks weeks."
}