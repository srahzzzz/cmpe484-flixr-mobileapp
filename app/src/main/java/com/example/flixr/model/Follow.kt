package com.example.flixr.model

/**
 * Maps to documents in the `Followers` collection:
 * `follower_id` — who follows; `following_id` — who is followed.
 */
data class Follow(
    val follower_id: String = "",
    val following_id: String = "",
)
