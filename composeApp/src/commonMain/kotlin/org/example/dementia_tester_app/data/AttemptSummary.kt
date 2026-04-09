package org.example.dementia_tester_app.data

/**
 * Data class representing a summary of a survey attempt
 * Used to display attempt information in the UI
 */
data class AttemptSummary(
    val attemptNumber: Int,
    val timestamp: String,
    val surveyComplete: Boolean,
    val questionsCompleted: Int,
    val totalQuestions: Int,
    val totalScore: Int = 0
) {
    /**
     * Calculate the completion percentage of this attempt
     * @return The percentage of questions completed (0-100)
     */
    fun getCompletionPercentage(): Int {
        if (totalQuestions == 0) return 0
        return ((questionsCompleted.toFloat() / totalQuestions.toFloat()) * 100).toInt()
    }
    
    /**
     * Format the completion status as a string (e.g., "5/10")
     * @return A string representing the number of questions completed out of the total
     */
    fun getCompletionText(): String {
        return "$questionsCompleted/$totalQuestions"
    }
}