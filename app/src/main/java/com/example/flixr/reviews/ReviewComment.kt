package com.example.flixr.reviews

/**
 * Firestore collection `ReviewComments` — replies attached to a [Review.review_id].
 */
data class ReviewComment(
    val comment_id: String = "",
    val review_id: String = "",
    val user_id: String = "",
    val author_username: String = "",
    val text: String = "",
    val created_at: Long = System.currentTimeMillis(),
)
