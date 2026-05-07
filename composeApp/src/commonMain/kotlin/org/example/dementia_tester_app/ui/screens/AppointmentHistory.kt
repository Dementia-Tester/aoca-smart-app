package org.example.dementia_tester_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.dementia_tester_app.data.Appointment
import org.example.dementia_tester_app.data.AppointmentStatus
import org.example.dementia_tester_app.ui.components.FormColors

@Composable
fun AppointmentHistory() {
    // Mock data for appointments
    val appointments = remember {
        listOf(
            Appointment(
                id = "1",
                doctor = "Dr. Sarah Johnson",
                type = "Consultation",
                date = "25/05/2024",
                time = "10:30 AM",
                status = AppointmentStatus.Upcoming,
                reason = "Regular check-up and cognitive assessment.",
            ),
            Appointment(
                id = "2",
                doctor = "Dr. Michael Chen",
                type = "Medication",
                date = "15/05/2024",
                time = "02:00 PM",
                status = AppointmentStatus.Completed,
                reason = "Reviewing current medication side effects.",
            ),
            Appointment(
                id = "3",
                doctor = "Dr. Emily Rodriguez",
                type = "Therapy",
                date = "10/05/2024",
                time = "09:00 AM",
                status = AppointmentStatus.Cancelled,
                reason = "Rescheduled due to personal reasons.",
            )
        )
    }

    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (selectedAppointment == null) {
            Text(
                text = "Appointment History",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(appointments) { appointment ->
                    AppointmentItem(
                        appointment = appointment,
                        onClick = { selectedAppointment = appointment }
                    )
                }
            }
        } else {
            AppointmentDetailView(
                appointment = selectedAppointment!!,
                onBack = { selectedAppointment = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentItem(
    appointment: Appointment,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.doctor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = appointment.type,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = "${appointment.date} at ${appointment.time}",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            StatusBadge(status = appointment.status)
        }
    }
}

@Composable
fun StatusBadge(status: AppointmentStatus) {
    val backgroundColor = when (status) {
        AppointmentStatus.Upcoming -> Color(0xFFE3F2FD)
        AppointmentStatus.Completed -> Color(0xFFE8F5E9)
        AppointmentStatus.Cancelled -> Color(0xFFFFEBEE)
    }
    
    val textColor = when (status) {
        AppointmentStatus.Upcoming -> Color(0xFF1976D2)
        AppointmentStatus.Completed -> Color(0xFF388E3C)
        AppointmentStatus.Cancelled -> Color(0xFFD32F2F)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = status.name,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AppointmentDetailView(
    appointment: Appointment,
    onBack: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            TextButton(onClick = onBack) {
                Text("< Back", color = FormColors.green, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Appointment Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow(label = "Doctor", value = appointment.doctor)
                DetailRow(label = "Type", value = appointment.type)
                DetailRow(label = "Date", value = appointment.date)
                DetailRow(label = "Time", value = appointment.time)
                
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status: ",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(100.dp)
                    )
                    StatusBadge(status = appointment.status)
                }

                if (appointment.reason.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Reason:", fontWeight = FontWeight.Bold)
                    Text(
                        text = appointment.reason,
                        modifier = Modifier.padding(top = 4.dp),
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(100.dp)
        )
        Text(text = value)
    }
}
