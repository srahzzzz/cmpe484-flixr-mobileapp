package com.example.flixr.auth

/**
 * Username rules + normalization.
 *
 * Why normalize?
 * - Firestore paths are case-sensitive, but humans treat "Ada" and "ada" as the same handle.
 * - We store usernames under `usernames/{normalized}` to enforce uniqueness cheaply.
 */
object Username {
    private val allowed = Regex("^[a-z0-9_]{3,20}$")

    fun normalize(raw: String): String = raw.trim().lowercase()

    fun validateOrThrow(normalized: String) {
        require(normalized.isNotBlank()) { "Username is required." }
        require(allowed.matches(normalized)) {
            "Usernames must be 3-20 characters: lowercase letters, numbers, underscore only."
        }
    }
}
