package org.example.dementia_tester_app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import org.example.dementia_tester_app.data.Reminder

/**
 * A form to create a reminder.
 *
 *@param onDismiss handles when the alert dialog containing the form is dismissed
 * @param onCreate handles creating a reminder
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderFormDialog(
    onDismiss: () -> Unit,
    onCreate: (Reminder) -> Unit
) {
    var selectedTaskType by remember { mutableStateOf("") }
    var tasksExpanded by remember { mutableStateOf(false) }
    var taskName by remember { mutableStateOf("") }
    var taskTime by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }

    val taskTypes = listOf(
        "Food", "Walk/Exercise", "Medication",
        "Personal Care", "Socialise", "Sleep/Rest", "Doctor Appointment"
    )

    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var fieldErrors by remember { mutableStateOf(mapOf<String, String>()) }

    /**
     * Helper function to reset the form fields and error flags
     */
    fun resetForm() {
        selectedTaskType = ""
        taskName = ""
        taskTime = ""
        tasksExpanded = false
        showTimePicker = false
    }
    /**
     * Helper function to ensure time is input in the format 'HH:MMAM/PM'
     * @param time the time to be checked
     * @return true if the format is valid, false otherwise
     */
    fun isValidTimeFormat(time: String): Boolean {
        val regex = Regex("^(0[1-9]|1[0-2]):[0-5][0-9](AM|PM)$")
        return regex.matches(time)
    }

    /**
     * Helper function to ensure the given string contains only safe characters.
     * Allows only alphanumeric characters and these symbols: .,!?'()-
     * @param input the string to check
     * @return true if the string contains only safe characters, false otherwise
     */
    fun isSafeText(input: String): Boolean {
        val regex = Regex("^[a-zA-Z0-9 .,!?'()-]*\$")
        return regex.matches(input)
    }

    // Helper function to get error state for a field
    fun isFieldError(field: String): Boolean = fieldErrors.containsKey(field)

    // Helper function to clear error for a field
    fun clearFieldError(field: String) {
        fieldErrors = fieldErrors - field
        showErrorMessage = fieldErrors.isNotEmpty()
        errorMessage = fieldErrors.values.joinToString("\n")
    }

    /**
     * Function to validate the form fields.
     * @return true if validation passes, false otherwise
     */
    fun validate(): Boolean {
        val errorsMap = mutableMapOf<String, String>()
        // Validate Task type
        when {
            selectedTaskType.isBlank() || selectedTaskType !in taskTypes ->
                errorsMap["taskType"] = "Please select a task type"
        }
        // Validate Task time
        when {
            taskTime.isBlank() ->
                errorsMap["taskTime"] = "Please select a task time"
            !isValidTimeFormat(taskTime) ->
                errorsMap["taskTime"] = "Time must be in the format HH:MMAM/PM (e.g. 08:30PM)"
        }
        // Validate Task name
        when {
            taskName.isBlank() ->
                errorsMap["taskName"] = "Please enter a task name"
            !isSafeText(taskName) ->
                errorsMap["taskName"] = "Task name contains invalid characters. \nOnly letters, numbers or the following symbols are allowed: .,!?'()-"
        }
        fieldErrors = errorsMap.toMap()
        errorMessage = errorsMap.values.joinToString("\n")
        showErrorMessage = errorsMap.isNotEmpty()

        return errorsMap.isEmpty()
    }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
            resetForm()
        },
        title = { Text("Create Reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {


                FormDropdown(
                    label = "Task Type",
                    value = selectedTaskType,
                    options = taskTypes,
                    onValueChange = {
                        selectedTaskType = it
                        clearFieldError("taskType")
                                    },
                    isError = isFieldError("taskType")
                )

                TimeField(
                    time = taskTime,
                    onTimeChange = {
                        taskTime = it
                        clearFieldError("taskTime")
                                   },
                    label = "Task Time",
                    isError = isFieldError("taskTime"),
                )

                FormTextField(
                    value = taskName,
                    onValueChange = {
                        taskName = it
                        clearFieldError("taskName")
                                    },
                    label = "Task Name",
                    isError = isFieldError("taskName")
                )

                ErrorMessage(
                    show = showErrorMessage,
                    message = errorMessage
                )
            }

        },
        confirmButton = {
            Button(
                onClick = {
                    if (validate()) {
                        onCreate(
                            Reminder(
                                id = null,
                                userId = null,
                                taskType = selectedTaskType,
                                taskTime = taskTime,
                                taskName = taskName
                            )
                        )
                        resetForm()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FormColors.green)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                onDismiss()
                resetForm()
            }) {
                Text("Cancel")
            }
        }
    )
}





