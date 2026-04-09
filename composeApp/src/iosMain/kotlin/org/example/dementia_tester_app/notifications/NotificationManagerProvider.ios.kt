package org.example.dementia_tester_app.notifications

import org.example.dementia_tester_app.IOSNotifications

/**
 * iOS implementation of NotificationManagerProvider.
 * Returns the singleton instance of LocalNotificationManager from IOSNotifications.
 */
actual object NotificationManagerProvider {
    /**
     * Get the iOS notification manager instance.
     */
    actual fun getNotificationManager(): LocalNotificationManager {
        return IOSNotifications.notificationManager
    }
}