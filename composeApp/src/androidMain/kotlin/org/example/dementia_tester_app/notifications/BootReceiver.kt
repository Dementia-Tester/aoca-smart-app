package org.example.dementia_tester_app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    companion object { private const val TAG = "BootReceiver" }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        Log.i(TAG, "boot complete; reschedule")
        try {
            LocalNotificationManager(context).rescheduleNotificationsAfterReboot()
        } catch (_: Exception) {
            Log.e(TAG, "boot reschedule error")
        }
    }
}