package org.example.dementia_tester_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import android.util.Log
import org.example.dementia_tester_app.notifications.LocalNotificationManager
import org.example.dementia_tester_app.notifications.NotificationGate
import org.example.dementia_tester_app.notifications.NotifPrefsAndroid
import org.example.dementia_tester_app.notifications.NotificationReceiver
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    
    // Create singleton instances of notification components
    companion object {
        private const val TAG = "MainActivity"
        
        lateinit var notificationManager: LocalNotificationManager
            private set
        
        lateinit var notificationGate: NotificationGate
            private set
    }
    
    // Request permission launcher for notifications
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
            // Permission granted, initialize notification manager
            initializeNotificationManager()
        } else {
            Log.d(TAG, "Notification permission denied")
            // Permission denied, handle gracefully
            // You might want to show a message to the user
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Ensure our notification channel exists at app start
        ensureHealthChannel()
        
        // Check and request notification permission if needed
        checkNotificationPermission()
        
        setContent {
            App()
        }
    }
    
    private fun checkNotificationPermission() {
        // For Android 13+ (API 33+), we need to request POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {
                PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Notification permission already granted")
                    // Permission already granted, initialize notification manager
                    initializeNotificationManager()
                }
                else -> {
                    // Request the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For older Android versions, no runtime permission needed
            initializeNotificationManager()
        }
    }
    
    private fun initializeNotificationManager() {
        // Initialize the notification manager with context
        notificationManager = LocalNotificationManager(this)
        
        // Initialize NotifPrefs and NotificationGate
        val notifPrefs = NotifPrefsAndroid(this)
        notificationGate = NotificationGate(notificationManager, notifPrefs)
    }

    private fun ensureHealthChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val nm = getSystemService(NotificationManager::class.java)
                val existing = nm.getNotificationChannel(NotificationReceiver.CHANNEL_ID)
                if (existing == null) {
                    val channel = NotificationChannel(
                        NotificationReceiver.CHANNEL_ID,
                        NotificationReceiver.CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = NotificationReceiver.CHANNEL_DESCRIPTION
                    }
                    nm.createNotificationChannel(channel)
                    Log.d(TAG, "Created notification channel '${NotificationReceiver.CHANNEL_ID}' at app start")
                } else {
                    Log.d(TAG, "Notification channel '${NotificationReceiver.CHANNEL_ID}' already exists (importance=${existing.importance})")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to ensure notification channel", e)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            App()
        }
    }
} 