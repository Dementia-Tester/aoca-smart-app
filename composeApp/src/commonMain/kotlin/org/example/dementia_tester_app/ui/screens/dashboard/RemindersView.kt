package org.example.dementia_tester_app.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.dementia_tester_app.auth.AuthService
import org.example.dementia_tester_app.data.Reminder
import org.example.dementia_tester_app.data.ReminderResult
import org.example.dementia_tester_app.data.ReminderService
import org.example.dementia_tester_app.ui.components.FormColors
import org.example.dementia_tester_app.ui.components.LoadingSpinner
import org.example.dementia_tester_app.ui.components.ReminderCard
import org.example.dementia_tester_app.ui.components.ReminderFormDialog


/**
 * View screen for the reminders. Allows users to view, create, delete
 * and activate/deactivate their reminders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersView() {

    var showDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val reminders = remember { mutableStateListOf<Reminder>()}

    val reminderService = ReminderService()

    val authService = AuthService()
    val userId = authService.getCurrentUserId()

    val scrollState = rememberScrollState()


    LaunchedEffect(userId){
        userId?.let {
            loadReminders(reminderService, it, reminders) { isLoading = it }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .verticalScroll(scrollState),
            contentAlignment = Alignment.Center
        ) {
            LoadingSpinner()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Text(
            text = "Your Reminders",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,

        )

        Text(
            text = "Create and view your reminders",
            fontSize = 16.sp,

        )



        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showDialog = true },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FormColors.green,
                contentColor = Color.White
            ),
            enabled = true
        ) {
            Text(text = "CREATE REMINDER")
        }

        if (reminders.isEmpty()) {
            Text("You don't have any reminders yet.")
        } else {
            LazyColumn {
                items(reminders) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onToggle = { id, newValue ->
                            reminderService.updateReminder(id, mapOf("taskActive" to newValue)){
                                userId?.let {
                                    loadReminders(reminderService, it, reminders) { isLoading = it }
                                }
                            }
                        },
                        onDelete = { id ->
                            reminderService.deleteReminder(id) {
                                userId?.let{
                                    loadReminders(reminderService, it, reminders) { isLoading = it }
                                }
                            }
                        }
                    )
                }
            }
        }


    }




    if (showDialog) {
        ReminderFormDialog(
            onDismiss = { showDialog = false },
            onCreate = { newReminder ->
                reminderService.createReminder(newReminder) {
                    userId?.let {
                        loadReminders(reminderService, it, reminders) { isLoading = it }
                    }
                }
                showDialog = false
            }
        )
    }
}

private fun loadReminders(
    reminderService: ReminderService,
    userId: String,
    reminders: SnapshotStateList<Reminder>,
    setLoading: (Boolean) -> Unit
) {
    setLoading(true)
    reminderService.getReminders(userId) { result ->
        setLoading(false)
        when (result) {
            is ReminderResult.Success -> {
                reminders.clear()
                reminders.addAll(result.data)
            }
            is ReminderResult.Error -> {
                reminders.clear()
            }
        }
    }
}



