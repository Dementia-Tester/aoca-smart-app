package org.example.dementia_tester_app.data

expect class AppointmentService() {
    fun createAppointment(
        appointment: Appointment,
        callback: (DatabaseResult<Unit>) -> Unit
    )

    fun getAppointments(
        callback: (DatabaseResult<List<Appointment>>) -> Unit
    )

    fun updateAppointmentStatus(
        appointmentId: String,
        newStatus: AppointmentStatus,
        callback: (DatabaseResult<Unit>) -> Unit
    )
}