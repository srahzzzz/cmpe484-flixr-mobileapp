package com.example.flixr.movies

import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApi {
    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String,
    ): MovieResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
    ): MovieResponse
}

