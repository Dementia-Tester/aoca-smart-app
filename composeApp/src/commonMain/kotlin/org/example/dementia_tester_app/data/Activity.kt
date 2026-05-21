package org.example.dementia_tester_app.data

import kotlinx.datetime.Instant
import kotlinx.datetime.Clock

/**
 * Data model for user activities
 */
data class Activity(
    val id: String = "",
    val title: String = "",
    val type: String = "", // "game", "test", "reminder", "appointment"
    val description: String = "",
    val timestamp: Instant = Clock.System.now()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "title" to title,
            "type" to type,
            "description" to description,
            "timestamp" to timestamp.toEpochMilliseconds()
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>, id: String): Activity {
            val title = map["title"] as? String ?: ""
            val type = map["type"] as? String ?: ""
            val description = map["description"] as? String ?: ""
            
            val timestampRaw = map["timestamp"]
            val timestamp = when (timestampRaw) {
                is Long -> Instant.fromEpochMilliseconds(timestampRaw)
                is Number -> Instant.fromEpochMilliseconds(timestampRaw.toLong())
                is String -> timestampRaw.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) } ?: Clock.System.now()
                else -> Clock.System.now()
            }

            return Activity(
                id = id,
                title = title,
                type = type,
                description = description,
                timestamp = timestamp
            )
        }
    }
}
