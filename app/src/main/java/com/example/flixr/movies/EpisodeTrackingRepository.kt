package com.example.flixr.movies

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Per-user episode marks for TV shows. Documents: `EpisodeTracking/{uid}_{showId}_{s}_{e}`.
 */
class EpisodeTrackingRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private fun docId(uid: String, showId: Int, season: Int, episode: Int) =
        "${uid}_${showId}_${season}_${episode}"

    suspend fun setEpisodeWatched(
        uid: String,
        showId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        watched: Boolean,
    ) {
        val id = docId(uid, showId, seasonNumber, episodeNumber)
        val now = Timestamp.now()
        val data =
            mapOf(
                "user_id" to uid,
                "show_id" to showId.toString(),
                "season_number" to seasonNumber,
                "episode_number" to episodeNumber,
                "watched" to watched,
                "updated_at" to now,
            )
        db.collection("EpisodeTracking").document(id).set(data, SetOptions.merge()).await()
    }

    /** Distinct TMDB show ids the user has marked at least one episode for. */
    suspend fun getTrackedShowIds(uid: String): List<Int> {
        val snap =
            db.collection("EpisodeTracking")
                .whereEqualTo("user_id", uid)
                .limit(500)
                .get()
                .await()
        return snap.documents
            .mapNotNull { it.getString("show_id")?.toIntOrNull() }
            .distinct()
    }

    suspend fun setSeasonWatched(
        uid: String,
        showId: Int,
        seasonNumber: Int,
        episodeNumbers: List<Int>,
        watched: Boolean,
    ) {
        for (ep in episodeNumbers) {
            setEpisodeWatched(uid, showId, seasonNumber, ep, watched)
        }
    }

    suspend fun getWatchedEpisodeKeys(uid: String, showId: Int): Set<String> {
        val snap =
            db.collection("EpisodeTracking")
                .whereEqualTo("user_id", uid)
                .whereEqualTo("show_id", showId.toString())
                .limit(500)
                .get()
                .await()
        return snap.documents.mapNotNull { doc ->
            if (doc.getBoolean("watched") != true) return@mapNotNull null
            val s = (doc.getLong("season_number") ?: return@mapNotNull null).toInt()
            val e = (doc.getLong("episode_number") ?: return@mapNotNull null).toInt()
            "${s}_$e"
        }.toSet()
    }
}
