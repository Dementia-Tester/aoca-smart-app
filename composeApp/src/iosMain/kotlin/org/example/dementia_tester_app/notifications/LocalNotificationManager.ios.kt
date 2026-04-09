package org.example.dementia_tester_app.notifications

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.Foundation.NSURL
import kotlin.math.max

/**
 * iOS implementation of the LocalNotificationManager class.
 * Uses UNUserNotificationCenter to schedule notifications.
 */
@OptIn(ExperimentalForeignApi::class)
actual class LocalNotificationManager actual constructor() {
    
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    private var isInitialized = false
    private var permissionGranted = false
    
    /**
     * Initialize the notification manager by requesting permission.
     * This should be called early in the app lifecycle.
     */
    fun initialize() {
        if (!isInitialized) {
            checkNotificationPermission()
            isInitialized = true
        }
    }
    
    /**
     * Check if notification permission is granted.
     */
    private fun checkNotificationPermission() {
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            permissionGranted = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
            
            if (!permissionGranted) {
                requestNotificationPermission()
            }
        }
    }
    
    /**
     * Request permission to show notifications.
     */
    private fun requestNotificationPermission() {
        val options = UNAuthorizationOptionAlert or 
                UNAuthorizationOptionBadge or 
                UNAuthorizationOptionSound
        
        notificationCenter.requestAuthorizationWithOptions(options) { granted, error ->
            permissionGranted = granted
            
            if (granted) {
                println("Notification permission granted")
            } else {
                println("Notification permission denied: ${error?.localizedDescription}")
            }
        }
    }
    
    /**
     * Schedule a notification after a delay (in millis)
     */
    actual fun scheduleNotificationIn(id: String, message: String, delayMillis: Long) {
        try {
            // Ensure we have at least 1 second delay (iOS requirement)
            val delaySeconds = max(1.0, delayMillis / 1000.0)
            
            // Create notification content
            val content = UNMutableNotificationContent().apply {
                setTitle("Dementia Tester")
                setBody(message)
                setSound(UNNotificationSound.defaultSound)
            }
            
            // Create time interval trigger
            val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                delaySeconds,
                repeats = false
            )
            
            // Create request
            val request = UNNotificationRequest.requestWithIdentifier(
                id,
                content,
                trigger
            )
            
            // Schedule notification
            notificationCenter.addNotificationRequest(request) { error ->
                if (error != null) {
                    println("Failed to schedule notification: ${error.localizedDescription}")
                }
            }
        } catch (e: Exception) {
            println("Error scheduling notification: ${e.message}")
        }
    }
    
    /**
     * Schedule a notification at an absolute time (UTC)
     */
    actual fun scheduleNotificationAt(id: String, message: String, utcTimeMillis: Long) {
        try {
            // Convert UTC time to NSDate
            val date = NSDate.dateWithTimeIntervalSince1970(utcTimeMillis / 1000.0)
            
            // Create calendar components from date
            val calendar = NSCalendar.currentCalendar
            val components = calendar.components(
                NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or
                        NSCalendarUnitHour or NSCalendarUnitMinute or NSCalendarUnitSecond,
                date
            )
            
            // Create notification content
            val content = UNMutableNotificationContent().apply {
                setTitle("Dementia Tester")
                setBody(message)
                setSound(UNNotificationSound.defaultSound)
            }
            
            // Create calendar trigger
            val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                components,
                repeats = false
            )
            
            // Create request
            val request = UNNotificationRequest.requestWithIdentifier(
                id,
                content,
                trigger
            )
            
            // Schedule notification
            notificationCenter.addNotificationRequest(request) { error ->
                if (error != null) {
                    println("Failed to schedule notification at specific time: ${error.localizedDescription}")
                }
            }
        } catch (e: Exception) {
            println("Error scheduling notification at specific time: ${e.message}")
        }
    }
    
    /**
     * Cancel a scheduled notification
     */
    actual fun cancelNotification(id: String) {
        try {
            notificationCenter.removePendingNotificationRequestsWithIdentifiers(listOf(id))
        } catch (e: Exception) {
            println("Error cancelling notification: ${e.message}")
        }
    }
    
    /**
     * Check if all required permissions for scheduling notifications are granted.
     * 
     * @return true if all required permissions are granted, false otherwise
     */
    actual fun areNotificationPermissionsGranted(): Boolean {
        // Return the cached permission status
        return permissionGranted
    }
    
    /**
     * Request or guide the user to grant all required permissions for scheduling notifications.
     * On iOS, this will open the app's settings page where the user can enable notifications.
     * 
     * @return true if the request was initiated successfully, false otherwise
     */
    actual fun requestNotificationPermissions(): Boolean {
        try {
            // Open the app's settings page
            val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
            if (settingsUrl != null) {
                UIApplication.sharedApplication.openURL(settingsUrl)
                return true
            }
            return false
        } catch (e: Exception) {
            println("Error opening app settings: ${e.message}")
            return false
        }
    }
}