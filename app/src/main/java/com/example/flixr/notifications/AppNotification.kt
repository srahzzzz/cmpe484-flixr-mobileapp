package com.example.flixr.notifications

/**
 * Firestore `Notifications/{notificationId}` — in-app alerts for social events.
 */
data class AppNotification(
    val notification_id: String = "",
    val user_id: String = "",
    val type: String = "",
    val actor_id: String = "",
    val actor_username: String = "",
    val reference_id: String = "",
    val content_id: String = "",
    val read: Boolean = false,
    val created_at: Long = 0L,
) {
    fun toMap(): Map<String, Any?> =
        mapOf(
            "notification_id" to notification_id,
            "user_id" to user_id,
            "type" to type,
            "actor_id" to actor_id,
            "actor_username" to actor_username,
            "reference_id" to reference_id,
            "content_id" to content_id,
            "read" to read,
            "created_at" to created_at,
        )

    companion object {
        fun fromMap(data: Map<String, Any?>, id: String): AppNotification =
            AppNotification(
                notification_id = id,
                user_id = data["user_id"] as? String ?: "",
                type = data["type"] as? String ?: "",
                actor_id = data["actor_id"] as? String ?: "",
                actor_username = data["actor_username"] as? String ?: "",
                reference_id = data["reference_id"] as? String ?: "",
                content_id = data["content_id"] as? String ?: "",
                read = data["read"] as? Boolean ?: false,
                created_at = (data["created_at"] as? Number)?.toLong() ?: 0L,
            )
    }
}
