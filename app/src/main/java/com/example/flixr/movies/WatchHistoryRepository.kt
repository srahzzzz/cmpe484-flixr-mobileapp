package com.example.flixr.movies

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * WatchHistory/{uid}_{contentId} — progress and completion for recommendations/analytics.
 */
class WatchHistoryRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun recordWatchProgress(
        uid: String,
        contentId: String,
        watched: Boolean,
        progressPercent: Int,
    ) {
        val docId = "${uid}_${contentId}"
        val p = progressPercent.coerceIn(0, 100)
        val now = Timestamp.now()
        val data =
            buildMap<String, Any> {
                put("user_id", uid)
                put("content_id", contentId)
                put("watched", watched)
                put("progress", p)
                put("updated_at", now)
            }
        db.collection("WatchHistory").document(docId).set(data, SetOptions.merge()).await()
    }

    /** Marks title fully watched (progress 100%). */
    suspend fun markWatched(uid: String, contentId: String) {
        recordWatchProgress(uid, contentId, watched = true, progressPercent = 100)
    }

    /**
     * TMDB movie ids the user marked watched, newest [updated_at] first.
     * Uses the same query shape as analytics (filter `watched` on the client).
     */
    suspend fun listWatchedMovieIds(uid: String, limit: Long = 200L): List<Int> {
        val snap =
            db.collection("WatchHistory")
                .whereEqualTo("user_id", uid)
                .limit(limit)
                .get()
                .await()
        return snap.documents
            .mapNotNull { doc ->
                if (doc.getBoolean("watched") != true) return@mapNotNull null
                val cid = doc.getString("content_id") ?: return@mapNotNull null
                val id = cid.toIntOrNull() ?: return@mapNotNull null
                val ts = doc.getTimestamp("updated_at")?.toDate()?.time ?: 0L
                id to ts
            }
            .sortedByDescending { it.second }
            .map { it.first }
            .distinct()
    }
}
