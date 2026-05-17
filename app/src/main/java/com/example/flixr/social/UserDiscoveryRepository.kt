package com.example.flixr.social

import com.example.flixr.auth.UserProfile
import com.example.flixr.auth.Username
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Username resolution and prefix search on `usernames/{normalized}`.
 */
class UserDiscoveryRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun getUserProfile(uid: String): UserProfile? {
        val snap = db.collection("users").document(uid).get().await()
        if (!snap.exists()) return null
        val data = snap.data ?: return null
        return UserProfile.fromFirestore(data, fallbackUid = uid)
    }

    suspend fun resolveUsernameToUid(raw: String): String? {
        val norm = Username.normalize(raw)
        if (norm.isBlank()) return null
        Username.validateOrThrow(norm)
        val snap = db.collection("usernames").document(norm).get().await()
        if (!snap.exists()) return null
        val uid = snap.getString("uid").orEmpty()
        if (uid.isBlank() || uid == "__RESERVED__") return null
        return uid
    }

    /** Prefix search on username document ids; returns (normalizedUsername, uid). */
    suspend fun searchUsernamesByPrefix(prefixRaw: String, limit: Long = 24): List<Pair<String, String>> {
        val prefix = Username.normalize(prefixRaw)
        if (prefix.length < 2) return emptyList()
        val end = prefix + "\uf8ff"
        val snap =
            db.collection("usernames")
                .orderBy(FieldPath.documentId())
                .startAt(prefix)
                .endAt(end)
                .limit(limit)
                .get()
                .await()
        return snap.documents.mapNotNull { doc ->
            val uid = doc.getString("uid").orEmpty()
            if (uid.isBlank() || uid == "__RESERVED__") return@mapNotNull null
            doc.id to uid
        }
    }

    suspend fun loadUsernameLabels(userIds: List<String>): Map<String, String> {
        val ids = userIds.distinct().filter { it.isNotBlank() }
        if (ids.isEmpty()) return emptyMap()
        val map = mutableMapOf<String, String>()
        for (id in ids.take(100)) {
            val snap = db.collection("users").document(id).get().await()
            val un = snap.getString("username").orEmpty().ifBlank { id.take(8) }
            map[id] = "@$un"
        }
        return map
    }
}
