package org.example.dementia_tester_app.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import org.example.dementia_tester_app.notifications.NotificationReceiver.Companion.ACTION_SHOW_NOTIFICATION
import org.example.dementia_tester_app.notifications.NotificationReceiver.Companion.NOTIFICATION_ID_KEY
import org.example.dementia_tester_app.notifications.NotificationReceiver.Companion.NOTIFICATION_MESSAGE_KEY
import org.example.dementia_tester_app.notifications.NotificationReceiver.Companion.EXPECTED_TIME_KEY
import androidx.core.net.toUri

/**
 * Android implementation of the LocalNotificationManager class.
 * Uses AlarmManager to schedule notifications.
 */
actual class LocalNotificationManager actual constructor() {
    
    private lateinit var context: Context
    
    // Secondary constructor to initialize with context
    constructor(context: Context) : this() {
        initialize(context)
    }
    
    // Initialize from MainActivity
    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    companion object {
        private const val TAG = "LocalNotificationMgr"
        private const val PREFS_NAME = "notifications_prefs"
        private const val KEY_PREFIX_TIME = "notification_time_"
        private const val KEY_PREFIX_MESSAGE = "notification_message_"
    }
    
    
    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Centralized intent and PendingIntent builders
    private fun buildNotificationIntent(id: String, message: String?, expectedTime: Long?): Intent =
        Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_SHOW_NOTIFICATION
            putExtra(NOTIFICATION_ID_KEY, id)
            if (message != null) putExtra(NOTIFICATION_MESSAGE_KEY, message)
            if (expectedTime != null) putExtra(EXPECTED_TIME_KEY, expectedTime)
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }

    private fun buildPendingIntent(id: String, message: String?, expectedTime: Long?): PendingIntent {
        val intent = buildNotificationIntent(id, message, expectedTime)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, id.hashCode(), intent, flags)
    }

    // Centralized exact vs inexact scheduling
    private fun scheduleAlarm(id: String, triggerAtMillis: Long, exactRequested: Boolean, pendingIntent: PendingIntent) {
        val canExact = (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) || alarmManager.canScheduleExactAlarms()
        val useExact = exactRequested && canExact
        if (useExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }
    
    /**
     * Schedule a notification after a delay (in millis)
     */
    actual fun scheduleNotificationIn(id: String, message: String, delayMillis: Long) {
        val triggerTimeMillis = System.currentTimeMillis() + delayMillis
        scheduleNotificationAt(id, message, triggerTimeMillis)
    }
    
    /**
     * Schedule a notification at an absolute time (UTC)
     * 
     * @param id The unique identifier for the notification
     * @param message The message to display in the notification
     * @param utcTimeMillis The absolute time in milliseconds (UTC) to show the notification
     */
    actual fun scheduleNotificationAt(id: String, message: String, utcTimeMillis: Long) {
        if (!::context.isInitialized) {
            Log.e(TAG, "Context not initialized - cannot schedule notification")
            return
        }

        // Check if the scheduled time is in the future
        val currentTimeMillis = System.currentTimeMillis()
        if (utcTimeMillis <= currentTimeMillis) {
            Log.e(TAG, "Cannot schedule notification for past time: $utcTimeMillis (current: $currentTimeMillis)")
            return
        }

        val delayMillis = utcTimeMillis - currentTimeMillis

        // One-line schedule request log
        Log.i(TAG, "schedule request id=$id delayMs=$delayMillis at=$utcTimeMillis")

        // Store details for reboot reschedule
        saveNotificationDetails(id, message, utcTimeMillis)

        try {
            val pi = buildPendingIntent(id, message, utcTimeMillis)
            // Exact requested by policy in this app (kept behavior)
            scheduleAlarm(id, utcTimeMillis, exactRequested = true, pendingIntent = pi)
        } catch (_: SecurityException) {
            Log.e(TAG, "schedule failed: permission")
        } catch (_: Exception) {
            Log.e(TAG, "schedule failed")
        }
    }
    
    /**
     * Cancel a scheduled notification
     */
    actual fun cancelNotification(id: String) {
        if (!::context.isInitialized) {
            Log.e(TAG, "Context not initialized - cannot cancel notification")
            return
        }
        
        // Remove notification details from storage
        removeNotificationDetails(id)
        
        try {
            val pi = buildPendingIntent(id, null, null)
            alarmManager.cancel(pi)
            pi.cancel()
            Log.i(TAG, "cancel id=$id")
        } catch (_: Exception) {
            Log.e(TAG, "cancel failed id=$id")
        }
    }
    
    /**
     * Check if notifications are enabled for the app.
     * 
     * @return true if notifications are enabled, false otherwise
     */
    actual fun areNotificationPermissionsGranted(): Boolean {
        if (!::context.isInitialized) {
            Log.w(TAG, "Context not initialized")
            return false
        }
        
        // Simply check if app notifications are enabled
        val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!notificationsEnabled) {
            Log.w(TAG, "App notifications are not enabled")
        }
        
        return notificationsEnabled
    }
    
    /**
     * Request or guide the user to grant all required permissions for scheduling notifications.
     * 
     * If API 26+: open app notification settings.
     * If API 31+ and exact alarms not allowed: open ACTION_REQUEST_SCHEDULE_EXACT_ALARM.
     * If API 23+ and battery optimization is enabled: request battery optimization exemption.
     * 
     * @return true if the request was initiated successfully, false otherwise
     */
    @SuppressLint("BatteryLife")
    actual fun requestNotificationPermissions(): Boolean {
        if (!::context.isInitialized) {
            Log.w(TAG, "Context not initialized")
            return false
        }
        
        try {
            // Check if we need to request battery optimization exemption
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                
                Log.d(TAG, "Requesting battery optimization exemption")
                try {
                    val intent = Intent().apply {
                        action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        data = "package:${context.packageName}".toUri()
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to request battery optimization exemption", e)
                    // Continue with other permission requests
                }
            }
            
            // For Android 12+ (API 31+), check if we need to guide for exact-alarm permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                
                val intent = Intent().apply {
                    action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    data = Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return true
            }
            
            // For Android 8.0+ (API 26+), open app notification settings
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent().apply {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return true
            } 
            
            // For older Android versions, open app settings
            else {
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open notification settings", e)
            return false
        }
    }
    
    /**
     * Reschedule all notifications after device reboot.
     * This method is called by the BootReceiver.
     */
    fun rescheduleNotificationsAfterReboot() {
        if (!::context.isInitialized) return

        val all = prefs.all ?: return
        val now = System.currentTimeMillis()
        val timeKeys = all.keys.filter { it.startsWith(KEY_PREFIX_TIME) }
        timeKeys.forEach { timeKey ->
            val id = timeKey.removePrefix(KEY_PREFIX_TIME)
            val at = (all[timeKey] as? Long) ?: return@forEach
            if (at <= now) {
                removeNotificationDetails(id)
                return@forEach
            }
            val message = all[(KEY_PREFIX_MESSAGE + id)] as? String ?: return@forEach
            try {
                val pi = buildPendingIntent(id, message, at)
                scheduleAlarm(id, at, exactRequested = true, pendingIntent = pi)
            } catch (_: Exception) {
                Log.e(TAG, "reschedule failed id=$id")
            }
        }
    }
    
    /**
     * Save notification details to SharedPreferences for potential rescheduling after reboot.
     */
    private fun saveNotificationDetails(id: String, message: String, timeMillis: Long) {
        if (!::context.isInitialized) {
            Log.w(TAG, "Context not initialized")
            return
        }
        
        prefs.edit {
            putLong(KEY_PREFIX_TIME + id, timeMillis)
            putString(KEY_PREFIX_MESSAGE + id, message)
        }
    }
    
    /**
     * Remove notification details from SharedPreferences.
     */
    private fun removeNotificationDetails(id: String) {
        if (!::context.isInitialized) {
            Log.w(TAG, "Context not initialized")
            return
        }
        
        prefs.edit {
            remove(KEY_PREFIX_TIME + id)
            remove(KEY_PREFIX_MESSAGE + id)
        }
    }
}