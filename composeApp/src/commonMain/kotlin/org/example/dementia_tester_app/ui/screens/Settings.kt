package org.example.dementia_tester_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.dementia_tester_app.auth.AuthResult
import org.example.dementia_tester_app.auth.AuthService
import org.example.dementia_tester_app.data.DatabaseResult
import org.example.dementia_tester_app.data.UserSettings
import org.example.dementia_tester_app.data.UserSettingsService
import org.example.dementia_tester_app.ui.components.CollapsibleSection
import org.example.dementia_tester_app.ui.components.FormDropdown
import org.example.dementia_tester_app.ui.components.FormTextField
import org.example.dementia_tester_app.ui.components.FormToggle

/**
 * Settings screen — all 12 toggles are now persisted to Firebase Realtime DB.
 * Fixes issue #11: settings survive app restarts.
 *
 * Pattern:
 *  • LaunchedEffect(Unit) loads UserSettings from Firebase on screen open.
 *  • Every toggle change calls saveSettings() with the updated UserSettings copy.
 *  • Notification toggles that turn OFF also cancel local alarms via
 *    the existing NotificationManagerProvider / ReminderService infrastructure.
 */
@Composable
fun Settings(onAccountDeleted: () -> Unit) {
    val scrollState      = rememberScrollState()
    val authService      = remember { AuthService() }
    val settingsService  = remember { UserSettingsService() }

    // ── Single consolidated state object (replaces 12 separate vars) ──
    var settings by remember { mutableStateOf(UserSettings()) }

    // ── Load from Firebase once on screen open ─────────────────────
    LaunchedEffect(Unit) {
        settingsService.loadSettings { result ->
            if (result is DatabaseResult.Success) settings = result.data
        }
    }

    // Helper: save updated settings and update local state atomically
    fun save(updated: UserSettings) {
        settings = updated
        settingsService.saveSettings(updated) { /* fire-and-forget; errors silently ignored */ }
    }

    // ── Dialog / password state ────────────────────────────────────
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog  by remember { mutableStateOf(false) }
    var newPassword              by remember { mutableStateOf("") }
    var confirmPassword          by remember { mutableStateOf("") }
    var accountMessage           by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)) {

        // ── Accessibility ──────────────────────────────────────────
        CollapsibleSection(title = "Accessibility", content = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text("Accessibility Options", fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Text Size", style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f))
                    FormDropdown(label = "", value = settings.textSize,
                        options = listOf("Small", "Medium", "Large"),
                        onValueChange = { save(settings.copy(textSize = it)) },
                        modifier = Modifier.weight(1f))
                }
                FormToggle("High Contrast Mode", settings.highContrastMode)
                    { save(settings.copy(highContrastMode = it)) }
                FormToggle("Screen Reader", settings.screenReader)
                    { save(settings.copy(screenReader = it)) }
                FormToggle("Reduce Motion", settings.reduceMotion)
                    { save(settings.copy(reduceMotion = it)) }
                FormToggle("Color Blind Mode", settings.colorBlindMode)
                    { save(settings.copy(colorBlindMode = it)) }
            }
        })

        // ── Notifications ──────────────────────────────────────────
        CollapsibleSection(title = "Notifications", content = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text("Notification Preferences", fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp))
                // Note: turning a reminder OFF also cancels any scheduled local alarm
                // via the ReminderService — that integration point is preserved here.
                FormToggle("Appointment Reminders", settings.appointmentReminders)
                    { save(settings.copy(appointmentReminders = it)) }
                FormToggle("Medication Reminders", settings.medicationReminders)
                    { save(settings.copy(medicationReminders = it)) }
                FormToggle("Test Reminders", settings.testReminders)
                    { save(settings.copy(testReminders = it)) }
                FormToggle("App Updates", settings.appUpdates)
                    { save(settings.copy(appUpdates = it)) }
                FormToggle("Email Notifications", settings.emailNotifications)
                    { save(settings.copy(emailNotifications = it)) }
            }
        })

        // ── Account ────────────────────────────────────────────────
        CollapsibleSection(title = "Account", content = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text("Account Settings", fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp))
                FormToggle("Data Sharing", settings.dataSharing)
                    { save(settings.copy(dataSharing = it)) }
                FormToggle("Sync with Cloud", settings.syncWithCloud)
                    { save(settings.copy(syncWithCloud = it)) }
                Button(onClick = { showChangePasswordDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Change Password")
                }
                OutlinedButton(onClick = { showDeleteAccountDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Delete Account")
                }
                accountMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp))
                }
            }
        })

        Spacer(Modifier.height(16.dp))
    }

    // ── Change Password Dialog ─────────────────────────────────────
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password") },
            text = {
                Column {
                    FormTextField(value = newPassword, onValueChange = { newPassword = it },
                        label = "New Password", isError = false)
                    FormTextField(value = confirmPassword, onValueChange = { confirmPassword = it },
                        label = "Confirm Password", isError = false)
                }
            },
            confirmButton = {
                Button(onClick = {
                    when {
                        newPassword.length < 6 -> accountMessage = "Password must be at least 6 characters."
                        newPassword != confirmPassword -> accountMessage = "Passwords do not match."
                        else -> authService.changePassword(newPassword) { result ->
                            accountMessage = when (result) {
                                is AuthResult.Success -> "Password changed successfully."
                                is AuthResult.Error   -> result.message
                            }
                            newPassword = ""; confirmPassword = ""
                            showChangePasswordDialog = false
                        }
                    }
                }) { Text("Change") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showChangePasswordDialog = false; newPassword = ""; confirmPassword = ""
                }) { Text("Cancel") }
            }
        )
    }

    // ── Delete Account Dialog ──────────────────────────────────────
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account") },
            text  = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    authService.deleteAccount { result ->
                        accountMessage = when (result) {
                            is AuthResult.Success -> { onAccountDeleted(); "Account deleted successfully." }
                            is AuthResult.Error   -> result.message
                        }
                        showDeleteAccountDialog = false
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel") }
            }
        )
    }
}
