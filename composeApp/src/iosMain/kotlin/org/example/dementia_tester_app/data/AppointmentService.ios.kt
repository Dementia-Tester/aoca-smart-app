package org.example.dementia_tester_app.data

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import platform.Foundation.NSDictionary
import platform.Foundation.NSError
import platform.Foundation.NSNull

/**
 * iOS actual — writes/reads Appointments/{userId}/{id} in Firebase Realtime DB.
 */
actual class AppointmentService {
    private val dbPath = "Appointments"

    actual fun createAppointment(appointment: Appointment, callback: (DatabaseResult<Unit>) -> Unit) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) { callback(DatabaseResult.Error("No user is signed in")); return }
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }

        val userRef = ref.child(dbPath).child(userId)
        val id = userRef.childByAutoId().key
        if (id == null) { callback(DatabaseResult.Error("Failed to generate appointment ID")); return }

        val appt = appointment.copy(id = id, userId = userId)
        val objcMap: Map<Any?, Any?> = appt.toMap().entries.associate { (k, v) ->
            (k as Any?) to (v ?: NSNull())
        }
        userRef.child(id).updateChildValues(objcMap) { error: NSError?, _ ->
            if (error == null) callback(DatabaseResult.Success(Unit))
            else callback(DatabaseResult.Error("Failed to book appointment: ${error.localizedDescription}"))
        }
    }

    actual fun getAppointments(callback: (DatabaseResult<List<Appointment>>) -> Unit) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) { callback(DatabaseResult.Error("No user is signed in")); return }
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }

        ref.child(dbPath).child(userId).getDataWithCompletionBlock { error: NSError?, snapshot: FIRDataSnapshot? ->
            if (error != null) {
                callback(DatabaseResult.Error("Failed to load appointments: ${error.localizedDescription}"))
                return@getDataWithCompletionBlock
            }
            if (snapshot == null || !snapshot.exists()) {
                callback(DatabaseResult.Success(emptyList()))
                return@getDataWithCompletionBlock
            }
            try {
                val list = mutableListOf<Appointment>()
                val value = snapshot.value
                when (value) {
                    is Map<*, *>  -> value.forEach  { (k, v) -> parseEntry(k, v)?.let { list.add(it) } }
                    is NSDictionary -> {
                        val keys = value.allKeys as List<*>
                        keys.forEach { k -> parseEntry(k, value.objectForKey(k))?.let { list.add(it) } }
                    }
                    else -> {}
                }
                callback(DatabaseResult.Success(list))
            } catch (t: Throwable) {
                callback(DatabaseResult.Error("Failed to parse appointments: ${t.message}"))
            }
        }
    }

    private fun parseEntry(k: Any?, v: Any?): Appointment? {
        val id   = k?.toString() ?: return null
        val data = when (v) {
            is Map<*, *>    -> v
            is NSDictionary -> nsDictionaryToMap(v)
            else            -> return null
        }
        return Appointment.fromMap(data, id)
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
