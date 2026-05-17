package com.example.flixr.lists

data class UserList(
    val list_id: String = "",
    val user_id: String = "",
    val name: String = "",
    val movie_ids: List<String> = emptyList(),
    val created_at: Long = 0L,
) {
    fun toMap(): Map<String, Any?> =
        mapOf(
            "list_id" to list_id,
            "user_id" to user_id,
            "name" to name,
            "movie_ids" to movie_ids,
            "created_at" to created_at,
        )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(data: Map<String, Any?>, id: String): UserList =
            UserList(
                list_id = id,
                user_id = data["user_id"] as? String ?: "",
                name = data["name"] as? String ?: "",
                movie_ids = (data["movie_ids"] as? List<*>)?.mapNotNull { it?.toString() }.orEmpty(),
                created_at = (data["created_at"] as? Number)?.toLong() ?: 0L,
            )
    }
}
