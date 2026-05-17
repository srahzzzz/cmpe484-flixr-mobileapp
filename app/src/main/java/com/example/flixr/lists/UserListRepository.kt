package com.example.flixr.lists

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserListRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun getListsForUser(userId: String): List<UserList> {
        val snap =
            db.collection("UserLists")
                .whereEqualTo("user_id", userId)
                .get()
                .await()
        return snap.documents
            .map { doc -> UserList.fromMap(doc.data ?: emptyMap(), doc.id) }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun createList(userId: String, name: String): UserList {
        val ref = db.collection("UserLists").document()
        val list =
            UserList(
                list_id = ref.id,
                user_id = userId,
                name = name.trim(),
                movie_ids = emptyList(),
                created_at = System.currentTimeMillis(),
            )
        ref.set(list.toMap()).await()
        return list
    }

    suspend fun renameList(listId: String, name: String) {
        db.collection("UserLists").document(listId).update("name", name.trim()).await()
    }

    suspend fun deleteList(listId: String) {
        db.collection("UserLists").document(listId).delete().await()
    }

    suspend fun addMovieToList(listId: String, movieId: String) {
        val ref = db.collection("UserLists").document(listId)
        val snap = ref.get().await()
        val current =
            (snap.get("movie_ids") as? List<*>)?.mapNotNull { it?.toString() }.orEmpty()
        if (movieId in current) return
        ref.update("movie_ids", current + movieId).await()
    }

    suspend fun removeMovieFromList(listId: String, movieId: String) {
        val ref = db.collection("UserLists").document(listId)
        val snap = ref.get().await()
        val current =
            (snap.get("movie_ids") as? List<*>)?.mapNotNull { it?.toString() }.orEmpty()
        ref.update("movie_ids", current.filter { it != movieId }).await()
    }
}
