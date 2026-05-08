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
import org.example.dementia_tester_app.ui.components.CollapsibleSection
import org.example.dementia_tester_app.ui.components.FormDropdown
import org.example.dementia_tester_app.ui.components.FormTextField
import org.example.dementia_tester_app.ui.components.FormToggle


/**
 * Settings screen with collapsible sections for different settings categories
 */
@Composable
fun Settings(
    onAccountDeleted: () -> Unit
) {

    val scrollState = rememberScrollState()
    val authService = remember { AuthService() }

    // Dialog states
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    // Password fields
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Message state
    var accountMessage by remember { mutableStateOf<String?>(null) }

    // State for settings
    var textSize by remember { mutableStateOf("Medium") }
    var highContrastMode by remember { mutableStateOf(false) }
    var screenReader by remember { mutableStateOf(false) }
    var reduceMotion by remember { mutableStateOf(false) }
    var colorBlindMode by remember { mutableStateOf(false) }

    var appointmentReminders by remember { mutableStateOf(true) }
    var medicationReminders by remember { mutableStateOf(true) }
    var testReminders by remember { mutableStateOf(true) }
    var appUpdates by remember { mutableStateOf(false) }
    var emailNotifications by remember { mutableStateOf(false) }

    var dataSharing by remember { mutableStateOf(false) }
    var syncWithCloud by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {

        CollapsibleSection(
            title = "Accessibility",
            content = {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {

                    Text(
                        text = "Accessibility Options",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Text Size",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )

                        FormDropdown(
                            label = "",
                            value = textSize,
                            options = listOf("Small", "Medium", "Large"),
                            onValueChange = { textSize = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    FormToggle(
                        title = "High Contrast Mode",
                        checked = highContrastMode,
                        onCheckedChange = { highContrastMode = it }
                    )

                    FormToggle(
                        title = "Screen Reader",
                        checked = screenReader,
                        onCheckedChange = { screenReader = it }
                    )

                    FormToggle(
                        title = "Reduce Motion",
                        checked = reduceMotion,
                        onCheckedChange = { reduceMotion = it }
                    )

                    FormToggle(
                        title = "Color Blind Mode",
                        checked = colorBlindMode,
                        onCheckedChange = { colorBlindMode = it }
                    )
                }
            }
        )

        CollapsibleSection(
            title = "Notifications",
            content = {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {

                    Text(
                        text = "Notification Preferences",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    FormToggle(
                        title = "Appointment Reminders",
                        checked = appointmentReminders,
                        onCheckedChange = { appointmentReminders = it }
                    )

                    FormToggle(
                        title = "Medication Reminders",
                        checked = medicationReminders,
                        onCheckedChange = { medicationReminders = it }
                    )

                    FormToggle(
                        title = "Test Reminders",
                        checked = testReminders,
                        onCheckedChange = { testReminders = it }
                    )

                    FormToggle(
                        title = "App Updates",
                        checked = appUpdates,
                        onCheckedChange = { appUpdates = it }
                    )

                    FormToggle(
                        title = "Email Notifications",
                        checked = emailNotifications,
                        onCheckedChange = { emailNotifications = it }
                    )
                }
            }
        )

        CollapsibleSection(
            title = "Account",
            content = {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {

                    Text(
                        text = "Account Settings",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    FormToggle(
                        title = "Data Sharing",
                        checked = dataSharing,
                        onCheckedChange = { dataSharing = it }
                    )

                    FormToggle(
                        title = "Sync with Cloud",
                        checked = syncWithCloud,
                        onCheckedChange = { syncWithCloud = it }
                    )

                    Button(
                        onClick = {
                            showChangePasswordDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Change Password")
                    }

                    OutlinedButton(
                        onClick = {
                            showDeleteAccountDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Delete Account")
                    }

                    accountMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {

        AlertDialog(
            onDismissRequest = {
                showChangePasswordDialog = false
            },

            title = {
                Text("Change Password")
            },

            text = {
                Column {

                    FormTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = "New Password",
                        isError = false
                    )

                    FormTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        isError = false
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        if (newPassword.length < 6) {

                            accountMessage =
                                "Password must be at least 6 characters."

                        } else if (newPassword != confirmPassword) {

                            accountMessage =
                                "Passwords do not match."

                        } else {

                            authService.changePassword(newPassword) { result ->

                                accountMessage =
                                    when (result) {

                                        is AuthResult.Success ->
                                            "Password changed successfully."

                                        is AuthResult.Error ->
                                            result.message
                                    }

                                newPassword = ""
                                confirmPassword = ""
                                showChangePasswordDialog = false
                            }
                        }
                    }
                ) {
                    Text("Change")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        showChangePasswordDialog = false
                        newPassword = ""
                        confirmPassword = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {

        AlertDialog(
            onDismissRequest = {
                showDeleteAccountDialog = false
            },

            title = {
                Text("Delete Account")
            },

            text = {
                Text(
                    "Are you sure you want to delete your account? This action cannot be undone."
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        authService.deleteAccount { result ->

                            accountMessage =
                                when (result) {

                                    is AuthResult.Success -> {
                                        onAccountDeleted()
                                        "Account deleted successfully."
                                    }

                                    is AuthResult.Error ->
                                        result.message
                                }

                            showDeleteAccountDialog = false
                        }
                    }
                ) {
                    Text("Delete")
                }
            },

            dismissButton = {

                OutlinedButton(
                    onClick = {
                        showDeleteAccountDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}