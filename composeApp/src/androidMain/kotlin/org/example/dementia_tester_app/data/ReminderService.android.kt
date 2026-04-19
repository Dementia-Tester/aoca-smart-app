package org.example.dementia_tester_app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.example.dementia_tester_app.notifications.NotificationManagerProvider
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

/**
 * Interface for reminder service
 */
actual class ReminderService actual constructor() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val notificationManager = NotificationManagerProvider.getNotificationManager()

    private fun scheduleLocalNotification(reminder: Reminder) {
        if (reminder.taskActive && !reminder.taskTime.isNullOrBlank() && !reminder.id.isNullOrBlank()) {
            try {
                val timeString = reminder.taskTime!! // UI format is "HH:MMAM/PM" (e.g., "11:19PM")
                val sdf = SimpleDateFormat("hh:mm a", Locale.US)
                val normalizedTimeString = timeString.replace("AM", " AM").replace("PM", " PM")
                val date = sdf.parse(normalizedTimeString)
                
                if (date != null) {
                    val calendar = Calendar.getInstance()
                    val now = Calendar.getInstance()
                    
                    val timeCalendar = Calendar.getInstance()
                    timeCalendar.time = date
                    
                    calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                    calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    
                    // If time is in the past, schedule for tomorrow
                    if (calendar.before(now)) {
                        calendar.add(Calendar.DATE, 1)
                    }
                    
                    val message = "Reminder: ${reminder.taskName} (${reminder.taskType})"
                    notificationManager.scheduleNotificationAt(reminder.id!!, message, calendar.timeInMillis)
                    Log.d("ReminderService", "Scheduled notification for ${reminder.id} at ${calendar.time}")
                }
            } catch (e: Exception) {
                Log.e("ReminderService", "Failed to schedule notification: ${e.message}")
            }
        } else if (!reminder.id.isNullOrBlank()) {
            notificationManager.cancelNotification(reminder.id!!)
            Log.d("ReminderService", "Cancelled notification for ${reminder.id}")
        }
    }


    /**
     * Helper function returns the database reference for the current user's reminders
     * or null if the user is not authenticated
     */
    private fun getUserRemindersRef(): DatabaseReference? {
        val userId = auth.currentUser?.uid
        return if (userId != null) {
            database.getReference("Reminders").child(userId)
        } else {
            null
        }
    }

    /**
     * Create a reminder
     * @param reminder the reminder to be created
     * @param callback callback to be invoked with the result of the operation
     */
    actual fun createReminder(
        reminder: Reminder,
        callback: (ReminderResult<Unit>) -> Unit
    ) {
        val remindersRef = getUserRemindersRef() ?: return callback(ReminderResult.Error("User not authenticated."))

        val newReminderRef = reminder.id?.let { remindersRef.child(it) } ?: remindersRef.push()
        // Ensure the Reminder object has the ID
        reminder.id = newReminderRef.key
        // Ensure reminder is linked to current user
        reminder.userId = auth.currentUser?.uid

        // Store only necessary fields (exclude id and userId); ensure taskActive is a proper boolean
        val toStore: Map<String, Any?> = mapOf(
            "taskType" to (reminder.taskType ?: ""),
            "taskTime" to (reminder.taskTime ?: ""),
            "taskName" to (reminder.taskName ?: ""),
            "taskActive" to if (reminder.taskActive) 1 else 0
        )
        newReminderRef.setValue(toStore)
            .addOnSuccessListener {
                scheduleLocalNotification(reminder)
                callback(ReminderResult.Success(Unit))
            }
            .addOnFailureListener { e ->
                callback(ReminderResult.Error("Failed to create reminder: ${e.message}"))
            }
    }

    /**
     * Get the user's reminders
     * @param userId the userID to fetch reminders for
     * @param callback callback to be invoked with the result of the operation
     */
    actual fun getReminders(
        userId: String,
        callback: (ReminderResult<List<Reminder>>) -> Unit
    ) {
        val remindersRef = getUserRemindersRef() ?: return callback(ReminderResult.Error("User not authenticated."))

        // Read all reminders under the current user's node; no filtering by userId stored in child
        remindersRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                    val reminders = mutableListOf<Reminder>()
                    val currentUid = auth.currentUser?.uid
                    for (child in snapshot.children) {
                        val taskType = child.child("taskType").value?.toString()
                        val taskTime = child.child("taskTime").value?.toString()
                        val taskName = child.child("taskName").value?.toString()
                        val raw = child.child("taskActive").value
                        val taskActive = when (raw) {
                            is Boolean -> raw
                            is Number -> raw.toInt() != 0
                            is String -> raw.equals("true", ignoreCase = true) || raw == "1"
                            else -> true
                        }
                        val reminder = Reminder(
                            id = child.key,
                            userId = currentUid,
                            taskType = taskType,
                            taskTime = taskTime,
                            taskName = taskName,
                            taskActive = taskActive
                        )
                        reminders.add(reminder)
                    }
                    callback(ReminderResult.Success(reminders))
            }
            override fun onCancelled(error: DatabaseError) {
                callback(ReminderResult.Error("Firebase read cancelled: ${error.message}"))
            }
        })
    }

    /**
     * Update a reminder
     * @param reminderId the id of the reminder to be updated
     * @param updates a map to update the specified value in the reminder
     * @param callback callback to be invoked with the result of the operation
     */
    actual fun updateReminder(
        reminderId: String,
        updates: Map<String, Any?>,
        callback: (ReminderResult<Unit>) -> Unit
    ) {
        val remindersRef = getUserRemindersRef() ?: return callback(ReminderResult.Error("User not authenticated."))

        val normalizedUpdates = updates.mapValues { (k, v) ->
            if (k == "taskActive") {
                when (v) {
                    is Boolean -> if (v) 1 else 0
                    is Number -> if (v.toInt() != 0) 1 else 0
                    is String -> if (v.equals("true", ignoreCase = true) || v == "1") 1 else 0
                    else -> v
                }
            } else v
        }
        val filteredUpdates = normalizedUpdates.filterKeys { it != "id" && it != "userId" }

        remindersRef.child(reminderId).updateChildren(filteredUpdates)
            .addOnSuccessListener {
                // Fetch the full reminder to reschedule it correctly
                remindersRef.child(reminderId).get().addOnSuccessListener { snapshot ->
                    val taskType = snapshot.child("taskType").value?.toString()
                    val taskTime = snapshot.child("taskTime").value?.toString()
                    val taskName = snapshot.child("taskName").value?.toString()
                    val raw = snapshot.child("taskActive").value
                    val taskActive = when (raw) {
                        is Boolean -> raw
                        is Number -> raw.toInt() != 0
                        else -> true
                    }
                    val updatedReminder = Reminder(reminderId, auth.currentUser?.uid, taskType, taskTime, taskName, taskActive)
                    scheduleLocalNotification(updatedReminder)
                }
                callback(ReminderResult.Success(Unit))
            }
            .addOnFailureListener { e ->
                callback(ReminderResult.Error("Failed to update reminder: ${e.message}"))
            }
    }

    /**
     * Delete a reminder
     * @param reminderID the ID of the reminder to be deleted
     * @param callback callback to be invoked with the result of the operation
     */
    actual fun deleteReminder(
        reminderID: String,
        callback: (ReminderResult<Unit>) -> Unit
    ) {
        val remindersRef = getUserRemindersRef() ?: return callback(ReminderResult.Error("User not authenticated."))
        remindersRef.child(reminderID).removeValue()
            .addOnSuccessListener {
                notificationManager.cancelNotification(reminderID)
                callback(ReminderResult.Success(Unit))
            }
            .addOnFailureListener { e ->
                callback(ReminderResult.Error("Failed to delete reminder: ${e.message}"))
            }
    }
}
