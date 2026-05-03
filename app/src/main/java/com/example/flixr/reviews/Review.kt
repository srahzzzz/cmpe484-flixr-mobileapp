package com.example.flixr.reviews

data class Review(
    val review_id: String = "",
    val user_id: String = "",
    val content_id: String = "",
    val rating: Int = 0,
    val review_text: String = "",
    val likes_count: Int = 0,
    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = 0,
)

