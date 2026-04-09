package org.example.dementia_tester_app.notifications

import org.example.dementia_tester_app.MainActivity

actual object NotificationManagerProvider {
    actual fun getNotificationManager(): LocalNotificationManager = MainActivity.notificationManager
}