package com.example.flixr.movies

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Optional persistence: save movies a user interacted with.
 *
 * We write:
 * - Content/{movieId} -> basic movie metadata (shared/global)
 * - users/{uid}/saved_movies/{movieId} -> per-user saved mapping
 */
class SavedMovieRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun saveMovieForUser(uid: String, movie: Movie) {
        val contentRef = db.collection("Content").document(movie.id.toString())
        val savedRef =
            db.collection("users")
                .document(uid)
                .collection("saved_movies")
                .document(movie.id.toString())

        val now = Timestamp.now()

        val contentDoc =
            mapOf(
                "movie_id" to movie.id.toString(),
                "title" to movie.title,
                "poster" to movie.posterPath,
                "release_date" to movie.releaseDate,
                "overview" to movie.overview,
                "updated_at" to now,
            )

        val savedDoc =
            mapOf(
                "movie_id" to movie.id.toString(),
                "saved_at" to now,
            )

        // Best-effort: make sure Content doc exists, then save mapping.
        contentRef.set(contentDoc, SetOptions.merge()).await()
        savedRef.set(savedDoc, SetOptions.merge()).await()
    }
}

