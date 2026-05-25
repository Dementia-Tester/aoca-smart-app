package org.example.dementia_tester_app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import android.util.Log
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant

/**
 * Android implementation of ActivityService using Firebase Firestore
 */
actual class ActivityService {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore
    private val tag = "ActivityService"

    private fun getActivitiesCollection() = 
        auth.currentUser?.uid?.let { userId ->
            firestore.collection("UserProfiles").document(userId).collection("activities")
        }

    actual fun logActivity(activity: Activity, callback: (DatabaseResult<Unit>) -> Unit) {
        val collection = getActivitiesCollection()
        if (collection == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }

        collection.add(activity.toMap())
            .addOnSuccessListener {
                Log.d(tag, "Activity logged: ${activity.title}")
                callback(DatabaseResult.Success(Unit))
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Failed to log activity", e)
                callback(DatabaseResult.Error("Failed to log activity: ${e.message}"))
            }
    }

    actual fun getActivitiesFlow(): Flow<List<Activity>> = callbackFlow {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        val collection = getActivitiesCollection()
        
        if (collection == null) {
            Log.e(tag, "getActivitiesFlow: No user signed in or collection is null")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        Log.d(tag, "Starting activities flow listener for UID: $userId")
        val registration = collection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(tag, "Listen failed for UserProfiles/$userId/activities. This might be a permission issue. Error: ${e.message}", e)
                    // We don't close the flow on a single error as it might be transient or recovered
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d(tag, "Received activities snapshot for $userId. Count: ${snapshot.size()}")
                    val activities = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { Activity.fromMap(it, doc.id) }
                    }
                    trySend(activities)
                }
            }

        awaitClose { 
            Log.d(tag, "Closing activities flow listener for $userId")
            registration.remove() 
        }
    }

    actual fun getTodaySummary(callback: (DatabaseResult<Map<String, Int>>) -> Unit) {
        val collection = getActivitiesCollection()
        if (collection == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }

        val now = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(tz).date
        
        // Start of today in milliseconds
        val startOfToday = kotlinx.datetime.LocalDateTime(today.year, today.monthNumber, today.dayOfMonth, 0, 0)
            .toInstant(TimeZone.UTC).toEpochMilliseconds()

        collection
            .whereGreaterThanOrEqualTo("timestamp", startOfToday)
            .get()
            .addOnSuccessListener { snapshot ->
                val summary = mutableMapOf<String, Int>()
                summary["total"] = snapshot.size()
                
                snapshot.documents.forEach { doc ->
                    val type = doc.getString("type") ?: "other"
                    summary[type] = summary.getOrDefault(type, 0) + 1
                }
                callback(DatabaseResult.Success(summary))
            }
            .addOnFailureListener { e ->
                callback(DatabaseResult.Error("Failed to get summary: ${e.message}"))
            }
    }
}
