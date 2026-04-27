package com.example.flixr.auth

import com.google.firebase.Timestamp

/**
 * Firestore document model for `users/{uid}`.
 *
 * We store the Firebase Auth UID as the **document id** (recommended),
 * and also keep `user_id` as a field for assignment requirements / debugging.
 */
data class UserProfile(
    val userId: String,
    val username: String,
    val email: String,
    val profilePictureUrl: String? = null,
    val bio: String = "",
    val createdAt: Timestamp,
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "user_id" to userId,
        "username" to username,
        "email" to email,
        "profile_picture" to profilePictureUrl,
        "bio" to bio,
        "created_at" to createdAt,
    )

    companion object {
        fun fromFirestore(data: Map<String, Any?>, fallbackUid: String): UserProfile {
            val userId = (data["user_id"] as? String).orEmpty().ifBlank { fallbackUid }
            val username = (data["username"] as? String).orEmpty()
            val email = (data["email"] as? String).orEmpty()
            val profilePicture = data["profile_picture"] as? String
            val bio = (data["bio"] as? String).orEmpty()
            val createdAt = data["created_at"] as? Timestamp ?: Timestamp.now()

            return UserProfile(
                userId = userId,
                username = username,
                email = email,
                profilePictureUrl = profilePicture,
                bio = bio,
                createdAt = createdAt,
            )
        }
    }
}
