package org.example.dementia_tester_app.data

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import platform.Foundation.NSDictionary
import platform.Foundation.NSError
import platform.Foundation.NSNull

/**
 * iOS actual — reads/writes UserSettings/{userId} in Firebase Realtime DB.
 * NSNull-safe: booleans stored as Firebase booleans, read back safely.
 */
actual class UserSettingsService {
    private val dbPath = "UserSettings"

    actual fun loadSettings(callback: (DatabaseResult<UserSettings>) -> Unit) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) { callback(DatabaseResult.Error("No user is signed in")); return }
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }

        ref.child(dbPath).child(userId).getDataWithCompletionBlock { error: NSError?, snapshot: FIRDataSnapshot? ->
            if (error != null) {
                callback(DatabaseResult.Error("Failed to load settings: ${error.localizedDescription}"))
                return@getDataWithCompletionBlock
            }
            if (snapshot == null || !snapshot.exists()) {
                callback(DatabaseResult.Success(UserSettings()))
                return@getDataWithCompletionBlock
            }
            try {
                val data = snapshotToMap(snapshot)
                callback(
                    if (data != null) DatabaseResult.Success(UserSettings.fromMap(data))
                    else              DatabaseResult.Success(UserSettings())
                )
            } catch (t: Throwable) {
                callback(DatabaseResult.Error("Failed to parse settings: ${t.message}"))
            }
        }
    }

    actual fun saveSettings(settings: UserSettings, callback: (DatabaseResult<Unit>) -> Unit) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) { callback(DatabaseResult.Error("No user is signed in")); return }
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) { callback(DatabaseResult.Error("Firebase not initialized")); return }

        val objcMap: Map<Any?, Any?> = settings.toMap().entries.associate { (k, v) ->
            (k as Any?) to (v ?: NSNull())
        }
        ref.child(dbPath).child(userId).updateChildValues(objcMap) { error: NSError?, _ ->
            if (error == null) callback(DatabaseResult.Success(Unit))
            else callback(DatabaseResult.Error("Failed to save settings: ${error.localizedDescription}"))
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
