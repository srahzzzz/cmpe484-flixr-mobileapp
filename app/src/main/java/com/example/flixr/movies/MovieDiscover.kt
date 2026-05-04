package com.example.flixr.movies

/**
 * Mood presets map to TMDB [discover/movie] genre filters (and optional minimum vote).
 * Comma-separated IDs follow TMDB “with_genres” (AND).
 */
object MoodPresets {
    data class Mood(
        val id: String,
        val label: String,
        /** Short line shown on Mood Match cards + Browse chips. */
        val subtitle: String,
        val emoji: String,
        /** TMDB genre ids, comma-separated; empty = do not constrain genres via mood. */
        val withGenres: String,
        val minVote: Float?,
    )

    val all: List<Mood> =
        listOf(
            Mood("thrilled", "Thrilled", "Edge-of-seat tension", "😰", "53", 6.5f),
            Mood("emotional", "Emotional", "Cry it out", "😭", "18", 7f),
            Mood("happy", "Happy", "Feel-good vibes", "😂", "35", 6.5f),
            Mood("comfort", "Comfort", "Cozy rewatch", "🛋️", "10751", 6.5f),
            Mood("thoughtful", "Thoughtful", "Mind-bending", "🧠", "9648", 6.5f),
            Mood("scared", "Scared", "Pure horror", "😱", "27", 5.5f),
        )

    fun byId(id: String?): Mood? = id?.let { wanted -> all.firstOrNull { it.id == wanted } }
}

/**
 * Client-side filters for [Movie] lists (e.g. TMDB search results where discover params do not apply).
 */
fun List<Movie>.withLocalFilters(
    yearFrom: Int,
    yearTo: Int,
    minVote: Float,
): List<Movie> {
    val yf = minOf(yearFrom, yearTo)
    val yt = maxOf(yearFrom, yearTo)
    return filter { m ->
        val y = m.releaseDate?.take(4)?.toIntOrNull()
        val okYear =
            when {
                y == null -> true
                y < yf -> false
                y > yt -> false
                else -> true
            }
        val v = m.voteAverage
        val okVote = v == null || v >= minVote
        okYear && okVote
    }
}

suspend fun discoverMoviesFiltered(
    api: TmdbApi,
    apiKey: String,
    genreId: String?,
    moodId: String?,
    yearFrom: Int,
    yearTo: Int,
    minVoteUser: Float,
): List<Movie> {
    val mood = MoodPresets.byId(moodId)
    val withGenres =
        when {
            mood != null && mood.withGenres.isNotBlank() -> mood.withGenres
            else -> genreId
        }
    val effectiveMinVote = maxOf(minVoteUser, mood?.minVote ?: 0f)
    val yf = minOf(yearFrom, yearTo).coerceIn(1900, 2100)
    val yt = maxOf(yearFrom, yearTo).coerceIn(1900, 2100)
    val gte = String.format("%04d-01-01", yf)
    val lte = String.format("%04d-12-31", yt)
    return api
        .discoverMovies(
            apiKey = apiKey,
            withGenres = withGenres?.takeIf { it.isNotBlank() },
            primaryReleaseDateGte = gte,
            primaryReleaseDateLte = lte,
            voteAverageGte = effectiveMinVote.takeIf { it > 0.05f },
            sortBy = "popularity.desc",
            page = 1,
        ).results
}

suspend fun loadBrowseTabResults(
    api: TmdbApi,
    apiKey: String,
    query: String,
    genreId: String?,
    moodId: String?,
    yearFrom: Int,
    yearTo: Int,
    minVote: Float,
): List<Movie> {
    val q = query.trim()
    return if (q.isNotEmpty()) {
        api.searchMovies(apiKey = apiKey, query = q).results.withLocalFilters(yearFrom, yearTo, minVote)
    } else {
        discoverMoviesFiltered(api, apiKey, genreId, moodId, yearFrom, yearTo, minVote)
    }
}
