package com.example.flixr.messages

/**
 * Firestore collection `DirectMessages` — simple 1:1 chat keyed by [chat_room_id].
 */
data class DirectMessage(
    val message_id: String = "",
    val chat_room_id: String = "",
    val sender_id: String = "",
    val recipient_id: String = "",
    val sender_username: String = "",
    val text: String = "",
    val created_at: Long = System.currentTimeMillis(),
)
