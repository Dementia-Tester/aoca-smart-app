package org.example.dementia_tester_app.data

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseFirestore.FIRFirestore
import cocoapods.FirebaseFirestore.FIRQuery
import cocoapods.FirebaseFirestore.FIRDocumentSnapshot
import cocoapods.FirebaseFirestore.FIRQuerySnapshot
import cocoapods.FirebaseFirestore.queryOrderBy
import platform.Foundation.NSError
import platform.Foundation.NSDictionary
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDateTime

/**
 * iOS implementation of ActivityService using Firebase Firestore
 */
actual class ActivityService {
    private val auth = FIRAuth.auth()
    private val firestore = FIRFirestore.firestore()

    private fun getActivitiesCollection() = 
        auth?.currentUser()?.uid()?.let { userId ->
            firestore.collectionWithPath("users").documentWithPath(userId).collectionWithPath("activities")
        }

    actual fun logActivity(activity: Activity, callback: (DatabaseResult<Unit>) -> Unit) {
        val collection = getActivitiesCollection()
        if (collection == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }

        val data = activity.toMap() as Map<Any?, *>
        collection.addDocumentWithData(data) { error: NSError? ->
            if (error == null) {
                callback(DatabaseResult.Success(Unit))
            } else {
                callback(DatabaseResult.Error("Failed to log activity: ${error.localizedDescription}"))
            }
        }
    }

    actual fun getActivitiesFlow(): Flow<List<Activity>> = callbackFlow {
        val collection = getActivitiesCollection()
        if (collection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = collection
            .queryOrderByField("timestamp", descending = true)
            .addSnapshotListener { snapshot: FIRQuerySnapshot?, error: NSError? ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val activities = snapshot.documents.mapNotNull { docAny ->
                        val doc = docAny as FIRDocumentSnapshot
                        val data = doc.data() as? Map<String, Any>
                        data?.let { Activity.fromMap(it, doc.documentID()) }
                    }
                    trySend(activities)
                }
            }

        awaitClose { registration.remove() }
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
        
        val startOfToday = LocalDateTime(today.year, today.monthNumber, today.dayOfMonth, 0, 0)
            .toInstant(TimeZone.UTC).toEpochMilliseconds()

        collection
            .queryWhereField("timestamp", isGreaterThanOrEqualTo = startOfToday)
            .getDocumentsWithCompletion { snapshot: FIRQuerySnapshot?, error: NSError? ->
                if (error != null) {
                    callback(DatabaseResult.Error("Failed to get summary: ${error.localizedDescription}"))
                    return@getDocumentsWithCompletion
                }

                if (snapshot != null) {
                    val summary = mutableMapOf<String, Int>()
                    summary["total"] = snapshot.count().toInt()
                    
                    snapshot.documents.forEach { docAny ->
                        val doc = docAny as FIRDocumentSnapshot
                        val type = doc.data()?.get("type") as? String ?: "other"
                        summary[type] = summary.getOrDefault(type, 0) + 1
                    }
                    callback(DatabaseResult.Success(summary))
                } else {
                    callback(DatabaseResult.Success(emptyMap()))
                }
            }
    }
}
