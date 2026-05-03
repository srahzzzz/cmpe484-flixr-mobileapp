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

/** TMDB GET /movie/{id} — used for analytics (runtime, genres). */
data class MovieDetails(
    val id: Int,
    val runtime: Int? = null,
    val genres: List<Genre> = emptyList(),
)

