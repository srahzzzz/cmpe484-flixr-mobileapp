package com.example.flixr.movies

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Watchlist storage in Firestore (course-style flat collection).
 *
 * Documents live at `Watchlist/{uid}_{movieId}` so each user saves a movie at most once
 * (overwrites / merge on repeat taps).
 *
 * Fields: `user_id`, `movie_id`, `title`, `poster`, `added_at`, plus optional metadata.
 */
class SavedMovieRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun saveMovieForUser(uid: String, movie: Movie) {
        val movieId = movie.id.toString()
        val docId = "${uid}_${movieId}"
        val now = Timestamp.now()

        val data = buildMap<String, Any> {
            put("user_id", uid)
            put("movie_id", movieId)
            put("title", movie.title)
            put("added_at", now)
            movie.posterPath?.let { put("poster", it) }
            movie.releaseDate?.let { put("release_date", it) }
            movie.overview?.let { put("overview", it) }
            movie.voteAverage?.let { put("vote_average", it) }
        }

        db.collection("Watchlist").document(docId).set(data, SetOptions.merge()).await()
    }

    /** Deletes `Watchlist/{uid}_{movieId}` — same document id used when saving. */
    suspend fun removeMovieFromWatchlist(uid: String, movieId: Int) {
        val docId = "${uid}_${movieId}"
        db.collection("Watchlist").document(docId).delete().await()
    }

    suspend fun getWatchlistForUser(uid: String): List<Movie> {
        val snap =
            db.collection("Watchlist")
                .whereEqualTo("user_id", uid)
                .get()
                .await()

        val pairs =
            snap.documents.mapNotNull { doc ->
                val idStr = doc.getString("movie_id") ?: return@mapNotNull null
                val id = idStr.toIntOrNull() ?: return@mapNotNull null
                val added = doc.getTimestamp("added_at")?.toDate()?.time ?: 0L
                val movie =
                    Movie(
                        id = id,
                        title = doc.getString("title") ?: "",
                        posterPath = doc.getString("poster"),
                        releaseDate = doc.getString("release_date"),
                        overview = doc.getString("overview"),
                        voteAverage = (doc.get("vote_average") as? Number)?.toDouble(),
                    )
                movie to added
            }
        return pairs.sortedByDescending { it.second }.map { it.first }
    }
}
