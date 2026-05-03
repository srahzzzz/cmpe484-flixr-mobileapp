package com.example.flixr.stats

import com.example.flixr.movies.TmdbApi
import com.example.flixr.movies.TmdbClient
import com.example.flixr.reviews.Review
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Derived analytics — computed from Firestore on demand (no analytics collection).
 * Genre + watch-time rows use TMDB movie details for watched [content_id]s.
 */
data class UserAnalytics(
    /** Rows in [Watchlist] with matching `user_id`. */
    val watchlistCount: Int,
    /** [WatchHistory] rows where `watched == true`. */
    val watchedTitlesCount: Int,
    val reviewsWritten: Int,
    /** Mean of `rating` across this user's reviews; null if none. */
    val averageRatingGiven: Double?,
    /** Distribution of ratings (0–10) for reviews written by this user. */
    val ratingsHistogram: Map<Int, Int>,
    /** Genre name with highest watch-based score (each watched title counts once per genre tag). */
    val topGenreName: String?,
    val topGenreScore: Int,
    /** Sorted descending by count for charts. */
    val genreBreakdown: List<Pair<String, Int>>,
    /** Sum of TMDB `runtime` (minutes) for watched titles that returned a runtime. */
    val estimatedWatchMinutes: Int,
)

class AnalyticsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val tmdbApi: TmdbApi = TmdbClient.api,
) {
    suspend fun loadUserAnalytics(uid: String, tmdbApiKey: String): UserAnalytics {
        val watchSnap =
            db.collection("Watchlist")
                .whereEqualTo("user_id", uid)
                .limit(FIRESTORE_QUERY_LIMIT.toLong())
                .get()
                .await()

        val historySnap =
            db.collection("WatchHistory")
                .whereEqualTo("user_id", uid)
                .limit(FIRESTORE_QUERY_LIMIT.toLong())
                .get()
                .await()

        val watchedTitlesCount =
            historySnap.documents.count { doc ->
                doc.getBoolean("watched") == true
            }

        val watchedMovieIds: List<Int> =
            historySnap.documents.mapNotNull { doc ->
                if (doc.getBoolean("watched") != true) return@mapNotNull null
                doc.getString("content_id")?.toIntOrNull()
            }.distinct()

        val reviewsSnap =
            db.collection("Reviews")
                .whereEqualTo("user_id", uid)
                .limit(FIRESTORE_QUERY_LIMIT.toLong())
                .get()
                .await()

        val reviews =
            reviewsSnap.documents.mapNotNull { it.toObject(Review::class.java) }

        val histogram = mutableMapOf<Int, Int>()
        for (r in reviews) {
            val k = r.rating.coerceIn(0, 10)
            histogram[k] = histogram.getOrDefault(k, 0) + 1
        }

        val avg =
            if (reviews.isEmpty()) {
                null
            } else {
                reviews.sumOf { it.rating }.toDouble() / reviews.size
            }

        val genreCounts = mutableMapOf<String, Int>()
        var estimatedMinutes = 0
        val key = tmdbApiKey.trim()
        if (key.isNotEmpty() && watchedMovieIds.isNotEmpty()) {
            val capped = watchedMovieIds.take(MAX_TMDB_DETAIL_FETCHES)
            for (movieId in capped) {
                try {
                    val details = tmdbApi.getMovieDetails(movieId, key)
                    details.runtime?.let { estimatedMinutes += it }
                    for (g in details.genres) {
                        val name = g.name.trim()
                        if (name.isNotEmpty()) {
                            genreCounts[name] = genreCounts.getOrDefault(name, 0) + 1
                        }
                    }
                } catch (_: Exception) {
                    // Skip missing/invalid TMDB ids or network errors — stats stay consistent for the rest.
                }
            }
        }

        val breakdownSorted =
            genreCounts.entries
                .sortedByDescending { it.value }
                .map { it.key to it.value }

        val topEntry = breakdownSorted.firstOrNull()

        return UserAnalytics(
            watchlistCount = watchSnap.size(),
            watchedTitlesCount = watchedTitlesCount,
            reviewsWritten = reviews.size,
            averageRatingGiven = avg,
            ratingsHistogram = histogram,
            topGenreName = topEntry?.first,
            topGenreScore = topEntry?.second ?: 0,
            genreBreakdown = breakdownSorted,
            estimatedWatchMinutes = estimatedMinutes,
        )
    }

    companion object {
        /** Upper bound per Firestore query — avoids loading unbounded collections on this screen. */
        private const val FIRESTORE_QUERY_LIMIT = 500

        /** Cap parallel TMDB detail calls (performance / API fairness). */
        private const val MAX_TMDB_DETAIL_FETCHES = 100
    }
}
