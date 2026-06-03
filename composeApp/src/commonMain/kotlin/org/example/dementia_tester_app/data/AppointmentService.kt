package org.example.dementia_tester_app.data

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import platform.Foundation.NSDictionary
import platform.Foundation.NSError
import platform.Foundation.NSNull

/**
e * iOS actual — writes/reads appointments in Firebase Realtime DB.
 * Nested under userId to match security rules and ensure consistency.
 */
actual class AppointmentService {
    private val collectionPath = "Appointments"

    actual fun createAppointment(appointment: Appointment, callback: (DatabaseResult<Unit>) -> Unit) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) { callback(DatabaseResult.Error("No user is signed in")); return }
        
        val ref = FIRDatabase.database()?.reference()?.child(collectionPath)?.child(userId)
        if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }

        // Generate a unique ID
        val newApptRef = ref.childByAutoID()
        val id = newApptRef.key() ?: ""

        val appt = appointment.copy(id = id, userId = userId)
        
        // Convert Kotlin map to Objective-C friendly map (handling nulls)
        val objcMap: Map<Any?, Any?> = appt.toMap().entries.associate { (k, v) ->
            (k as Any?) to (v ?: NSNull())
        }
        
        newApptRef.setValue(objcMap) { error, _ ->
            if (error == null) callback(DatabaseResult.Success(Unit))
            else callback(DatabaseResult.Error("Failed to book appointment: ${error.localizedDescription}"))
        }
    }

    actual fun getAppointments(callback: (DatabaseResult<List<Appointment>>) -> Unit) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) { callback(DatabaseResult.Error("No user is signed in")); return }
        
        val ref = FIRDatabase.database()?.reference()?.child(collectionPath)?.child(userId)
        if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }

        ref.observeSingleEventOfType(cocoapods.FirebaseDatabase.FIRDataEventTypeValue) { snapshot ->
            if (snapshot == null || !snapshot.exists()) {
                callback(DatabaseResult.Success(emptyList()))
                return@observeSingleEventOfType
            }
            try {
                val list = mutableListOf<Appointment>()
                snapshot.children.allObjects.forEach { child ->
                    val childSnapshot = child as? FIRDataSnapshot ?: return@forEach
                    val data = snapshotToMap(childSnapshot) ?: return@forEach
                    list.add(Appointment.fromMap(data, childSnapshot.key() ?: ""))
                }
                callback(DatabaseResult.Success(list))
            } catch (t: Throwable) {
                callback(DatabaseResult.Error("Failed to parse appointments: ${t.message}"))
            }
        }
    }

    private fun snapshotToMap(snapshot: FIRDataSnapshot): Map<*, *>? {
        val value = snapshot.value
        return when (value) {
            is Map<*, *>    -> value
            is NSDictionary -> nsDictionaryToMap(value)
            else            -> null
        }
    }

    private fun nsDictionaryToMap(dict: NSDictionary): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        val keys = dict.allKeys as List<*>
        for (k in keys) {
            val key = k?.toString() ?: continue
            val v   = dict.objectForKey(k)
            result[key] = when (v) {
                is NSDictionary -> nsDictionaryToMap(v)
                is NSNull       -> null
                else            -> v
            }
        }
        return result
    }
}
