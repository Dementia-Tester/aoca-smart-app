package org.example.dementia_tester_app.data

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import platform.Foundation.NSDictionary
import platform.Foundation.NSError
import platform.Foundation.NSNull
import platform.Foundation.allKeys

/**
 * iOS implementation for reminder service backed by Firebase Realtime Database.
 */
actual class ReminderService actual constructor() {

    private val remindersPath = "Reminders"

    /**
     * Create a reminder
     * @param reminder the reminder to be created
     * @param callback callback to be invoked with the result of the operation
     */
    actual fun createReminder(
        reminder: Reminder,
        callback: (ReminderResult<Unit>) -> Unit
    ) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) {
            callback(ReminderResult.Error("User not authenticated."))
            return
        }
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) {
            callback(ReminderResult.Error("Firebase not initialized"))
            return
        }

        val userRef = ref.child(remindersPath).child(userId)

        // Build data to store (do not rely on generated key field; we'll derive key on read)
        val data: Map<String, Any?> = mapOf(
            "taskType" to (reminder.taskType ?: ""),
            "taskTime" to (reminder.taskTime ?: ""),
            "taskName" to (reminder.taskName ?: ""),
            "taskActive" to if (reminder.taskActive) 1 else 0
        )

        if (!reminder.id.isNullOrBlank()) {
            val reminderRef = userRef.child(reminder.id!!)
            reminderRef.setValue(data) { error: NSError?, _ ->
                if (error == null) {
                    callback(ReminderResult.Success(Unit))
                } else {
                    callback(ReminderResult.Error("Failed to create reminder: ${'$'}{error.localizedDescription}"))
                }
            }
        } else {
            val autoRef = userRef.childByAutoId()
            if (autoRef == null) {
                callback(ReminderResult.Error("Failed to generate key for reminder"))
                return
            }
            autoRef.setValue(data) { error: NSError?, _ ->
                if (error == null) {
                    callback(ReminderResult.Success(Unit))
                } else {
                    callback(ReminderResult.Error("Failed to create reminder: ${'$'}{error.localizedDescription}"))
                }
            }
        }
    }

    /**
 * Get the user's reminders
 * @param callback callback to be invoked with the result of the operation
 */
actual fun getReminders(
    userId: String,
    callback: (ReminderResult<List<Reminder>>) -> Unit
) {
    // Mirror Android behavior: require current auth, otherwise return empty list
    val currentUserId = FIRAuth.auth()?.currentUser()?.uid()
    if (currentUserId == null) {
        callback(ReminderResult.Success(emptyList()))
        return
    }

    val ref = FIRDatabase.database()?.reference()
    if (ref == null) {
        callback(ReminderResult.Success(emptyList()))
        return
    }

    ref.child(remindersPath).child(currentUserId)
        .getDataWithCompletionBlock { error: NSError?, snapshot: FIRDataSnapshot? ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                callback(ReminderResult.Success(emptyList()))
                return@getDataWithCompletionBlock
            }

            val reminders = mutableListOf<Reminder>()
            val value = snapshot.value

            when (value) {
                is Map<*, *> -> {
                    for ((k, v) in value) {
                        val childMap = when (v) {
                            is Map<*, *> -> v
                            is NSDictionary -> nsDictionaryToKotlinMap(v)
                            else -> null
                        } ?: continue

                        val rem = mapToReminder(childMap)
                        if (rem.id.isNullOrBlank()) rem.id = k?.toString()
                        if (rem.userId.isNullOrBlank()) rem.userId = currentUserId
                        reminders.add(rem)
                    }
                }

                is NSDictionary -> {
                    val keys = value.allKeys
                    for (k in keys) {
                        val v = value.objectForKey(k)
                        val childMap = when (v) {
                            is Map<*, *> -> v
                            is NSDictionary -> nsDictionaryToKotlinMap(v)
                            else -> null
                        } ?: continue

                        val rem = mapToReminder(childMap)
                        if (rem.id.isNullOrBlank()) rem.id = k?.toString()
                        if (rem.userId.isNullOrBlank()) rem.userId = currentUserId
                        reminders.add(rem)
                    }
                }
            }

            callback(ReminderResult.Success(reminders))
        }
}


    /**
     * Update a reminder
     * @param reminderId the id of the reminder to be updated
     * @param callback callback to be invoked with the result of the operation
     */
    actual fun updateReminder(reminderId: String, updates: Map<String, Any?>, callback: (ReminderResult<Unit>) -> Unit) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) {
            callback(ReminderResult.Error("User not authenticated."))
            return
        }
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) {
            callback(ReminderResult.Error("Firebase not initialized"))
            return
        }

        // Normalize taskActive to 1/0 for storage consistency
        val normalizedUpdates: Map<String, Any?> = updates.mapValues { (k, v) ->
            if (k == "taskActive") {
                when (v) {
                    is Boolean -> if (v) 1 else 0
                    is Number -> if (v.toInt() != 0) 1 else 0
                    is String -> if (v.equals("true", ignoreCase = true) || v == "1") 1 else 0
                    else -> v
                }
            } else v
        }

        val sanitizedUpdates = normalizedUpdates.filterKeys { it != "id" && it != "userId" }
        val objcUpdates: Map<Any?, Any?> =
            sanitizedUpdates.entries.associate { (k, v) -> k as Any? to (v ?: NSNull()) }

        ref.child(remindersPath).child(userId).child(reminderId).updateChildValues(objcUpdates) { error: NSError?, _ ->
            if (error == null) {
                callback(ReminderResult.Success(Unit))
            } else {
                callback(ReminderResult.Error("Failed to update reminder: ${'$'}{error.localizedDescription}"))
            }
        }
    }

    /**
     * Delete a reminder
     * @param id the id of the reminder to be deleted
     * @param callback callback to be invoked with the result of the operation
     */
    actual fun deleteReminder(
        reminderID: String,
        callback: (ReminderResult<Unit>) -> Unit
    ) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) {
            callback(ReminderResult.Error("User not authenticated."))
            return
        }
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) {
            callback(ReminderResult.Error("Firebase not initialized"))
            return
        }
        ref.child(remindersPath).child(userId).child(reminderID).removeValueWithCompletionBlock { error: NSError?, _ ->
            if (error == null) {
                callback(ReminderResult.Success(Unit))
            } else {
                callback(ReminderResult.Error("Failed to delete reminder: ${'$'}{error.localizedDescription}"))
            }
        }
    }

    private fun nsDictionaryToKotlinMap(dict: NSDictionary): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        val keys = dict.allKeys
        for (k in keys) {
            val keyStr = k?.toString() ?: continue
            val v = dict.objectForKey(k)
            result[keyStr] = when (v) {
                is NSDictionary -> nsDictionaryToKotlinMap(v)
                else -> v
            }
        }
        return result
    }

    private fun mapToReminder(map: Map<*, *>): Reminder {
        val id = map["id"]?.toString()
        val userId = map["userId"]?.toString()
        val taskType = map["taskType"]?.toString()
        val taskTime = map["taskTime"]?.toString()
        val taskName = map["taskName"]?.toString()
        val taskActive = anyToBoolean(map["taskActive"]) ?: true
        return Reminder(
            id = id,
            userId = userId,
            taskType = taskType,
            taskTime = taskTime,
            taskName = taskName,
            taskActive = taskActive
        )
    }

    private fun anyToBoolean(v: Any?): Boolean? = when (v) {
        null -> null
        is Boolean -> v
        is Number -> v.toInt() != 0
        is String -> v.equals("true", ignoreCase = true) || v == "1"
        else -> null
    }
}
