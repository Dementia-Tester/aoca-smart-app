package org.example.dementia_tester_app.notifications


/**
 * Interface for scheduling reminders across platforms.
 * This interface provides methods to schedule reminders after a delay,
 * at a specific time, and to cancel scheduled reminders.
 */
interface ReminderScheduler {
    /**
     * Schedule a reminder at an absolute time (UTC)
     */
    fun scheduleAt(id: String, message: String, utcMillis: Long, policy: ReminderPolicy = ReminderPolicy())
    
    /**
     * Schedule a reminder after a delay (in millis)
     */
    fun scheduleIn(id: String, message: String, delayMillis: Long, policy: ReminderPolicy = ReminderPolicy())
    
    /**
     * Cancel a scheduled reminder
     */
    fun cancel(id: String)
}

/**
 * Configuration options for scheduling reminders.
 *
 * @param channel The notification channel to use (e.g., "general", "health")
 * @param exact Whether the reminder should be scheduled at the exact time (may use more battery)
 * @param allowAfterReboot Whether the reminder should persist after device reboot
 */
data class ReminderPolicy(
    val channel: String = ReminderChannels.GENERAL,
    val exact: Boolean = false,
    val allowAfterReboot: Boolean = false
)

/**
 * Predefined notification channels for different types of reminders.
 */
object ReminderChannels {
    const val GENERAL = "general"
    const val HEALTH = "health"
}

/**
 * Helper methods for generating consistent reminder IDs.
 */
object ReminderIds {
    /**
     * Generate a reminder ID for a health survey.
     *
     * @param surveyId The unique identifier for the survey
     * @return The reminder ID for the health survey
     */
    fun healthSurvey(surveyId: String) = "health_survey_$surveyId"
}

/**
 * Adapter that implements ReminderScheduler using LocalNotificationManager.
 * This allows the existing platform-specific implementations to be used with
 * the new reminder system without modification.
 */
class LocalNotificationManagerAdapter(private val notificationManager: LocalNotificationManager) : ReminderScheduler {
    override fun scheduleAt(id: String, message: String, utcMillis: Long, policy: ReminderPolicy) {
        try {
            notificationManager.scheduleNotificationAt(id, message, utcMillis)
        } catch (_: Throwable) {
            // no-op: keep UI stable, platform code logs
        }
    }

    override fun scheduleIn(id: String, message: String, delayMillis: Long, policy: ReminderPolicy) {
        try {
            notificationManager.scheduleNotificationIn(id, message, delayMillis)
        } catch (_: Throwable) {
            // no-op
        }
    }

    override fun cancel(id: String) {
        try {
            notificationManager.cancelNotification(id)
        } catch (_: Throwable) {
            // no-op
        }
    }
}

/**
 * Helper class for scheduling reminders with common patterns.
 * This class provides convenience methods for scheduling reminders
 * with de-duplication and other common patterns.
 */
class ReminderHelper(private val scheduler: ReminderScheduler) {
    fun upsertIn(id: String, message: String, delayMillis: Long, policy: ReminderPolicy = ReminderPolicy()): Boolean {
        return try {
            try { scheduler.cancel(id) } catch (_: Throwable) {}
            scheduler.scheduleIn(id, message, delayMillis, policy)
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun upsertAt(id: String, message: String, utcMillis: Long, policy: ReminderPolicy = ReminderPolicy()): Boolean {
        return try {
            try { scheduler.cancel(id) } catch (_: Throwable) {}
            scheduler.scheduleAt(id, message, utcMillis, policy)
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun cancel(id: String): Boolean {
        return try {
            scheduler.cancel(id)
            true
        } catch (_: Throwable) {
            false
        }
    }
}
