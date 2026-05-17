package com.example.flixr.notifications

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun createNotification(
        recipientUserId: String,
        type: String,
        actorId: String,
        actorUsername: String,
        referenceId: String = "",
        contentId: String = "",
    ) {
        if (recipientUserId.isBlank() || actorId.isBlank() || recipientUserId == actorId) return
        val ref = db.collection("Notifications").document()
        ref.set(
            AppNotification(
                notification_id = ref.id,
                user_id = recipientUserId,
                type = type,
                actor_id = actorId,
                actor_username = actorUsername,
                reference_id = referenceId,
                content_id = contentId,
                read = false,
                created_at = System.currentTimeMillis(),
            ).toMap(),
        ).await()
    }

    suspend fun markRead(notificationId: String) {
        if (notificationId.isBlank()) return
        db.collection("Notifications").document(notificationId)
            .update("read", true)
            .await()
    }

    suspend fun markAllRead(userId: String) {
        val snap =
            db.collection("Notifications")
                .whereEqualTo("user_id", userId)
                .whereEqualTo("read", false)
                .limit(100)
                .get()
                .await()
        for (doc in snap.documents) {
            doc.reference.update("read", true).await()
        }
    }

    fun listenNotifications(userId: String): Flow<List<AppNotification>> =
        callbackFlow {
            if (userId.isBlank()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            val reg =
                db.collection("Notifications")
                    .whereEqualTo("user_id", userId)
                    .orderBy("created_at", Query.Direction.DESCENDING)
                    .limit(80)
                    .addSnapshotListener { snap, err ->
                        if (err != null) {
                            close(err)
                            return@addSnapshotListener
                        }
                        val list =
                            snap?.documents?.map { doc ->
                                AppNotification.fromMap(doc.data ?: emptyMap(), doc.id)
                            }.orEmpty()
                        trySend(list)
                    }
            awaitClose { reg.remove() }
        }

    fun listenUnreadCount(userId: String): Flow<Int> =
        callbackFlow {
            if (userId.isBlank()) {
                trySend(0)
                awaitClose { }
                return@callbackFlow
            }
            val reg =
                db.collection("Notifications")
                    .whereEqualTo("user_id", userId)
                    .whereEqualTo("read", false)
                    .addSnapshotListener { snap, err ->
                        if (err != null) {
                            trySend(0)
                            return@addSnapshotListener
                        }
                        trySend(snap?.size() ?: 0)
                    }
            awaitClose { reg.remove() }
        }

    suspend fun resolveActorUsername(actorId: String): String {
        if (actorId.isBlank()) return ""
        val snap = db.collection("users").document(actorId).get().await()
        return snap.getString("username").orEmpty().ifBlank { actorId.take(8) }
    }
}
