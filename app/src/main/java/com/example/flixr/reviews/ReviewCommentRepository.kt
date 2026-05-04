package com.example.flixr.reviews

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReviewCommentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private fun parse(doc: com.google.firebase.firestore.DocumentSnapshot): ReviewComment? {
        val id = doc.id
        val reviewId = doc.getString("review_id") ?: return null
        val userId = doc.getString("user_id") ?: return null
        return ReviewComment(
            comment_id = doc.getString("comment_id") ?: id,
            review_id = reviewId,
            user_id = userId,
            author_username = doc.getString("author_username").orEmpty(),
            text = doc.getString("text").orEmpty(),
            created_at = doc.getLong("created_at") ?: 0L,
        )
    }

    suspend fun addComment(reviewId: String, userId: String, text: String) {
        val trimmed = text.trim()
        require(trimmed.isNotBlank()) { "Comment cannot be empty." }
        val usernameSnap = db.collection("users").document(userId).get().await()
        val author =
            usernameSnap.getString("username").orEmpty().ifBlank {
                userId.take(8)
            }
        val ref = db.collection("ReviewComments").document()
        val data =
            mapOf(
                "comment_id" to ref.id,
                "review_id" to reviewId,
                "user_id" to userId,
                "author_username" to author,
                "text" to trimmed,
                "created_at" to System.currentTimeMillis(),
            )
        ref.set(data).await()
    }

    suspend fun deleteComment(commentId: String, userId: String) {
        val snap = db.collection("ReviewComments").document(commentId).get().await()
        if (!snap.exists()) return
        if (snap.getString("user_id") != userId) error("Not your comment.")
        db.collection("ReviewComments").document(commentId).delete().await()
    }

    fun listenCommentsForReview(reviewId: String): Flow<List<ReviewComment>> =
        callbackFlow {
            val reg =
                db.collection("ReviewComments")
                    .whereEqualTo("review_id", reviewId)
                    .limit(100)
                    .addSnapshotListener { snap, err ->
                        if (err != null) {
                            close(err)
                            return@addSnapshotListener
                        }
                        val list =
                            snap?.documents
                                ?.mapNotNull { parse(it) }
                                ?.sortedBy { it.created_at }
                                .orEmpty()
                        trySend(list)
                    }
            awaitClose { reg.remove() }
        }
}
