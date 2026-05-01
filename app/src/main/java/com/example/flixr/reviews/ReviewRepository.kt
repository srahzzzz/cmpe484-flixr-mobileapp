package com.example.flixr.reviews

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReviewRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun addReview(review: Review) {
        val docRef = db.collection("Reviews").document()
        val newReview = review.copy(review_id = docRef.id)
        docRef.set(newReview).await()
    }

    suspend fun getReviewsForContent(contentId: String): List<Review> {
        val snap =
            db.collection("Reviews")
                .whereEqualTo("content_id", contentId)
                .get()
                .await()
        return snap.documents.mapNotNull { it.toObject(Review::class.java) }
    }
}

