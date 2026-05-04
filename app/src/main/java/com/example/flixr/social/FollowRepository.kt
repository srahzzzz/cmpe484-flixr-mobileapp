package com.example.flixr.social

import com.example.flixr.model.Follow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Followers/{followerUid}_{followingUid} — directed follow edges for social feed.
 */
class FollowRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private fun edgeId(followerId: String, followingId: String) = "${followerId}_${followingId}"

    suspend fun follow(followerId: String, followingId: String) {
        if (followerId == followingId) return
        val docId = edgeId(followerId, followingId)
        db.collection("Followers")
            .document(docId)
            .set(Follow(follower_id = followerId, following_id = followingId))
            .await()
    }

    suspend fun unfollow(followerId: String, followingId: String) {
        db.collection("Followers").document(edgeId(followerId, followingId)).delete().await()
    }

    suspend fun getFollowingIds(followerId: String): List<String> {
        val snap =
            db.collection("Followers")
                .whereEqualTo("follower_id", followerId)
                .get()
                .await()
        return snap.documents.mapNotNull { it.getString("following_id") }.distinct()
    }

    suspend fun isFollowing(followerId: String, followingId: String): Boolean =
        db.collection("Followers").document(edgeId(followerId, followingId)).get().await().exists()

    /** How many accounts follow [userId] (capped by query limit for profile display). */
    suspend fun countFollowers(userId: String): Int {
        val snap =
            db.collection("Followers")
                .whereEqualTo("following_id", userId)
                .limit(500)
                .get()
                .await()
        return snap.size()
    }

    /** Users who follow [userId] (capped). */
    suspend fun getFollowerIds(userId: String): List<String> {
        val snap =
            db.collection("Followers")
                .whereEqualTo("following_id", userId)
                .limit(500)
                .get()
                .await()
        return snap.documents.mapNotNull { it.getString("follower_id") }.distinct()
    }

    /** Live list of user IDs this account follows (re-attaches when follow graph changes). */
    fun listenFollowingIds(followerId: String): Flow<List<String>> =
        callbackFlow {
            val reg =
                db.collection("Followers")
                    .whereEqualTo("follower_id", followerId)
                    .addSnapshotListener { snap, err ->
                        if (err != null) {
                            close(err)
                            return@addSnapshotListener
                        }
                        val ids =
                            snap?.documents?.mapNotNull { it.getString("following_id") }?.distinct()
                                ?: emptyList()
                        trySend(ids)
                    }
            awaitClose { reg.remove() }
        }
}
