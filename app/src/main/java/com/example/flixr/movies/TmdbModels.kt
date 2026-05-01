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
)

data class MovieResponse(
    val results: List<Movie> = emptyList(),
)

