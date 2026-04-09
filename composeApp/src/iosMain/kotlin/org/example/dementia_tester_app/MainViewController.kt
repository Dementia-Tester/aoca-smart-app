package org.example.dementia_tester_app

import androidx.compose.ui.window.ComposeUIViewController
import org.example.dementia_tester_app.notifications.LocalNotificationManager
import platform.UIKit.UIViewController
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier


/**
 * Singleton object to hold the notification manager instance for iOS
 */
object IOSNotifications {
    val notificationManager: LocalNotificationManager by lazy {
        LocalNotificationManager().apply {
            initialize()
        }
    }
}

fun MainViewController(): UIViewController = ComposeUIViewController {
    IOSNotifications.notificationManager
    
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            App()
        }
    }
}

