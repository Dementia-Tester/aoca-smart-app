package org.example.dementia_tester_app.notifications

/**
 * Provider for accessing the platform-specific notification manager.
 * This ensures that we use the properly initialized notification manager
 * with the correct context on Android.
 */
expect object NotificationManagerProvider {
    /**
     * Get the platform-specific notification manager instance.
     * On Android, this returns the singleton instance from MainActivity.
     * On iOS, this returns the singleton instance from IOSNotifications.
     */
    fun getNotificationManager(): LocalNotificationManager
}