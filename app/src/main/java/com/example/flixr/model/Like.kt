package com.example.flixr.model

/** Stored at `Likes/{user_id}_{review_id}`. */
data class Like(
    val user_id: String = "",
    val review_id: String = "",
)
