package org.example.dementia_tester_app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Android actual — writes/reads appointments in Firebase Firestore.
 */
actual class AppointmentService {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore
    private val collectionPath = "appointments"

    actual fun createAppointment(appointment: Appointment, callback: (DatabaseResult<Unit>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) { callback(DatabaseResult.Error("No user is signed in")); return }

        // We'll let Firestore generate the ID, or we can use the one from the collection ref
        val docRef = firestore.collection(collectionPath).document()
        val id = docRef.id

        val appt = appointment.copy(id = id, userId = userId)
        docRef.set(appt.toMap())
            .addOnSuccessListener { callback(DatabaseResult.Success(Unit)) }
            .addOnFailureListener { e ->
                callback(DatabaseResult.Error("Failed to book appointment: ${e.message}"))
            }
    }

    actual fun getAppointments(callback: (DatabaseResult<List<Appointment>>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) { callback(DatabaseResult.Error("No user is signed in")); return }

        firestore.collection(collectionPath)
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING) // Optional: sort by date
            .get()
            .addOnSuccessListener { snapshot ->
                try {
                    val list = mutableListOf<Appointment>()
                    for (doc in snapshot.documents) {
                        val data = doc.data ?: continue
                        list.add(Appointment.fromMap(data, doc.id))
                    }
                    callback(DatabaseResult.Success(list))
                } catch (e: Exception) {
                    callback(DatabaseResult.Error("Failed to parse appointments: ${e.message}"))
                }
            }
            .addOnFailureListener { e ->
                callback(DatabaseResult.Error("Failed to load appointments: ${e.message}"))
            }
    }
}
