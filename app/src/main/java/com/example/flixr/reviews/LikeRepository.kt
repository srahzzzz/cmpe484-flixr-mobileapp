package com.example.flixr.reviews

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

class LikeRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private fun likeDocId(userId: String, reviewId: String) = "${userId}_${reviewId}"

    suspend fun hasLiked(userId: String, reviewId: String): Boolean {
        if (userId.isBlank() || reviewId.isBlank()) return false
        return db.collection("Likes").document(likeDocId(userId, reviewId)).get().await().exists()
    }

    /** Which of [reviewIds] the user has liked (N reads; fine for small lists). */
    suspend fun filterLikedReviewIds(userId: String, reviewIds: List<String>): Set<String> {
        if (userId.isBlank()) return emptySet()
        val ids = reviewIds.filter { it.isNotBlank() }.distinct()
        if (ids.isEmpty()) return emptySet()
        val out = mutableSetOf<String>()
        for (rid in ids) {
            if (hasLiked(userId, rid)) out.add(rid)
        }
        return out
    }

    /**
     * Atomically creates/deletes the like doc and updates [Review.likes_count] on the review.
     * Document id: `{userId}_{reviewId}` to prevent duplicate likes.
     */
    suspend fun toggleLike(userId: String, reviewId: String) {
        require(userId.isNotBlank() && reviewId.isNotBlank())
        val likeRef = db.collection("Likes").document(likeDocId(userId, reviewId))
        val reviewRef = db.collection("Reviews").document(reviewId)

        db.runTransaction { tx ->
            val likeSnap = tx.get(likeRef)
            val reviewSnap = tx.get(reviewRef)
            if (!reviewSnap.exists()) {
                throw FirebaseFirestoreException(
                    "Review not found",
                    FirebaseFirestoreException.Code.NOT_FOUND,
                )
            }
            val current = readLikesCount(reviewSnap)
            if (likeSnap.exists()) {
                tx.delete(likeRef)
                tx.update(reviewRef, mapOf("likes_count" to (current - 1).coerceAtLeast(0)))
            } else {
                tx.set(likeRef, mapOf("user_id" to userId, "review_id" to reviewId))
                tx.update(reviewRef, mapOf("likes_count" to current + 1))
            }
            null
        }.await()
    }

    private fun readLikesCount(snap: DocumentSnapshot): Int {
        val v = snap.get("likes_count") ?: return 0
        return when (v) {
            is Long -> v.toInt().coerceAtLeast(0)
            is Int -> v.coerceAtLeast(0)
            is Number -> v.toInt().coerceAtLeast(0)
            else -> 0
        }
    }
}
