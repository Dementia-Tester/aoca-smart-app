package org.example.dementia_tester_app.data

/**
 * Service for creating and fetching appointments via Firebase.
 * Closes issues #18 (mock data in history) and #24 (navigation bug).
 */
expect class AppointmentService() {
    /**
     * Write a new appointment to Firebase Realtime DB at Appointments/{userId}/{id}.
     */
    fun createAppointment(appointment: Appointment, callback: (DatabaseResult<Unit>) -> Unit)

    /**
     * Read all appointments for the current user from Firebase.
     */
    fun getAppointments(callback: (DatabaseResult<List<Appointment>>) -> Unit)
}
