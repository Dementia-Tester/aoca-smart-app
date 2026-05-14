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
import org.example.dementia_tester_app.data.AppointmentService
import org.example.dementia_tester_app.data.AppointmentStatus
import org.example.dementia_tester_app.data.DatabaseResult
import org.example.dementia_tester_app.ui.components.FormColors

/**
 * Appointment History screen.
 * Loads real data from Firebase (fixes issue #18 — was mock data).
 * Back navigation fixed so system back stays within the list/detail split (issue #24).
 */
@Composable
fun AppointmentHistory() {
    var appointments      by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var isLoading         by remember { mutableStateOf(true) }
    var loadError         by remember { mutableStateOf<String?>(null) }
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }

    // Load from Firebase on first composition
    LaunchedEffect(Unit) {
        AppointmentService().getAppointments { result ->
            isLoading = false
            when (result) {
                is DatabaseResult.Success -> appointments = result.data
                is DatabaseResult.Error   -> loadError  = result.message
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (selectedAppointment == null) {
            // ── List / loading / empty / error ───────────────────────
            Text("Appointment History", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp))

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FormColors.green)
                    }
                }
                loadError != null -> {
                    Text("Failed to load appointments: $loadError",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp))
                }
                appointments.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No appointments found.", color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(appointments) { appt ->
                            AppointmentItem(appointment = appt,
                                onClick = { selectedAppointment = appt })
                        }
                    }
                }
            }
        } else {
            // ── Detail view ──────────────────────────────────────────
            // Issue #24 fix: onBack sets selectedAppointment = null, keeping
            // the user inside AppointmentHistory rather than popping the nav stack.
            AppointmentDetailView(
                appointment = selectedAppointment!!,
                onBack      = { selectedAppointment = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentItem(appointment: Appointment, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(appointment.doctor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(appointment.type, color = Color.Gray, fontSize = 14.sp)
                Text("${appointment.date} at ${appointment.time}", fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp))
            }
            StatusBadge(status = appointment.status)
        }
    }
}

@Composable
fun StatusBadge(status: AppointmentStatus) {
    val bg   = when (status) {
        AppointmentStatus.Upcoming   -> Color(0xFFE3F2FD)
        AppointmentStatus.Completed  -> Color(0xFFE8F5E9)
        AppointmentStatus.Cancelled  -> Color(0xFFFFEBEE)
    }
    val text = when (status) {
        AppointmentStatus.Upcoming   -> Color(0xFF1976D2)
        AppointmentStatus.Completed  -> Color(0xFF388E3C)
        AppointmentStatus.Cancelled  -> Color(0xFFD32F2F)
    }
    Surface(color = bg, shape = RoundedCornerShape(16.dp)) {
        Text(status.name, color = text, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
    }
}

@Composable
fun AppointmentDetailView(appointment: Appointment, onBack: () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)) {
            TextButton(onClick = onBack) {
                Text("< Back", color = FormColors.green, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Text("Appointment Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Card(modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(Modifier.padding(16.dp)) {
                DetailRow("Doctor", appointment.doctor)
                DetailRow("Type",   appointment.type)
                DetailRow("Date",   appointment.date)
                DetailRow("Time",   appointment.time)
                Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Status: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                    StatusBadge(status = appointment.status)
                }
                if (appointment.reason.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Reason:", fontWeight = FontWeight.Bold)
                    Text(appointment.reason, modifier = Modifier.padding(top = 4.dp), color = Color.DarkGray)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 8.dp)) {
        Text("$label:", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
        Text(value)
    }
}
