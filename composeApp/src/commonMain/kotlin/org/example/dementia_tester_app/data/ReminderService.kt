package org.example.dementia_tester_app.data


/**
 * Data class to represent a reminder
 */
data class Reminder(
    var id: String? = null,
    var userId: String? = null,
    var taskType: String? = "",
    var taskTime: String? = "",
    var taskName: String? = "",
    var taskActive: Boolean = true
)

/**
 * Result class to handle the results of reminder operations
 */
sealed class ReminderResult<out T> {
    data class Success<T>(val data: T) : ReminderResult<T>()
    data class Error(val message: String) : ReminderResult<Nothing>()
}

/**
 * Interface for reminder service
 */
expect class ReminderService() {
    /**
     * Create a reminder
     * @param reminder the reminder to be created
     * @param callback callback to be invoked with the result of the operation
     */
    fun createReminder(reminder: Reminder, callback: (ReminderResult<Unit>) -> Unit)

    /**
     * Get the user's reminders
     * @param userId the ID of the user's reminders to be retrieved
     * @param callback callback to be invoked with the result of the operation
     */
    fun getReminders(userId: String, callback: (ReminderResult<List<Reminder>>) -> Unit)

    /**
     * Update a reminder
     * @param reminderId the id of the reminder to be updated
     * @param updates a map to update the specified value in the reminder
     * @param callback callback to be invoked with the result of the operation
     */
    fun updateReminder(reminderId: String, updates: Map<String, Any?>, callback: (ReminderResult<Unit>) -> Unit)

    /**
     * Delete a reminder
     * @param reminderID the id of the reminder to be deleted
     * @param callback callback to be invoked with the result of the operation
     */
    fun deleteReminder(reminderID: String, callback: (ReminderResult<Unit>) -> Unit)


}