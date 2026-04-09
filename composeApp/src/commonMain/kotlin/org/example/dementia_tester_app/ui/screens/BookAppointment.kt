package org.example.dementia_tester_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.dementia_tester_app.ui.components.DateField
import org.example.dementia_tester_app.ui.components.SuccessMessage
import org.example.dementia_tester_app.ui.components.ErrorMessage
import org.example.dementia_tester_app.ui.components.FormColors
import org.example.dementia_tester_app.ui.components.FormDropdown
import org.example.dementia_tester_app.ui.components.FormTextField

/**
 * Selectable button for appointment types and time slots
 */
@Composable
fun SelectableButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = FormColors.green,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = if (isSelected) FormColors.green else FormColors.green.copy(alpha = 0.1f)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else FormColors.green,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Data class to represent an appointment
 */
data class AppointmentData(
    val doctor: String,
    val appointmentType: String,
    val date: String,
    val time: String,
    val reason: String
)

/**
 * Validates all appointment fields and returns true if all fields are valid
 */
fun validateAppointmentFields(
    doctor: String,
    appointmentType: String,
    date: String,
    time: String,
    reason: String,
    setDoctorError: (Boolean) -> Unit,
    setAppointmentTypeError: (Boolean) -> Unit,
    setDateError: (Boolean) -> Unit,
    setTimeError: (Boolean) -> Unit,
    setReasonError: (Boolean) -> Unit
): Boolean {
    var isValid = true
    
    if (doctor.isEmpty()) {
        setDoctorError(true)
        isValid = false
    }
    
    if (appointmentType.isEmpty()) {
        setAppointmentTypeError(true)
        isValid = false
    }
    
    if (date.isEmpty()) {
        setDateError(true)
        isValid = false
    }
    
    if (time.isEmpty()) {
        setTimeError(true)
        isValid = false
    }
    
    if (reason.trim().isEmpty()) {
        setReasonError(true)
        isValid = false
    }
    
    return isValid
}

/**
 * Book Appointment screen
 * 
 * @param onCancel Callback to be invoked when the user cancels the appointment booking
 */
@Composable
fun BookAppointment(onCancel: () -> Unit = {}) {
    // Mock data for doctors
    val doctors = listOf(
        "Dr. Sarah Johnson",
        "Dr. Michael Chen",
        "Dr. Emily Rodriguez",
        "Dr. David Kim",
        "Dr. Jessica Patel"
    )
    
    // Get today's date
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val todayFormatted = "${today.dayOfMonth}/${today.monthNumber}/${today.year}"
    
    // State variables for form fields
    var selectedDoctor by remember { mutableStateOf("") }
    var selectedAppointmentType by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(todayFormatted) }
    var selectedTime by remember { mutableStateOf("") }
    var reasonForAppointment by remember { mutableStateOf("") }
    
    // Track if a date has been explicitly selected by the user
    var dateSelected by remember { mutableStateOf(false) }
    
    // Keep track of the minimum allowed date (today)
    val minDate = remember { today }
    
    // Error state
    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Success state
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    
    // Field error states
    var doctorError by remember { mutableStateOf(false) }
    var appointmentTypeError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var timeError by remember { mutableStateOf(false) }
    var reasonError by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        // Doctor Selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Doctor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                FormDropdown(
                    label = "Select a doctor",
                    value = selectedDoctor,
                    options = doctors,
                    onValueChange = { 
                        selectedDoctor = it
                        doctorError = false
                        showErrorMessage = false
                        showSuccessMessage = false
                    },
                    isError = doctorError,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Appointment Type Selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Select Appointment Type",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Show error message if appointment type is not selected
                    if (appointmentTypeError) {
                        Text(
                            text = "Please select an appointment type",
                            color = FormColors.errorColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                
                // First row of appointment type buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SelectableButton(
                        text = "Consultation",
                        isSelected = selectedAppointmentType == "Consultation",
                        onClick = { 
                            selectedAppointmentType = "Consultation"
                            appointmentTypeError = false
                            showErrorMessage = false
                            showSuccessMessage = false
                        },
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    
                    SelectableButton(
                        text = "Carer Support",
                        isSelected = selectedAppointmentType == "Carer Support",
                        onClick = { 
                            selectedAppointmentType = "Carer Support"
                            appointmentTypeError = false
                            showErrorMessage = false
                            showSuccessMessage = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Second row of appointment type buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SelectableButton(
                        text = "Medication",
                        isSelected = selectedAppointmentType == "Medication",
                        onClick = { 
                            selectedAppointmentType = "Medication"
                            appointmentTypeError = false
                            showErrorMessage = false
                            showSuccessMessage = false
                        },
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    
                    SelectableButton(
                        text = "Assessment",
                        isSelected = selectedAppointmentType == "Assessment",
                        onClick = { 
                            selectedAppointmentType = "Assessment"
                            appointmentTypeError = false
                            showErrorMessage = false
                            showSuccessMessage = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Third row of appointment type buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SelectableButton(
                        text = "Therapy",
                        isSelected = selectedAppointmentType == "Therapy",
                        onClick = { 
                            selectedAppointmentType = "Therapy"
                            appointmentTypeError = false
                            showErrorMessage = false
                            showSuccessMessage = false
                        },
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    
                    SelectableButton(
                        text = "Telehealth",
                        isSelected = selectedAppointmentType == "Telehealth",
                        onClick = { 
                            selectedAppointmentType = "Telehealth"
                            appointmentTypeError = false
                            showErrorMessage = false
                            showSuccessMessage = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Date and Time Selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Date",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Date picker
                DateField(
                    date = selectedDate,
                    onDateChange = { newDateStr -> 
                        selectedDate = newDateStr
                        dateError = false
                        showErrorMessage = false
                        showSuccessMessage = false
                        // Mark that a date has been selected
                        dateSelected = true
                    },
                    label = "Appointment Date",
                    isError = dateError,
                    isEditable = true,
                    allowDatesAfterToday = true, // Only allow dates after or equal to today
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
                
                // Only show time selection if a date has been selected
                if (dateSelected) {
                    // Time selection
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Available Slots",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Show error message if time is not selected
                        if (timeError) {
                            Text(
                                text = "Please select an appointment time",
                                color = FormColors.errorColor,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                    
                    // First row of time buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SelectableButton(
                            text = "9:00 AM",
                            isSelected = selectedTime == "9:00 AM",
                            onClick = { 
                                selectedTime = "9:00 AM"
                                timeError = false
                                showErrorMessage = false
                                showSuccessMessage = false
                            },
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        
                        SelectableButton(
                            text = "10:30 AM",
                            isSelected = selectedTime == "10:30 AM",
                            onClick = { 
                                selectedTime = "10:30 AM"
                                timeError = false
                                showErrorMessage = false
                                showSuccessMessage = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Second row of time buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SelectableButton(
                            text = "1:00 PM",
                            isSelected = selectedTime == "1:00 PM",
                            onClick = { 
                                selectedTime = "1:00 PM"
                                timeError = false
                                showErrorMessage = false
                                showSuccessMessage = false
                            },
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        
                        SelectableButton(
                            text = "3:30 PM",
                            isSelected = selectedTime == "3:30 PM",
                            onClick = { 
                                selectedTime = "3:30 PM"
                                timeError = false
                                showErrorMessage = false
                                showSuccessMessage = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        // Reason for Appointment
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Reason for Appointment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                FormTextField(
                    value = reasonForAppointment,
                    onValueChange = { 
                        reasonForAppointment = it
                        reasonError = false
                        showErrorMessage = false
                        showSuccessMessage = false
                    },
                    label = "Enter reason or comments",
                    isError = reasonError,
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error message
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            ErrorMessage(
                show = showErrorMessage,
                message = errorMessage
            )
        }
        
        // Success message
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            SuccessMessage(
                message = successMessage,
                isVisible = showSuccessMessage,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Submit and Cancel buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cancel button
            OutlinedButton(
                onClick = { onCancel() },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = FormColors.green
                )
            ) {
                Text("Cancel")
            }
            
            // Submit button
            Button(
                onClick = { 
                    // Validate all fields
                    val isValid = validateAppointmentFields(
                        doctor = selectedDoctor,
                        appointmentType = selectedAppointmentType,
                        date = selectedDate,
                        time = selectedTime,
                        reason = reasonForAppointment,
                        setDoctorError = { doctorError = it },
                        setAppointmentTypeError = { appointmentTypeError = it },
                        setDateError = { dateError = it },
                        setTimeError = { timeError = it },
                        setReasonError = { reasonError = it }
                    )
                    
                    if (isValid) {
                        // Create appointment data object
                        val appointmentData = AppointmentData(
                            doctor = selectedDoctor,
                            appointmentType = selectedAppointmentType,
                            date = selectedDate,
                            time = selectedTime,
                            reason = reasonForAppointment
                        )
                        
                        // Log the appointment data
                        println("Appointment booked: $appointmentData")
                        
                        // Show success message
                        successMessage = "Appointment booked successfully!"
                        showSuccessMessage = true
                        showErrorMessage = false
                    } else {
                        // Show error message
                        errorMessage = "Please fill in all required fields"
                        showErrorMessage = true
                        showSuccessMessage = false
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FormColors.green
                )
            ) {
                Text("Submit")
            }
        }
    }
}