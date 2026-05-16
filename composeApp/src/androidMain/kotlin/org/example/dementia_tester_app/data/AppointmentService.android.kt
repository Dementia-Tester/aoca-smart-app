package org.example.dementia_tester_app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * Android actual — writes/reads Appointments/{userId}/{id} in Firebase Realtime DB.
 */
actual class AppointmentService {
    private val auth     = FirebaseAuth.getInstance()
    private val database = Firebase.database.reference
    private val dbPath   = "Appointments"

    actual fun createAppointment(appointment: Appointment, callback: (DatabaseResult<Unit>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) { callback(DatabaseResult.Error("No user is signed in")); return }

        val id = database.child(dbPath).child(userId).push().key
        if (id == null) { callback(DatabaseResult.Error("Failed to generate appointment ID")); return }

        val appt = appointment.copy(id = id, userId = userId)
        database.child(dbPath).child(userId).child(id)
            .setValue(appt.toMap())
            .addOnSuccessListener { callback(DatabaseResult.Success(Unit)) }
            .addOnFailureListener { e ->
                callback(DatabaseResult.Error("Failed to book appointment: ${e.message}"))
            }
    }

    actual fun getAppointments(callback: (DatabaseResult<List<Appointment>>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) { callback(DatabaseResult.Error("No user is signed in")); return }

        database.child(dbPath).child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val list = mutableListOf<Appointment>()
                        for (child in snapshot.children) {
                            val id   = child.key ?: continue
                            val data = child.value as? Map<*, *> ?: continue
                            list.add(Appointment.fromMap(data, id))
                        }
                        callback(DatabaseResult.Success(list))
                    } catch (e: Exception) {
                        callback(DatabaseResult.Error("Failed to parse appointments: ${e.message}"))
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(DatabaseResult.Error("Failed to load appointments: ${error.message}"))
                }
            })
    }
}
