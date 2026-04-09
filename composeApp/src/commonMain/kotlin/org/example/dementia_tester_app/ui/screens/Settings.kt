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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.dementia_tester_app.ui.components.CollapsibleSection
import org.example.dementia_tester_app.ui.components.FormDropdown
import org.example.dementia_tester_app.ui.components.TextLink
import org.example.dementia_tester_app.ui.components.FormToggle

/**
 * Settings screen with collapsible sections for different settings categories
 */
@Composable
fun Settings() {
    val scrollState = rememberScrollState()
    
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
                    TextLink(
                        title = "Change Password",
                        onClick = { /* Handle click */ }
                    )
                    TextLink(
                        title = "Delete Account",
                        onClick = { /* Handle click */ }
                    )
                    

                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}


