package org.example.dementia_tester_app.notifications

/**
 * Interface for storing notification preferences.
 * This interface provides methods for checking and setting whether the user has dismissed
 * the notification nudge.
 */
interface NotifPrefs {
    /**
     * Whether the user has dismissed the notification nudge.
     */
    var dismissedNotifNudge: Boolean
}

/**
 * A gate that decides if we should nudge the user to enable notifications.
 * 
 * This class wraps the LocalNotificationManager and provides methods for checking if
 * we should nudge the user to enable notifications, marking the nudge as dismissed,
 * and opening the notification settings.
 * 
 * @param mgr The LocalNotificationManager to use for checking permissions and opening settings
 * @param prefs The NotifPrefs to use for storing user preferences
 */
class NotificationGate(
    private val mgr: LocalNotificationManager,
    private val prefs: NotifPrefs
) {
    /**
     * Check if we should nudge the user to enable notifications.
     * 
     * @return true if notifications are not enabled and the user has not dismissed the nudge
     */
    fun shouldNudgeUser(): Boolean =
        !mgr.areNotificationPermissionsGranted() && !prefs.dismissedNotifNudge

    /**
     * Mark the notification nudge as dismissed.
     */
    fun markDismissed() { 
        prefs.dismissedNotifNudge = true 
    }

    /**
     * Open the notification settings.
     * 
     * @return true if the settings were opened successfully
     */
    fun openSettings(): Boolean = mgr.requestNotificationPermissions()
}