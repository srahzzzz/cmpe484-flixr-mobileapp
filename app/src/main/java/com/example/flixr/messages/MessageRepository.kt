package com.example.flixr.messages

import com.example.flixr.notifications.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MessageRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val notificationRepo: NotificationRepository = NotificationRepository(),
) {
    companion object {
        fun chatRoomId(uidA: String, uidB: String): String =
            if (uidA <= uidB) "${uidA}_${uidB}" else "${uidB}_${uidA}"
    }

    private fun parse(doc: com.google.firebase.firestore.DocumentSnapshot): DirectMessage? {
        val id = doc.id
        return DirectMessage(
            message_id = doc.getString("message_id") ?: id,
            chat_room_id = doc.getString("chat_room_id").orEmpty(),
            sender_id = doc.getString("sender_id").orEmpty(),
            recipient_id = doc.getString("recipient_id").orEmpty(),
            sender_username = doc.getString("sender_username").orEmpty(),
            text = doc.getString("text").orEmpty(),
            created_at = doc.getLong("created_at") ?: 0L,
        )
    }

    suspend fun sendMessage(senderId: String, recipientId: String, text: String) {
        if (senderId == recipientId) return
        val trimmed = text.trim()
        require(trimmed.isNotBlank()) { "Message cannot be empty." }
        val room = chatRoomId(senderId, recipientId)
        val snap = db.collection("users").document(senderId).get().await()
        val senderName =
            snap.getString("username").orEmpty().ifBlank {
                senderId.take(8)
            }
        val ref = db.collection("DirectMessages").document()
        ref.set(
            mapOf(
                "message_id" to ref.id,
                "chat_room_id" to room,
                "sender_id" to senderId,
                "recipient_id" to recipientId,
                "sender_username" to senderName,
                "text" to trimmed,
                "created_at" to System.currentTimeMillis(),
            ),
        ).await()
        notificationRepo.createNotification(
            recipientUserId = recipientId,
            type = "message",
            actorId = senderId,
            actorUsername = senderName,
            referenceId = ref.id,
        )
    }

    fun listenMessages(chatRoomId: String): Flow<List<DirectMessage>> =
        callbackFlow {
            val reg =
                db.collection("DirectMessages")
                    .whereEqualTo("chat_room_id", chatRoomId)
                    .limit(200)
                    .addSnapshotListener { snap, err ->
                        if (err != null) {
                            close(err)
                            return@addSnapshotListener
                        }
                        val list =
                            snap?.documents
                                ?.mapNotNull { parse(it) }
                                ?.sortedBy { it.created_at }
                                .orEmpty()
                        trySend(list)
                    }
            awaitClose { reg.remove() }
        }
}
