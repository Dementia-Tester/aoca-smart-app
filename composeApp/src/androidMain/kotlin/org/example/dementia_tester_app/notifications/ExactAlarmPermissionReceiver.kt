package org.example.dementia_tester_app.notifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * BroadcastReceiver for handling changes to the exact alarm permission.
 * This receiver is triggered when the user grants or revokes the SCHEDULE_EXACT_ALARM permission
 * on Android 12+ (API 31+) and is responsible for rescheduling pending alarms when permission is granted.
 */
class ExactAlarmPermissionReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "ExactAlarmPermissionReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        if (intent.action != AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) return
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            if (alarmManager.canScheduleExactAlarms()) {
                LocalNotificationManager(context).rescheduleNotificationsAfterReboot()
                Log.i(TAG, "exact permission granted; reschedule")
            } else {
                Log.i(TAG, "exact permission revoked")
            }
        } catch (_: Exception) {
            Log.e(TAG, "exact permission change error")
        }
    }
}