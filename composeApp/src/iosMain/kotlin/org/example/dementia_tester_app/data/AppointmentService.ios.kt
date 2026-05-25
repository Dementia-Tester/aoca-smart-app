package org.example.dementia_tester_app.data

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseFirestore.FIRFirestore
import cocoapods.FirebaseFirestore.FIRQuery
import platform.Foundation.NSDictionary
import platform.Foundation.NSError
import platform.Foundation.NSNull

/**
 * iOS actual — writes/reads appointments in Firebase Firestore.
 */
actual class AppointmentService {
    private val collectionPath = "appointments"

    actual fun createAppointment(appointment: Appointment, callback: (DatabaseResult<Unit>) -> Unit) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) { callback(DatabaseResult.Error("No user is signed in")); return }
        val firestore = FIRFirestore.firestore()

        // Let Firestore generate the unique ID
        val docRef = firestore.collectionWithPath(collectionPath).documentWithAutoID()
        val id = docRef.documentID()

        val appt = appointment.copy(id = id, userId = userId)
        
        // Convert Kotlin map to Objective-C friendly map (handling nulls)
        val objcMap: Map<Any?, Any?> = appt.toMap().entries.associate { (k, v) ->
            (k as Any?) to (v ?: NSNull())
        }
        
        docRef.setData(objcMap as Map<Any?, *>) { error ->
            if (error == null) callback(DatabaseResult.Success(Unit))
            else callback(DatabaseResult.Error("Failed to book appointment: ${error.localizedDescription}"))
        }
    }

    actual fun getAppointments(callback: (DatabaseResult<List<Appointment>>) -> Unit) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) { callback(DatabaseResult.Error("No user is signed in")); return }
        val firestore = FIRFirestore.firestore()

        firestore.collectionWithPath(collectionPath)
            .queryWhereField("userId", isEqualTo = userId)
            .getDocumentsWithCompletion { snapshot, error ->
                if (error != null) {
                    callback(DatabaseResult.Error("Failed to load appointments: ${error.localizedDescription}"))
                    return@getDocumentsWithCompletion
                }
                if (snapshot == null || snapshot.isEmpty()) {
                    callback(DatabaseResult.Success(emptyList()))
                    return@getDocumentsWithCompletion
                }
                try {
                    val list = mutableListOf<Appointment>()
                    snapshot.documents.forEach { doc ->
                        val data = (doc as? cocoapods.FirebaseFirestore.FIRDocumentSnapshot)?.data() ?: return@forEach
                        val id = (doc as? cocoapods.FirebaseFirestore.FIRDocumentSnapshot)?.documentID() ?: ""
                        list.add(Appointment.fromMap(data as Map<*, *>, id))
                    }
                    callback(DatabaseResult.Success(list))
                } catch (t: Throwable) {
                    callback(DatabaseResult.Error("Failed to parse appointments: ${t.message}"))
                }
            }
    }
}
