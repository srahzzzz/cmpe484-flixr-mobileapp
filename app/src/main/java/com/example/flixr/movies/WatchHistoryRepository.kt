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
}
