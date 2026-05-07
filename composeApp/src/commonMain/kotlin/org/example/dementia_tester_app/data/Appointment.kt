package org.example.dementia_tester_app.data

enum class AppointmentStatus {
    Upcoming,
    Completed,
    Cancelled
}

data class Appointment(
    val id: String,
    val doctor: String,
    val type: String,
    val date: String,
    val time: String,
    val status: AppointmentStatus,
    val reason: String = ""
)
