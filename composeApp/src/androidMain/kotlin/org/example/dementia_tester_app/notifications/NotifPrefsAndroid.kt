package org.example.dementia_tester_app.notifications

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Android implementation of NotifPrefs using SharedPreferences.
 * 
 * This class stores notification preferences in SharedPreferences
 * with keys following the pattern defined in PREF_KEY_*.
 */
class NotifPrefsAndroid(private val context: Context) : NotifPrefs {
    
    companion object {
        private const val PREFS_NAME = "notification_prefs"
        private const val PREF_KEY_DISMISSED_NUDGE = "dismissed_notif_nudge"
    }
    
    private val prefs: SharedPreferences by lazy {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Whether the user has dismissed the notification nudge.
     */
    override var dismissedNotifNudge: Boolean
        get() = prefs.getBoolean(PREF_KEY_DISMISSED_NUDGE, false)
        set(value) {
            prefs.edit {
                putBoolean(PREF_KEY_DISMISSED_NUDGE, value)
            }
        }
}