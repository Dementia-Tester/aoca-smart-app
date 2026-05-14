package org.example.dementia_tester_app.data

enum class AppointmentStatus {
    Upcoming,
    Completed,
    Cancelled;

    companion object {
        fun fromString(value: String): AppointmentStatus =
            entries.find { it.name.lowercase() == value.lowercase() } ?: Upcoming
    }
}

data class Appointment(
    val id: String = "",
    val userId: String = "",
    val doctor: String = "",
    val type: String = "",
    val date: String = "",
    val time: String = "",
    val status: AppointmentStatus = AppointmentStatus.Upcoming,
    val reason: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id"     to id,
        "userId" to userId,
        "doctor" to doctor,
        "type"   to type,
        "date"   to date,
        "time"   to time,
        "status" to status.name,
        "reason" to reason
    )

    companion object {
        fun fromMap(map: Map<*, *>, id: String): Appointment {
            fun str(key: String) = (map[key] as? String) ?: ""
            return Appointment(
                id     = id,
                userId = str("userId"),
                doctor = str("doctor"),
                type   = str("type"),
                date   = str("date"),
                time   = str("time"),
                status = AppointmentStatus.fromString(str("status")),
                reason = str("reason")
            )
        }
    }
}
