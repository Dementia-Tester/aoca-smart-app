package org.example.dementia_tester_app.notifications

/**
 * Common interface for scheduling local notifications across platforms.
 * This class provides methods to schedule notifications after a delay,
 * at a specific time, and to cancel scheduled notifications.
 */
expect class LocalNotificationManager() {
    /**
     * Schedule a notification after a delay (in millis)
     */
    fun scheduleNotificationIn(id: String, message: String, delayMillis: Long)

    /**
     * Schedule a notification at an absolute time (UTC)
     */
    fun scheduleNotificationAt(id: String, message: String, utcTimeMillis: Long)

    /**
     * Cancel a scheduled notification
     */
    fun cancelNotification(id: String)
    
    /**
     * Check if all required permissions for scheduling notifications are granted.
     * This includes platform-specific permissions like SCHEDULE_EXACT_ALARM on Android 12+.
     * 
     * @return true if all required permissions are granted, false otherwise
     */
    fun areNotificationPermissionsGranted(): Boolean
    
    /**
     * Request or guide the user to grant all required permissions for scheduling notifications.
     * On Android 12+, this will open the system settings for the SCHEDULE_EXACT_ALARM permission.
     * 
     * @return true if the request was initiated successfully, false otherwise
     */
    fun requestNotificationPermissions(): Boolean
}