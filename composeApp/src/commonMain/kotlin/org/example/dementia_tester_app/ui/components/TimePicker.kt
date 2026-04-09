package org.example.dementia_tester_app.ui.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * A reusable time field component that displays a time picker.
 * @param time The selected time
 * @param label The label for the time field
 * @param onTimeChange Callback when the time changes
 * @param isError Whether the field is in the error state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(
    time: String,
    label: String,
    onTimeChange: (String) -> Unit,
    isError: Boolean,
) {
    /**
     * A helper function to format the time in the format 'HH:MMAM/PM'
     * @param state the time selected using the time picker
     * @return a string to represent the time in the format 'HH:MMAM/PM'
     */
    fun formatTime(state: TimePickerState): String {
        // Handle values for the hour
        val displayHour = when (state.hour) {
            0 -> 12
            in 1..11 -> state.hour
            12 -> 12
            else -> state.hour - 12
        }

        val hours = displayHour.toString().padStart(2, '0')
        val minutes = state.minute.toString().padStart(2, '0')
        val amPm = if (state.isAfternoon) "PM" else "AM"

        return "$hours:$minutes$amPm"
    }

    var showTimePicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
                showTimePicker = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = time,
                onValueChange = {  },
                readOnly = true,
                label = { Text(label) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        type = MenuAnchorType.PrimaryNotEditable,
                       // enabled = !isError
                    ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isError) FormColors.errorColor else FormColors.green,
                    unfocusedBorderColor = if (isError) FormColors.errorColor else FormColors.green,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    errorBorderColor = FormColors.errorColor
                ),
                isError = isError,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = "Select Time",
                        tint = FormColors.green
                    )
                }
            )
        }
        // Show time picker dialog when requested
        if (showTimePicker) {
            TimePicker(
                onConfirm = { state ->
                    val formattedTime = formatTime(state)
                    onTimeChange(formattedTime)
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false }
            )
        }
}

/**
 * A reusable component to display a time picker. The user can choose between a dial-style
 * or input-style time picker. Displays dial-style first by default. The time picker is displayed
 * in an alert dialog using the TimePickerDialog function
 *
 * @param onConfirm handles when the desired time is selected
 * @param onDismiss handles when the time picker is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePicker(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
) {

    val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.hour,
        initialMinute = currentTime.minute,
        is24Hour = false
    )

    var showDial by remember { mutableStateOf(true)}

    val toggleIcon = if (showDial) {
        Icons.Filled.Keyboard
    } else {
        Icons.Filled.AccessTime
    }

    TimePickerDialog(
        onDismiss = { onDismiss() },
        onConfirm = { onConfirm(timePickerState) },
        toggle = {
            IconButton(onClick = { showDial = !showDial }) {
                Icon(
                    imageVector = toggleIcon,
                    contentDescription = "Time picker type toggle",
                )
            }
        }
    ) {
        if (showDial) {
            TimePicker(
                state = timePickerState,
            )
        } else {
            TimeInput (
                state = timePickerState,
            )
        }
    }

}

/**
 * The dialog box that displays the time picker.
 * @param onDismiss handles when the dialog is dismissed
 * @param onConfirm handles when the time is chosen
 * @param toggle the toggle icon to switch between a dial or keypad input
 * @param content the time picker composable function
 */
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    toggle: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { content() },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {

                toggle()

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onClick = { onDismiss() }) {
                    Text("Dismiss")
                }

                TextButton(onClick = { onConfirm() }) {
                    Text("OK")
                }
            }
        }
    )
}



