package com.example.flixr.reviews

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReviewRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    /** Avoid unbounded reads on popular titles; increase if you add pagination. */
    private val reviewsPerContentLimit = 200L

    suspend fun addReview(review: Review) {
        val docRef = db.collection("Reviews").document()
        val newReview = review.copy(review_id = docRef.id)
        docRef.set(newReview).await()
    }

    suspend fun updateReview(review: Review) {
        require(review.review_id.isNotBlank()) { "Missing review_id" }
        db.collection("Reviews").document(review.review_id).set(review).await()
    }

    suspend fun deleteReview(reviewId: String) {
        db.collection("Reviews").document(reviewId).delete().await()
    }

    suspend fun getReviewsForUser(userId: String): List<Review> {
        val snap =
            db.collection("Reviews")
                .whereEqualTo("user_id", userId)
                .limit(reviewsPerContentLimit)
                .get()
                .await()
        return snap.documents
            .mapNotNull { it.toObject(Review::class.java) }
            .sortedWith(
                compareByDescending<Review> { it.created_at }
                    .thenByDescending { it.updated_at },
            )
    }

    suspend fun getReviewsForContent(contentId: String): List<Review> {
        val snap =
            db.collection("Reviews")
                .whereEqualTo("content_id", contentId)
                .limit(reviewsPerContentLimit)
                .get()
                .await()
        return snap.documents
            .mapNotNull { it.toObject(Review::class.java) }
            .sortedWith(
                compareByDescending<Review> { it.created_at }
                    .thenByDescending { it.updated_at },
            )
    }

    /** Real-time reviews for one movie/TV content. */
    fun listenReviewsForContent(contentId: String): Flow<List<Review>> =
        callbackFlow {
            val reg =
                db.collection("Reviews")
                    .whereEqualTo("content_id", contentId)
                    .limit(reviewsPerContentLimit)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }
                        val list =
                            snapshot?.documents
                                ?.mapNotNull { it.toObject(Review::class.java) }
                                .orEmpty()
                                .sortedWith(
                                    compareByDescending<Review> { it.created_at }
                                        .thenByDescending { it.updated_at },
                                )
                        trySend(list)
                    }
            awaitClose { reg.remove() }
        }

    /** One-shot fetch: reviews whose authors are in [userIds] (chunks of 30 for Firestore `in`). */
    suspend fun getReviewsFromUsers(userIds: List<String>): List<Review> {
        val ids = userIds.distinct().filter { it.isNotBlank() }
        if (ids.isEmpty()) return emptyList()
        val out = mutableListOf<Review>()
        for (chunk in ids.chunked(30)) {
            val snap =
                db.collection("Reviews")
                    .whereIn("user_id", chunk)
                    .get()
                    .await()
            out.addAll(snap.documents.mapNotNull { it.toObject(Review::class.java) })
        }
        return out.sortedWith(
            compareByDescending<Review> { it.created_at }.thenByDescending { it.updated_at },
        )
    }

    /**
     * Live activity feed: reviews by followed users. Uses one listener per chunk of ≤30 UIDs.
     */
    fun listenReviewsFromUsers(userIds: List<String>): Flow<List<Review>> =
        callbackFlow {
            val ids = userIds.distinct().filter { it.isNotBlank() }
            if (ids.isEmpty()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            val chunks = ids.chunked(30)
            val chunkResults = Array(chunks.size) { emptyList<Review>() }
            fun emitMerged() {
                val merged =
                    chunkResults
                        .flatMap { it }
                        .sortedWith(
                            compareByDescending<Review> { it.created_at }
                                .thenByDescending { it.updated_at },
                        )
                trySend(merged)
            }
            val regs =
                chunks.mapIndexed { index, chunk ->
                    db.collection("Reviews")
                        .whereIn("user_id", chunk)
                        .addSnapshotListener { snap, err ->
                            if (err != null) {
                                close(err)
                                return@addSnapshotListener
                            }
                            chunkResults[index] =
                                snap?.documents
                                    ?.mapNotNull { it.toObject(Review::class.java) }
                                    .orEmpty()
                            emitMerged()
                        }
                }
            awaitClose { regs.forEach { it.remove() } }
        }
}
