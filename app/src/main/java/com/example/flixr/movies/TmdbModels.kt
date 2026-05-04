package com.example.flixr.movies

import com.google.gson.annotations.SerializedName

data class Movie(
    val id: Int,
    val title: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    val overview: String?,
    @SerializedName("vote_average")
    val voteAverage: Double? = null,
)

data class MovieResponse(
    val results: List<Movie> = emptyList(),
)

data class Genre(
    val id: Int,
    val name: String,
)

/**
 * TMDB GET /movie/{id} — fields used for detail hero + analytics (runtime, genres).
 * Extra JSON fields from TMDB are ignored by Gson.
 */
data class MovieDetails(
    val id: Int,
    val title: String? = null,
    @SerializedName("poster_path")
    val posterPath: String? = null,
    @SerializedName("release_date")
    val releaseDate: String? = null,
    val overview: String? = null,
    @SerializedName("vote_average")
    val voteAverage: Double? = null,
    val runtime: Int? = null,
    val genres: List<Genre> = emptyList(),
)

data class TvShowResponse(
    val results: List<TvShowItem> = emptyList(),
)

data class TvShowItem(
    val id: Int,
    val name: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("first_air_date")
    val firstAirDate: String?,
    val overview: String?,
)

data class TvDetails(
    val id: Int,
    val name: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("first_air_date")
    val firstAirDate: String?,
    val overview: String?,
    @SerializedName("number_of_seasons")
    val numberOfSeasons: Int? = null,
)

data class TvSeasonDetails(
    @SerializedName("season_number")
    val seasonNumber: Int,
    val name: String? = null,
    val episodes: List<TvEpisode> = emptyList(),
)

data class TvEpisode(
    @SerializedName("episode_number")
    val episodeNumber: Int,
    val name: String? = null,
    @SerializedName("still_path")
    val stillPath: String? = null,
    val overview: String? = null,
)

